INSERT INTO users (
    id, run, first_name, last_name, email, role,
    active, password_hash, birth_date
) VALUES
(
    '11111111-1111-4111-8111-111111111111',
    '11111111-1',
    'Agricultor',
    'Prueba',
    'farmer@test.cl',
    'FARMER',
    TRUE,
    '$2a$10$ZOo3ndmnv/athLWTCqiawOb4qeZGCiJ2iNdaXP0shkWvP9LLTKMwe',
    DATE '2000-01-01'
),
(
    '22222222-2222-4222-8222-222222222222',
    '22222222-2',
    'Asesor',
    'Prueba',
    'advisor@test.cl',
    'ADVISOR',
    TRUE,
    '$2a$10$a4U/fAJqNzDw.DeC8qoAHewM5yhJFjBl58hG7JHiDDZJIC9QJd7pO',
    DATE '2000-01-01'
);
