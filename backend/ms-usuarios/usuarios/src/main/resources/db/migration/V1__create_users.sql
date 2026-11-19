CREATE TABLE users (
    id UUID PRIMARY KEY,
    run VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    password_hash VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    phone_number VARCHAR(255),
    region VARCHAR(255),
    comuna VARCHAR(255),

    CONSTRAINT ck_users_role
        CHECK (role IN ('FARMER', 'ADVISOR'))
);
