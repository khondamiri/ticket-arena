CREATE TABLE seats
(
    id          BIGINT PRIMARY KEY NOT NULL,
    section_id  BIGINT             NOT NULL REFERENCES sections (id) ON DELETE CASCADE  ,
    row_label   VARCHAR(10) UNIQUE NOT NULL,
    seat_number BIGINT             NOT NULL,
    created_at  TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    UNIQUE (section_id, seat_number)
);

CREATE INDEX idx_seats_id ON seats (id);
CREATE INDEX idx_seats_composite_section_seat_id ON seats (section_id, seat_number);
