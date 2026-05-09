CREATE TABLE events
(
    id           BIGINT PRIMARY KEY  NOT NULL,
    public_id    UUID                NOT NULL,
    organizer_id BIGINT              NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    venue_id     BIGINT              NOT NULL REFERENCES venues (id) ON DELETE RESTRICT,
    title        VARCHAR(255) UNIQUE NOT NULL,
    description  VARCHAR UNIQUE      NOT NULL,
    starts_at    TIMESTAMPTZ         NOT NULL,
    ends_at      TIMESTAMPTZ         NOT NULL,
    status       VARCHAR(15)         NOT NULL CHECK ( status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED') ),
    created_at   TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ,
    deleted_at   TIMESTAMPTZ
);

CREATE INDEX idx_events_organized_id ON events (organizer_id);
CREATE INDEX idx_events_venue_id ON events (venue_id);
CREATE INDEX idx_events_public_id ON events (public_id);
CREATE INDEX idx_events_starts_at ON events (starts_at);
