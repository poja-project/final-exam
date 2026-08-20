CREATE TABLE teacher_assignment (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id  UUID NOT NULL REFERENCES teacher(id),
    course_id   UUID NOT NULL REFERENCES course(id),
    group_id    UUID NOT NULL REFERENCES student_group(id),
    semester_id UUID NOT NULL REFERENCES semester(id),
    UNIQUE (teacher_id, course_id, group_id, semester_id)
);

CREATE INDEX idx_teacher_assignment_teacher ON teacher_assignment(teacher_id);
CREATE INDEX idx_teacher_assignment_group_semester ON teacher_assignment(group_id, semester_id);
