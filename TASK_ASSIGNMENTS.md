# Employee Management System - Task Assignment Guide

## Team Information
- **Team Name**: Team 6
- **Course**: CSc3350 Software Development - Fall 2025
- **Members**: James Thuy, Jemal Hussien, Julian Perdomo
- **Project**: Employee Management System for Company 'Z'

## Overview of Required Features

Based on your requirements document, here are the main features that need to be implemented:

1. **User Authentication System** (HR Admin vs Employee roles)
2. **Employee Search & Management** (CRUD operations for HR Admin)
3. **Salary Management** (Percentage-based updates for salary ranges)
4. **Reporting System** (Pay statements, monthly reports, hiring reports)
5. **Database Integration** (MySQL with proper schema)
6. **JavaFX User Interface** (Professional GUI application)

## Task Distribution Strategies

### Option 1: By Feature Area (Recommended)

#### 🔐 **AUTHENTICATION & SECURITY** 
**Assigned to: [Team Member Name]**

**Starter Files Provided:**
- `SecurityService.java` - Password verification, user authentication
- `AuthenticationController.java` - Login/logout, session management
- `PasswordUtils.java` - Secure password hashing utilities
- `LoginView.java` - JavaFX login interface

**Tasks to Complete:**
1. **User Management System**
   - Complete user registration functionality
   - Implement password strength validation
   - Add "forgot password" feature (optional)

2. **Session Management**
   - Implement session timeout handling
   - Add concurrent login prevention
   - Create audit logging for login attempts

3. **Security Enhancements**
   - Add input sanitization for login forms
   - Implement account lockout after failed attempts
   - Create security configuration settings

4. **Testing & Validation**
   - Create unit tests for authentication logic
   - Test password hashing/verification
   - Validate role-based access control

**Estimated Time:** 15-20 hours

---

#### 👥 **EMPLOYEE MANAGEMENT** 
**Assigned to: [Team Member Name]**

**Starter Files Provided:**
- `Employee.java` - Employee data model
- `Address.java` - Employee address/demographics
- `EmployeeDAO.java` - Database operations for employees
- `City.java`, `State.java` - Location lookup data
- `CityDAO.java`, `StateDAO.java` - Location database access

**Tasks to Complete:**
1. **Employee CRUD Operations**
   - Create `EmployeeController.java` - Business logic for employee management
   - Implement employee creation with validation
   - Build employee update functionality
   - Add soft delete (mark as terminated)

2. **Employee Search System**
   - Create `EmployeeSearchView.java` - Search interface
   - Implement search by name, SSN, employee ID, DOB
   - Add advanced search filters
   - Create search result display with pagination

3. **Employee Management UI**
   - Create `EmployeeCreateView.java` - Add new employee form
   - Create `EmployeeEditView.java` - Edit employee details
   - Create `EmployeeListView.java` - Display employee lists
   - Implement data validation in UI forms

4. **Address & Demographics**
   - Complete address management system
   - Implement city/state lookup dropdowns
   - Add demographic data entry forms
   - Create address validation logic

**Estimated Time:** 25-30 hours

---

#### 💰 **PAYROLL & SALARY MANAGEMENT** 
**Assigned to: [Team Member Name]**

**Starter Files Provided:**
- `PayrollRecord.java` - Pay statement data model
- `PayrollController.java` - Salary and payroll business logic
- `SalaryManagementView.java` - Salary update interface

**Tasks to Complete:**
1. **Payroll Data Management**
   - Complete `PayrollDAO.java` - Database operations for payroll
   - Implement payroll record creation
   - Build pay statement history tracking
   - Add payroll calculation utilities

2. **Salary Update System**
   - Implement percentage-based salary increases by range
   - Create salary update validation (prevent invalid increases)
   - Add salary history tracking
   - Build bulk salary update functionality

3. **Pay Statement Views**
   - Create `PayStatementView.java` - Individual pay statement display
   - Implement pay statement search and filtering
   - Add pay statement PDF generation (optional)
   - Create employee self-service pay statement access

4. **Salary Management UI**
   - Complete salary management interface
   - Add salary range selection widgets
   - Implement percentage calculation preview
   - Create confirmation dialogs for salary changes

**Estimated Time:** 20-25 hours

---

#### 📊 **REPORTS & DASHBOARD** 
**Assigned to: [Team Member Name]**

**Starter Files Provided:**
- `MainDashboard.java` - Main application interface
- `ReportController.java` - Report generation logic
- `Division.java`, `JobTitle.java` - Organizational data models

**Tasks to Complete:**
1. **Report Generation System**
   - Complete `ReportDAO.java` - Database queries for reports
   - Implement "Monthly Pay by Job Title" report
   - Implement "Monthly Pay by Division" report
   - Implement "Employees Hired by Date Range" report

