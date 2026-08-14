CREATE TABLE exam (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id   UUID          NOT NULL REFERENCES course(id),
    semester_id UUID          NOT NULL REFERENCES semester(id),
    exam_date   TIMESTAMPTZ   NOT NULL,
    coeff       NUMERIC(5, 4) NOT NULL CHECK (coeff > 0 AND coeff <= 1)
);

CREATE INDEX idx_exam_course_semester ON exam(course_id, semester_id);
