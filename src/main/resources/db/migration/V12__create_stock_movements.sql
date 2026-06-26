CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    location_id BIGINT NOT NULL REFERENCES locations(id),
    quantity_change INTEGER NOT NULL,
    reason VARCHAR(50) NOT NULL,
    reference_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_stock_movements_variant_location ON stock_movements(product_variant_id, location_id);