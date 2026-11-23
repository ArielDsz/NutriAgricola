-- Datos ficticios para la presentación; no son recomendaciones de aplicación.

-- Las composiciones y requerimientos siguen CropNutritionCatalog y ProgramCalculator.

-- No modifica otros UUID ni elimina registros creados por los usuarios.

UPDATE farms SET name = 'El Maitén', location = 'Limache', image_path = 'drawable/farm_el_maiten' WHERE id = '33333333-3333-4333-8333-333333333333' AND name = 'Predio de demostración' AND location = 'Maule, Chile';

INSERT INTO farms (id, name, location, image_path, active)
SELECT '33333333-3333-4333-8333-333333333333', 'El Maitén', 'Limache', 'drawable/farm_el_maiten', TRUE
WHERE NOT EXISTS (SELECT 1 FROM farms WHERE id = '33333333-3333-4333-8333-333333333333');

INSERT INTO farm_users (id, farm_id, user_id) SELECT '5eba44a4-d8fc-537e-96a8-656ff5a15e71', '33333333-3333-4333-8333-333333333333', '11111111-1111-4111-8111-111111111111' WHERE NOT EXISTS (SELECT 1 FROM farm_users WHERE farm_id = '33333333-3333-4333-8333-333333333333' AND user_id = '11111111-1111-4111-8111-111111111111');

INSERT INTO farm_users (id, farm_id, user_id) SELECT '5e79f4ab-d1b2-5f57-ba25-748e8cff3c43', '33333333-3333-4333-8333-333333333333', '22222222-2222-4222-8222-222222222222' WHERE NOT EXISTS (SELECT 1 FROM farm_users WHERE farm_id = '33333333-3333-4333-8333-333333333333' AND user_id = '22222222-2222-4222-8222-222222222222');

INSERT INTO farms (id, name, location, image_path, active)
SELECT '70dc836e-8043-50c3-8538-e84cf5d95d42', 'Los Aromos', 'Quillota', 'drawable/farm_los_aromos', TRUE
WHERE NOT EXISTS (SELECT 1 FROM farms WHERE id = '70dc836e-8043-50c3-8538-e84cf5d95d42');

INSERT INTO farm_users (id, farm_id, user_id) SELECT 'bd24b90e-e65a-5728-98b2-2e4c6b508e26', '70dc836e-8043-50c3-8538-e84cf5d95d42', '11111111-1111-4111-8111-111111111111' WHERE NOT EXISTS (SELECT 1 FROM farm_users WHERE farm_id = '70dc836e-8043-50c3-8538-e84cf5d95d42' AND user_id = '11111111-1111-4111-8111-111111111111');

INSERT INTO farm_users (id, farm_id, user_id) SELECT '93c6048f-88e2-5114-a46e-51eddadcafb2', '70dc836e-8043-50c3-8538-e84cf5d95d42', '22222222-2222-4222-8222-222222222222' WHERE NOT EXISTS (SELECT 1 FROM farm_users WHERE farm_id = '70dc836e-8043-50c3-8538-e84cf5d95d42' AND user_id = '22222222-2222-4222-8222-222222222222');

INSERT INTO farms (id, name, location, image_path, active)
SELECT '85040a0f-218a-5281-8031-793aa68aa6c1', 'Santa Elena', 'La Cruz', 'drawable/farm_santa_elena', TRUE
WHERE NOT EXISTS (SELECT 1 FROM farms WHERE id = '85040a0f-218a-5281-8031-793aa68aa6c1');

INSERT INTO farm_users (id, farm_id, user_id) SELECT '89bc98a9-5313-5e58-ade3-dec6fddf3465', '85040a0f-218a-5281-8031-793aa68aa6c1', '11111111-1111-4111-8111-111111111111' WHERE NOT EXISTS (SELECT 1 FROM farm_users WHERE farm_id = '85040a0f-218a-5281-8031-793aa68aa6c1' AND user_id = '11111111-1111-4111-8111-111111111111');

INSERT INTO farm_users (id, farm_id, user_id) SELECT 'ebe64ed6-5c84-5ef5-a8e6-9d546002159b', '85040a0f-218a-5281-8031-793aa68aa6c1', '22222222-2222-4222-8222-222222222222' WHERE NOT EXISTS (SELECT 1 FROM farm_users WHERE farm_id = '85040a0f-218a-5281-8031-793aa68aa6c1' AND user_id = '22222222-2222-4222-8222-222222222222');

UPDATE crops SET variety = 'Thompson Seedless', image_path = 'drawable/crop_grape' WHERE id = '44444444-4444-4444-8444-444444444444' AND name = 'Uva de mesa' AND variety = 'Variedad de prueba' AND area_hectares = 2.5 AND phenological_stage = 'Brotación';

INSERT INTO crops (id, farm_id, name, variety, area_hectares, phenological_stage, image_path, active)
SELECT '44444444-4444-4444-8444-444444444444', '33333333-3333-4333-8333-333333333333', 'Uva de mesa', 'Thompson Seedless', 2.5, 'Brotación', 'drawable/crop_grape', TRUE
WHERE NOT EXISTS (SELECT 1 FROM crops WHERE id = '44444444-4444-4444-8444-444444444444');

INSERT INTO crops (id, farm_id, name, variety, area_hectares, phenological_stage, image_path, active)
SELECT '9409e2f9-bda3-5e33-bcce-2aefcc0387e3', '33333333-3333-4333-8333-333333333333', 'Clementinas / Mandarinos', 'Clemenules', 1.8, 'Floración', 'drawable/crop_mandarin', TRUE
WHERE NOT EXISTS (SELECT 1 FROM crops WHERE id = '9409e2f9-bda3-5e33-bcce-2aefcc0387e3');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT '1be8f986-7560-552e-832f-6fed84d79a9d', '9409e2f9-bda3-5e33-bcce-2aefcc0387e3', '11111111-1111-4111-8111-111111111111', '2026-11-21', 'SOIL', 'PENDING', 'Muestra compuesta del sector norte, a 20 cm de profundidad.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = '1be8f986-7560-552e-832f-6fed84d79a9d');

