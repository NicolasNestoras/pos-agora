CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    customer_id BIGINT REFERENCES customers(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Seeded admin account. Password is "ChangeMe123!" — bcrypt hash below.
-- Log in once the app is running, then this should be the very first
-- thing you change(via a future "change password" endpoint).
INSERT INTO users (email, password_hash, role) VALUES
    ('admin@agora-retail.com', '$2a$12$DjbtJQ57oXi38UP1dAepUOPyKBhwwOF7e.dNS/H5RM/oolLDc2obu', 'ADMIN');