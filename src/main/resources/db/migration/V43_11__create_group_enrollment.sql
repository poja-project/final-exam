CREATE TABLE group_enrollment (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID        NOT NULL REFERENCES student(id),
    group_id   UUID        NOT NULL REFERENCES student_group(id),
    start_date TIMESTAMPTZ NOT NULL,
    end_date   TIMESTAMPTZ
);

CREATE INDEX idx_group_enrollment_student ON group_enrollment(student_id);
