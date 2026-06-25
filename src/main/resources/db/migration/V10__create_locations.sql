CREATE TABLE locations(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL
);

INSERT INTO locations (name, type) VALUES
    ('Agora store', 'STORE'),
    ('Main Warehouse', 'WAREHOUSE');
    