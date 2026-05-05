CREATE TABLE tickets
(
    id            BIGINT PRIMARY KEY NOT NULL,
    public_id     UUID               NOT NULL,
    booking_id    BIGINT             NOT NULL REFERENCES bookings (id) ON DELETE RESTRICT,
    event_seat_id BIGINT             NOT NULL REFERENCES event_seats (id) ON DELETE RESTRICT,
    price_paid    NUMERIC(10, 2)     NOT NULL CHECK ( price_paid >= 0 ),
    seat_label    VARCHAR(50)        NOT NULL,
    created_at    TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);

CREATE INDEX idx_tickets_id ON tickets (id);
CREATE INDEX idx_tickets_public_id ON tickets (public_id);
