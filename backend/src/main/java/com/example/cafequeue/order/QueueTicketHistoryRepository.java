package com.example.cafequeue.order;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import java.util.UUID;

public interface QueueTicketHistoryRepository extends ReactiveCrudRepository<QueueTicketHistory, UUID> {
    @Query("SELECT * FROM queue_ticket_history WHERE queue_ticket_id = :queueTicketId ORDER BY status_timestamp ASC")
    Flux<QueueTicketHistory> findByQueueTicketIdOrderByStatusTimestampAsc(UUID queueTicketId);
}
