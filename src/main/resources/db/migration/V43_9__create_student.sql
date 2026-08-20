CREATE TABLE student (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_number VARCHAR(30)  NOT NULL UNIQUE,
    last_name      VARCHAR(100) NOT NULL,
    first_name     VARCHAR(100) NOT NULL,
    user_id        UUID         NOT NULL UNIQUE REFERENCES users(id),
    cohort_id      UUID         NOT NULL REFERENCES cohort(id)
);
