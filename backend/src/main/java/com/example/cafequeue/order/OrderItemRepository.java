package com.example.cafequeue.order;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import java.util.UUID;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, UUID> {
    @Query("SELECT * FROM order_item WHERE order_id = :orderId AND mark_for_delete = false")
    Flux<OrderItem> findByOrderIdAndNotDeleted(UUID orderId);
}
