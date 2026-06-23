CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    sku VARCHAR(100) NOT NULL UNIQUE,
    size VARCHAR(50),
    color VARCHAR(50),
    price NUMERIC(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);