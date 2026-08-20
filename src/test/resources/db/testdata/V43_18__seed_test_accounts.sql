-- password: TestPass123!

INSERT INTO cohort (id, label, entry_year)
VALUES ('00000000-0000-0000-0000-000000000001', 'IT Test Cohort', 2023);

INSERT INTO users (id, email, password, role)
VALUES
    ('00000000-0000-0000-0000-000000000002', 'teacher-it@prog4.local',
     '$2a$10$/KxrB3KKZr8oN/GLECr9pu2bdOmO40xol8Co12SnWMdOBlNUPPoXS', 'TEACHER'),
    ('00000000-0000-0000-0000-000000000003', 'student-it-a@prog4.local',
     '$2a$10$/KxrB3KKZr8oN/GLECr9pu2bdOmO40xol8Co12SnWMdOBlNUPPoXS', 'STUDENT'),
    ('00000000-0000-0000-0000-000000000004', 'student-it-b@prog4.local',
     '$2a$10$/KxrB3KKZr8oN/GLECr9pu2bdOmO40xol8Co12SnWMdOBlNUPPoXS', 'STUDENT');

INSERT INTO teacher (id, last_name, first_name, user_id)
VALUES ('00000000-0000-0000-0000-000000000005', 'Test', 'Teacher',
        '00000000-0000-0000-0000-000000000002');

INSERT INTO student (id, student_number, last_name, first_name, user_id, cohort_id)
VALUES
    ('00000000-0000-0000-0000-000000000006', 'STD-IT-A', 'Test', 'StudentA',
     '00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000007', 'STD-IT-B', 'Test', 'StudentB',
     '00000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000001');