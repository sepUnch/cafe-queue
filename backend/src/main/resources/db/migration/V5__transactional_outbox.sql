CREATE TABLE outbox_event (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type    VARCHAR(50) NOT NULL,
    aggregate_id      UUID NOT NULL,
    topic             VARCHAR(100) NOT NULL,
    payload           JSONB NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count       INT NOT NULL DEFAULT 0,
    created_date      TIMESTAMP NOT NULL DEFAULT now(),
    published_date    TIMESTAMP NULL,
    created_by        VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    updated_by        VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    updated_date      TIMESTAMP NOT NULL DEFAULT now(),
    mark_for_delete   BOOLEAN NOT NULL DEFAULT false,
    optlock           BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_pending ON outbox_event (status, created_date) WHERE status = 'PENDING';