2. **Dashboard Interface**
   - Complete main dashboard functionality
   - Add quick action buttons with real functionality
   - Implement role-based menu systems
   - Create dashboard summary widgets

3. **Report Display Views**
   - Create `ReportView.java` - Base report display
   - Create `MonthlyPayByJobTitleReport.java` - Specific report view
   - Create `MonthlyPayByDivisionReport.java` - Specific report view
   - Create `HiringDateRangeReport.java` - Specific report view

4. **Data Visualization**
   - Add charts and graphs for report data
   - Implement export functionality (CSV, PDF)
   - Create print functionality for reports
   - Add report scheduling (optional)

**Estimated Time:** 20-25 hours

---

#### 🗄️ **DATABASE & INFRASTRUCTURE** 
**Assigned to: [Team Member Name]**

**Starter Files Provided:**
- `employee_management_schema.sql` - Complete database schema
- `sample_data.sql` - Test data for development
- `DatabaseConnection.java` - Connection pooling and management
- `BaseDAO.java` - Common database operations
- `database.properties` - Database configuration

**Tasks to Complete:**
1. **Database Setup & Management**
   - Set up MySQL database for all team members
   - Create database setup documentation
   - Implement database migration scripts
   - Add database backup/restore procedures

2. **DAO Framework Completion**
   - Complete remaining DAO classes (DivisionDAO, JobTitleDAO)
   - Implement relationship management (EmployeeDivision, EmployeeJobTitle)
   - Add database transaction management
   - Create database error handling utilities

3. **Data Validation & Integrity**
   - Create `ValidationService.java` - Business rule validation
   - Implement database constraints and triggers
   - Add data integrity checks
   - Create database performance optimization

4. **Build & Deployment**
   - Complete Maven configuration (`pom.xml`)
   - Set up build scripts and automation
   - Create deployment documentation
   - Implement logging configuration

**Estimated Time:** 15-20 hours

---

### Option 2: By Layer (Alternative)

If you prefer to organize by architectural layers:

- **Frontend Team (JavaFX Views)**: All UI classes and user interaction
- **Backend Team (Controllers & Services)**: Business logic and validation
- **Data Team (Models & DAOs)**: Database operations and data modeling

## Shared Responsibilities

### Everyone Should:
1. **Write unit tests** for their components
2. **Document their code** with JavaDoc comments
3. **Follow naming conventions** established in starter files
4. **Test integration** with other team members' components
5. **Participate in code reviews** before major commits

### Team Coordination Tasks:
1. **Integration Testing** - Test all components working together
2. **User Acceptance Testing** - Verify all user stories are implemented
3. **Performance Testing** - Ensure system meets performance requirements
4. **Documentation** - Complete user manual and technical documentation

## Development Milestones

### Week 1 (By Nov 11):
- [ ] Complete basic authentication system
- [ ] Implement core employee CRUD operations
- [ ] Set up working database connections
- [ ] Create basic UI navigation structure

### Week 2 (By Nov 18):
- [ ] Complete employee search functionality
- [ ] Implement salary management system
- [ ] Create basic reporting framework
- [ ] Complete most UI components

### Week 3 (By Nov 25):
- [ ] Complete all reports and dashboard
- [ ] Implement data validation and error handling
- [ ] Complete integration testing
- [ ] Finalize UI polish and user experience

### Week 4 (By Dec 8):
- [ ] Complete final testing and bug fixes
- [ ] Finalize documentation
- [ ] Prepare demonstration video
- [ ] Submit final deliverables

## Quick Start Instructions

1. **Clone Repository**: Get the shared code repository
2. **Database Setup**: Run `employee_management_schema.sql` and `sample_data.sql`
3. **Configure Database**: Update `database.properties` with your MySQL settings
4. **Build Project**: Run `mvn clean compile` to build the project
5. **Run Application**: Use `mvn javafx:run` to start the application
6. **Test Login**: Use username "admin" password "admin123" for HR admin

## Communication & Coordination

- **Weekly Team Meetings**: Schedule regular check-ins
- **Code Reviews**: Review each other's code before merging
- **Issue Tracking**: Use issues/tickets to track bugs and features
- **Documentation**: Keep README.md updated with setup instructions

## Success Criteria

Your project will be successful when:
- ✅ All 10+ programming tasks from user story are complete
- ✅ All test cases pass (authentication, search, salary updates)
- ✅ All required reports are generated correctly
- ✅ Both HR Admin and Employee roles work properly
- ✅ System handles 55+ employees and scales to 165+
- ✅ Professional UI with good user experience
- ✅ Complete documentation and demonstration video

Good luck! The starter files provide a solid foundation - focus on completing the functionality and ensuring everything works together seamlessly.