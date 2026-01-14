-- Sample Data for Employee Management System
-- This script populates the database with test data for development and testing
-- Run this after creating the database schema

USE employee_management_system;

-- Insert sample states
INSERT INTO state (state_code, state_name) VALUES 
('GA', 'Georgia'),
('FL', 'Florida'),
('AL', 'Alabama'),
('TN', 'Tennessee'),
('NC', 'North Carolina'),
('SC', 'South Carolina');

-- Insert sample cities
INSERT INTO city (city_name, state_id) VALUES 
-- Georgia cities
('Atlanta', 1),
('Savannah', 1),
('Augusta', 1),
('Columbus', 1),
-- Florida cities
('Miami', 2),
('Jacksonville', 2),
('Tampa', 2),
('Orlando', 2),
-- Alabama cities
('Birmingham', 3),
('Mobile', 3),
-- Tennessee cities
('Nashville', 4),
('Memphis', 4),
-- North Carolina cities
('Charlotte', 5),
('Raleigh', 5),
-- South Carolina cities
('Charleston', 6),
('Columbia', 6);

-- Insert sample divisions
INSERT INTO division (division_name, division_code) VALUES 
('Information Technology', 'IT'),
('Human Resources', 'HR'),
('Finance', 'FIN'),
('Marketing', 'MKT'),
('Operations', 'OPS'),
('Sales', 'SALES'),
('Customer Service', 'CS'),
('Research and Development', 'RND');

-- Insert sample job titles
INSERT INTO job_titles (job_title) VALUES 
('Software Developer'),
('Senior Software Developer'),
('Software Engineer'),
('Database Administrator'),
('System Administrator'),
('HR Manager'),
('HR Assistant'),
('Recruiter'),
('Financial Analyst'),
('Accountant'),
('Finance Manager'),
('Marketing Specialist'),
('Marketing Manager'),
('Content Creator'),
('Operations Manager'),
('Operations Coordinator'),
('Sales Representative'),
('Sales Manager'),
('Customer Service Representative'),
('Customer Service Manager'),
('Research Scientist'),
('Product Manager'),
('Project Manager'),
('Quality Assurance Engineer'),
('Business Analyst');

-- Insert sample employees
INSERT INTO employees (emp_number, first_name, last_name, email, ssn, hire_date, current_salary, employment_status) VALUES 
('EMP001', 'John', 'Smith', 'john.smith@companyz.com', '123-45-6789', '2023-01-15', 75000.00, 'ACTIVE'),
('EMP002', 'Sarah', 'Johnson', 'sarah.johnson@companyz.com', '234-56-7890', '2023-02-01', 82000.00, 'ACTIVE'),
('EMP003', 'Michael', 'Brown', 'michael.brown@companyz.com', '345-67-8901', '2023-02-15', 68000.00, 'ACTIVE'),
('EMP004', 'Emily', 'Davis', 'emily.davis@companyz.com', '456-78-9012', '2023-03-01', 95000.00, 'ACTIVE'),
('EMP005', 'David', 'Wilson', 'david.wilson@companyz.com', '567-89-0123', '2023-03-15', 72000.00, 'ACTIVE'),
('EMP006', 'Lisa', 'Miller', 'lisa.miller@companyz.com', '678-90-1234', '2023-04-01', 88000.00, 'ACTIVE'),
('EMP007', 'James', 'Garcia', 'james.garcia@companyz.com', '789-01-2345', '2023-04-15', 76000.00, 'ACTIVE'),
('EMP008', 'Jennifer', 'Martinez', 'jennifer.martinez@companyz.com', '890-12-3456', '2023-05-01', 91000.00, 'ACTIVE'),
('EMP009', 'Robert', 'Anderson', 'robert.anderson@companyz.com', '901-23-4567', '2023-05-15', 64000.00, 'ACTIVE'),
('EMP010', 'Michelle', 'Taylor', 'michelle.taylor@companyz.com', '012-34-5678', '2023-06-01', 79000.00, 'ACTIVE'),
-- HR Admin user
('EMP011', 'Admin', 'User', 'admin@companyz.com', '111-22-3333', '2022-01-01', 85000.00, 'ACTIVE'),
-- Additional employees
('EMP012', 'Christopher', 'Thomas', 'chris.thomas@companyz.com', '123-45-6780', '2023-06-15', 73000.00, 'ACTIVE'),
('EMP013', 'Amanda', 'Jackson', 'amanda.jackson@companyz.com', '234-56-7891', '2023-07-01', 86000.00, 'ACTIVE'),
('EMP014', 'Daniel', 'White', 'daniel.white@companyz.com', '345-67-8902', '2023-07-15', 70000.00, 'ACTIVE'),
('EMP015', 'Jessica', 'Harris', 'jessica.harris@companyz.com', '456-78-9013', '2023-08-01', 92000.00, 'ACTIVE');

