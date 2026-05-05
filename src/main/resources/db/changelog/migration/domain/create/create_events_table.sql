CREATE TABLE events
(
    id           BIGINT PRIMARY KEY NOT NULL,
    public_id    UUID               NOT NULL,
    organizer_id BIGINT             NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    venue_id     BIGINT             NOT NULL REFERENCES venues (id) ON DELETE RESTRICT,
    title        VARCHAR(40) UNIQUE NOT NULL,
    description  VARCHAR UNIQUE     NOT NULL,
    starts_at    TIMESTAMPTZ        NOT NULL,
    ends_at      TIMESTAMPTZ        NOT NULL,
    status       VARCHAR(15)        NOT NULL CHECK ( status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED') ),
    created_at   TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_events_id ON events (id);
CREATE INDEX idx_events_public_id ON events (public_id);
CREATE INDEX idx_events_starts_at ON events (starts_at);
