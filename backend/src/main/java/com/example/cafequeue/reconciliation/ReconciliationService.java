package com.example.cafequeue.reconciliation;

import com.example.cafequeue.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    private final CashierReportUploadRepository uploadRepository;
    private final CashierReportRecordRepository recordRepository;
    private final OrderRepository orderRepository;

    public ReconciliationService(CashierReportUploadRepository uploadRepository, CashierReportRecordRepository recordRepository, OrderRepository orderRepository) {
        this.uploadRepository = uploadRepository;
        this.recordRepository = recordRepository;
        this.orderRepository = orderRepository;
    }

    public Mono<CashierReportUpload> processCsv(FilePart filePart) {
        CashierReportUpload initialUpload = new CashierReportUpload(filePart.filename(), 0, "PROCESSING");

        return uploadRepository.save(initialUpload)
                .flatMap(savedUpload -> DataBufferUtils.join(filePart.content())
                        .map(dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            return new String(bytes, StandardCharsets.UTF_8);
                        })
                        .flatMap(content -> {
                            String[] lines = content.split("\\r?\\n");
                            if (lines.length <= 1) {
                                return Mono.error(new IllegalArgumentException("CSV file is empty or only contains header"));
                            }

                            savedUpload.setTotalRows(lines.length - 1); // Exclude header

                            return Flux.range(1, lines.length - 1)
                                    .flatMap(i -> processLine(savedUpload.getId(), lines[i]))
                                    .collectList()
                                    .flatMap(records -> recordRepository.saveAll(records).collectList())
                                    .flatMap(savedRecords -> {
                                        savedUpload.setUploadStatus("COMPLETED");
                                        return uploadRepository.save(savedUpload);
                                    });
                        })
                        .onErrorResume(e -> {
                            log.error("Failed to process CSV file for upload ID: {}", savedUpload.getId(), e);
                            savedUpload.setUploadStatus("FAILED");
                            return uploadRepository.save(savedUpload).then(Mono.error(e));
                        })
                );
    }

    private Mono<CashierReportRecord> processLine(UUID uploadId, String line) {
        String[] columns = line.split(",");
        if (columns.length < 3) {
            return Mono.error(new IllegalArgumentException("Invalid CSV format at line: " + line));
        }

        String orderIdRaw = columns[0].trim();
        BigDecimal amountPaid = new BigDecimal(columns[1].trim());
        LocalDateTime paidTimestamp = LocalDateTime.parse(columns[2].trim(), DateTimeFormatter.ISO_DATE_TIME);

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdRaw);
        } catch (IllegalArgumentException e) {
            return Mono.just(new CashierReportRecord(uploadId, orderIdRaw, amountPaid, paidTimestamp, "ORDER_NOT_FOUND", "Invalid UUID format"));
        }

        return orderRepository.findByIdAndNotDeleted(orderId)
                .map(order -> {
                    if (order.getTotalPrice().compareTo(amountPaid) == 0) {
                        return new CashierReportRecord(uploadId, orderIdRaw, amountPaid, paidTimestamp, "MATCHED", null);
                    } else {
                        String note = String.format("Expected: %s, Paid: %s", order.getTotalPrice(), amountPaid);
                        return new CashierReportRecord(uploadId, orderIdRaw, amountPaid, paidTimestamp, "AMOUNT_MISMATCH", note);
                    }
                })
                .defaultIfEmpty(new CashierReportRecord(uploadId, orderIdRaw, amountPaid, paidTimestamp, "ORDER_NOT_FOUND", "Order not found in database"));
    }

    public Flux<CashierReportRecord> getDiscrepancies(UUID uploadId) {
        return recordRepository.findDiscrepanciesByUploadId(uploadId);
    }
}
