package com.employeemgmt.controller;

import com.employeemgmt.dao.EmployeeDAO;
import com.employeemgmt.dao.AddressDAO;
import com.employeemgmt.model.Employee;
import com.employeemgmt.model.Address;
import com.employeemgmt.model.Employee.EmploymentStatus;
import com.employeemgmt.service.ValidationService;
import com.employeemgmt.util.UserRole;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller class for Employee business logic
 * Handles operations between the View layer and DAO layer
 * Implements authorization and validation for employee operations
 * 
 * @author Team 6
 */
public class EmployeeController {
    
    private static final Logger LOGGER = Logger.getLogger(EmployeeController.class.getName());
    
    // DAOs for data access
    private final EmployeeDAO employeeDAO;
    private final AddressDAO addressDAO;
    
    // Services for validation and business logic
    private final ValidationService validationService;
    
    // Current user context for authorization
    private UserRole currentUserRole;
    private Employee currentUser;

    /**
     * Constructor with dependency injection
     * 
     * @param employeeDAO Employee data access object
     * @param addressDAO Address data access object
     * @param validationService Validation service
     */
    public EmployeeController(EmployeeDAO employeeDAO, AddressDAO addressDAO, ValidationService validationService) {
        if (employeeDAO == null || addressDAO == null || validationService == null) {
            throw new IllegalArgumentException("Dependencies cannot be null");
        }
        
        this.employeeDAO = employeeDAO;
        this.addressDAO = addressDAO;
        this.validationService = validationService;
        
        LOGGER.info("EmployeeController initialized");
    }
    
    /**
     * Set the current user context for authorization
     * 
     * @param userRole Current user's role
     * @param user Current logged-in user
     */
    public void setUserContext(UserRole userRole, Employee user) {
        this.currentUserRole = userRole;
        this.currentUser = user;
        LOGGER.info("User context set: " + userRole + " for user ID: " + (user != null ? user.getEmpId() : "null"));
    }

