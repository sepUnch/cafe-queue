package com.example.cafequeue.payment;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/payment")
public class PaymentMockController {

    private static final Logger log = LoggerFactory.getLogger(PaymentMockController.class);
    private final Random random = new Random();
    private final WebClient webClient;

    public PaymentMockController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    // --- TARGET: The flaky 3rd party API ---
    @PostMapping("/mock-external-api")
    public Mono<ResponseEntity<Map<String, String>>> flakyExternalApi() {
        // Simulate 30% failure rate
        if (random.nextInt(100) < 30) {
            log.warn("[EXTERNAL API] Simulating 500 Internal Server Error");
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment Gateway Down")));
        }
        
        log.info("[EXTERNAL API] Payment Processed Successfully");
        return Mono.just(ResponseEntity.ok(Map.of("status", "SUCCESS")));
    }

    // --- CALLER: The resilient client using our CircuitBreaker and Retry ---
    @PostMapping("/mock-charge")
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "chargeFallback")
    @Retry(name = "paymentGateway")
    public Mono<ResponseEntity<Map<String, String>>> resilientCharge() {
        log.info("[CLIENT] Initiating payment request...");
        
        return webClient.post()
                .uri("/payment/mock-external-api")
                .retrieve()
                .onStatus(status -> status.is5xxServerError(), response -> {
                    log.error("[CLIENT] Received 500 from External API");
                    return Mono.error(new RuntimeException("External API failed"));
                })
                .bodyToMono(Map.class)
                .map(res -> ResponseEntity.ok((Map<String, String>) res));
    }

    // --- FALLBACK: Called when Circuit Breaker is OPEN or Retries are exhausted ---
    public Mono<ResponseEntity<Map<String, String>>> chargeFallback(Exception e) {
        log.error("[FALLBACK] Circuit open or Retries exhausted. Rejecting payment. Reason: {}", e.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Service temporarily unavailable. Please try again later.")));
    }
}
