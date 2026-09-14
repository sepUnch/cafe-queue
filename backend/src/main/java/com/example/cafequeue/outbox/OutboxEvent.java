package com.example.cafequeue.outbox;

import com.example.cafequeue.common.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("outbox_event")
public class OutboxEvent extends BaseEntity {

    @Id
    private UUID id;

    @Column("aggregate_type")
    private String aggregateType;

    @Column("aggregate_id")
    private UUID aggregateId;

    @Column("topic")
    private String topic;

    @Column("payload")
    private io.r2dbc.postgresql.codec.Json payload;

    @Column("status")
    private String status = "PENDING";

    @Column("retry_count")
    private int retryCount = 0;

    @Column("published_date")
    private LocalDateTime publishedDate;

    public OutboxEvent() {}

    public OutboxEvent(String aggregateType, UUID aggregateId, String topic, io.r2dbc.postgresql.codec.Json payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.topic = topic;
        this.payload = payload;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }

    public UUID getAggregateId() { return aggregateId; }
    public void setAggregateId(UUID aggregateId) { this.aggregateId = aggregateId; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public io.r2dbc.postgresql.codec.Json getPayload() { return payload; }
    public void setPayload(io.r2dbc.postgresql.codec.Json payload) { this.payload = payload; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public LocalDateTime getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDateTime publishedDate) { this.publishedDate = publishedDate; }
}
