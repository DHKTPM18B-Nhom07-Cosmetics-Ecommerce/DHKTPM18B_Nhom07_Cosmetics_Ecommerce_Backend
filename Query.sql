INSERT INTO accounts (created_at, disabled_reason, full_name, password, phone_number, role, status, username)
VALUES (NOW(), NULL, 'Nguyễn Văn A', '123456', '0901112222', 'ADMIN', 'ACTIVE', 'admin@example.com');

INSERT INTO employees (hire_date, account_id)
VALUES (NOW(), 1);

INSERT INTO accounts (created_at, disabled_reason, full_name, password, phone_number, role, status, username)
VALUES (NOW(), NULL, 'Trần Thị B', '123456', '0903334444', 'EMPLOYEE', 'ACTIVE', 'employee2@example.com');

INSERT INTO employees (hire_date, account_id)
VALUES (NOW(), 2);