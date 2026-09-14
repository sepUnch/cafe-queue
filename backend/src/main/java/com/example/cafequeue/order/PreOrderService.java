package com.example.cafequeue.order;

import com.example.cafequeue.order.dto.PreOrderRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class PreOrderService {

    private final PreOrderRepository preOrderRepository;

    public PreOrderService(PreOrderRepository preOrderRepository) {
        this.preOrderRepository = preOrderRepository;
    }

    public Mono<PreOrderSchedule> schedulePreOrder(PreOrderRequest request) {
        if (request.pickupTime().isBefore(LocalDateTime.now())) {
            return Mono.error(new IllegalArgumentException("pickupTime cannot be in the past"));
        }

        PreOrderSchedule schedule = new PreOrderSchedule(
                request.customerName(),
                request.items(),
                request.pickupTime(),
                "PENDING"
        );

        return preOrderRepository.save(schedule);
    }
}
