-- employee_management_sample_data.sql
-- Run AFTER employee_management_schema.sql

USE employee_management_system;

-- ----------------------------------------------------------------------------------
-- IMPORTANT: Make sure job_titles has column `title_name` (not `job_title`).
-- If needed, run this once:
-- ALTER TABLE job_titles
--     CHANGE COLUMN job_title title_name VARCHAR(100) NOT NULL UNIQUE;
-- ----------------------------------------------------------------------------------

-- 1. States
INSERT INTO state (state_code, state_name) VALUES
  ('GA','Georgia'),
  ('FL','Florida'),
  ('AL','Alabama'),
  ('TN','Tennessee');

-- 2. Cities (linked by state_code to be robust to IDs)
INSERT INTO city (city_name, state_id) VALUES
  ('Atlanta',
      (SELECT state_id FROM state WHERE state_code = 'GA')),
  ('Savannah',
      (SELECT state_id FROM state WHERE state_code = 'GA')),
  ('Miami',
      (SELECT state_id FROM state WHERE state_code = 'FL')),
  ('Orlando',
      (SELECT state_id FROM state WHERE state_code = 'FL')),
  ('Birmingham',
      (SELECT state_id FROM state WHERE state_code = 'AL')),
  ('Nashville',
      (SELECT state_id FROM state WHERE state_code = 'TN'));

-- 3. Divisions
INSERT INTO division (division_name, division_code) VALUES
  ('Information Technology', 'IT'),
  ('Human Resources',        'HR'),
  ('Finance',                'FIN'),
  ('Marketing',              'MKT'),
  ('Operations',             'OPS');

-- 4. Job titles (uses `title_name` to match JobTitleDAO)
INSERT INTO job_titles (title_name, base_salary) VALUES
  ('Software Developer',              85000.00),
  ('Senior Software Developer',      105000.00),
  ('HR Manager',                      90000.00),
  ('HR Generalist',                   65000.00),
  ('Financial Analyst',               78000.00),
  ('Senior Financial Analyst',        95000.00),
  ('Marketing Specialist',            68000.00),
  ('Marketing Manager',               88000.00),
  ('Operations Manager',              92000.00),
  ('Customer Support Representative', 45000.00);

-- 5. Employees (mix of roles/statuses for reports)
INSERT INTO employees (emp_number, first_name, last_name, email, ssn,
                       hire_date, current_salary, employment_status)
VALUES
  ('E1001','Alice','Johnson','alice.johnson@example.com','111-11-1111',
   '2020-03-15', 85000.00,'ACTIVE'),
  ('E1002','Bob','Smith','bob.smith@example.com','222-22-2222',
   '2019-07-01', 92000.00,'ACTIVE'),
  ('E1003','Carol','Davis','carol.davis@example.com','333-33-3333',
   '2021-11-20', 78000.00,'ACTIVE'),
  ('E1004','David','Lee','david.lee@example.com','444-44-4444',
   '2018-01-08', 68000.00,'TERMINATED'),
  ('E1005','Emma','Wilson','emma.wilson@example.com','555-55-5555',
   '2022-06-10', 45000.00,'ACTIVE');

-- 6. Addresses (1:1 per employee, all FKs satisfied)
INSERT INTO address (empid, street, city_id, state_id, zip,
                     gender, race, date_of_birth, phone)
VALUES
  ((SELECT empid FROM employees WHERE emp_number = 'E1001'),
   '123 Peachtree St NE',
   (SELECT city_id FROM city
      WHERE city_name = 'Atlanta'
        AND state_id = (SELECT state_id FROM state WHERE state_code = 'GA')),
   (SELECT state_id FROM state WHERE state_code = 'GA'),
   '30303','F','White','1990-04-10','404-555-0101'),

  ((SELECT empid FROM employees WHERE emp_number = 'E1002'),
   '200 Biscayne Blvd',
   (SELECT city_id FROM city
      WHERE city_name = 'Miami'
        AND state_id = (SELECT state_id FROM state WHERE state_code = 'FL')),
   (SELECT state_id FROM state WHERE state_code = 'FL'),
   '33101','M','Black or African American','1985-09-22','305-555-0102'),

  ((SELECT empid FROM employees WHERE emp_number = 'E1003'),
   '10 Bay St',
   (SELECT city_id FROM city
      WHERE city_name = 'Savannah'
        AND state_id = (SELECT state_id FROM state WHERE state_code = 'GA')),
   (SELECT state_id FROM state WHERE state_code = 'GA'),
   '31401','F','Asian','1992-02-18','912-555-0103'),

  ((SELECT empid FROM employees WHERE emp_number = 'E1004'),
   '500 3rd Ave N',
   (SELECT city_id FROM city
      WHERE city_name = 'Nashville'
        AND state_id = (SELECT state_id FROM state WHERE state_code = 'TN')),
   (SELECT state_id FROM state WHERE state_code = 'TN'),
   '37201','M','White','1980-12-05','615-555-0104'),

  ((SELECT empid FROM employees WHERE emp_number = 'E1005'),
   '700 20th St S',
   (SELECT city_id FROM city
      WHERE city_name = 'Birmingham'
        AND state_id = (SELECT state_id FROM state WHERE state_code = 'AL')),
   (SELECT state_id FROM state WHERE state_code = 'AL'),
   '35233','F','Hispanic or Latino','1995-07-30','205-555-0105');

