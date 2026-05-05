CREATE TABLE users
(
    id            BIGINT PRIMARY KEY                                               NOT NULL,
    public_id     UUID UNIQUE                                                      NOT NULL,
    email         VARCHAR(30)                                                      NOT NULL,
    password_hash VARCHAR(30)                                                      NOT NULL,
    full_name     VARCHAR(40)                                                      NOT NULL,
    role          VARCHAR(15) CHECK ( role IN ('CUSTOMER', 'ORGANIZER', 'ADMIN') ) NOT NULL,
    created_at    TIMESTAMPTZ                                                      NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);

CREATE INDEX idx_users_id ON users (id);
CREATE INDEX idx_users_public_id ON users (public_id);