INSERT INTO soil_data (record_id, ph, electrical_conductivity, nitrogen, phosphorus, potassium, calcium, magnesium, organic_matter)
SELECT '1be8f986-7560-552e-832f-6fed84d79a9d', 6.2, 0.88, 30, 15, 153, 950, 115, 2.5
WHERE NOT EXISTS (SELECT 1 FROM soil_data WHERE record_id = '1be8f986-7560-552e-832f-6fed84d79a9d');

INSERT INTO record_reviews (id, record_id, advisor_id, advisor_name, date, result, comment)
SELECT 'fdef10fa-d4dc-50fc-adb0-6978a2c817a3', '1be8f986-7560-552e-832f-6fed84d79a9d', '22222222-2222-4222-8222-222222222222', 'María González', '2026-11-21T16:30:00', 'OBSERVED', 'Adjuntar los antecedentes del sector antes de validar.'
WHERE NOT EXISTS (SELECT 1 FROM record_reviews WHERE id = 'fdef10fa-d4dc-50fc-adb0-6978a2c817a3');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT '2352c472-510f-5115-8b29-afb5b377f086', '9409e2f9-bda3-5e33-bcce-2aefcc0387e3', '11111111-1111-4111-8111-111111111111', '2026-11-20', 'WATER', 'PENDING', 'Muestra tomada en la salida del sistema de riego.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = '2352c472-510f-5115-8b29-afb5b377f086');

INSERT INTO water_data (record_id, ph, electrical_conductivity, nitrogen, phosphorus, potassium, calcium, magnesium, water_source)
SELECT '2352c472-510f-5115-8b29-afb5b377f086', 6.96, 0.55, 5, 0.6, 9, 58, 16, 'Pozo profundo'
WHERE NOT EXISTS (SELECT 1 FROM water_data WHERE record_id = '2352c472-510f-5115-8b29-afb5b377f086');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT '05bcf696-d080-50ba-ad51-127c269d073a', '9409e2f9-bda3-5e33-bcce-2aefcc0387e3', '11111111-1111-4111-8111-111111111111', '2026-11-19', 'FOLIAR', 'VALIDATED', 'Muestra de hojas del tercio medio de la planta.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = '05bcf696-d080-50ba-ad51-127c269d073a');

INSERT INTO foliar_data (record_id, nitrogen, phosphorus, potassium, calcium, magnesium, symptoms)
SELECT '05bcf696-d080-50ba-ad51-127c269d073a', 1.9, 0.19, 1.28, 1.45, 0.32, 'Sin síntomas visibles en las plantas muestreadas.'
WHERE NOT EXISTS (SELECT 1 FROM foliar_data WHERE record_id = '05bcf696-d080-50ba-ad51-127c269d073a');

INSERT INTO record_reviews (id, record_id, advisor_id, advisor_name, date, result, comment)
SELECT 'e168ba11-66bd-53d3-9911-d3eb56167575', '05bcf696-d080-50ba-ad51-127c269d073a', '22222222-2222-4222-8222-222222222222', 'María González', '2026-11-19T16:30:00', 'VALIDATED', 'Muestra y antecedentes revisados.'
WHERE NOT EXISTS (SELECT 1 FROM record_reviews WHERE id = 'e168ba11-66bd-53d3-9911-d3eb56167575');

INSERT INTO crops (id, farm_id, name, variety, area_hectares, phenological_stage, image_path, active)
SELECT 'f4337edf-24ef-52d0-84b9-376e2069f153', '70dc836e-8043-50c3-8538-e84cf5d95d42', 'Palto (Avocado)', 'Hass', 3.0, 'Floración', 'drawable/crop_avocado', TRUE
WHERE NOT EXISTS (SELECT 1 FROM crops WHERE id = 'f4337edf-24ef-52d0-84b9-376e2069f153');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT '2c6894d5-fb74-517b-9346-5702e67824df', 'f4337edf-24ef-52d0-84b9-376e2069f153', '11111111-1111-4111-8111-111111111111', '2026-11-20', 'SOIL', 'PENDING', 'Muestra compuesta del sector norte, a 20 cm de profundidad.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = '2c6894d5-fb74-517b-9346-5702e67824df');

INSERT INTO soil_data (record_id, ph, electrical_conductivity, nitrogen, phosphorus, potassium, calcium, magnesium, organic_matter)
SELECT '2c6894d5-fb74-517b-9346-5702e67824df', 6.3, 0.96, 32, 16, 161, 980, 120, 2.6
WHERE NOT EXISTS (SELECT 1 FROM soil_data WHERE record_id = '2c6894d5-fb74-517b-9346-5702e67824df');

INSERT INTO record_reviews (id, record_id, advisor_id, advisor_name, date, result, comment)
SELECT '9b3e06f0-5f97-5fcd-ab4c-af98cc7c20d7', '2c6894d5-fb74-517b-9346-5702e67824df', '22222222-2222-4222-8222-222222222222', 'María González', '2026-11-20T16:30:00', 'OBSERVED', 'Adjuntar los antecedentes del sector antes de validar.'
WHERE NOT EXISTS (SELECT 1 FROM record_reviews WHERE id = '9b3e06f0-5f97-5fcd-ab4c-af98cc7c20d7');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT 'dc344cb4-51bd-510c-84c3-b10be2a1bf08', 'f4337edf-24ef-52d0-84b9-376e2069f153', '11111111-1111-4111-8111-111111111111', '2026-11-19', 'WATER', 'VALIDATED', 'Muestra tomada en la salida del sistema de riego.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = 'dc344cb4-51bd-510c-84c3-b10be2a1bf08');

INSERT INTO water_data (record_id, ph, electrical_conductivity, nitrogen, phosphorus, potassium, calcium, magnesium, water_source)
SELECT 'dc344cb4-51bd-510c-84c3-b10be2a1bf08', 7.02, 0.6, 6, 0.7, 10, 61, 17, 'Pozo profundo'
WHERE NOT EXISTS (SELECT 1 FROM water_data WHERE record_id = 'dc344cb4-51bd-510c-84c3-b10be2a1bf08');

