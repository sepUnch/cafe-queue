CREATE TABLE orders (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_name     VARCHAR(255) NOT NULL,
    order_type        VARCHAR(20) NOT NULL,   -- DINE_IN, TAKEAWAY, PREORDER
    status            VARCHAR(20) NOT NULL,   -- QUEUED, COOKING, READY, SERVED, CANCELLED
    total_price       NUMERIC(12,2) NOT NULL,
    created_by        VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    created_date      TIMESTAMP NOT NULL DEFAULT now(),
    updated_by        VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    updated_date      TIMESTAMP NOT NULL DEFAULT now(),
    mark_for_delete   BOOLEAN NOT NULL DEFAULT false,
    optlock           BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE order_item (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id          UUID NOT NULL REFERENCES orders(id),
    menu_item_name    VARCHAR(255) NOT NULL,
    quantity          INT NOT NULL,
    price             NUMERIC(12,2) NOT NULL,
    created_by        VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    created_date      TIMESTAMP NOT NULL DEFAULT now(),
    updated_by        VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    updated_date      TIMESTAMP NOT NULL DEFAULT now(),
    mark_for_delete   BOOLEAN NOT NULL DEFAULT false,
    optlock           BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE queue_ticket (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id          UUID NOT NULL REFERENCES orders(id),
    ticket_number     INT NOT NULL,
    status            VARCHAR(20) NOT NULL,   -- sama dengan status di orders, disinkronkan
    status_timestamp  TIMESTAMP NOT NULL DEFAULT now(),
    created_by        VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    created_date      TIMESTAMP NOT NULL DEFAULT now(),
    updated_by        VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    updated_date      TIMESTAMP NOT NULL DEFAULT now(),
    mark_for_delete   BOOLEAN NOT NULL DEFAULT false,
    optlock           BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE queue_ticket_history (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    queue_ticket_id   UUID NOT NULL REFERENCES queue_ticket(id),
    status            VARCHAR(20) NOT NULL,
    status_timestamp  TIMESTAMP NOT NULL DEFAULT now(),
    created_by        VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    created_date      TIMESTAMP NOT NULL DEFAULT now()
);
