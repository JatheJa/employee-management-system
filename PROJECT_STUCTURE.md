# Employee Management System - Project Structure

## Complete Directory Layout

```
employee-management-system/
├── README.md                           # Project documentation and setup instructions
├── pom.xml                            # Maven build configuration and dependencies
├── .gitignore                         # Git ignore rules for Java/Maven projects
│
├── docs/                              # Project documentation
│   ├── database/
│   │   ├── employee_management_schema.sql    # Database schema creation script
│   │   ├── sample_data.sql                   # Sample data for testing
│   │   └── database_setup_instructions.md   # Database setup guide
│   ├── uml-diagrams/                         # UML diagrams (use case, sequence, class)
│   ├── design-document/                      # Software design document
│   └── user-manual/                         # User manual and guides
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── employeemgmt/
│   │   │           │
│   │   │           ├── Application.java              # Main application entry point
│   │   │           │
│   │   │           ├── model/                        # Data model classes
│   │   │           │   ├── Employee.java             # Employee entity
│   │   │           │   ├── Address.java              # Address entity with demographics
│   │   │           │   ├── PayrollRecord.java        # Payroll/pay statement data
│   │   │           │   ├── Division.java             # Organizational divisions
│   │   │           │   ├── JobTitle.java             # Job title definitions
│   │   │           │   ├── City.java                 # City lookup data
│   │   │           │   ├── State.java                # State lookup data
│   │   │           │   └── UserRole.java             # User role enumeration
│   │   │           │
│   │   │           ├── dao/                          # Data Access Objects
│   │   │           │   ├── base/
│   │   │           │   │   ├── BaseDAO.java          # Abstract base DAO
│   │   │           │   │   ├── SearchableDAO.java    # Search interface
│   │   │           │   │   └── DatabaseConnection.java # Connection manager
│   │   │           │   ├── EmployeeDAO.java          # Employee database operations
│   │   │           │   ├── PayrollDAO.java           # Payroll database operations
│   │   │           │   ├── ReportDAO.java            # Report generation queries
│   │   │           │   ├── DivisionDAO.java          # Division database operations
│   │   │           │   ├── JobTitleDAO.java          # Job title database operations
│   │   │           │   └── UserDAO.java              # User authentication data
│   │   │           │
│   │   │           ├── controller/                   # Business logic controllers
│   │   │           │   ├── AuthenticationController.java  # User auth and sessions
│   │   │           │   ├── EmployeeController.java        # Employee management
│   │   │           │   ├── PayrollController.java         # Payroll and salary mgmt
│   │   │           │   └── ReportController.java          # Report generation
│   │   │           │
│   │   │           ├── service/                      # Business services
│   │   │           │   ├── SecurityService.java     # Authentication service
│   │   │           │   ├── ValidationService.java   # Data validation
│   │   │           │   └── ReportGenerationService.java # Report creation
│   │   │           │
│   │   │           ├── view/                         # JavaFX user interface
│   │   │           │   ├── common/
│   │   │           │   │   ├── BaseView.java         # Base view class
│   │   │           │   │   ├── AlertUtils.java       # Alert/dialog utilities
│   │   │           │   │   └── ViewUtils.java        # Common UI utilities
│   │   │           │   ├── auth/
│   │   │           │   │   └── LoginView.java        # Login window
│   │   │           │   ├── dashboard/
│   │   │           │   │   └── MainDashboard.java    # Main application window
│   │   │           │   ├── employee/
│   │   │           │   │   ├── EmployeeSearchView.java    # Employee search UI
│   │   │           │   │   ├── EmployeeEditView.java      # Employee edit form
│   │   │           │   │   ├── EmployeeCreateView.java    # Add new employee
│   │   │           │   │   └── EmployeeListView.java      # Employee list display
│   │   │           │   ├── payroll/
│   │   │           │   │   ├── SalaryManagementView.java  # Salary update UI
│   │   │           │   │   └── PayStatementView.java      # Pay statement display
│   │   │           │   └── reports/
│   │   │           │       ├── ReportView.java            # Base report viewer
│   │   │           │       ├── MonthlyPayByJobTitleReport.java
│   │   │           │       ├── MonthlyPayByDivisionReport.java
│   │   │           │       └── HiringDateRangeReport.java
│   │   │           │
│   │   │           └── util/                         # Utility classes
│   │   │               ├── DatabaseConfig.java      # Database configuration
│   │   │               ├── DateUtils.java           # Date formatting utilities
│   │   │               ├── PasswordUtils.java       # Password hashing/verification
│   │   │               └── ValidationUtils.java     # Input validation
│   │   │
│   │   └── resources/                               # Application resources
│   │       ├── database.properties                 # Database connection config
│   │       ├── application.properties              # Application settings
│   │       ├── fxml/                              # FXML layout files (if used)
│   │       ├── css/                               # Stylesheet files
│   │       │   └── application.css                # Main application styles
│   │       └── images/                            # Icons and images
│   │           ├── logo.png
│   │           └── icons/
│   │
│   └── test/                                      # Test classes
│       ├── java/
│       │   └── com/
│       │       └── employeemgmt/
│       │           ├── dao/                       # DAO tests
│       │           │   ├── EmployeeDAOTest.java
│       │           │   └── PayrollDAOTest.java
│       │           ├── controller/                # Controller tests
│       │           │   ├── AuthenticationControllerTest.java
│       │           │   └── EmployeeControllerTest.java
│       │           ├── service/                   # Service tests
│       │           │   └── SecurityServiceTest.java
│       │           ├── view/                      # UI tests (TestFX)
│       │           │   └── LoginViewTest.java
│       │           └── integration/               # Integration tests
│       │               └── DatabaseIntegrationTest.java
│       │
│       └── resources/                             # Test resources
│           ├── test-database.properties          # Test database config
│           └── test-data/                        # Test data files
│
└── target/                                       # Maven build output (auto-generated)
    ├── classes/                                  # Compiled classes
    ├── test-classes/                            # Compiled test classes
    └── employee-management-system.jar           # Built application JAR
```

