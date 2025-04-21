-- admin (role: ADMIN)
INSERT INTO users (id, username, password, roles) VALUES
(1,
 'admin',
 -- BCrypt hash of 'admin123'
 '$2a$10$Dow1y8v.8k74sbdWnDs87.VLmFXcbVuqk0lutpBdPKQ4IkD6.LqfS',
 'ADMIN');

-- customer (role: CUSTOMER)
INSERT INTO users (id, username, password, roles) VALUES
(2,
 '1001',                           -- username = customerId string!
 '$2a$10$ZYUUEYZ1GVYDfp6JxkyE8OI.MypyE6Wfp7yH5YqObzwB6mhuCzq1K', -- hash of 'cust123'
 'CUSTOMER');

INSERT INTO assets (customer_id, asset_name, size, usable_size) VALUES
(1001, 'TRY', 100000, 100000);
