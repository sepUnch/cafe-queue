package com.example.cafequeue.board;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/queue-board")
public class LiveBoardController {

    private final LiveBoardService liveBoardService;

    public LiveBoardController(LiveBoardService liveBoardService) {
        this.liveBoardService = liveBoardService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<QueueBoardEvent>> streamQueueEvents() {
        return liveBoardService.getStream()
                .map(event -> ServerSentEvent.<QueueBoardEvent>builder()
                        .event("status-changed")
                        .data(event)
                        .build())
                // Kirim heartbeat agar koneksi tidak diputus oleh proxy/browser jika lama tak ada pesanan
                .mergeWith(Flux.interval(java.time.Duration.ofSeconds(15))
                        .map(i -> ServerSentEvent.<QueueBoardEvent>builder()
                                .comment("keep-alive")
                                .build()));
    }

    @GetMapping("/current")
    public Flux<QueueBoardEvent> getCurrentSnapshot() {
        return liveBoardService.getCurrentSnapshot();
    }
}
