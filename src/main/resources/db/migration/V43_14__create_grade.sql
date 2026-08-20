CREATE TABLE grade (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID          NOT NULL REFERENCES student(id),
    exam_id    UUID          NOT NULL REFERENCES exam(id),
    value      NUMERIC(4, 2) NOT NULL CHECK (value >= 0 AND value <= 20),
    entered_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    reason     VARCHAR(500),
    author_id  UUID          NOT NULL REFERENCES users(id)
);

CREATE INDEX idx_grade_student_exam ON grade(student_id, exam_id, entered_at DESC);
