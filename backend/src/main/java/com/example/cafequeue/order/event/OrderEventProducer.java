package com.example.cafequeue.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Void> publishOrderPlaced(OrderPlacedEvent event) {
        return Mono.fromRunnable(() -> {
            kafkaTemplate.send("cafe.order.placed", event.orderId().toString(), event);
            log.info("Published OrderPlacedEvent to cafe.order.placed topic for order {}", event.orderId());
        });
    }

    public Mono<Void> publishOrderStatusChanged(OrderStatusChangedEvent event) {
        return Mono.fromRunnable(() -> {
            kafkaTemplate.send("cafe.order.status.changed", event.orderId().toString(), event);
            log.info("Published OrderStatusChangedEvent to cafe.order.status.changed topic for order {}", event.orderId());
        });
    }
}