    /**
     * Create a new employee (HR Admin only)
     * 
     * @param employee Employee data to create
     * @param address Employee address data
     * @return ControllerResult containing success/failure and data
     */
    public ControllerResult<Employee> createEmployee(Employee employee, Address address) {
        LOGGER.info("Attempting to create new employee");
        
        try {
            // Authorization check
            if (!isAuthorizedForWrite()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required");
            }
            
            // Validate employee data
            ValidationResult validation = validationService.validateEmployeeForCreation(employee);
            if (!validation.isValid()) {
                return ControllerResult.failure("Validation failed: " + validation.getErrorMessage());
            }
            
            // Validate address data if provided
            if (address != null) {
                ValidationResult addressValidation = validationService.validateAddress(address);
                if (!addressValidation.isValid()) {
                    return ControllerResult.failure("Address validation failed: " + addressValidation.getErrorMessage());
                }
            }
            
            // Check for duplicate employee number or SSN
            if (employeeDAO.searchByEmpNumber(employee.getEmpNumber()) != null) {
                return ControllerResult.failure("Employee number already exists");
            }
            
            if (employeeDAO.searchBySsn(employee.getSsn()) != null) {
                return ControllerResult.failure("SSN already exists in system");
            }
            
            // Create employee in database
            Employee createdEmployee = employeeDAO.save(employee);
            
            // Create address record if provided
            if (address != null && createdEmployee != null) {
                address.setEmpId(createdEmployee.getEmpId());
                addressDAO.save(address);
            }
            
            LOGGER.info("Employee created successfully with ID: " + createdEmployee.getEmpId());
            return ControllerResult.success("Employee created successfully", createdEmployee);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error creating employee", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error creating employee", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Update existing employee data (HR Admin only)
     * 
     * @param empId Employee ID to update
     * @param updatedEmployee Updated employee data
     * @param updatedAddress Updated address data (optional)
     * @return ControllerResult with operation status
     */
    public ControllerResult<Employee> updateEmployee(int empId, Employee updatedEmployee, Address updatedAddress) {
        LOGGER.info("Attempting to update employee ID: " + empId);
        
        try {
            // Authorization check
            if (!isAuthorizedForWrite()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required");
            }
            
            // Check if employee exists
            Employee existingEmployee = employeeDAO.findById(empId);
            if (existingEmployee == null) {
                return ControllerResult.failure("Employee not found with ID: " + empId);
            }
            
            // Validate updated data
            ValidationResult validation = validationService.validateEmployeeForUpdate(updatedEmployee);
            if (!validation.isValid()) {
                return ControllerResult.failure("Validation failed: " + validation.getErrorMessage());
            }
            
            // Check for duplicate employee number or SSN (excluding current employee)
            Employee empWithSameNumber = employeeDAO.searchByEmpNumber(updatedEmployee.getEmpNumber());
            if (empWithSameNumber != null && empWithSameNumber.getEmpId() != empId) {
                return ControllerResult.failure("Employee number already exists for another employee");
            }
            
            Employee empWithSameSsn = employeeDAO.searchBySsn(updatedEmployee.getSsn());
            if (empWithSameSsn != null && empWithSameSsn.getEmpId() != empId) {
                return ControllerResult.failure("SSN already exists for another employee");
            }
            
            // Set the ID to ensure update (not insert)
            updatedEmployee.setEmpId(empId);
            
            // Update employee in database
            Employee updated = employeeDAO.update(updatedEmployee);
            
            // Update address if provided
            if (updatedAddress != null) {
                ValidationResult addressValidation = validationService.validateAddress(updatedAddress);
                if (!addressValidation.isValid()) {
                    return ControllerResult.failure("Address validation failed: " + addressValidation.getErrorMessage());
                }
                
                updatedAddress.setEmpId(empId);
                addressDAO.update(updatedAddress);
            }
            
            LOGGER.info("Employee updated successfully: " + empId);
            return ControllerResult.success("Employee updated successfully", updated);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating employee", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error updating employee", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Search for employees with role-based filtering
     * 
     * @param searchCriteria Search parameters
     * @return ControllerResult containing search results
     */
    public ControllerResult<List<Employee>> searchEmployees(Map<String, Object> searchCriteria) {
        LOGGER.info("Searching employees with criteria: " + searchCriteria);
        
        try {
            List<Employee> results = new ArrayList<>();
            
            // Extract search parameters
            String name = (String) searchCriteria.get("name");
            String ssn = (String) searchCriteria.get("ssn");
            String empNumber = (String) searchCriteria.get("empNumber");
            Integer empId = (Integer) searchCriteria.get("empId");
            LocalDate dateOfBirth = (LocalDate) searchCriteria.get("dateOfBirth");
            
            // Role-based search logic
            if (isAuthorizedForRead()) {
                // HR Admin - full search capabilities
                if (empId != null) {
                    Employee emp = employeeDAO.findById(empId);
                    if (emp != null) {
                        results.add(emp);
                    }
                } else if (ssn != null && !ssn.trim().isEmpty()) {
                    Employee emp = employeeDAO.searchBySsn(ssn.trim());
                    if (emp != null) {
                        results.add(emp);
                    }
                } else if (empNumber != null && !empNumber.trim().isEmpty()) {
                    Employee emp = employeeDAO.searchByEmpNumber(empNumber.trim());
                    if (emp != null) {
                        results.add(emp);
                    }
                } else if (dateOfBirth != null) {
                    // NEW: search by Date of Birth via AddressDAO
                    List<Integer> empIds = addressDAO.findEmployeeIdsByDateOfBirth(dateOfBirth);
                    for (Integer id : empIds) {
                        Employee emp = employeeDAO.findById(id);
                        if (emp != null) {
                            results.add(emp);
                        }
                    }
                } else if (name != null && !name.trim().isEmpty()) {
                    results = employeeDAO.searchByName(name.trim());
                } else {
                    // No specific criteria - return all employees for admin
                    results = employeeDAO.findAllActive();
                }
                
            } else if (currentUserRole == UserRole.EMPLOYEE && currentUser != null) {
                // General employee - can only search for their own data
                if (empId != null && empId.equals(currentUser.getEmpId())) {
                    results.add(currentUser);
                } else if (ssn != null && ssn.equals(currentUser.getSsn())) {
                    results.add(currentUser);
                } else if (empNumber != null && empNumber.equals(currentUser.getEmpNumber())) {
                    results.add(currentUser);
                } else if (dateOfBirth != null) {
                    // Optional: allow self-lookup by DOB (address-based)
                    Address address = addressDAO.findByEmployeeId(currentUser.getEmpId());
                    if (address != null && dateOfBirth.equals(address.getDateOfBirth())) {
                        results.add(currentUser);
                    }
                } else {
                    // For general employees, only show their own data
                    results.add(currentUser);
                }
                
            } else {
                return ControllerResult.failure("Access denied: Login required");
            }
            
            LOGGER.info("Search completed. Found " + results.size() + " employees");
            return ControllerResult.success("Search completed", results);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error searching employees", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error searching employees", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Get employee by ID with role-based access control
     * 
     * @param empId Employee ID
     * @return ControllerResult containing employee data
     */
    public ControllerResult<Employee> getEmployeeById(int empId) {
        LOGGER.info("Getting employee by ID: " + empId);
        
        try {
            // Authorization check
            if (!isAuthorizedToViewEmployee(empId)) {
                return ControllerResult.failure("Access denied: Cannot view this employee's data");
            }
            
            Employee employee = employeeDAO.findById(empId);
            
            if (employee == null) {
                return ControllerResult.failure("Employee not found with ID: " + empId);
            }
            
            // Load address data
            Address address = addressDAO.findByEmployeeId(empId);
            employee.setAddress(address);
            
            return ControllerResult.success("Employee retrieved successfully", employee);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error getting employee", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error getting employee", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Terminate employee (soft delete) - HR Admin only
     * 
     * @param empId Employee ID to terminate
     * @param terminationDate Termination date
     * @return ControllerResult with operation status
     */
    public ControllerResult<Boolean> terminateEmployee(int empId, LocalDate terminationDate) {
        LOGGER.info("Attempting to terminate employee ID: " + empId);
        
        try {
            // Authorization check
            if (!isAuthorizedForWrite()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required");
            }
            
            Employee employee = employeeDAO.findById(empId);
            if (employee == null) {
                return ControllerResult.failure("Employee not found with ID: " + empId);
            }
            
            if (employee.getEmploymentStatus() == EmploymentStatus.TERMINATED) {
                return ControllerResult.failure("Employee is already terminated");
            }
            
            // Update employment status
            employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
            employeeDAO.update(employee);
            
            LOGGER.info("Employee terminated successfully: " + empId);
            return ControllerResult.success("Employee terminated successfully", true);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error terminating employee", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error terminating employee", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Update salaries by percentage for employees in specified salary range (HR Admin only)
     * 
     * @param minSalary Minimum salary in range
     * @param maxSalary Maximum salary in range  
     * @param percentageIncrease Percentage increase (e.g., 3.2 for 3.2%)
     * @return ControllerResult with operation status
     */
    public ControllerResult<Map<String, Object>> updateSalariesByPercentage(
            BigDecimal minSalary, BigDecimal maxSalary, double percentageIncrease) {
        
        LOGGER.info(String.format("Attempting salary update: %.2f%% for range $%.2f - $%.2f", 
                    percentageIncrease, minSalary, maxSalary));
        
        try {
            // Authorization check
            if (!isAuthorizedForWrite()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required");
            }
            
            // Validation
            if (minSalary.compareTo(BigDecimal.ZERO) < 0 || maxSalary.compareTo(BigDecimal.ZERO) < 0) {
                return ControllerResult.failure("Salary values cannot be negative");
            }
            
            if (minSalary.compareTo(maxSalary) > 0) {
                return ControllerResult.failure("Minimum salary cannot be greater than maximum salary");
            }
            
            if (percentageIncrease < -50 || percentageIncrease > 100) {
                return ControllerResult.failure("Percentage increase must be between -50% and 100%");
            }
            
            // Get employees in salary range
            List<Employee> employeesInRange = employeeDAO.findEmployeesBySalaryRange(minSalary, maxSalary);
            
            if (employeesInRange.isEmpty()) {
                return ControllerResult.failure("No employees found in specified salary range");
            }
            
            // Update salaries
            int updatedCount = 0;
            BigDecimal totalOldSalary = BigDecimal.ZERO;
            BigDecimal totalNewSalary = BigDecimal.ZERO;
            
            for (Employee employee : employeesInRange) {
                BigDecimal oldSalary = employee.getCurrentSalary();
                BigDecimal increase = oldSalary.multiply(BigDecimal.valueOf(percentageIncrease / 100.0));
                BigDecimal newSalary = oldSalary.add(increase);
                
                employee.setCurrentSalary(newSalary);
                employeeDAO.update(employee);
                
                totalOldSalary = totalOldSalary.add(oldSalary);
                totalNewSalary = totalNewSalary.add(newSalary);
                updatedCount++;
            }
            
            // Prepare result summary
            Map<String, Object> result = new HashMap<>();
            result.put("employeesUpdated", updatedCount);
            result.put("totalOldSalary", totalOldSalary);
            result.put("totalNewSalary", totalNewSalary);
            result.put("totalIncrease", totalNewSalary.subtract(totalOldSalary));
            result.put("percentageApplied", percentageIncrease);
            
            String message = String.format("Successfully updated salaries for %d employees", updatedCount);
            LOGGER.info(message);
            return ControllerResult.success(message, result);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating salaries", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error updating salaries", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Get all active employees (HR Admin only)
     * 
     * @return ControllerResult containing list of active employees
     */
    public ControllerResult<List<Employee>> getAllActiveEmployees() {
        LOGGER.info("Getting all active employees");
        
        try {
            if (!isAuthorizedForRead()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required");
            }
            
            List<Employee> employees = employeeDAO.findAllActive();
            
            String message = "Retrieved " + employees.size() + " active employees";
            LOGGER.info(message);
            return ControllerResult.success(message, employees);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error getting active employees", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error getting active employees", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    // Authorization helper methods
    
    /**
     * Check if current user is authorized for read operations
     */
    private boolean isAuthorizedForRead() {
        return currentUserRole == UserRole.HR_ADMIN;
    }
    
    /**
     * Check if current user is authorized for write operations
     */
    private boolean isAuthorizedForWrite() {
        return currentUserRole == UserRole.HR_ADMIN;
    }
    
    /**
     * Check if current user is authorized to view specific employee data
     */
    private boolean isAuthorizedToViewEmployee(int empId) {
        if (currentUserRole == UserRole.HR_ADMIN) {
            return true;
        }
        
        if (currentUserRole == UserRole.EMPLOYEE && currentUser != null) {
            return currentUser.getEmpId() == empId;
        }
        
        return false;
    }
    
    /**
     * Result wrapper class for controller operations
     */
    public static class ControllerResult<T> {
        private final boolean success;
        private final String message;
        private final T data;
        
        private ControllerResult(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public static <T> ControllerResult<T> success(String message, T data) {
            return new ControllerResult<>(true, message, data);
        }
        
        public static <T> ControllerResult<T> success(String message) {
            return new ControllerResult<>(true, message, null);
        }
        
        public static <T> ControllerResult<T> failure(String message) {
            return new ControllerResult<>(false, message, null);
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public T getData() { return data; }
    }
    
    /**
     * Validation result wrapper class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }
}
