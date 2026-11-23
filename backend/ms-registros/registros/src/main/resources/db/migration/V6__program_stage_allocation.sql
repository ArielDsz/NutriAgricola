-- Conserva las etapas manuales anteriores sin inventar su distribución.
ALTER TABLE program_stages ADD COLUMN allocation_percent DOUBLE PRECISION
    CHECK (allocation_percent >= 0 AND allocation_percent <= 100);
