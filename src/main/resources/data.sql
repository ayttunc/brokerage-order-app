-- Customer
INSERT INTO customers (id, username, password, roles) VALUES (1, 'testuser', 'password', 'ROLE_USER');

-- TRY Asset (usable and equals to total size)
INSERT INTO assets (id, customer_id, asset_name, size, usable_size)
VALUES (1, 1, 'TRY', 10000.00, 10000.00);

-- Different asset (e.g THYAO)
INSERT INTO assets (id, customer_id, asset_name, size, usable_size)
VALUES (2, 1, 'THYAO', 100, 100);