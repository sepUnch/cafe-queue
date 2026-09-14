package com.example.cafequeue.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class PreOrderScheduler {

    private static final Logger log = LoggerFactory.getLogger(PreOrderScheduler.class);

    private final PreOrderRepository preOrderRepository;
    private final OrderService orderService;

    public PreOrderScheduler(PreOrderRepository preOrderRepository, OrderService orderService) {
        this.preOrderRepository = preOrderRepository;
        this.orderService = orderService;
    }

    @Scheduled(fixedRate = 10000)
    public void processPendingPreOrders() {
        LocalDateTime triggerThreshold = LocalDateTime.now().plusMinutes(5);
        log.info("Checking PreOrders. Threshold time: {}", triggerThreshold);

        preOrderRepository.findByScheduleStatus("PENDING")
                .doOnNext(s -> log.info("Found PENDING PreOrder: id={}, pickupTime={}", s.getId(), s.getPickupTime()))
                .filter(schedule -> !schedule.getPickupTime().isAfter(triggerThreshold))
                .flatMap(schedule -> {
                    log.info("Triggering PreOrder for {}, Pickup Time: {}", schedule.getCustomerName(), schedule.getPickupTime());
                    
                    OrderRequest request = new OrderRequest(
                            schedule.getCustomerName(),
                            "PREORDER",
                            schedule.getPickupTime(),
                            schedule.getItems()
                    );

                    return orderService.createOrder(request)
                            .then(Mono.defer(() -> {
                                schedule.setScheduleStatus("TRIGGERED");
                                return preOrderRepository.save(schedule);
                            }))
                            .onErrorResume(e -> {
                                log.error("Failed to trigger PreOrder for {}", schedule.getId(), e);
                                return Mono.empty();
                            });
                })
                .subscribe(
                        null,
                        error -> log.error("Scheduler encountered an error: ", error)
                );
    }
}
