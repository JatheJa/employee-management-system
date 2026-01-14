-- Employee Management System Database Schema
-- Generated from existing database structure

-- Create the database
CREATE DATABASE IF NOT EXISTS employee_management_system;
USE employee_management_system;

-- Drop tables in reverse order of dependencies to avoid foreign key conflicts
DROP TABLE IF EXISTS employee_job_titles;
DROP TABLE IF EXISTS employee_division;
DROP TABLE IF EXISTS payroll;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS city;
DROP TABLE IF EXISTS state;
DROP TABLE IF EXISTS job_titles;
DROP TABLE IF EXISTS division;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS users;

-- Create state table (no dependencies)
CREATE TABLE state (
    state_id INT NOT NULL AUTO_INCREMENT,
    state_code CHAR(2) NOT NULL UNIQUE,
    state_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (state_id)
);

-- Create city table (depends on state)
CREATE TABLE city (
    city_id INT NOT NULL AUTO_INCREMENT,
    city_name VARCHAR(100) NOT NULL,
    state_id INT NOT NULL,
    PRIMARY KEY (city_id),
    INDEX idx_city_name (city_name),
    INDEX idx_state_id (state_id),
    FOREIGN KEY (state_id) REFERENCES state(state_id)
);

-- Create division table (no dependencies)
CREATE TABLE division (
    div_id INT NOT NULL AUTO_INCREMENT,
    division_name VARCHAR(100) NOT NULL UNIQUE,
    division_code VARCHAR(20) NOT NULL UNIQUE,
    PRIMARY KEY (div_id)
);

-- Create job_titles table (no dependencies)
CREATE TABLE job_titles (
    job_title_id INT NOT NULL AUTO_INCREMENT,
    job_title VARCHAR(100) NOT NULL UNIQUE,
    base_salary DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (job_title_id)
);

-- Create employees table (no dependencies)
CREATE TABLE employees (
    empid INT NOT NULL AUTO_INCREMENT,
    emp_number VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    ssn VARCHAR(11) NOT NULL UNIQUE,
    hire_date DATE NOT NULL,
    current_salary DECIMAL(12,2) NOT NULL,
    employment_status ENUM('ACTIVE', 'TERMINATED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (empid)
);

-- Create address table (depends on employees, city, state)
CREATE TABLE address (
    empid INT NOT NULL,
    street VARCHAR(100),
    city_id INT NOT NULL,
    state_id INT NOT NULL,
    zip VARCHAR(10),
    gender ENUM('M', 'F', 'Other', 'Prefer not to say'),
    race VARCHAR(50),
    date_of_birth DATE NULL,
    phone VARCHAR(20),
    PRIMARY KEY (empid),
    INDEX idx_city_id (city_id),
    INDEX idx_state_id (state_id),
    FOREIGN KEY (empid) REFERENCES employees(empid) ON DELETE CASCADE,
    FOREIGN KEY (city_id) REFERENCES city(city_id),
    FOREIGN KEY (state_id) REFERENCES state(state_id)
);

-- Create payroll table (depends on employees)
CREATE TABLE payroll (
    payroll_id INT NOT NULL AUTO_INCREMENT,
    empid INT NOT NULL,
    pay_date DATE NOT NULL,
    pay_period_start DATE NOT NULL,
    pay_period_end DATE NOT NULL,
    gross_pay DECIMAL(12,2) NOT NULL,
    net_pay DECIMAL(12,2) NOT NULL,
    federal_tax DECIMAL(12,2) DEFAULT 0.00,
    state_tax DECIMAL(12,2) DEFAULT 0.00,
    other_deductions DECIMAL(12,2) DEFAULT 0.00,
    PRIMARY KEY (payroll_id),
    INDEX idx_empid (empid),
    INDEX idx_pay_date (pay_date),
    FOREIGN KEY (empid) REFERENCES employees(empid) ON DELETE CASCADE
);

-- Create employee_division table (depends on employees, division)
CREATE TABLE employee_division (
    empid INT NOT NULL,
    div_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    is_current TINYINT(1) DEFAULT 1,
    PRIMARY KEY (empid, div_id, start_date),
    FOREIGN KEY (empid) REFERENCES employees(empid) ON DELETE CASCADE,
    FOREIGN KEY (div_id) REFERENCES division(div_id)
);

-- Create employee_job_titles table (depends on employees, job_titles)
CREATE TABLE employee_job_titles (
    empid INT NOT NULL,
    job_title_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    is_current TINYINT(1) DEFAULT 1,
    PRIMARY KEY (empid, job_title_id, start_date),
    FOREIGN KEY (empid) REFERENCES employees(empid) ON DELETE CASCADE,
    FOREIGN KEY (job_title_id) REFERENCES job_titles(job_title_id)
);

-- Optional: Create audit_log table (if needed for tracking changes)
CREATE TABLE audit_log (
    log_id INT NOT NULL AUTO_INCREMENT,
    table_name VARCHAR(50) NOT NULL,
    operation VARCHAR(10) NOT NULL,
    empid INT,
    changed_by VARCHAR(50),
    change_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    old_values JSON,
    new_values JSON,
    PRIMARY KEY (log_id),
    INDEX idx_empid (empid),
    INDEX idx_timestamp (change_timestamp)
);

-- Optional: Create users table (for authentication system)
CREATE TABLE users (
    user_id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_role ENUM('HR_ADMIN', 'EMPLOYEE') NOT NULL,
    empid INT,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    PRIMARY KEY (user_id),
    INDEX idx_empid (empid),
    FOREIGN KEY (empid) REFERENCES employees(empid) ON DELETE CASCADE
);

-- Add some useful indexes for performance
CREATE INDEX idx_employees_hire_date ON employees(hire_date);
CREATE INDEX idx_employees_salary ON employees(current_salary);
CREATE INDEX idx_employees_status ON employees(employment_status);
CREATE INDEX idx_payroll_period ON payroll(pay_period_start, pay_period_end);
CREATE INDEX idx_employee_division_current ON employee_division(is_current);
CREATE INDEX idx_employee_job_titles_current ON employee_job_titles(is_current);

-- Sample data insertion commands (optional)
-- You can uncomment and modify these to add initial data

/*
-- Insert sample states
INSERT INTO state (state_code, state_name) VALUES 
('GA', 'Georgia'),
('FL', 'Florida'),
('AL', 'Alabama'),
('TN', 'Tennessee');

-- Insert sample cities
INSERT INTO city (city_name, state_id) VALUES 
('Atlanta', 1),
('Savannah', 1),
('Miami', 2),
('Jacksonville', 2);

-- Insert sample divisions
INSERT INTO division (division_name, division_code) VALUES 
('Information Technology', 'IT'),
('Human Resources', 'HR'),
('Finance', 'FIN'),
('Marketing', 'MKT'),
('Operations', 'OPS');

-- Insert sample job titles
INSERT INTO job_titles (job_title, base_salary) VALUES 
('Software Developer', 80000.00),
('HR Manager', 90000.00),
('Financial Analyst', 75000.00),
('Marketing Specialist', 65000.00),
('Operations Manager', 85000.00);
*/
