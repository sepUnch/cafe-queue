package com.example.cafequeue.reconciliation;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CashierReportUploadRepository extends R2dbcRepository<CashierReportUpload, UUID> {
}
