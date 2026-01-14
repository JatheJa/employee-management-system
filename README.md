# Employee Management System

## Project Overview

The Employee Management System is a comprehensive Java-based application designed to modernize and secure employee data management for Company 'Z'. This system replaces manual database management with a robust, secure, role-based application supporting current operations with 55 employees and scalable to 165+ employees within 18 months.

**Course:** CSc3350 Software Development  
**Semester:** Fall 2025  

## System Purpose

The application serves two distinct user roles with different access levels:

### HR Administrators
- Full CRUD (Create, Read, Update, Delete) access to all employee data
- Salary management and updates by percentage for salary ranges
- Comprehensive reporting capabilities
- Access to sensitive information (SSN, salary details)

### General Employees
- Read-only access to personal data
- View individual pay statement history
- Limited search capabilities (own records only)

## Technical Architecture

### Design Patterns
- **MVC (Model-View-Controller):** Clean separation of data, presentation, and business logic
- **DAO (Data Access Object):** Abstraction layer for database operations
- **Role-Based Access Control:** Security implementation for HR vs Employee access

### Technology Stack
- **Programming Language:** Java
- **GUI Framework:** JavaFX
- **Database:** MySQL
- **Database Connectivity:** JDBC with connection pooling
- **Build Tool:** (To be specified)

## Database Schema

### Core Tables

#### employees
Primary employee information table
- `empid` (PK, AUTO_INCREMENT)
- `emp_number` (UNIQUE)
- `first_name`, `last_name`
- `email` (UNIQUE)
- `ssn` (UNIQUE)
- `hire_date`
- `current_salary`
- `employment_status` (ENUM: ACTIVE, TERMINATED)
- Timestamps: `created_at`, `updated_at`

#### address
Employee address and demographic information
- `empid` (PK, FK → employees)
- `street`, `zip`, `phone`
- `city_id` (FK → city)
- `state_id` (FK → state)
- `gender`, `race`, `date_of_birth`

#### city
Normalized city data
- `city_id` (PK)
- `city_name`
- `state_id` (FK → state)

#### state
Normalized state data
- `state_id` (PK)
- `state_code` (CHAR(2), UNIQUE)
- `state_name`

#### division
Organizational divisions
- `div_id` (PK)
- `division_name` (UNIQUE)
- `division_code` (UNIQUE)

#### job_titles
Job title definitions
- `job_title_id` (PK)
- `job_title` (UNIQUE)

#### payroll
Employee pay history
- `payroll_id` (PK)
- `empid` (FK → employees)
- `pay_date`, `pay_period_start`, `pay_period_end`
- `gross_pay`, `net_pay`
- `federal_tax`, `state_tax`, `other_deductions`

#### employee_division
Employee-to-division mapping (many-to-many)
- `empid`, `div_id` (Composite PK)
- `start_date`, `end_date`
- `is_current`

#### employee_job_titles
Employee-to-job title mapping (many-to-many)
- `empid`, `job_title_id` (Composite PK)
- `start_date`, `end_date`
- `is_current`

#### users
Authentication and authorization
- `user_id` (PK)
- `username` (UNIQUE)
- `password_hash`
- `user_role` (ENUM: HR_ADMIN, EMPLOYEE)
- `empid` (FK → employees)
- `is_active`, `last_login`

#### audit_log
Change tracking and audit trail
- `log_id` (PK)
- `table_name`, `operation`
- `empid`, `changed_by`
- `change_timestamp`
- `old_values`, `new_values` (JSON)

## Application Structure

### Model Classes (Data Entities)
- `Employee.java` - Core employee data
- `Address.java` - Address information
- `PayrollRecord.java` - Pay history data
- `Division.java` - Organizational division
- `JobTitle.java` - Job title definition

### Data Access Object (DAO) Classes
- `EmployeeDAO.java` - CRUD operations for employee data
- `PayrollDAO.java` - Pay history and salary operations
- `ReportDAO.java` - Report generation
- `DatabaseConnection.java` - MySQL connection management and pooling

### Controller Classes
- `AuthenticationController.java` - User login and authorization
- `EmployeeController.java` - Employee data operations
- `PayrollController.java` - Salary and payroll operations
- `ReportController.java` - Report generation and display

### View Classes (JavaFX GUI)
- `LoginView.java` - Login interface
- `MainDashboard.java` - Primary application window
- `EmployeeSearchView.java` - Employee search and display
- `EmployeeEditView.java` - Employee data editing (HR only)
- `ReportView.java` - Report generation and display

### Abstract Classes and Interfaces
- `BaseDAO<T>` (Abstract) - Common database operations
- `SearchableDAO<T>` (Interface) - Search functionality contract
- `UserRole` (Enum) - HR_ADMIN, EMPLOYEE user roles

## Key Features

### 1. User Authentication System
Secure login with role-based access control distinguishing HR Administrators from regular Employees.

### 2. Employee Search Functionality
Search by name, date of birth, SSN, or employee ID with role-appropriate access levels.

### 3. Employee Data Management (HR Admin Only)
Full CRUD operations for creating, reading, updating, and deleting employee records.

### 4. Salary Management
Update salaries by percentage for employees within specific salary ranges.

### 5. Reporting Capabilities
- Pay Statement History Report (individual employee)
- Monthly Pay by Job Title Report
- Monthly Pay by Division Report
- Employee Hiring Date Range Report

### 6. Database Connection Management
JDBC connectivity with connection pooling and transaction management for optimal performance.

## Database Setup

### Prerequisites
- MySQL Server 5.7 or higher
- MySQL Workbench (optional, for GUI management)
- Java Development Kit (JDK) 11 or higher

### Installation Steps

1. Create the database and tables:
```bash
mysql -u root -p < employee_management_schema.sql
```

2. Verify table creation:
```sql
USE employee_management_system;
SHOW TABLES;
```

3. (Optional) Insert sample data by uncommenting the INSERT statements in the schema file.

## Development Guidelines

### Code Organization Principles
- **High Cohesion:** Classes should have a single, well-defined purpose
- **Weak Coupling:** Minimize dependencies between modules
- **Interface Segregation:** Use interfaces to define contracts between components
- **Dependency Injection:** Pass dependencies rather than creating them within classes

### Testing Requirements
All features must include corresponding test cases covering:
- Valid input scenarios
- Invalid input handling
- Authorization checks
- Edge cases and boundary conditions


## Glossary

| Term | Definition |
|------|------------|
| **CRUD** | Create, Read, Update, Delete - Basic database operations |
| **DAO** | Data Access Object - Design pattern for database access abstraction |
| **MVC** | Model-View-Controller - Architectural pattern separating data, presentation, and logic |
| **FK** | Foreign Key - Database field referencing primary key in another table |
| **PK** | Primary Key - Unique identifier field in database table |
| **JavaFX** | Modern Java GUI framework for desktop applications |
| **JDBC** | Java Database Connectivity - API for database access in Java applications |
| **HR** | Human Resources - Department responsible for employee management |
| **SSN** | Social Security Number - Unique employee identifier |

## License

This project is developed as part of CSc3350 Software Development coursework.

## Contact

For questions or concerns regarding this project, please contact the development team through the course instructor.