-- Insert sample addresses with demographics
INSERT INTO address (empid, street, city_id, state_id, zip, gender, race, date_of_birth, phone) VALUES 
(1, '123 Main St', 1, 1, '30301', 'M', 'White', '1990-05-15', '404-555-0101'),
(2, '456 Oak Ave', 1, 1, '30302', 'F', 'African American', '1988-08-22', '404-555-0102'),
(3, '789 Pine Rd', 2, 1, '31401', 'M', 'Hispanic', '1992-03-10', '912-555-0103'),
(4, '321 Elm St', 1, 1, '30303', 'F', 'Asian', '1985-11-30', '404-555-0104'),
(5, '654 Maple Dr', 5, 2, '33101', 'M', 'White', '1991-07-18', '305-555-0105'),
(6, '987 Cedar Ln', 1, 1, '30304', 'F', 'African American', '1989-12-05', '404-555-0106'),
(7, '147 Birch Ct', 6, 2, '32201', 'M', 'Hispanic', '1993-02-28', '904-555-0107'),
(8, '258 Willow Way', 1, 1, '30305', 'F', 'White', '1987-09-14', '404-555-0108'),
(9, '369 Spruce St', 9, 3, '35201', 'M', 'White', '1994-04-07', '205-555-0109'),
(10, '741 Poplar Ave', 1, 1, '30306', 'F', 'Asian', '1986-06-25', '404-555-0110'),
(11, '852 Admin Blvd', 1, 1, '30307', 'Other', 'Prefer not to say', '1980-01-01', '404-555-0111'),
(12, '963 Tech Dr', 7, 2, '33602', 'M', 'White', '1990-10-12', '813-555-0112'),
(13, '159 Market St', 13, 5, '28201', 'F', 'African American', '1991-01-20', '704-555-0113'),
(14, '357 Business Ct', 11, 4, '37201', 'M', 'Hispanic', '1989-08-15', '615-555-0114'),
(15, '951 Corporate Way', 1, 1, '30308', 'F', 'Asian', '1988-05-03', '404-555-0115');

-- Insert employee-division relationships (current assignments)
INSERT INTO employee_division (empid, div_id, start_date, end_date, is_current) VALUES 
(1, 1, '2023-01-15', NULL, 1),  -- John - IT
(2, 1, '2023-02-01', NULL, 1),  -- Sarah - IT
(3, 1, '2023-02-15', NULL, 1),  -- Michael - IT
(4, 1, '2023-03-01', NULL, 1),  -- Emily - IT
(5, 3, '2023-03-15', NULL, 1),  -- David - Finance
(6, 4, '2023-04-01', NULL, 1),  -- Lisa - Marketing
(7, 5, '2023-04-15', NULL, 1),  -- James - Operations
(8, 6, '2023-05-01', NULL, 1),  -- Jennifer - Sales
(9, 7, '2023-05-15', NULL, 1),  -- Robert - Customer Service
(10, 2, '2023-06-01', NULL, 1), -- Michelle - HR
(11, 2, '2022-01-01', NULL, 1), -- Admin - HR
(12, 1, '2023-06-15', NULL, 1), -- Christopher - IT
(13, 4, '2023-07-01', NULL, 1), -- Amanda - Marketing
(14, 3, '2023-07-15', NULL, 1), -- Daniel - Finance
(15, 8, '2023-08-01', NULL, 1); -- Jessica - R&D

