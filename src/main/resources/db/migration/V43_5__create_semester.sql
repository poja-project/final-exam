CREATE TABLE semester (
                          id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          number         INT         NOT NULL CHECK (number BETWEEN 1 AND 6),
                          school_year_id UUID        NOT NULL REFERENCES school_year(id),
                          start_date     DATE        NOT NULL,
                          end_date       DATE        NOT NULL,
                          UNIQUE (school_year_id, number)
);