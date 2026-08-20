INSERT INTO users (id, email, password, role)
VALUES (
    gen_random_uuid(),
    'admin@prog4.local',
    '$2a$10$1Hyy8H61LQ84XtS69S61qeDy8QvQFqY3Y10lUx4c/BIbC47T0pkZ6',
    'ADMIN'
);