INSERT INTO record_reviews (id, record_id, advisor_id, advisor_name, date, result, comment)
SELECT '024f50b5-f048-5839-bac9-4c481380c134', 'dc344cb4-51bd-510c-84c3-b10be2a1bf08', '22222222-2222-4222-8222-222222222222', 'María González', '2026-11-19T16:30:00', 'VALIDATED', 'Muestra y antecedentes revisados.'
WHERE NOT EXISTS (SELECT 1 FROM record_reviews WHERE id = '024f50b5-f048-5839-bac9-4c481380c134');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT 'b2c627a1-52f1-5ea3-9a5d-9a84957bb03c', 'f4337edf-24ef-52d0-84b9-376e2069f153', '11111111-1111-4111-8111-111111111111', '2026-11-18', 'FOLIAR', 'PENDING', 'Muestra de hojas del tercio medio de la planta.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = 'b2c627a1-52f1-5ea3-9a5d-9a84957bb03c');

INSERT INTO foliar_data (record_id, nitrogen, phosphorus, potassium, calcium, magnesium, symptoms)
SELECT 'b2c627a1-52f1-5ea3-9a5d-9a84957bb03c', 2.0, 0.2, 1.36, 1.5, 0.34, 'Amarillamiento leve en hojas del sector norte.'
WHERE NOT EXISTS (SELECT 1 FROM foliar_data WHERE record_id = 'b2c627a1-52f1-5ea3-9a5d-9a84957bb03c');

INSERT INTO crops (id, farm_id, name, variety, area_hectares, phenological_stage, image_path, active)
SELECT '14aba14a-d5c3-5471-b602-e49740433b21', '70dc836e-8043-50c3-8538-e84cf5d95d42', 'Limones', 'Eureka', 1.6, 'Desarrollo del fruto', 'drawable/crop_lemon', TRUE
WHERE NOT EXISTS (SELECT 1 FROM crops WHERE id = '14aba14a-d5c3-5471-b602-e49740433b21');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT 'cbd036e5-76db-5090-a7db-edff639238ae', '14aba14a-d5c3-5471-b602-e49740433b21', '11111111-1111-4111-8111-111111111111', '2026-11-19', 'SOIL', 'VALIDATED', 'Muestra compuesta del sector norte, a 20 cm de profundidad.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = 'cbd036e5-76db-5090-a7db-edff639238ae');

INSERT INTO soil_data (record_id, ph, electrical_conductivity, nitrogen, phosphorus, potassium, calcium, magnesium, organic_matter)
SELECT 'cbd036e5-76db-5090-a7db-edff639238ae', 6.4, 1.04, 34, 17, 169, 1010, 125, 2.7
WHERE NOT EXISTS (SELECT 1 FROM soil_data WHERE record_id = 'cbd036e5-76db-5090-a7db-edff639238ae');

INSERT INTO record_reviews (id, record_id, advisor_id, advisor_name, date, result, comment)
SELECT '02ea1b2c-08d8-5484-83a5-99c595bd198d', 'cbd036e5-76db-5090-a7db-edff639238ae', '22222222-2222-4222-8222-222222222222', 'María González', '2026-11-19T16:30:00', 'VALIDATED', 'Muestra y antecedentes revisados.'
WHERE NOT EXISTS (SELECT 1 FROM record_reviews WHERE id = '02ea1b2c-08d8-5484-83a5-99c595bd198d');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT '98f678b2-025e-550e-94e0-d9e81d8c8ba9', '14aba14a-d5c3-5471-b602-e49740433b21', '11111111-1111-4111-8111-111111111111', '2026-11-18', 'WATER', 'PENDING', 'Muestra tomada en la salida del sistema de riego.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = '98f678b2-025e-550e-94e0-d9e81d8c8ba9');

INSERT INTO water_data (record_id, ph, electrical_conductivity, nitrogen, phosphorus, potassium, calcium, magnesium, water_source)
SELECT '98f678b2-025e-550e-94e0-d9e81d8c8ba9', 7.08, 0.65, 7, 0.8, 11, 64, 18, 'Pozo profundo'
WHERE NOT EXISTS (SELECT 1 FROM water_data WHERE record_id = '98f678b2-025e-550e-94e0-d9e81d8c8ba9');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT '1056222b-876d-545e-bc25-eaa478e9f834', '14aba14a-d5c3-5471-b602-e49740433b21', '11111111-1111-4111-8111-111111111111', '2026-11-17', 'FOLIAR', 'PENDING', 'Muestra de hojas del tercio medio de la planta.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = '1056222b-876d-545e-bc25-eaa478e9f834');

INSERT INTO foliar_data (record_id, nitrogen, phosphorus, potassium, calcium, magnesium, symptoms)
SELECT '1056222b-876d-545e-bc25-eaa478e9f834', 2.1, 0.21, 1.44, 1.55, 0.36, 'Sin síntomas visibles en las plantas muestreadas.'
WHERE NOT EXISTS (SELECT 1 FROM foliar_data WHERE record_id = '1056222b-876d-545e-bc25-eaa478e9f834');

INSERT INTO crops (id, farm_id, name, variety, area_hectares, phenological_stage, image_path, active)
SELECT '62c7d8d1-fa20-5f85-8606-c54f64247b2a', '85040a0f-218a-5281-8031-793aa68aa6c1', 'Naranjos', 'Navel', 2.2, 'Desarrollo del fruto', 'drawable/crop_orange', TRUE
WHERE NOT EXISTS (SELECT 1 FROM crops WHERE id = '62c7d8d1-fa20-5f85-8606-c54f64247b2a');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT '262c43c1-e00f-5414-9df1-b326a5b4ed1d', '62c7d8d1-fa20-5f85-8606-c54f64247b2a', '11111111-1111-4111-8111-111111111111', '2026-11-18', 'SOIL', 'PENDING', 'Muestra compuesta del sector norte, a 20 cm de profundidad.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = '262c43c1-e00f-5414-9df1-b326a5b4ed1d');

