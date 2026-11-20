-- Datos ficticios para la entrega. No son mediciones ni recomendaciones reales.
-- Los usuarios pertenecen a la V2 de ms-usuarios; no se copian sus contraseñas ni roles.
INSERT INTO farms (id, name, location, active) VALUES
('33333333-3333-4333-8333-333333333333', 'Predio de demostración', 'Maule, Chile', TRUE);

INSERT INTO farm_users (id, farm_id, user_id) VALUES
('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1', '33333333-3333-4333-8333-333333333333', '11111111-1111-4111-8111-111111111111'),
('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa2', '33333333-3333-4333-8333-333333333333', '22222222-2222-4222-8222-222222222222');

INSERT INTO crops (id, farm_id, name, variety, area_hectares, phenological_stage, active) VALUES
('44444444-4444-4444-8444-444444444444', '33333333-3333-4333-8333-333333333333',
 'Uva de mesa', 'Variedad de prueba', 2.5, 'Brotación', TRUE);

INSERT INTO records (id, crop_id, user_id, date, type, status, observations) VALUES
('55555555-5555-4555-8555-555555555555', '44444444-4444-4444-8444-444444444444',
 '11111111-1111-4111-8111-111111111111', '2026-11-19', 'SOIL', 'PENDING', 'Análisis ficticio de suelo'),
('66666666-6666-4666-8666-666666666666', '44444444-4444-4444-8444-444444444444',
 '11111111-1111-4111-8111-111111111111', '2026-11-19', 'WATER', 'PENDING', 'Análisis ficticio de agua'),
('77777777-7777-4777-8777-777777777777', '44444444-4444-4444-8444-444444444444',
 '11111111-1111-4111-8111-111111111111', '2026-11-19', 'FOLIAR', 'PENDING', 'Análisis ficticio foliar');

INSERT INTO soil_data (record_id, ph, electrical_conductivity, nitrogen, organic_matter) VALUES
('55555555-5555-4555-8555-555555555555', 6.5, 1.0, 20, 2.0);
INSERT INTO water_data (record_id, ph, electrical_conductivity, water_source) VALUES
('66666666-6666-4666-8666-666666666666', 7.1, 0.8, 'Pozo de demostración');
INSERT INTO foliar_data (record_id, nitrogen, symptoms) VALUES
('77777777-7777-4777-8777-777777777777', 2.5, 'Observación visual ficticia');

INSERT INTO record_reviews (id, record_id, advisor_id, advisor_name, date, result, comment) VALUES
('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', '55555555-5555-4555-8555-555555555555',
 '22222222-2222-4222-8222-222222222222', 'Asesor Prueba', '2026-11-19T12:00:00', 'OBSERVED', 'Observación ficticia de demostración');

INSERT INTO programs (id, crop_id, advisor_id, advisor_name, name, season, start_date, end_date,
 target_yield_ton_ha, irrigation_efficiency_percent, nitrogen, phosphorus_oxide, potassium_oxide,
 calcium_oxide, magnesium_oxide, sulfur, irrigation_notes, active, created_at) VALUES
('88888888-8888-4888-8888-888888888888', '44444444-4444-4444-8444-444444444444',
 '22222222-2222-4222-8222-222222222222', 'Asesor Prueba', 'Programa manual de demostración', '2026-2027',
 DATE '2026-11-19', DATE '2027-02-28', 10, 90, 20, 5, 15, 8, 4, 2,
 'Datos ficticios ingresados manualmente; el prototipo no calcula demandas, mezclas ni operación de estanques.',
 TRUE, TIMESTAMP '2026-11-19 12:00:00');
INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, notes) VALUES
('99999999-9999-4999-8999-999999999999', '88888888-8888-4888-8888-888888888888', 0,
 'Producto de demostración', 10, 'Dosis ficticia, no es una recomendación de aplicación');
INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, instructions) VALUES
('cccccccc-cccc-4ccc-8ccc-cccccccccccc', '88888888-8888-4888-8888-888888888888', 0,
 'Brotación', 4, 3, 'Instrucciones ficticias ingresadas por el asesor');