-- Insert employee-job title relationships (current positions)
INSERT INTO employee_job_titles (empid, job_title_id, start_date, end_date, is_current) VALUES 
(1, 1, '2023-01-15', NULL, 1),  -- John - Software Developer
(2, 2, '2023-02-01', NULL, 1),  -- Sarah - Senior Software Developer
(3, 3, '2023-02-15', NULL, 1),  -- Michael - Software Engineer
(4, 4, '2023-03-01', NULL, 1),  -- Emily - Database Administrator
(5, 9, '2023-03-15', NULL, 1),  -- David - Financial Analyst
(6, 12, '2023-04-01', NULL, 1), -- Lisa - Marketing Specialist
(7, 15, '2023-04-15', NULL, 1), -- James - Operations Manager
(8, 17, '2023-05-01', NULL, 1), -- Jennifer - Sales Representative
(9, 19, '2023-05-15', NULL, 1), -- Robert - Customer Service Representative
(10, 7, '2023-06-01', NULL, 1), -- Michelle - HR Assistant
(11, 6, '2022-01-01', NULL, 1), -- Admin - HR Manager
(12, 1, '2023-06-15', NULL, 1), -- Christopher - Software Developer
(13, 13, '2023-07-01', NULL, 1), -- Amanda - Marketing Manager
(14, 10, '2023-07-15', NULL, 1), -- Daniel - Accountant
(15, 21, '2023-08-01', NULL, 1); -- Jessica - Research Scientist

-- Insert sample payroll records (last 6 months)
INSERT INTO payroll (empid, pay_date, pay_period_start, pay_period_end, gross_pay, net_pay, federal_tax, state_tax, other_deductions) VALUES 
-- January 2024 payroll
(1, '2024-01-31', '2024-01-01', '2024-01-31', 6250.00, 4687.50, 1250.00, 312.50, 0.00),
(2, '2024-01-31', '2024-01-01', '2024-01-31', 6833.33, 5125.00, 1366.67, 341.67, 0.00),
(3, '2024-01-31', '2024-01-01', '2024-01-31', 5666.67, 4250.00, 1133.33, 283.33, 0.00),
-- February 2024 payroll
(1, '2024-02-29', '2024-02-01', '2024-02-29', 6250.00, 4687.50, 1250.00, 312.50, 0.00),
(2, '2024-02-29', '2024-02-01', '2024-02-29', 6833.33, 5125.00, 1366.67, 341.67, 0.00),
(3, '2024-02-29', '2024-02-01', '2024-02-29', 5666.67, 4250.00, 1133.33, 283.33, 0.00),
(5, '2024-02-29', '2024-02-01', '2024-02-29', 6000.00, 4500.00, 1200.00, 300.00, 0.00),
-- March 2024 payroll (add more employees as they were hired)
(1, '2024-03-31', '2024-03-01', '2024-03-31', 6250.00, 4687.50, 1250.00, 312.50, 0.00),
(2, '2024-03-31', '2024-03-01', '2024-03-31', 6833.33, 5125.00, 1366.67, 341.67, 0.00),
(3, '2024-03-31', '2024-03-01', '2024-03-31', 5666.67, 4250.00, 1133.33, 283.33, 0.00),
(4, '2024-03-31', '2024-03-01', '2024-03-31', 7916.67, 5937.50, 1583.33, 395.83, 0.00),
(5, '2024-03-31', '2024-03-01', '2024-03-31', 6000.00, 4500.00, 1200.00, 300.00, 0.00),
-- Add more recent months as needed...
-- October 2024 payroll (most recent)
(1, '2024-10-31', '2024-10-01', '2024-10-31', 6250.00, 4687.50, 1250.00, 312.50, 0.00),
(2, '2024-10-31', '2024-10-01', '2024-10-31', 6833.33, 5125.00, 1366.67, 341.67, 0.00),
(3, '2024-10-31', '2024-10-01', '2024-10-31', 5666.67, 4250.00, 1133.33, 283.33, 0.00),
(4, '2024-10-31', '2024-10-01', '2024-10-31', 7916.67, 5937.50, 1583.33, 395.83, 0.00),
(5, '2024-10-31', '2024-10-01', '2024-10-31', 6000.00, 4500.00, 1200.00, 300.00, 0.00),
(6, '2024-10-31', '2024-10-01', '2024-10-31', 7333.33, 5500.00, 1466.67, 366.67, 0.00),
(7, '2024-10-31', '2024-10-01', '2024-10-31', 6333.33, 4750.00, 1266.67, 316.67, 0.00),
(8, '2024-10-31', '2024-10-01', '2024-10-31', 7583.33, 5687.50, 1516.67, 379.17, 0.00),
(9, '2024-10-31', '2024-10-01', '2024-10-31', 5333.33, 4000.00, 1066.67, 266.67, 0.00),
(10, '2024-10-31', '2024-10-01', '2024-10-31', 6583.33, 4937.50, 1316.67, 329.17, 0.00),
(11, '2024-10-31', '2024-10-01', '2024-10-31', 7083.33, 5312.50, 1416.67, 354.17, 0.00);

