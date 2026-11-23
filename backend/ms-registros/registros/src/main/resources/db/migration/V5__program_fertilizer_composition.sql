-- Instantánea de porcentajes del catálogo usado. NULL conserva los productos manuales anteriores.
ALTER TABLE program_fertilizers ADD COLUMN nitrogen DOUBLE PRECISION;
ALTER TABLE program_fertilizers ADD COLUMN phosphorus_oxide DOUBLE PRECISION;
ALTER TABLE program_fertilizers ADD COLUMN potassium_oxide DOUBLE PRECISION;
ALTER TABLE program_fertilizers ADD COLUMN calcium_oxide DOUBLE PRECISION;
ALTER TABLE program_fertilizers ADD COLUMN magnesium_oxide DOUBLE PRECISION;
ALTER TABLE program_fertilizers ADD COLUMN sulfur DOUBLE PRECISION;
