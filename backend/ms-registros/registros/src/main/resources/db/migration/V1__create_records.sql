CREATE TABLE farms (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(150) NOT NULL,
    image_path VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE
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
    path TEXT NOT NULL
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

-- Indices para buscar hijos sin recorrer todas las filas de cada tabla.
CREATE INDEX idx_crops_farm ON crops(farm_id);
CREATE INDEX idx_records_crop_date ON records(crop_id, date);
CREATE INDEX idx_records_status ON records(status);
CREATE INDEX idx_records_user ON records(user_id);
CREATE INDEX idx_foliar_photos_record ON foliar_photos(record_id);
CREATE INDEX idx_record_reviews_record ON record_reviews(record_id);
