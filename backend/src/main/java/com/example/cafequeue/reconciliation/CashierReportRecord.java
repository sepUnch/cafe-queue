package com.example.cafequeue.reconciliation;

import com.example.cafequeue.common.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("cashier_report_record")
public class CashierReportRecord extends BaseEntity {

    @Id
    private UUID id;

    @Column("upload_id")
    private UUID uploadId;

    @Column("order_id_raw")
    private String orderIdRaw;

    @Column("amount_paid")
    private BigDecimal amountPaid;

    @Column("paid_timestamp")
    private LocalDateTime paidTimestamp;

    @Column("reconciliation_status")
    private String reconciliationStatus; // MATCHED, AMOUNT_MISMATCH, ORDER_NOT_FOUND

    @Column("note")
    private String note;

    public CashierReportRecord() {}

    public CashierReportRecord(UUID uploadId, String orderIdRaw, BigDecimal amountPaid, LocalDateTime paidTimestamp, String reconciliationStatus, String note) {
        this.uploadId = uploadId;
        this.orderIdRaw = orderIdRaw;
        this.amountPaid = amountPaid;
        this.paidTimestamp = paidTimestamp;
        this.reconciliationStatus = reconciliationStatus;
        this.note = note;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUploadId() { return uploadId; }
    public void setUploadId(UUID uploadId) { this.uploadId = uploadId; }

    public String getOrderIdRaw() { return orderIdRaw; }
    public void setOrderIdRaw(String orderIdRaw) { this.orderIdRaw = orderIdRaw; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public LocalDateTime getPaidTimestamp() { return paidTimestamp; }
    public void setPaidTimestamp(LocalDateTime paidTimestamp) { this.paidTimestamp = paidTimestamp; }

    public String getReconciliationStatus() { return reconciliationStatus; }
    public void setReconciliationStatus(String reconciliationStatus) { this.reconciliationStatus = reconciliationStatus; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
