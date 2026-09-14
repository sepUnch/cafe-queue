package com.example.cafequeue.outbox;

import com.example.cafequeue.order.event.OrderPlacedEvent;
import com.example.cafequeue.order.event.OrderStatusChangedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository,
                           KafkaTemplate<String, Object> kafkaTemplate,
                           ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void processPendingEvents() {
        outboxEventRepository.findTop20ByStatusOrderByCreatedDateAsc("PENDING")
                .flatMap(event -> {
                    try {
                        Object payloadObj = parsePayload(event.getTopic(), event.getPayload().asString());
                        
                        // Gunakan send() dari KafkaTemplate dan tangani callback
                        return org.springframework.boot.actuate.endpoint.invoke.OperationInvoker.class.isAssignableFrom(Object.class) ? reactor.core.publisher.Mono.empty() : reactor.core.publisher.Mono.fromFuture(
                            kafkaTemplate.send(event.getTopic(), event.getAggregateId().toString(), payloadObj)
                        ).flatMap(result -> {
                            event.setStatus("PUBLISHED");
                            event.setPublishedDate(LocalDateTime.now());
                            return outboxEventRepository.save(event);
                        }).onErrorResume(e -> {
                            event.setRetryCount(event.getRetryCount() + 1);
                            if (event.getRetryCount() >= 5) {
                                event.setStatus("FAILED");
                                log.error("OutboxEvent {} FAILED permanently after 5 retries for topic {}. Error: {}", event.getId(), event.getTopic(), e.getMessage());
                            } else {
                                log.warn("OutboxEvent {} failed to publish to {}. Retry {}/5. Error: {}", event.getId(), event.getTopic(), event.getRetryCount(), e.getMessage());
                            }
                            return outboxEventRepository.save(event);
                        });

                    } catch (Exception e) {
                        event.setRetryCount(event.getRetryCount() + 1);
                        if (event.getRetryCount() >= 5) {
                            event.setStatus("FAILED");
                            log.error("OutboxEvent {} FAILED due to parsing/processing error. Topic {}. Error: {}", event.getId(), event.getTopic(), e.getMessage());
                        }
                        return outboxEventRepository.save(event);
                    }
                })
                .subscribe(); // Best effort per schedule
    }

    private Object parsePayload(String topic, String payloadJson) throws Exception {
        if ("cafe.order.placed".equals(topic)) {
            return objectMapper.readValue(payloadJson, OrderPlacedEvent.class);
        } else if ("cafe.order.status.changed".equals(topic)) {
            return objectMapper.readValue(payloadJson, OrderStatusChangedEvent.class);
        } else {
            throw new IllegalArgumentException("Unknown topic: " + topic);
        }
    }
}
