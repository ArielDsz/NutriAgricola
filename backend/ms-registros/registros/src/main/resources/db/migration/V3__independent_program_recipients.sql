-- Conserva los programas anteriores y su cultivo como referencia opcional.
ALTER TABLE programs ALTER COLUMN crop_id DROP NOT NULL;
ALTER TABLE programs ADD COLUMN farmer_id VARCHAR(36);
ALTER TABLE programs ADD COLUMN farmer_name VARCHAR(511);
ALTER TABLE programs ADD COLUMN species VARCHAR(150);
ALTER TABLE programs ADD COLUMN plot VARCHAR(150);

-- Destinatario conocido del ejemplo ficticio. No se infieren responsables de otros programas.
UPDATE programs SET farmer_id = '11111111-1111-4111-8111-111111111111',
    farmer_name = 'Agricultor Prueba', species = 'Uva de mesa'
WHERE id = '88888888-8888-4888-8888-888888888888';

CREATE INDEX idx_programs_farmer_date ON programs(farmer_id, start_date);
CREATE INDEX idx_programs_advisor_date ON programs(advisor_id, start_date);
