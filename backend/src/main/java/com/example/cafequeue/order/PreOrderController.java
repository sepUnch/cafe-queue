package com.example.cafequeue.order;

import com.example.cafequeue.order.dto.PreOrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/preorders")
public class PreOrderController {

    private final PreOrderService preOrderService;

    public PreOrderController(PreOrderService preOrderService) {
        this.preOrderService = preOrderService;
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> createPreOrder(@RequestBody PreOrderRequest request) {
        return preOrderService.schedulePreOrder(request)
                .<ResponseEntity<Object>>map(schedule -> ResponseEntity.ok(schedule))
                .onErrorResume(IllegalArgumentException.class, e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }
}
