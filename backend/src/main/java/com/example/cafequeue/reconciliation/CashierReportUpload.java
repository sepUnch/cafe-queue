package com.example.cafequeue.reconciliation;

import com.example.cafequeue.common.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("cashier_report_upload")
public class CashierReportUpload extends BaseEntity {

    @Id
    private UUID id;

    @Column("file_name")
    private String fileName;

    @Column("total_rows")
    private Integer totalRows;

    @Column("upload_status")
    private String uploadStatus; // PROCESSING, COMPLETED, FAILED

    public CashierReportUpload() {}

    public CashierReportUpload(String fileName, Integer totalRows, String uploadStatus) {
        this.fileName = fileName;
        this.totalRows = totalRows;
        this.uploadStatus = uploadStatus;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }

    public String getUploadStatus() { return uploadStatus; }
    public void setUploadStatus(String uploadStatus) { this.uploadStatus = uploadStatus; }
}