-- Insert system users for authentication
-- Note: These passwords are hashed versions of simple passwords for demo purposes
-- In production, users should set their own secure passwords
INSERT INTO users (username, password_hash, user_role, empid, is_active) VALUES 
-- HR Admin user (password: admin123)
('admin', 'dGVzdFNhbHQ=:5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8', 'HR_ADMIN', 11, 1),
-- Sample employee user (password: emp123)
('jsmith', 'dGVzdFNhbHQ=:A665A45920422F9D417E4867EFDC4FB8A04A1F3FFF1FA07E998E86F7F7A27AE3', 'EMPLOYEE', 1, 1),
-- Additional demo users
('sjohnson', 'dGVzdFNhbHQ=:A665A45920422F9D417E4867EFDC4FB8A04A1F3FFF1FA07E998E86F7F7A27AE3', 'EMPLOYEE', 2, 1),
('mbrown', 'dGVzdFNhbHQ=:A665A45920422F9D417E4867EFDC4FB8A04A1F3FFF1FA07E998E86F7F7A27AE3', 'EMPLOYEE', 3, 1);

-- Insert some audit log entries (optional)
INSERT INTO audit_log (table_name, operation, empid, changed_by, old_values, new_values) VALUES 
('employees', 'INSERT', 1, 'admin', NULL, '{"first_name": "John", "last_name": "Smith", "email": "john.smith@companyz.com"}'),
('employees', 'INSERT', 2, 'admin', NULL, '{"first_name": "Sarah", "last_name": "Johnson", "email": "sarah.johnson@companyz.com"}'),
('employees', 'UPDATE', 1, 'admin', '{"current_salary": 70000}', '{"current_salary": 75000}');

-- Verify data insertion
SELECT 'Data insertion completed successfully!' as Status;

-- Display summary statistics
SELECT 
    (SELECT COUNT(*) FROM employees WHERE employment_status = 'ACTIVE') as 'Active Employees',
    (SELECT COUNT(*) FROM division) as 'Divisions',
    (SELECT COUNT(*) FROM job_titles) as 'Job Titles',
    (SELECT COUNT(*) FROM users WHERE is_active = 1) as 'Active Users',
    (SELECT COUNT(*) FROM payroll) as 'Payroll Records';

-- Display sample employee data for verification
SELECT 
    e.emp_number,
    e.first_name,
    e.last_name,
    d.division_name,
    jt.job_title,
    e.current_salary
FROM employees e
JOIN employee_division ed ON e.empid = ed.empid AND ed.is_current = 1
JOIN division d ON ed.div_id = d.div_id
JOIN employee_job_titles ejt ON e.empid = ejt.empid AND ejt.is_current = 1
JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id
WHERE e.employment_status = 'ACTIVE'
LIMIT 10;