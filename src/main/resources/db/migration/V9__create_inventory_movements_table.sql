CREATE TABLE inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    product_variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    movement_type VARCHAR(20) NOT NULL,
    quantity_change INT NOT NULL,
    reference_id BIGINT,
    note VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_inventory_movements_variant_id ON inventory_movements(product_variant_id);
CREATE INDEX idx_inventory_movements_created_at ON inventory_movements(created_at);