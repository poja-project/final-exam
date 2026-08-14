CREATE TABLE course (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference VARCHAR(50)  NOT NULL UNIQUE,
    title     VARCHAR(255) NOT NULL,
    credit    INT          NOT NULL CHECK (credit > 0)
);
