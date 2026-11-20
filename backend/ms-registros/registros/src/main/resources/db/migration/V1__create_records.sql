CREATE TABLE farms (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(150) NOT NULL,
    image_path VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- user_id pertenece al ms de usuarios; no hay una FK entre microservicios.
CREATE TABLE farm_users (
    id VARCHAR(36) PRIMARY KEY,
    farm_id VARCHAR(36) NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL,
    CONSTRAINT uk_farm_users_farm_user UNIQUE (farm_id, user_id)
);

CREATE TABLE crops (
    id VARCHAR(36) PRIMARY KEY,
    farm_id VARCHAR(36) NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    variety VARCHAR(100),
    area_hectares DOUBLE PRECISION,
    phenological_stage VARCHAR(100),
    image_path VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE records (
    id VARCHAR(36) PRIMARY KEY,
    crop_id VARCHAR(36) NOT NULL REFERENCES crops(id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL,
    date VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    observations TEXT
);

CREATE TABLE soil_data (
    record_id VARCHAR(36) PRIMARY KEY REFERENCES records(id) ON DELETE CASCADE,
    ph DOUBLE PRECISION,
    electrical_conductivity DOUBLE PRECISION,
    nitrogen DOUBLE PRECISION,
    phosphorus DOUBLE PRECISION,
    potassium DOUBLE PRECISION,
    calcium DOUBLE PRECISION,
    magnesium DOUBLE PRECISION,
    organic_matter DOUBLE PRECISION
);

CREATE TABLE water_data (
    record_id VARCHAR(36) PRIMARY KEY REFERENCES records(id) ON DELETE CASCADE,
    ph DOUBLE PRECISION,
    electrical_conductivity DOUBLE PRECISION,
    nitrogen DOUBLE PRECISION,
    phosphorus DOUBLE PRECISION,
    potassium DOUBLE PRECISION,
    calcium DOUBLE PRECISION,
    magnesium DOUBLE PRECISION,
    water_source VARCHAR(150)
);

CREATE TABLE foliar_data (
    record_id VARCHAR(36) PRIMARY KEY REFERENCES records(id) ON DELETE CASCADE,
    nitrogen DOUBLE PRECISION,
    phosphorus DOUBLE PRECISION,
    potassium DOUBLE PRECISION,
    calcium DOUBLE PRECISION,
    magnesium DOUBLE PRECISION,
    symptoms TEXT
);

CREATE TABLE foliar_photos (
    id VARCHAR(36) PRIMARY KEY,
    record_id VARCHAR(36) NOT NULL REFERENCES records(id) ON DELETE CASCADE,
    path TEXT NOT NULL,
    storage_key VARCHAR(40),
    content_type VARCHAR(50),
    size_bytes BIGINT CHECK (size_bytes > 0)
);

CREATE TABLE record_reviews (
    id VARCHAR(36) PRIMARY KEY,
    record_id VARCHAR(36) NOT NULL REFERENCES records(id) ON DELETE CASCADE,
    advisor_id VARCHAR(36) NOT NULL,
    advisor_name VARCHAR(150) NOT NULL,
    date VARCHAR(30) NOT NULL,
    result VARCHAR(20) NOT NULL,
    comment TEXT NOT NULL
);

CREATE TABLE programs (
    id VARCHAR(36) PRIMARY KEY,
    crop_id VARCHAR(36) NOT NULL REFERENCES crops(id) ON DELETE CASCADE,
    advisor_id VARCHAR(36) NOT NULL,
    advisor_name VARCHAR(511) NOT NULL,
    name VARCHAR(150) NOT NULL,
    season VARCHAR(30),
    start_date DATE NOT NULL,
    end_date DATE,
    target_yield_ton_ha DOUBLE PRECISION,
    irrigation_efficiency_percent DOUBLE PRECISION,
    nitrogen DOUBLE PRECISION,
    phosphorus_oxide DOUBLE PRECISION,
    potassium_oxide DOUBLE PRECISION,
    calcium_oxide DOUBLE PRECISION,
    magnesium_oxide DOUBLE PRECISION,
    sulfur DOUBLE PRECISION,
    irrigation_notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE program_fertilizers (
    id VARCHAR(36) PRIMARY KEY,
    program_id VARCHAR(36) NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    item_order INTEGER NOT NULL,
    name VARCHAR(150) NOT NULL,
    dose_kg_ha DOUBLE PRECISION NOT NULL CHECK (dose_kg_ha >= 0),
    notes TEXT,
    UNIQUE (program_id, item_order)
);

CREATE TABLE program_stages (
    id VARCHAR(36) PRIMARY KEY,
    program_id VARCHAR(36) NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    item_order INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    weeks INTEGER CHECK (weeks > 0),
    irrigations_per_week INTEGER CHECK (irrigations_per_week >= 0),
    instructions TEXT,
    UNIQUE (program_id, item_order)
);

CREATE INDEX idx_programs_crop_date ON programs(crop_id, start_date);

-- Indices para buscar hijos sin recorrer todas las filas de cada tabla.
CREATE INDEX idx_farm_users_user ON farm_users(user_id);
CREATE INDEX idx_crops_farm ON crops(farm_id);
CREATE INDEX idx_records_crop_date ON records(crop_id, date);
CREATE INDEX idx_records_status ON records(status);
CREATE INDEX idx_records_user ON records(user_id);
CREATE INDEX idx_foliar_photos_record ON foliar_photos(record_id);
CREATE INDEX idx_record_reviews_record ON record_reviews(record_id);
