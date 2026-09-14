package com.example.cafequeue.order;

import com.example.cafequeue.common.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("queue_ticket")
public class QueueTicket extends BaseEntity {

    @Id
    private UUID id;

    @Column("order_id")
    private UUID orderId;

    @Column("ticket_number")
    private Integer ticketNumber;

    @Column("status")
    private String status;

    @Column("status_timestamp")
    private LocalDateTime statusTimestamp;

    @Column("pickup_time")
    private LocalDateTime pickupTime;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public Integer getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(Integer ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStatusTimestamp() { return statusTimestamp; }
    public void setStatusTimestamp(LocalDateTime statusTimestamp) { this.statusTimestamp = statusTimestamp; }

    public LocalDateTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; }
}
