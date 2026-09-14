package com.example.cafequeue.order;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface QueueTicketRepository extends ReactiveCrudRepository<QueueTicket, UUID> {
    @Query("SELECT * FROM queue_ticket WHERE order_id = :orderId AND mark_for_delete = false")
    Mono<QueueTicket> findByOrderIdAndNotDeleted(UUID orderId);

    @Query("SELECT COALESCE(MAX(ticket_number), 0) + 1 FROM queue_ticket WHERE DATE(created_date) = CURRENT_DATE")
    Mono<Integer> getNextTicketNumberForToday();

    @Query("SELECT * FROM queue_ticket WHERE status IN ('QUEUED', 'COOKING', 'READY') AND DATE(created_date) = CURRENT_DATE ORDER BY status_timestamp ASC")
    reactor.core.publisher.Flux<QueueTicket> findActiveTicketsForToday();
}
