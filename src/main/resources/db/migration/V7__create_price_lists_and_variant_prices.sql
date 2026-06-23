CREATE TABLE price_lists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE variant_prices (
    id BIGSERIAL PRIMARY KEY,
    product_variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    price_list_id BIGINT NOT NULL REFERENCES price_lists(id),
    price NUMERIC(10,2) NOT NULL,
    UNIQUE (product_variant_id, price_list_id)
);

INSERT INTO price_lists (name) VALUES ('RETAIL'), ('WHOLESALE');