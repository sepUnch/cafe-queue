package com.example.cafequeue.common;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import java.time.LocalDateTime;

public abstract class BaseEntity {

    @CreatedBy
    @Column("created_by")
    private String createdBy = "SYSTEM";

    @CreatedDate
    @Column("created_date")
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column("updated_by")
    private String updatedBy = "SYSTEM";

    @LastModifiedDate
    @Column("updated_date")
    private LocalDateTime updatedDate;

    @Column("mark_for_delete")
    private boolean markForDelete = false;

    @Version
    @Column("optlock")
    private Long optlock;

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public boolean isMarkForDelete() { return markForDelete; }
    public void setMarkForDelete(boolean markForDelete) { this.markForDelete = markForDelete; }

    public Long getOptlock() { return optlock; }
    public void setOptlock(Long optlock) { this.optlock = optlock; }
}