INSERT INTO soil_data (record_id, ph, electrical_conductivity, nitrogen, phosphorus, potassium, calcium, magnesium, organic_matter)
SELECT '262c43c1-e00f-5414-9df1-b326a5b4ed1d', 6.5, 1.12, 36, 18, 177, 1040, 130, 2.8
WHERE NOT EXISTS (SELECT 1 FROM soil_data WHERE record_id = '262c43c1-e00f-5414-9df1-b326a5b4ed1d');

INSERT INTO record_reviews (id, record_id, advisor_id, advisor_name, date, result, comment)
SELECT 'e4c4a05a-f924-51e5-9eca-148256764a79', '262c43c1-e00f-5414-9df1-b326a5b4ed1d', '22222222-2222-4222-8222-222222222222', 'María González', '2026-11-18T16:30:00', 'OBSERVED', 'Adjuntar los antecedentes del sector antes de validar.'
WHERE NOT EXISTS (SELECT 1 FROM record_reviews WHERE id = 'e4c4a05a-f924-51e5-9eca-148256764a79');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT '25bd17f9-1af6-5f1a-9924-e4df842bec86', '62c7d8d1-fa20-5f85-8606-c54f64247b2a', '11111111-1111-4111-8111-111111111111', '2026-11-17', 'WATER', 'PENDING', 'Muestra tomada en la salida del sistema de riego.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = '25bd17f9-1af6-5f1a-9924-e4df842bec86');

INSERT INTO water_data (record_id, ph, electrical_conductivity, nitrogen, phosphorus, potassium, calcium, magnesium, water_source)
SELECT '25bd17f9-1af6-5f1a-9924-e4df842bec86', 7.14, 0.7, 8, 0.9, 12, 67, 19, 'Canal de riego'
WHERE NOT EXISTS (SELECT 1 FROM water_data WHERE record_id = '25bd17f9-1af6-5f1a-9924-e4df842bec86');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT '0996780f-ac54-51f4-9d9a-3b3eb6eb3fd6', '62c7d8d1-fa20-5f85-8606-c54f64247b2a', '11111111-1111-4111-8111-111111111111', '2026-11-16', 'FOLIAR', 'VALIDATED', 'Muestra de hojas del tercio medio de la planta.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = '0996780f-ac54-51f4-9d9a-3b3eb6eb3fd6');

INSERT INTO foliar_data (record_id, nitrogen, phosphorus, potassium, calcium, magnesium, symptoms)
SELECT '0996780f-ac54-51f4-9d9a-3b3eb6eb3fd6', 2.2, 0.22, 1.52, 1.6, 0.38, 'Amarillamiento leve en hojas del sector norte.'
WHERE NOT EXISTS (SELECT 1 FROM foliar_data WHERE record_id = '0996780f-ac54-51f4-9d9a-3b3eb6eb3fd6');

INSERT INTO record_reviews (id, record_id, advisor_id, advisor_name, date, result, comment)
SELECT '846f6857-35af-5376-827c-d0b07a78c77d', '0996780f-ac54-51f4-9d9a-3b3eb6eb3fd6', '22222222-2222-4222-8222-222222222222', 'María González', '2026-11-16T16:30:00', 'VALIDATED', 'Muestra y antecedentes revisados.'
WHERE NOT EXISTS (SELECT 1 FROM record_reviews WHERE id = '846f6857-35af-5376-827c-d0b07a78c77d');

INSERT INTO crops (id, farm_id, name, variety, area_hectares, phenological_stage, image_path, active)
SELECT '168de4f4-75af-59b7-a21f-56ac1a1128f6', '85040a0f-218a-5281-8031-793aa68aa6c1', 'Uva vinífera', 'Cabernet Sauvignon', 4.0, 'Brotación', 'drawable/crop_grape', TRUE
WHERE NOT EXISTS (SELECT 1 FROM crops WHERE id = '168de4f4-75af-59b7-a21f-56ac1a1128f6');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT 'bebeda43-ffce-5591-a7a8-68823c530764', '168de4f4-75af-59b7-a21f-56ac1a1128f6', '11111111-1111-4111-8111-111111111111', '2026-11-17', 'SOIL', 'PENDING', 'Muestra compuesta del sector norte, a 20 cm de profundidad.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = 'bebeda43-ffce-5591-a7a8-68823c530764');

INSERT INTO soil_data (record_id, ph, electrical_conductivity, nitrogen, phosphorus, potassium, calcium, magnesium, organic_matter)
SELECT 'bebeda43-ffce-5591-a7a8-68823c530764', 6.6, 1.2, 38, 19, 185, 1070, 135, 2.9
WHERE NOT EXISTS (SELECT 1 FROM soil_data WHERE record_id = 'bebeda43-ffce-5591-a7a8-68823c530764');

INSERT INTO record_reviews (id, record_id, advisor_id, advisor_name, date, result, comment)
SELECT '3eb8d5f0-96fc-565f-8cd0-cea157084e58', 'bebeda43-ffce-5591-a7a8-68823c530764', '22222222-2222-4222-8222-222222222222', 'María González', '2026-11-17T16:30:00', 'OBSERVED', 'Adjuntar los antecedentes del sector antes de validar.'
WHERE NOT EXISTS (SELECT 1 FROM record_reviews WHERE id = '3eb8d5f0-96fc-565f-8cd0-cea157084e58');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT '3ac3417f-cff2-5576-83c3-6be3de85a4b1', '168de4f4-75af-59b7-a21f-56ac1a1128f6', '11111111-1111-4111-8111-111111111111', '2026-11-16', 'WATER', 'VALIDATED', 'Muestra tomada en la salida del sistema de riego.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = '3ac3417f-cff2-5576-83c3-6be3de85a4b1');

INSERT INTO water_data (record_id, ph, electrical_conductivity, nitrogen, phosphorus, potassium, calcium, magnesium, water_source)
SELECT '3ac3417f-cff2-5576-83c3-6be3de85a4b1', 7.2, 0.75, 9, 1.0, 13, 70, 20, 'Canal de riego'
WHERE NOT EXISTS (SELECT 1 FROM water_data WHERE record_id = '3ac3417f-cff2-5576-83c3-6be3de85a4b1');

