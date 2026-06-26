CREATE TABLE backorders (
    id BIGSERIAL PRIMARY KEY,
    product_variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    order_id BIGINT NOT NULL REFERENCES orders(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    linked_incoming_stock_id BIGINT REFERENCES incoming_stock(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_backorders_variant_status ON backorders(product_variant_id, status);
