CREATE TABLE teacher (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    last_name  VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    user_id    UUID         NOT NULL UNIQUE REFERENCES users(id)
);