INSERT INTO record_reviews (id, record_id, advisor_id, advisor_name, date, result, comment)
SELECT 'f59f3a87-aee9-58ea-8bdd-153b175266d3', '3ac3417f-cff2-5576-83c3-6be3de85a4b1', '22222222-2222-4222-8222-222222222222', 'María González', '2026-11-16T16:30:00', 'VALIDATED', 'Muestra y antecedentes revisados.'
WHERE NOT EXISTS (SELECT 1 FROM record_reviews WHERE id = 'f59f3a87-aee9-58ea-8bdd-153b175266d3');

INSERT INTO records (id, crop_id, user_id, date, type, status, observations)
SELECT '500363e8-caad-59bc-8cf8-eb1e307ebde6', '168de4f4-75af-59b7-a21f-56ac1a1128f6', '11111111-1111-4111-8111-111111111111', '2026-11-15', 'FOLIAR', 'PENDING', 'Muestra de hojas del tercio medio de la planta.'
WHERE NOT EXISTS (SELECT 1 FROM records WHERE id = '500363e8-caad-59bc-8cf8-eb1e307ebde6');

INSERT INTO foliar_data (record_id, nitrogen, phosphorus, potassium, calcium, magnesium, symptoms)
SELECT '500363e8-caad-59bc-8cf8-eb1e307ebde6', 2.3, 0.23, 1.6, 1.65, 0.4, 'Sin síntomas visibles en las plantas muestreadas.'
WHERE NOT EXISTS (SELECT 1 FROM foliar_data WHERE record_id = '500363e8-caad-59bc-8cf8-eb1e307ebde6');

CREATE TABLE sample_program_to_replace (id VARCHAR(36) PRIMARY KEY);

INSERT INTO sample_program_to_replace SELECT id FROM programs WHERE id = '88888888-8888-4888-8888-888888888888' AND name = 'Programa manual de demostración' AND nitrogen = 20 AND phosphorus_oxide = 5 AND potassium_oxide = 15 AND calcium_oxide = 8 AND magnesium_oxide = 4 AND sulfur = 2 AND target_yield_ton_ha = 10 AND irrigation_efficiency_percent = 90 AND active = TRUE AND crop_id = '44444444-4444-4444-8444-444444444444' AND advisor_id = '22222222-2222-4222-8222-222222222222' AND start_date = DATE '2026-11-19' AND end_date = DATE '2027-02-28' AND season = '2026-2027' AND irrigation_notes = 'Datos ficticios ingresados manualmente; el prototipo no calcula demandas, mezclas ni operación de estanques.' AND (SELECT COUNT(*) FROM program_fertilizers f WHERE f.program_id = programs.id) = 1 AND EXISTS (SELECT 1 FROM program_fertilizers f WHERE f.program_id = programs.id AND f.name = 'Producto de demostración' AND f.dose_kg_ha = 10 AND f.notes = 'Dosis ficticia, no es una recomendación de aplicación' AND f.nitrogen IS NULL AND f.phosphorus_oxide IS NULL AND f.potassium_oxide IS NULL AND f.calcium_oxide IS NULL AND f.magnesium_oxide IS NULL AND f.sulfur IS NULL) AND (SELECT COUNT(*) FROM program_stages st WHERE st.program_id = programs.id) = 1 AND EXISTS (SELECT 1 FROM program_stages st WHERE st.program_id = programs.id AND st.name = 'Brotación' AND st.weeks = 4 AND st.irrigations_per_week = 3 AND st.allocation_percent IS NULL AND st.instructions = 'Instrucciones ficticias ingresadas por el asesor');

DELETE FROM programs WHERE id IN (SELECT id FROM sample_program_to_replace);

INSERT INTO programs (id, advisor_id, advisor_name, name, season, start_date, end_date, target_yield_ton_ha, irrigation_efficiency_percent, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur, irrigation_notes, active, created_at, species, plot)
SELECT '88888888-8888-4888-8888-888888888888', '22222222-2222-4222-8222-222222222222', 'María González', 'Uva de mesa · Thompson Seedless', '2026-2027', '2026-11-23', '2027-04-25', 50, 90, 133, 78, 267, 178, 56, 44.8, NULL, TRUE, '2026-11-23 10:00:00', 'Uva de mesa', 'El Maitén · Thompson Seedless'
WHERE NOT EXISTS (SELECT 1 FROM programs WHERE id = '88888888-8888-4888-8888-888888888888');

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '712c6b9f-ee88-5c42-a953-9e517d8adb4e', '88888888-8888-4888-8888-888888888888', 0, 'Fosfato monoamónico', 130, 12, 60, 0, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '88888888-8888-4888-8888-888888888888' AND name = 'Uva de mesa · Thompson Seedless') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = '88888888-8888-4888-8888-888888888888' AND item_order = 0);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '60177c49-bd1b-5b6d-b536-9c84177b9de4', '88888888-8888-4888-8888-888888888888', 1, 'Nitrato de calcio', 659, 16, 0, 0, 27, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '88888888-8888-4888-8888-888888888888' AND name = 'Uva de mesa · Thompson Seedless') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = '88888888-8888-4888-8888-888888888888' AND item_order = 1);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '0ffb00fe-530f-50ff-94d3-b421466fc721', '88888888-8888-4888-8888-888888888888', 2, 'Sulfato de Magnesio Heptahidratado', 350, 0, 0, 0, 0, 16, 13 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '88888888-8888-4888-8888-888888888888' AND name = 'Uva de mesa · Thompson Seedless') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = '88888888-8888-4888-8888-888888888888' AND item_order = 2);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '7123b27f-a673-51e8-b49f-7ce3eb9b8786', '88888888-8888-4888-8888-888888888888', 3, 'Nitrato de potasio', 593, 13, 0, 45, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '88888888-8888-4888-8888-888888888888' AND name = 'Uva de mesa · Thompson Seedless') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = '88888888-8888-4888-8888-888888888888' AND item_order = 3);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '1a26335c-4281-545c-854b-81c6e559cc5e', '88888888-8888-4888-8888-888888888888', 4, 'SmartBlue + N2 Stabil', 0, 46, 0, 0, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '88888888-8888-4888-8888-888888888888' AND name = 'Uva de mesa · Thompson Seedless') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = '88888888-8888-4888-8888-888888888888' AND item_order = 4);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '1af6821c-f911-565c-b56e-c00595de6fd1', '88888888-8888-4888-8888-888888888888', 0, 'Brotación y crecimiento', 6, 3, 35 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '88888888-8888-4888-8888-888888888888' AND name = 'Uva de mesa · Thompson Seedless') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = '88888888-8888-4888-8888-888888888888' AND item_order = 0);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '60a58b9c-e342-5cc7-be50-e9d74556ac16', '88888888-8888-4888-8888-888888888888', 1, 'Floración y cuaja', 4, 4, 25 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '88888888-8888-4888-8888-888888888888' AND name = 'Uva de mesa · Thompson Seedless') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = '88888888-8888-4888-8888-888888888888' AND item_order = 1);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT 'ae8091f1-b87d-5b1b-a8e9-f71de80dffd4', '88888888-8888-4888-8888-888888888888', 2, 'Desarrollo del fruto', 8, 5, 30 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '88888888-8888-4888-8888-888888888888' AND name = 'Uva de mesa · Thompson Seedless') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = '88888888-8888-4888-8888-888888888888' AND item_order = 2);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '0ca5a0f5-b8f2-5555-a24b-da01daed9507', '88888888-8888-4888-8888-888888888888', 3, 'Maduración y cosecha', 4, 2, 10 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '88888888-8888-4888-8888-888888888888' AND name = 'Uva de mesa · Thompson Seedless') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = '88888888-8888-4888-8888-888888888888' AND item_order = 3);

