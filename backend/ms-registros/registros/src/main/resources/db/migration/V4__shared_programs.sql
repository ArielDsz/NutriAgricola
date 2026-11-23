-- Los programas son de consulta común; no tienen destinatario individual.
-- Migración separada por si V3 ya fue aplicada al reiniciar el servicio.
DROP INDEX idx_programs_farmer_date;
ALTER TABLE programs DROP COLUMN farmer_id;
ALTER TABLE programs DROP COLUMN farmer_name;
