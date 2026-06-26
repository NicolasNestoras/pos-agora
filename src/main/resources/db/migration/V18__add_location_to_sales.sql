ALTER TABLE sales ADD COLUMN location_id BIGINT;

UPDATE sales SET location_id = (SELECT id FROM locations WHERE name = 'Main Store')
WHERE location_id IS NULL;

ALTER TABLE sales ALTER COLUMN location_id SET NOT NULL;
ALTER TABLE sales ADD CONSTRAINT fk_sales_location FOREIGN KEY (location_id) REFERENCES locations(id);

/*Postgres cannot add NOT NULL to a column that already has rows, so we have to
add values to all the existing rows first, and then set it to NOT NULL.