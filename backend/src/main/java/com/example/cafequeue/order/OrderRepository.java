package com.example.cafequeue.order;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<Order, UUID> {
    @Query("SELECT * FROM orders WHERE id = :id AND mark_for_delete = false")
    Mono<Order> findByIdAndNotDeleted(UUID id);
}