-- 7. Employee division assignments (used by division reports)
INSERT INTO employee_division (empid, div_id, start_date, end_date, is_current)
VALUES
  ((SELECT empid FROM employees WHERE emp_number = 'E1001'),
   (SELECT div_id FROM division WHERE division_code = 'IT'),
   '2020-03-15', NULL, 1),

  ((SELECT empid FROM employees WHERE emp_number = 'E1002'),
   (SELECT div_id FROM division WHERE division_code = 'HR'),
   '2019-07-01', NULL, 1),

  ((SELECT empid FROM employees WHERE emp_number = 'E1003'),
   (SELECT div_id FROM division WHERE division_code = 'FIN'),
   '2021-11-20', NULL, 1),

  ((SELECT empid FROM employees WHERE emp_number = 'E1004'),
   (SELECT div_id FROM division WHERE division_code = 'MKT'),
   '2018-01-08','2023-01-15',0),

  ((SELECT empid FROM employees WHERE emp_number = 'E1005'),
   (SELECT div_id FROM division WHERE division_code = 'OPS'),
   '2022-06-10', NULL, 1);

-- 8. Employee job title assignments (used by job-title reports)
INSERT INTO employee_job_titles (empid, job_title_id, start_date, end_date, is_current)
VALUES
  ((SELECT empid FROM employees WHERE emp_number = 'E1001'),
   (SELECT job_title_id FROM job_titles WHERE title_name = 'Software Developer'),
   '2020-03-15', NULL, 1),

  ((SELECT empid FROM employees WHERE emp_number = 'E1002'),
   (SELECT job_title_id FROM job_titles WHERE title_name = 'HR Manager'),
   '2019-07-01', NULL, 1),

  ((SELECT empid FROM employees WHERE emp_number = 'E1003'),
   (SELECT job_title_id FROM job_titles WHERE title_name = 'Financial Analyst'),
   '2021-11-20', NULL, 1),

  ((SELECT empid FROM employees WHERE emp_number = 'E1004'),
   (SELECT job_title_id FROM job_titles WHERE title_name = 'Marketing Specialist'),
   '2018-01-08','2023-01-15',0),

  ((SELECT empid FROM employees WHERE emp_number = 'E1005'),
   (SELECT job_title_id FROM job_titles WHERE title_name = 'Customer Support Representative'),
   '2022-06-10', NULL, 1);

-- 9. Payroll history (3 months in 2024 for each ACTIVE employee)
-- January 2024
INSERT INTO payroll (empid, pay_date, pay_period_start, pay_period_end,
                     gross_pay, net_pay, federal_tax, state_tax, other_deductions)
VALUES
  ((SELECT empid FROM employees WHERE emp_number = 'E1001'),
   '2024-01-31','2024-01-01','2024-01-31',7083.33,5400.00,1100.00,400.00,583.33),

  ((SELECT empid FROM employees WHERE emp_number = 'E1002'),
   '2024-01-31','2024-01-01','2024-01-31',7666.67,5800.00,1200.00,420.00,246.67),

  ((SELECT empid FROM employees WHERE emp_number = 'E1003'),
   '2024-01-31','2024-01-01','2024-01-31',6500.00,5000.00,900.00,350.00,250.00),

  ((SELECT empid FROM employees WHERE emp_number = 'E1005'),
   '2024-01-31','2024-01-01','2024-01-31',3750.00,2900.00,500.00,200.00,150.00);

-- February 2024
INSERT INTO payroll (empid, pay_date, pay_period_start, pay_period_end,
                     gross_pay, net_pay, federal_tax, state_tax, other_deductions)
VALUES
  ((SELECT empid FROM employees WHERE emp_number = 'E1001'),
   '2024-02-29','2024-02-01','2024-02-29',7083.33,5450.00,1080.00,400.00,553.33),

  ((SELECT empid FROM employees WHERE emp_number = 'E1002'),
   '2024-02-29','2024-02-01','2024-02-29',7666.67,5850.00,1180.00,420.00,216.67),

  ((SELECT empid FROM employees WHERE emp_number = 'E1003'),
   '2024-02-29','2024-02-01','2024-02-29',6500.00,5050.00,880.00,350.00,220.00),

  ((SELECT empid FROM employees WHERE emp_number = 'E1005'),
   '2024-02-29','2024-02-01','2024-02-29',3750.00,2950.00,480.00,200.00,120.00);

-- March 2024
INSERT INTO payroll (empid, pay_date, pay_period_start, pay_period_end,
                     gross_pay, net_pay, federal_tax, state_tax, other_deductions)
VALUES
  ((SELECT empid FROM employees WHERE emp_number = 'E1001'),
   '2024-03-31','2024-03-01','2024-03-31',7083.33,5500.00,1060.00,400.00,523.33),

  ((SELECT empid FROM employees WHERE emp_number = 'E1002'),
   '2024-03-31','2024-03-01','2024-03-31',7666.67,5900.00,1160.00,420.00,186.67),

  ((SELECT empid FROM employees WHERE emp_number = 'E1003'),
   '2024-03-31','2024-03-01','2024-03-31',6500.00,5100.00,860.00,350.00,190.00),

  ((SELECT empid FROM employees WHERE emp_number = 'E1005'),
   '2024-03-31','2024-03-01','2024-03-31',3750.00,3000.00,460.00,200.00,90.00);

-- 10. Users (logins)
-- Not inserted here because password_hash must be created
-- via your Java PasswordUtils (SecurityService.createUser).
-- See next step in our conversation for a small Java helper
-- to create 'hradmin' and one or two employee logins.