INSERT INTO programs (id, advisor_id, advisor_name, name, season, start_date, end_date, target_yield_ton_ha, irrigation_efficiency_percent, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur, irrigation_notes, active, created_at, species, plot)
SELECT 'a89e9b00-7ed9-51de-96fc-00e4421876a8', '22222222-2222-4222-8222-222222222222', 'María González', 'Clementinas / Mandarinos · Clemenules', '2026-2027', '2026-11-23', '2027-04-25', 50, 90, 267, 80, 267, 200, 56, 44.8, NULL, TRUE, '2026-11-23 10:00:00', 'Clementinas / Mandarinos', 'El Maitén · Clemenules'
WHERE NOT EXISTS (SELECT 1 FROM programs WHERE id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8');

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '3d980a06-b48f-5699-9c3b-b7c3a8cc74c1', 'a89e9b00-7ed9-51de-96fc-00e4421876a8', 0, 'Fosfato monoamónico', 133, 12, 60, 0, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND name = 'Clementinas / Mandarinos · Clemenules') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND item_order = 0);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '2940c788-e76f-5d6d-98a9-962b94397fde', 'a89e9b00-7ed9-51de-96fc-00e4421876a8', 1, 'Nitrato de calcio', 741, 16, 0, 0, 27, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND name = 'Clementinas / Mandarinos · Clemenules') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND item_order = 1);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '2420d4f6-42ff-5f5f-8b44-cc0659d6b25a', 'a89e9b00-7ed9-51de-96fc-00e4421876a8', 2, 'Sulfato de Magnesio Heptahidratado', 350, 0, 0, 0, 0, 16, 13 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND name = 'Clementinas / Mandarinos · Clemenules') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND item_order = 2);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '7ecb1533-3c35-5297-86ff-83bcdc7140a4', 'a89e9b00-7ed9-51de-96fc-00e4421876a8', 3, 'Nitrato de potasio', 593, 13, 0, 45, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND name = 'Clementinas / Mandarinos · Clemenules') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND item_order = 3);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '11768aaa-820f-5265-aa86-91801a424613', 'a89e9b00-7ed9-51de-96fc-00e4421876a8', 4, 'SmartBlue + N2 Stabil', 120, 46, 0, 0, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND name = 'Clementinas / Mandarinos · Clemenules') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND item_order = 4);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT 'a43bbec8-c0fd-5fea-9cec-6143809468fe', 'a89e9b00-7ed9-51de-96fc-00e4421876a8', 0, 'Brotación y crecimiento', 6, 3, 35 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND name = 'Clementinas / Mandarinos · Clemenules') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND item_order = 0);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '7b2e86b7-8b55-5bdd-867e-18fa9efe3a4f', 'a89e9b00-7ed9-51de-96fc-00e4421876a8', 1, 'Floración y cuaja', 4, 4, 25 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND name = 'Clementinas / Mandarinos · Clemenules') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND item_order = 1);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '60de630e-09b9-55ef-8291-8c63cac7ddb1', 'a89e9b00-7ed9-51de-96fc-00e4421876a8', 2, 'Desarrollo del fruto', 8, 5, 30 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND name = 'Clementinas / Mandarinos · Clemenules') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND item_order = 2);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '57dbf0e8-27e2-587a-a6c7-fb4f2a5c3e2d', 'a89e9b00-7ed9-51de-96fc-00e4421876a8', 3, 'Maduración y cosecha', 4, 2, 10 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND name = 'Clementinas / Mandarinos · Clemenules') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'a89e9b00-7ed9-51de-96fc-00e4421876a8' AND item_order = 3);

