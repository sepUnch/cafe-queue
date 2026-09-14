package com.example.cafequeue.order;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.cafequeue.common.BaseEntity;

@Table("queue_ticket_history")
public class QueueTicketHistory extends BaseEntity {

    @Id
    private UUID id;

    @Column("queue_ticket_id")
    private UUID queueTicketId;

    @Column("status")
    private String status;

    @Column("status_timestamp")
    private LocalDateTime statusTimestamp;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getQueueTicketId() { return queueTicketId; }
    public void setQueueTicketId(UUID queueTicketId) { this.queueTicketId = queueTicketId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStatusTimestamp() { return statusTimestamp; }
    public void setStatusTimestamp(LocalDateTime statusTimestamp) { this.statusTimestamp = statusTimestamp; }
}
