CREATE TABLE venues
(
    id             BIGINT PRIMARY KEY NOT NULL,
    name           VARCHAR(30)        NOT NULL,
    address        VARCHAR(40)        NOT NULL,
    city           VARCHAR(40)        NOT NULL,
    total_capacity INTEGER            NOT NULL,
    created_at     TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_venues_id ON venues (id);
