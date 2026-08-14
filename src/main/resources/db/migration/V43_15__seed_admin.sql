INSERT INTO users (id, email, password, role)
VALUES (
    gen_random_uuid(),
    'admin@gmail.com',
    '$2a$10$PYb6ptjnHNkzVkrG/V5ITeN6/FUWYwvgNUyjH90hFT7fgugg/xKd2',
    'ADMIN'
);
