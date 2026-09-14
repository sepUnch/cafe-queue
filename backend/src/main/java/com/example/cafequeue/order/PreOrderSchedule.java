package com.example.cafequeue.order;


import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "pre_order_schedule")
public class PreOrderSchedule {

    @Id
    private String id;

    private String customerName;
    private List<OrderRequest.OrderItemRequest> items;
    
    private LocalDateTime pickupTime;
    private String scheduleStatus; // PENDING, TRIGGERED

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedBy
    private String lastModifiedBy;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    private boolean markForDelete = false;

    // Constructors
    public PreOrderSchedule() {}

    public PreOrderSchedule(String customerName, List<OrderRequest.OrderItemRequest> items, LocalDateTime pickupTime, String scheduleStatus) {
        this.customerName = customerName;
        this.items = items;
        this.pickupTime = pickupTime;
        this.scheduleStatus = scheduleStatus;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public List<OrderRequest.OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderRequest.OrderItemRequest> items) { this.items = items; }

    public LocalDateTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; }

    public String getScheduleStatus() { return scheduleStatus; }
    public void setScheduleStatus(String scheduleStatus) { this.scheduleStatus = scheduleStatus; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

    public boolean isMarkForDelete() { return markForDelete; }
    public void setMarkForDelete(boolean markForDelete) { this.markForDelete = markForDelete; }
}
