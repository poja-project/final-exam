CREATE TABLE cohort (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label      VARCHAR(100) NOT NULL,
    entry_year INT          NOT NULL
);
