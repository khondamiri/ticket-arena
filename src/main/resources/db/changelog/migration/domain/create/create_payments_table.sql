CREATE TABLE payments
(
    id                 BIGINT PRIMARY KEY NOT NULL,
    booking_id         BIGINT UNIQUE      NOT NULL REFERENCES bookings (id) ON DELETE RESTRICT,
    amount             NUMERIC(10, 2)     NOT NULL CHECK ( amount >= 0 ),
    status             VARCHAR(15)        NOT NULL CHECK ( status in ('PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED') ),
    provider_reference VARCHAR(50)        NOT NULL,
    updated_at         TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    created_at         TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_id ON payments (id);
