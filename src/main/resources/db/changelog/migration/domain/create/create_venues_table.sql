CREATE TABLE venues
(
    id             BIGINT PRIMARY KEY NOT NULL,
    name           VARCHAR(150)       NOT NULL,
    country        VARCHAR(40)        NOT NULL,
    city           VARCHAR(40)        NOT NULL,
    address        VARCHAR(40)        NOT NULL,
    total_capacity INTEGER            NOT NULL,
    created_at     TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);