package com.example.cafequeue.reconciliation;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/upload")
    public Mono<ResponseEntity<Object>> uploadCashierReport(@RequestPart("file") FilePart file) {
        return reconciliationService.processCsv(file)
                .<ResponseEntity<Object>>map(upload -> ResponseEntity.ok(upload))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }

    @GetMapping("/{uploadId}/discrepancies")
    public Flux<CashierReportRecord> getDiscrepancies(@PathVariable UUID uploadId) {
        return reconciliationService.getDiscrepancies(uploadId);
    }
}
