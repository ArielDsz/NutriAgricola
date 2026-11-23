-- Mantiene los UUID, correos y contraseñas de las cuentas de prueba.
UPDATE users SET first_name = 'Ángel', last_name = 'Muñoz'
WHERE id = '11111111-1111-4111-8111-111111111111' AND first_name = 'Agricultor' AND last_name = 'Prueba';

UPDATE users SET first_name = 'María', last_name = 'González'
WHERE id = '22222222-2222-4222-8222-222222222222' AND first_name = 'Asesor' AND last_name = 'Prueba';