INSERT INTO programs (id, advisor_id, advisor_name, name, season, start_date, end_date, target_yield_ton_ha, irrigation_efficiency_percent, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur, irrigation_notes, active, created_at, species, plot)
SELECT '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f', '22222222-2222-4222-8222-222222222222', 'María González', 'Palto (Avocado) · Hass', '2026-2027', '2026-11-23', '2027-04-25', 28, 90, 244, 72, 278, 156, 61, 48.8, NULL, TRUE, '2026-11-23 10:00:00', 'Palto (Avocado)', 'Los Aromos · Hass'
WHERE NOT EXISTS (SELECT 1 FROM programs WHERE id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f');

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '6250e13a-5d85-5d99-8d26-d5f132c7d471', '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f', 0, 'Fosfato monoamónico', 120, 12, 60, 0, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND name = 'Palto (Avocado) · Hass') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND item_order = 0);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '0181e099-dc45-5945-849a-b9784468abe3', '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f', 1, 'Nitrato de calcio', 578, 16, 0, 0, 27, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND name = 'Palto (Avocado) · Hass') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND item_order = 1);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT 'd8271519-0d12-5db3-995b-8c393309bce9', '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f', 2, 'Sulfato de Magnesio Heptahidratado', 381, 0, 0, 0, 0, 16, 13 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND name = 'Palto (Avocado) · Hass') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND item_order = 2);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT 'aa3e85db-4fee-5086-90f6-938017d4f7a4', '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f', 3, 'Nitrato de potasio', 618, 13, 0, 45, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND name = 'Palto (Avocado) · Hass') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND item_order = 3);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '6b8835db-688f-54ca-bb21-baa557095fa4', '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f', 4, 'SmartBlue + N2 Stabil', 124, 46, 0, 0, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND name = 'Palto (Avocado) · Hass') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND item_order = 4);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '32344ec7-60a3-5ba7-87de-1504440bfebb', '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f', 0, 'Brotación y crecimiento', 6, 3, 35 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND name = 'Palto (Avocado) · Hass') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND item_order = 0);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '1c2452d8-0717-5fc5-8139-3d11e6935d54', '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f', 1, 'Floración y cuaja', 4, 4, 25 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND name = 'Palto (Avocado) · Hass') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND item_order = 1);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '7e0dc8ba-4ffb-5b12-884e-9f7328458c71', '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f', 2, 'Desarrollo del fruto', 8, 5, 30 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND name = 'Palto (Avocado) · Hass') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND item_order = 2);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '73259328-5559-5bd2-9d28-e63873dcfed4', '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f', 3, 'Maduración y cosecha', 4, 2, 10 WHERE EXISTS (SELECT 1 FROM programs WHERE id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND name = 'Palto (Avocado) · Hass') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = '02ff20a3-03cb-5b32-bc85-0c248ebd8d8f' AND item_order = 3);

INSERT INTO programs (id, advisor_id, advisor_name, name, season, start_date, end_date, target_yield_ton_ha, irrigation_efficiency_percent, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur, irrigation_notes, active, created_at, species, plot)
SELECT 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1', '22222222-2222-4222-8222-222222222222', 'María González', 'Limones · Eureka', '2026-2027', '2026-11-23', '2027-04-25', 50, 90, 244, 73, 244, 222, 67, 53.6, NULL, TRUE, '2026-11-23 10:00:00', 'Limones', 'Los Aromos · Eureka'
WHERE NOT EXISTS (SELECT 1 FROM programs WHERE id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1');

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '2be701c1-ef25-53c1-bd07-0873dc4761b5', 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1', 0, 'Fosfato monoamónico', 122, 12, 60, 0, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND name = 'Limones · Eureka') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND item_order = 0);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT 'f9293bbc-9bd2-59bd-a074-f73f0e45f96b', 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1', 1, 'Nitrato de calcio', 822, 16, 0, 0, 27, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND name = 'Limones · Eureka') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND item_order = 1);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '2207eaed-cbcb-5427-8104-39eede355322', 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1', 2, 'Sulfato de Magnesio Heptahidratado', 419, 0, 0, 0, 0, 16, 13 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND name = 'Limones · Eureka') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND item_order = 2);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT 'dfd1a811-b3d1-5e97-aae1-803c5627bb7c', 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1', 3, 'Nitrato de potasio', 542, 13, 0, 45, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND name = 'Limones · Eureka') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND item_order = 3);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '267da70d-da90-5caa-81e8-8498d36d9d02', 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1', 4, 'SmartBlue + N2 Stabil', 59, 46, 0, 0, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND name = 'Limones · Eureka') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND item_order = 4);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '8657962b-2b21-50cc-9909-1d130ec63a6e', 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1', 0, 'Brotación y crecimiento', 6, 3, 35 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND name = 'Limones · Eureka') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND item_order = 0);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '8d644c2d-1dcd-5c7e-92bf-fef0a9555eec', 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1', 1, 'Floración y cuaja', 4, 4, 25 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND name = 'Limones · Eureka') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND item_order = 1);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT 'dac98e05-6535-5a47-a41d-a647660882ab', 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1', 2, 'Desarrollo del fruto', 8, 5, 30 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND name = 'Limones · Eureka') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND item_order = 2);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT 'f8359f76-ec2d-523c-96ac-3986c696fcb3', 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1', 3, 'Maduración y cosecha', 4, 2, 10 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND name = 'Limones · Eureka') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'e7ac4b6d-31f6-5037-ba80-924bcf074fd1' AND item_order = 3);

