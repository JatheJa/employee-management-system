# Employee Management System - Getting Started Guide

## 🚀 Quick Setup for Team Members

This guide will help you get the Employee Management System running on your development machine in under 30 minutes.

## 📋 Prerequisites

### Required Software:
1. **Java JDK 11 or higher**
   - Download: https://adoptopenjdk.net/
   - Verify: `java -version` (should show 11+)

2. **MySQL 8.0+**
   - Download: https://dev.mysql.com/downloads/mysql/
   - Alternative: Use XAMPP or Docker

3. **Maven 3.6+**
   - Download: https://maven.apache.org/download.cgi
   - Verify: `mvn -version`

4. **IDE (Choose one):**
   - IntelliJ IDEA (Recommended)
   - Eclipse with JavaFX plugin
   - VS Code with Java extensions

5. **JavaFX SDK** (if not included in your JDK)
   - Download: https://gluonhq.com/products/javafx/

## 🗂️ Project Structure

You'll be working with these key directories:
```
employee-management-system/
├── README.md                 # Project overview and instructions
├── TASK_ASSIGNMENTS.md      # Detailed task breakdown for team
├── PROJECT_STRUCTURE.md     # Complete file organization guide
├── pom.xml                  # Maven configuration
├── .gitignore               # Git ignore rules
├── database.properties      # Database configuration
├── sample_data.sql          # Test data for development
└── [All Java source files]  # Model, DAO, Controller, and View classes
```

## 🔧 Step-by-Step Setup

### Step 1: Database Setup

1. **Start MySQL** server on your machine

2. **Create the database** by running this script:
   ```sql
   -- Copy content from employee_management_schema.sql
   -- Run in MySQL Workbench, command line, or phpMyAdmin
   ```

3. **Load sample data** (optional but recommended):
   ```sql
   -- Copy content from sample_data.sql
   -- This creates test employees, divisions, etc.
   ```

4. **Test database connection:**
   ```sql
   USE employee_management_system;
   SELECT COUNT(*) FROM employees; -- Should return 15 if sample data loaded
   ```

### Step 2: Configure Database Connection

1. **Edit `database.properties`:**
   ```properties
   db.url=jdbc:mysql://localhost:3306/employee_management_system
   db.username=YOUR_MYSQL_USERNAME
   db.password=YOUR_MYSQL_PASSWORD
   ```

2. **Common configurations:**
   - **XAMPP default:** username=`root`, password=`` (empty)
   - **MySQL default:** username=`root`, password=`YOUR_PASSWORD`
   - **Local dev:** Update host/port if different

### Step 3: Project Setup

1. **Create project directory:**
   ```bash
   mkdir employee-management-system
   cd employee-management-system
   ```

2. **Organize source files** following this structure:
   ```
   src/
   ├── main/
   │   ├── java/com/employeemgmt/
   │   │   ├── Application.java           # Main entry point
   │   │   ├── model/                     # Data models
   │   │   │   ├── Employee.java
   │   │   │   ├── Address.java
   │   │   │   ├── PayrollRecord.java
   │   │   │   ├── Division.java
   │   │   │   ├── JobTitle.java
   │   │   │   ├── City.java
   │   │   │   ├── State.java
   │   │   │   ├── UserRole.java
   │   │   │   ├── EmployeeDivision.java
   │   │   │   └── EmployeeJobTitle.java
   │   │   ├── dao/                       # Database access
   │   │   │   ├── base/
   │   │   │   │   ├── BaseDAO.java
   │   │   │   │   ├── SearchableDAO.java
   │   │   │   │   └── DatabaseConnection.java
   │   │   │   ├── EmployeeDAO.java
   │   │   │   ├── CityDAO.java
   │   │   │   └── StateDAO.java
   │   │   ├── controller/                # Business logic
   │   │   │   └── AuthenticationController.java
   │   │   ├── service/                   # Services
   │   │   │   └── SecurityService.java
   │   │   ├── view/                      # JavaFX UI
   │   │   │   ├── auth/
   │   │   │   │   └── LoginView.java
   │   │   │   └── dashboard/
   │   │   │       └── MainDashboard.java
   │   │   └── util/                      # Utilities
   │   │       └── PasswordUtils.java
   │   └── resources/
   │       └── database.properties
   ```

