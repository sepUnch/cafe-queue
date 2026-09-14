package com.example.cafequeue.kitchen;

import com.example.cafequeue.order.OrderService;
import com.example.cafequeue.order.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

@Component
public class KitchenSimulatorListener {

    private static final Logger log = LoggerFactory.getLogger(KitchenSimulatorListener.class);
    
    private final OrderService orderService;
    private final double failureRate;
    private final Random random = new Random();

    public KitchenSimulatorListener(OrderService orderService, 
                                    @Value("${cafe.simulation.failure-rate:0.05}") double failureRate) {
        this.orderService = orderService;
        this.failureRate = failureRate;
    }

    @KafkaListener(topics = "cafe.order.placed", groupId = "kitchen-simulator-group")
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Kitchen simulator received order: {} (Ticket: {})", event.orderId(), event.ticketNumber());
        
        // Cek probabilitas kegagalan sebelum mulai dimasak
        if (random.nextDouble() < failureRate) {
            log.warn("Order {} failed kitchen preparation! Cancelling immediately.", event.orderId());
            orderService.updateOrderStatus(event.orderId(), "CANCELLED").subscribe();
            return;
        }
        
        // Simulasi proses memasak
        Mono.delay(Duration.ofSeconds(10))
            .flatMap(ignored -> {
                log.info("Order {} is now COOKING", event.orderId());
                return orderService.updateOrderStatus(event.orderId(), "COOKING");
            })
            .then(Mono.delay(Duration.ofSeconds(20)))
            .flatMap(ignored -> {
                log.info("Order {} is now READY", event.orderId());
                return orderService.updateOrderStatus(event.orderId(), "READY");
            })
            .subscribe();
    }
}
