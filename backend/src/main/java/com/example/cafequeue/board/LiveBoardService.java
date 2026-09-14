package com.example.cafequeue.board;

import com.example.cafequeue.order.QueueTicketRepository;
import com.example.cafequeue.order.event.OrderStatusChangedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LiveBoardService {

    private static final Logger log = LoggerFactory.getLogger(LiveBoardService.class);

    private final Sinks.Many<QueueBoardEvent> sink;
    private final QueueTicketRepository queueTicketRepository;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public LiveBoardService(QueueTicketRepository queueTicketRepository,
                            ReactiveStringRedisTemplate redisTemplate,
                            ObjectMapper objectMapper) {
        this.queueTicketRepository = queueTicketRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        // Gunakan directBestEffort agar Sink tidak ter-terminate saat koneksi klien terputus/refresh
        this.sink = Sinks.many().multicast().directBestEffort();
    }

    private String getRedisKey() {
        return "queue-board:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @KafkaListener(topics = "cafe.order.status.changed", groupId = "live-board-group")
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("LiveBoard received status change for ticket {}: {} -> {}", 
                event.ticketNumber(), event.previousStatus(), event.newStatus());
                
        QueueBoardEvent boardEvent = new QueueBoardEvent(
                event.ticketNumber(),
                event.newStatus(),
                event.eventTimestamp(),
                event.pickupTime()
        );
        
        Sinks.EmitResult result = sink.tryEmitNext(boardEvent);
        if (result.isFailure()) {
            log.warn("Failed to emit event to live board: {}", result);
        }

        // Partial Update Cache
        updateCachePartially(boardEvent).subscribe(
                success -> log.debug("Cache partial update successful"),
                error -> log.error("Cache partial update failed: {}", error.getMessage())
        );
    }

    private Mono<Void> updateCachePartially(QueueBoardEvent newEvent) {
        String key = getRedisKey();
        return redisTemplate.opsForValue().get(key)
                .timeout(Duration.ofSeconds(1))
                .flatMap(json -> {
                    try {
                        List<QueueBoardEvent> events = objectMapper.readValue(json, new TypeReference<List<QueueBoardEvent>>() {});
                        
                        // Cari dan ganti, atau tambahkan jika tidak ada
                        boolean found = false;
                        for (int i = 0; i < events.size(); i++) {
                            if (events.get(i).ticketNumber().equals(newEvent.ticketNumber())) {
                                events.set(i, newEvent);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            events.add(newEvent);
                        }
                        
                        String updatedJson = objectMapper.writeValueAsString(events);
                        return redisTemplate.opsForValue().set(key, updatedJson, Duration.ofHours(25))
                                .timeout(Duration.ofSeconds(1))
                                .then();
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Jika cache belum ada, biarkan kosong. Nanti akan diisi penuh saat ada GET request (Warming).
                    return Mono.empty();
                }))
                .onErrorResume(e -> {
                    log.warn("Ignored redis error during partial update: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    public Flux<QueueBoardEvent> getStream() {
        return sink.asFlux();
    }

    public Flux<QueueBoardEvent> getCurrentSnapshot() {
        String key = getRedisKey();
        
        return redisTemplate.opsForValue().get(key)
                .timeout(Duration.ofSeconds(1))
                .flatMapMany(json -> {
                    log.info("Cache Hit for key {}", key);
                    try {
                        List<QueueBoardEvent> events = objectMapper.readValue(json, new TypeReference<List<QueueBoardEvent>>() {});
                        return Flux.fromIterable(events);
                    } catch (JsonProcessingException e) {
                        return Flux.error(e);
                    }
                })
                .switchIfEmpty(Flux.defer(() -> fetchFromDbAndWarmCache(key)))
                .onErrorResume(e -> {
                    log.warn("Redis error or parsing error, falling back to PostgreSQL: {}", e.getMessage());
                    // Fallback to DB without warming cache (since Redis might be down)
                    return fetchFromDbFallback();
                });
    }

    private Flux<QueueBoardEvent> fetchFromDbAndWarmCache(String key) {
        log.info("Cache Miss for key {}. Fetching from DB and warming cache...", key);
        return fetchFromDbFallback()
                .collectList()
                .flatMapMany(events -> {
                    try {
                        String json = objectMapper.writeValueAsString(events);
                        return redisTemplate.opsForValue().set(key, json, Duration.ofHours(25))
                                .timeout(Duration.ofSeconds(1))
                                .onErrorResume(e -> {
                                    log.warn("Failed to warm cache, ignoring error: {}", e.getMessage());
                                    return Mono.empty();
                                })
                                .thenMany(Flux.fromIterable(events));
                    } catch (JsonProcessingException e) {
                        return Flux.fromIterable(events);
                    }
                });
    }

    private Flux<QueueBoardEvent> fetchFromDbFallback() {
        return queueTicketRepository.findActiveTicketsForToday()
                .map(ticket -> new QueueBoardEvent(
                        String.valueOf(ticket.getTicketNumber()),
                        ticket.getStatus(),
                        ticket.getStatusTimestamp(),
                        ticket.getPickupTime()
                ));
    }
}
