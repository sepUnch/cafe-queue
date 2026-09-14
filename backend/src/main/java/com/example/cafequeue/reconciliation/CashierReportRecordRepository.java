package com.example.cafequeue.reconciliation;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface CashierReportRecordRepository extends R2dbcRepository<CashierReportRecord, UUID> {
    
    @Query("SELECT * FROM cashier_report_record WHERE upload_id = :uploadId AND reconciliation_status != 'MATCHED' AND mark_for_delete = false")
    Flux<CashierReportRecord> findDiscrepanciesByUploadId(UUID uploadId);
}
