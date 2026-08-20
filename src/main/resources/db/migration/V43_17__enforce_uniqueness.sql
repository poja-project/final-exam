ALTER TABLE cohort ADD CONSTRAINT uk_cohort_label UNIQUE (label);

ALTER TABLE student_group ADD CONSTRAINT uk_student_group_reference_cohort UNIQUE (reference, cohort_id);

CREATE UNIQUE INDEX uk_course_offering_common ON course_offering(course_id, semester_id) WHERE track IS NULL;