## File Descriptions by Category

### Core Application Files
- **Application.java**: Main entry point, handles app initialization and login flow
- **pom.xml**: Maven configuration with all dependencies and build settings
- **README.md**: Project documentation, setup instructions, and team assignments

### Model Classes (Data Entities)
- **Employee.java**: Core employee data matching database employees table
- **Address.java**: Employee address and demographics (address table)
- **PayrollRecord.java**: Pay history and statements (payroll table)
- **Division.java**: Organizational divisions (division table)
- **JobTitle.java**: Job classifications (job_titles table)
- **UserRole.java**: Enum for HR_ADMIN vs EMPLOYEE roles

### Database Layer (DAO)
- **BaseDAO.java**: Abstract class with common CRUD operations
- **SearchableDAO.java**: Interface for search functionality
- **DatabaseConnection.java**: Connection pooling and MySQL management
- **EmployeeDAO.java**: Employee-specific database operations and searches
- **PayrollDAO.java**: Payroll and salary database operations
- **ReportDAO.java**: Report generation database queries

### Business Logic (Controllers & Services)
- **AuthenticationController.java**: User login, logout, and authorization
- **EmployeeController.java**: Employee management business logic
- **PayrollController.java**: Salary updates and payroll management
- **SecurityService.java**: Password verification and user authentication
- **ValidationService.java**: Input validation and business rules

### User Interface (JavaFX Views)
- **LoginView.java**: Username/password login window
- **MainDashboard.java**: Main application interface after login
- **Employee views**: Search, create, edit, and list employees
- **Payroll views**: Salary management and pay statement viewing
- **Report views**: Various report generation and display

### Utilities
- **PasswordUtils.java**: Secure password hashing with salt
- **DatabaseConfig.java**: Database configuration management
- **DateUtils.java**: Date formatting and validation
- **ValidationUtils.java**: Common input validation functions

### Configuration
- **database.properties**: MySQL connection settings
- **application.css**: JavaFX styling

## Quick Start Instructions

1. **Database Setup**:
   ```sql
   mysql -u root -p < docs/database/employee_management_schema.sql
   ```

2. **Update Configuration**:
   - Edit `src/main/resources/database.properties` with your MySQL credentials

3. **Build and Run**:
   ```bash
   mvn clean compile
   mvn javafx:run
   ```

## Team Task Assignment Template

### Authentication & Security (Team Member: ______)
- SecurityService.java
- AuthenticationController.java
- LoginView.java
- Password utilities

### Employee Management (Team Member: ______)
- Employee.java, Address.java
- EmployeeDAO.java
- EmployeeController.java
- Employee UI views

### Payroll & Salary Management (Team Member: ______)
- PayrollRecord.java
- PayrollDAO.java, PayrollController.java
- Salary management views

### Reports & Dashboard (Team Member: ______)
- ReportDAO.java, ReportController.java
- MainDashboard.java
- Report generation views

### Database & Infrastructure (Team Member: ______)
- DatabaseConnection.java
- Database schema and setup
- Base DAO classes
- Build configuration

This structure provides clear separation of concerns and makes it easy for team members to work on different features without conflicts.