### Step 4: Build and Test

1. **Compile the project:**
   ```bash
   mvn clean compile
   ```

2. **Run the application:**
   ```bash
   mvn javafx:run
   ```

3. **Test login with sample data:**
   - Username: `admin`
   - Password: `admin123`
   - Role: HR Administrator

## 🧪 Verification Checklist

After setup, verify these work:

- [ ] Application starts without errors
- [ ] Login window appears
- [ ] Database connection successful
- [ ] Can login with admin credentials
- [ ] Main dashboard loads
- [ ] Can logout and return to login
- [ ] Sample employee data visible (if loaded)

## 🐛 Common Issues & Solutions

### Database Connection Issues:
```
Error: "Access denied for user"
Solution: Check username/password in database.properties

Error: "Unknown database 'employee_management_system'"
Solution: Run the schema.sql script to create database

Error: "Table 'employees' doesn't exist"
Solution: Ensure schema.sql completed successfully
```

### JavaFX Issues:
```
Error: "JavaFX runtime components are missing"
Solution: Add JavaFX to module path or use JavaFX-enabled JDK

Error: "Module not found: javafx.controls"
Solution: Update IDE settings to include JavaFX libraries
```

### Maven Issues:
```
Error: "Plugin execution not covered by lifecycle"
Solution: Run 'mvn clean compile' before 'mvn javafx:run'

Error: "Could not resolve dependencies"
Solution: Check internet connection, update Maven repositories
```

## 👨‍💻 Development Workflow

### For Each Feature:
1. **Create feature branch** from main
2. **Implement functionality** in your assigned area
3. **Test thoroughly** with sample data
4. **Update documentation** if needed
5. **Create pull request** for team review
6. **Merge after approval** from team members

### Daily Development:
1. **Pull latest changes** from main branch
2. **Work on assigned tasks** from TASK_ASSIGNMENTS.md
3. **Test integration** with other components
4. **Commit frequently** with descriptive messages
5. **Push changes** at end of day

## 📚 Key Resources

### Documentation:
- **PROJECT_STRUCTURE.md**: Complete file organization
- **TASK_ASSIGNMENTS.md**: Detailed task breakdown
- **JavaDoc**: In-code documentation for all classes
- **Database Schema**: Review schema.sql for table relationships

### Sample Login Accounts (if sample_data.sql loaded):
- **HR Admin**: username=`admin`, password=`admin123`
- **Employee**: username=`jsmith`, password=`emp123`
- **Employee**: username=`sjohnson`, password=`emp123`

### Testing Data:
- **15 sample employees** with various divisions and job titles
- **Sample addresses** with city/state relationships
- **Sample payroll records** for testing reports
- **Multiple divisions**: IT, HR, Finance, Marketing, Operations

## 🎯 Next Steps

1. **Review TASK_ASSIGNMENTS.md** to see what you're responsible for
2. **Set up your development environment** following this guide
3. **Test the basic application** to ensure everything works
4. **Start working on your assigned feature area**
5. **Coordinate with team members** for integration points

## 🆘 Getting Help

### If you get stuck:
1. **Check this guide** for common solutions
2. **Review starter code comments** for implementation hints
3. **Ask team members** for help with integration
4. **Search online** for JavaFX, Maven, or MySQL specific issues
5. **Check course resources** or office hours

### Contact Information:
- **Team Lead**: [Assign a team lead for coordination]
- **Database Expert**: [Who handles DB issues]
- **UI Expert**: [Who handles JavaFX issues]

## 🏆 Success Tips

- **Start early** - Don't wait until deadline
- **Test frequently** - Catch issues early
- **Communicate often** - Coordinate with team regularly
- **Document changes** - Help your teammates understand your code
- **Focus on core features** - Get basic functionality working first
- **Polish later** - Functionality first, then UI improvements

Good luck with your Employee Management System! 🚀

Remember: You have comprehensive starter files that provide a solid foundation. Focus on completing the functionality rather than starting from scratch.