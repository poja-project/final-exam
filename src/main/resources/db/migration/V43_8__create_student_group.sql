CREATE TABLE student_group (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference  VARCHAR(50) NOT NULL,
    track      VARCHAR(10) CHECK (track IN ('EL', 'TN')),
    cohort_id  UUID        NOT NULL REFERENCES cohort(id)
);
