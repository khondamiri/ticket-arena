CREATE TABLE sections
(
    id            BIGINT PRIMARY KEY NOT NULL,
    venue_id      BIGINT UNIQUE      NOT NULL REFERENCES venues (id) ON DELETE CASCADE,
    name          VARCHAR(30)        NOT NULL,
    display_order INTEGER            NOT NULL,
    created_at    TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sections_id ON sections (id);
