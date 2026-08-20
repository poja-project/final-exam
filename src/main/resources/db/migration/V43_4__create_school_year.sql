CREATE TABLE school_year (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label       VARCHAR(20) NOT NULL,
    year_number INT         NOT NULL CHECK (year_number BETWEEN 1 AND 3),
    cohort_id   UUID        NOT NULL REFERENCES cohort(id),
    UNIQUE (cohort_id, year_number)
);