INSERT INTO programs (id, advisor_id, advisor_name, name, season, start_date, end_date, target_yield_ton_ha, irrigation_efficiency_percent, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur, irrigation_notes, active, created_at, species, plot)
SELECT 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7', '22222222-2222-4222-8222-222222222222', 'María González', 'Naranjos · Navel', '2026-2027', '2026-11-23', '2027-04-25', 50, 90, 289, 87, 289, 233, 67, 53.6, NULL, TRUE, '2026-11-23 10:00:00', 'Naranjos', 'Santa Elena · Navel'
WHERE NOT EXISTS (SELECT 1 FROM programs WHERE id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7');

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '5ba8fa94-720a-5313-864a-f044fe62ba6d', 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7', 0, 'Fosfato monoamónico', 145, 12, 60, 0, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND name = 'Naranjos · Navel') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND item_order = 0);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT 'ed658c91-c58d-590f-8855-eac1afc4dd25', 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7', 1, 'Nitrato de calcio', 863, 16, 0, 0, 27, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND name = 'Naranjos · Navel') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND item_order = 1);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '6c6a42a6-2428-5b8d-87b1-4c50a334d999', 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7', 2, 'Sulfato de Magnesio Heptahidratado', 419, 0, 0, 0, 0, 16, 13 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND name = 'Naranjos · Navel') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND item_order = 2);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT 'b70a54c4-6543-579f-b028-c2d9463100ce', 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7', 3, 'Nitrato de potasio', 642, 13, 0, 45, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND name = 'Naranjos · Navel') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND item_order = 3);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '9c9eed8d-2b00-5e61-8374-c3ff2894713b', 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7', 4, 'SmartBlue + N2 Stabil', 109, 46, 0, 0, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND name = 'Naranjos · Navel') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND item_order = 4);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT 'e9c2bda9-7ae1-537d-9519-ba381bbc5183', 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7', 0, 'Brotación y crecimiento', 6, 3, 35 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND name = 'Naranjos · Navel') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND item_order = 0);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT 'a1066ee1-2c38-587c-8742-d330262e92f7', 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7', 1, 'Floración y cuaja', 4, 4, 25 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND name = 'Naranjos · Navel') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND item_order = 1);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '43cb66c8-646e-592b-8105-c3ff8b132939', 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7', 2, 'Desarrollo del fruto', 8, 5, 30 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND name = 'Naranjos · Navel') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND item_order = 2);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '88951f5b-c81b-5db7-9911-516f6077e557', 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7', 3, 'Maduración y cosecha', 4, 2, 10 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND name = 'Naranjos · Navel') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'c32ec4b8-1ccf-5ad9-b2d1-794b175ef2d7' AND item_order = 3);

INSERT INTO programs (id, advisor_id, advisor_name, name, season, start_date, end_date, target_yield_ton_ha, irrigation_efficiency_percent, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur, irrigation_notes, active, created_at, species, plot)
SELECT 'eb905714-a7cc-5865-b736-d69e22dde0a7', '22222222-2222-4222-8222-222222222222', 'María González', 'Uva vinífera · Cabernet Sauvignon', '2026-2027', '2026-11-23', '2027-04-25', 25, 90, 122, 56, 167, 122, 39, 31.2, NULL, TRUE, '2026-11-23 10:00:00', 'Uva vinífera', 'Santa Elena · Cabernet Sauvignon'
WHERE NOT EXISTS (SELECT 1 FROM programs WHERE id = 'eb905714-a7cc-5865-b736-d69e22dde0a7');

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT 'e6b3ad88-5c72-527c-8def-d2a186bd12a9', 'eb905714-a7cc-5865-b736-d69e22dde0a7', 0, 'Fosfato monoamónico', 93, 12, 60, 0, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND name = 'Uva vinífera · Cabernet Sauvignon') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND item_order = 0);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '4474d3d2-8d24-5c1c-9ef1-d28dd3312009', 'eb905714-a7cc-5865-b736-d69e22dde0a7', 1, 'Nitrato de calcio', 452, 16, 0, 0, 27, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND name = 'Uva vinífera · Cabernet Sauvignon') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND item_order = 1);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '4a187cda-d6cc-5870-8e1a-53fec826ebe3', 'eb905714-a7cc-5865-b736-d69e22dde0a7', 2, 'Sulfato de Magnesio Heptahidratado', 244, 0, 0, 0, 0, 16, 13 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND name = 'Uva vinífera · Cabernet Sauvignon') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND item_order = 2);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '381f43c3-c06a-5e97-ba28-41e68021d85b', 'eb905714-a7cc-5865-b736-d69e22dde0a7', 3, 'Nitrato de potasio', 371, 13, 0, 45, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND name = 'Uva vinífera · Cabernet Sauvignon') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND item_order = 3);

INSERT INTO program_fertilizers (id, program_id, item_order, name, dose_kg_ha, nitrogen, phosphorus_oxide, potassium_oxide, calcium_oxide, magnesium_oxide, sulfur) SELECT '010e015b-7109-5d09-ad0d-9c6e5657899c', 'eb905714-a7cc-5865-b736-d69e22dde0a7', 4, 'SmartBlue + N2 Stabil', 0, 46, 0, 0, 0, 0, 0 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND name = 'Uva vinífera · Cabernet Sauvignon') AND NOT EXISTS (SELECT 1 FROM program_fertilizers WHERE program_id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND item_order = 4);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT 'b2f9804b-4ed8-5f92-a968-a90f7fce81ca', 'eb905714-a7cc-5865-b736-d69e22dde0a7', 0, 'Brotación y crecimiento', 6, 3, 35 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND name = 'Uva vinífera · Cabernet Sauvignon') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND item_order = 0);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '5308cb33-167b-5556-9e42-38c9c656db62', 'eb905714-a7cc-5865-b736-d69e22dde0a7', 1, 'Floración y cuaja', 4, 4, 25 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND name = 'Uva vinífera · Cabernet Sauvignon') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND item_order = 1);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT 'b21412b7-39f7-59c2-89e6-65a3a0d2b552', 'eb905714-a7cc-5865-b736-d69e22dde0a7', 2, 'Desarrollo del fruto', 8, 5, 30 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND name = 'Uva vinífera · Cabernet Sauvignon') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND item_order = 2);

INSERT INTO program_stages (id, program_id, item_order, name, weeks, irrigations_per_week, allocation_percent) SELECT '52b4d768-0447-55f0-b5cc-d8d5abb4dad9', 'eb905714-a7cc-5865-b736-d69e22dde0a7', 3, 'Maduración y cosecha', 4, 2, 10 WHERE EXISTS (SELECT 1 FROM programs WHERE id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND name = 'Uva vinífera · Cabernet Sauvignon') AND NOT EXISTS (SELECT 1 FROM program_stages WHERE program_id = 'eb905714-a7cc-5865-b736-d69e22dde0a7' AND item_order = 3);

DROP TABLE sample_program_to_replace;
