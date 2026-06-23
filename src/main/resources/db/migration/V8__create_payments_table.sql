CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT REFERENCES sales(id),
    order_id BIGINT REFERENCES orders(id),
    method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_payment_target CHECK (
        (sale_id IS NOT NULL AND order_id IS NULL) OR
        (sale_id IS NULL AND order_id IS NOT NULL)
    )
);

CREATE INDEX idx_payments_sale_id ON payments(sale_id);
CREATE INDEX idx_payments_order_id ON payments(order_id);