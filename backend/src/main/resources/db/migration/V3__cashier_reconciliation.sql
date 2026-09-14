CREATE TABLE cashier_report_upload (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_name         VARCHAR(255) NOT NULL,
    total_rows        INT NOT NULL,
    upload_status     VARCHAR(20) NOT NULL,  -- PROCESSING, COMPLETED, FAILED
    created_by        VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    created_date      TIMESTAMP NOT NULL DEFAULT now(),
    updated_by        VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    updated_date      TIMESTAMP NOT NULL DEFAULT now(),
    mark_for_delete   BOOLEAN NOT NULL DEFAULT false,
    optlock           BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE cashier_report_record (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    upload_id             UUID NOT NULL REFERENCES cashier_report_upload(id),
    order_id_raw          VARCHAR(255) NOT NULL,  -- as-is dari CSV, belum tentu UUID valid
    amount_paid           NUMERIC(12,2) NOT NULL,
    paid_timestamp        TIMESTAMP NOT NULL,
    reconciliation_status VARCHAR(20) NOT NULL,  -- MATCHED, AMOUNT_MISMATCH, ORDER_NOT_FOUND
    note                  VARCHAR(500),
    created_by            VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    created_date          TIMESTAMP NOT NULL DEFAULT now(),
    updated_by            VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    updated_date          TIMESTAMP NOT NULL DEFAULT now(),
    mark_for_delete       BOOLEAN NOT NULL DEFAULT false,
    optlock               BIGINT NOT NULL DEFAULT 0
);
