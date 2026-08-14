
CREATE TABLE course_offering (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id   UUID NOT NULL REFERENCES course(id),
    semester_id UUID NOT NULL REFERENCES semester(id),
    track       VARCHAR(10) CHECK (track IN ('EL', 'TN')),
    UNIQUE (course_id, semester_id, track)
);
