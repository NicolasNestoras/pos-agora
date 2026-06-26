CREATE TABLE incoming_stock (
    id BIGSERIAL PRIMARY KEY,
    product_variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    location_id BIGINT NOT NULL REFERENCES locations(id),
    expected_quantity INTEGER NOT NULL CHECK (expected_quantity > 0),
    expected_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'EXPECTED',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
