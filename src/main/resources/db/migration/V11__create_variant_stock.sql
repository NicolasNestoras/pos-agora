CREATE TABLE variant_stock (
    id BIGSERIAL PRIMARY KEY,
    product_variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    location_id BIGINT NOT NULL REFERENCES locations(id),
    on_hand_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_variant_stock_variant_location UNIQUE (product_variant_id, location_id),
    CONSTRAINT chk_on_hand_non_negative CHECK (on_hand_quantity >= 0),
    CONSTRAINT chk_reserved_non_negative CHECK (reserved_quantity >= 0),
    CONSTRAINT chk_reserved_not_exceeding_on_hand CHECK (reserved_quantity <= on_hand_quantity)
);