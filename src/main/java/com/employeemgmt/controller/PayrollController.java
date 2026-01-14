package com.employeemgmt.controller;

import com.employeemgmt.dao.PayrollDAO;
import com.employeemgmt.dao.EmployeeDAO;
import com.employeemgmt.model.PayrollRecord;
import com.employeemgmt.model.Employee;
import com.employeemgmt.service.ValidationService;
import com.employeemgmt.util.UserRole;
import com.employeemgmt.controller.EmployeeController.ControllerResult;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller class for Payroll business logic
 * Handles operations between the View layer and DAO layer for payroll data
 * Implements authorization and validation for payroll operations
 * 
 * @author Team 6
 */
public class PayrollController {
    
    private static final Logger LOGGER = Logger.getLogger(PayrollController.class.getName());
    
    // DAOs for data access
    private final PayrollDAO payrollDAO;
    private final EmployeeDAO employeeDAO;
    
    // Services for validation and business logic
    private final ValidationService validationService;
    
    // Current user context for authorization
    private UserRole currentUserRole;
    private Employee currentUser;

    /**
     * Constructor with dependency injection
     * 
     * @param payrollDAO Payroll data access object
     * @param employeeDAO Employee data access object
     * @param validationService Validation service
     */
    public PayrollController(PayrollDAO payrollDAO, EmployeeDAO employeeDAO, ValidationService validationService) {
        if (payrollDAO == null || employeeDAO == null || validationService == null) {
            throw new IllegalArgumentException("Dependencies cannot be null");
        }
        
        this.payrollDAO = payrollDAO;
        this.employeeDAO = employeeDAO;
        this.validationService = validationService;
        
        LOGGER.info("PayrollController initialized");
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
     * Get pay statement history for an employee
     * HR Admin can view any employee, regular employees can only view their own
     * 
     * @param empId Employee ID
     * @return ControllerResult containing pay statement history
     */
    public ControllerResult<List<PayrollRecord>> getPayStatementHistory(int empId) {
        LOGGER.info("Getting pay statement history for employee ID: " + empId);
        
        try {
            // Authorization check
            if (!isAuthorizedToViewPayroll(empId)) {
                return ControllerResult.failure("Access denied: Cannot view this employee's pay statements");
            }
            
            // Verify employee exists
            Employee employee = employeeDAO.findById(empId);
            if (employee == null) {
                return ControllerResult.failure("Employee not found with ID: " + empId);
            }
            
            // Get pay statement history (already sorted by pay_date DESC in DAO)
            List<PayrollRecord> payrollHistory = payrollDAO.findByEmployeeId(empId);
            
            String message = String.format("Retrieved %d pay statements for employee %s", 
                                         payrollHistory.size(), employee.getFirstName() + " " + employee.getLastName());
            LOGGER.info(message);
            return ControllerResult.success(message, payrollHistory);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error getting pay statement history", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error getting pay statement history", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Create a new payroll record (HR Admin only)
     * 
     * @param payrollRecord Payroll record to create
     * @return ControllerResult with operation status
     */
    public ControllerResult<PayrollRecord> createPayrollRecord(PayrollRecord payrollRecord) {
        LOGGER.info("Attempting to create new payroll record");
        
        try {
            // Authorization check
            if (!isAuthorizedForWrite()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required");
            }
            
            // Validate payroll record
            if (payrollRecord == null) {
                return ControllerResult.failure("Payroll record cannot be null");
            }
            
            if (!payrollRecord.isValid()) {
                return ControllerResult.failure("Invalid payroll record data");
            }
            
            // Verify employee exists
            Employee employee = employeeDAO.findById(payrollRecord.getEmpId());
            if (employee == null) {
                return ControllerResult.failure("Employee not found with ID: " + payrollRecord.getEmpId());
            }
            
            // Additional business validation
            if (payrollRecord.getPayDate().isAfter(LocalDate.now())) {
                return ControllerResult.failure("Pay date cannot be in the future");
            }
            
            if (payrollRecord.getNetPay().compareTo(payrollRecord.getGrossPay()) > 0) {
                return ControllerResult.failure("Net pay cannot exceed gross pay");
            }
            
            // Create payroll record
            PayrollRecord created = payrollDAO.save(payrollRecord);
            
            LOGGER.info("Payroll record created successfully with ID: " + created.getPayrollId());
            return ControllerResult.success("Payroll record created successfully", created);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error creating payroll record", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error creating payroll record", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Update existing payroll record (HR Admin only)
     * 
     * @param payrollRecord Updated payroll record
     * @return ControllerResult with operation status
     */
    public ControllerResult<PayrollRecord> updatePayrollRecord(PayrollRecord payrollRecord) {
        LOGGER.info("Attempting to update payroll record ID: " + payrollRecord.getPayrollId());
        
        try {
            // Authorization check
            if (!isAuthorizedForWrite()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required");
            }
            
            // Validate payroll record
            if (payrollRecord == null || payrollRecord.getPayrollId() <= 0) {
                return ControllerResult.failure("Invalid payroll record or missing ID");
            }
            
            if (!payrollRecord.isValid()) {
                return ControllerResult.failure("Invalid payroll record data");
            }
            
            // Verify payroll record exists
            PayrollRecord existing = payrollDAO.findById(payrollRecord.getPayrollId());
            if (existing == null) {
                return ControllerResult.failure("Payroll record not found with ID: " + payrollRecord.getPayrollId());
            }
            
            // Update payroll record
            PayrollRecord updated = payrollDAO.update(payrollRecord);
            
            LOGGER.info("Payroll record updated successfully: " + payrollRecord.getPayrollId());
            return ControllerResult.success("Payroll record updated successfully", updated);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating payroll record", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error updating payroll record", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Generate monthly pay report by job title (HR Admin only)
     * 
     * @param year Report year
     * @param month Report month (1-12)
     * @return ControllerResult containing monthly pay summary by job title
     */
    public ControllerResult<List<Map<String, Object>>> getMonthlyPayByJobTitle(int year, int month) {
        LOGGER.info(String.format("Generating monthly pay report by job title for %d/%d", month, year));
        
        try {
            // Authorization check
            if (!isAuthorizedForRead()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required");
            }
            
            // Validate date parameters
            if (year < 2000 || year > LocalDate.now().getYear() + 1) {
                return ControllerResult.failure("Invalid year: must be between 2000 and " + (LocalDate.now().getYear() + 1));
            }
            
            if (month < 1 || month > 12) {
                return ControllerResult.failure("Invalid month: must be between 1 and 12");
            }
            
            // Calculate date range for the month
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();
            
            // Get payroll records for the month grouped by job title
            // This would require a custom query in PayrollDAO
            List<Map<String, Object>> report = generateMonthlyPayByJobTitleReport(monthStart, monthEnd);
            
            String message = String.format("Generated monthly pay report by job title for %s %d", 
                                         yearMonth.getMonth().toString(), year);
            LOGGER.info(message);
            return ControllerResult.success(message, report);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating monthly pay report by job title", e);
            return ControllerResult.failure("Error generating report: " + e.getMessage());
        }
    }
    
    /**
     * Generate monthly pay report by division (HR Admin only)
     * 
     * @param year Report year
     * @param month Report month (1-12)
     * @return ControllerResult containing monthly pay summary by division
     */
    public ControllerResult<List<Map<String, Object>>> getMonthlyPayByDivision(int year, int month) {
        LOGGER.info(String.format("Generating monthly pay report by division for %d/%d", month, year));
        
        try {
            // Authorization check
            if (!isAuthorizedForRead()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required");
            }
            
            // Validate date parameters
            if (year < 2000 || year > LocalDate.now().getYear() + 1) {
                return ControllerResult.failure("Invalid year: must be between 2000 and " + (LocalDate.now().getYear() + 1));
            }
            
            if (month < 1 || month > 12) {
                return ControllerResult.failure("Invalid month: must be between 1 and 12");
            }
            
            // Calculate date range for the month
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();
            
            // Get payroll records for the month grouped by division
            List<Map<String, Object>> report = generateMonthlyPayByDivisionReport(monthStart, monthEnd);
            
            String message = String.format("Generated monthly pay report by division for %s %d", 
                                         yearMonth.getMonth().toString(), year);
            LOGGER.info(message);
            return ControllerResult.success(message, report);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating monthly pay report by division", e);
            return ControllerResult.failure("Error generating report: " + e.getMessage());
        }
    }
    
    /**
     * Get payroll records by date range (HR Admin only)
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return ControllerResult containing payroll records within date range
     */
    public ControllerResult<List<PayrollRecord>> getPayrollByDateRange(LocalDate startDate, LocalDate endDate) {
        LOGGER.info(String.format("Getting payroll records for date range: %s to %s", startDate, endDate));
        
        try {
            // Authorization check
            if (!isAuthorizedForRead()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required");
            }
            
            // Validate date parameters
            if (startDate == null || endDate == null) {
                return ControllerResult.failure("Start date and end date are required");
            }
            
            if (startDate.isAfter(endDate)) {
                return ControllerResult.failure("Start date cannot be after end date");
            }
            
            if (startDate.isAfter(LocalDate.now())) {
                return ControllerResult.failure("Start date cannot be in the future");
            }
            
            // Get payroll records within date range
            List<PayrollRecord> payrollRecords = payrollDAO.findByDateRange(startDate, endDate);
            
            String message = String.format("Retrieved %d payroll records for date range %s to %s", 
                                         payrollRecords.size(), startDate, endDate);
            LOGGER.info(message);
            return ControllerResult.success(message, payrollRecords);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error getting payroll by date range", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error getting payroll by date range", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Calculate and create payroll record for an employee
     * This method demonstrates integration with salary management
     * 
     * @param empId Employee ID
     * @param payPeriodStart Pay period start date
     * @param payPeriodEnd Pay period end date
     * @param payDate Pay date
     * @return ControllerResult with created payroll record
     */
    public ControllerResult<PayrollRecord> calculateAndCreatePayroll(int empId, LocalDate payPeriodStart, 
                                                                   LocalDate payPeriodEnd, LocalDate payDate) {
        LOGGER.info(String.format("Calculating payroll for employee %d, period %s to %s", 
                                empId, payPeriodStart, payPeriodEnd));
        
        try {
            // Authorization check
            if (!isAuthorizedForWrite()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required");
            }
            
            // Get employee information
            Employee employee = employeeDAO.findById(empId);
            if (employee == null) {
                return ControllerResult.failure("Employee not found with ID: " + empId);
            }
            
            // Calculate pay based on current salary
            BigDecimal currentSalary = employee.getCurrentSalary();
            
            // Simple calculation: assume monthly salary, calculate pro-rated amount
            // In real system, this would be much more complex with hours, overtime, etc.
            BigDecimal grossPay = calculateGrossPay(currentSalary, payPeriodStart, payPeriodEnd);
            
            // Calculate taxes and deductions (simplified calculation)
            BigDecimal federalTax = grossPay.multiply(BigDecimal.valueOf(0.20)); // 20% federal tax
            BigDecimal stateTax = grossPay.multiply(BigDecimal.valueOf(0.05));   // 5% state tax
            BigDecimal otherDeductions = BigDecimal.ZERO; // No other deductions for now
            
            BigDecimal netPay = grossPay.subtract(federalTax).subtract(stateTax).subtract(otherDeductions);
            
            // Create payroll record
            PayrollRecord payrollRecord = new PayrollRecord(empId, payDate, payPeriodStart, payPeriodEnd, grossPay, netPay);
            payrollRecord.setFederalTax(federalTax);
            payrollRecord.setStateTax(stateTax);
            payrollRecord.setOtherDeductions(otherDeductions);
            
            // Save payroll record
            PayrollRecord created = payrollDAO.save(payrollRecord);
            
            String message = String.format("Payroll calculated and created for employee %s %s", 
                                         employee.getFirstName(), employee.getLastName());
            LOGGER.info(message);
            return ControllerResult.success(message, created);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error calculating payroll", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error calculating payroll", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    // Private helper methods
    
    /**
     * Generate monthly pay report by job title
     */
    private List<Map<String, Object>> generateMonthlyPayByJobTitleReport(LocalDate monthStart, LocalDate monthEnd) {
        // This would use a specialized query in PayrollDAO
        // For now, return a placeholder structure
        List<Map<String, Object>> report = new ArrayList<>();
        
        try {
            // Get all payroll records for the month
            List<PayrollRecord> monthlyPayroll = payrollDAO.findByDateRange(monthStart, monthEnd);
            
            // Group by job title and calculate totals
            Map<String, Map<String, Object>> jobTitleTotals = new HashMap<>();
            
            for (PayrollRecord record : monthlyPayroll) {
                // In a real implementation, you'd join with employee_job_titles table
                // For now, create placeholder data
                String jobTitle = "Software Developer"; // This would come from join query
                
                Map<String, Object> totals = jobTitleTotals.computeIfAbsent(jobTitle, k -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("jobTitle", k);
                    map.put("totalGrossPay", BigDecimal.ZERO);
                    map.put("totalNetPay", BigDecimal.ZERO);
                    map.put("employeeCount", 0);
                    return map;
                });
                
                BigDecimal currentGross = (BigDecimal) totals.get("totalGrossPay");
                BigDecimal currentNet = (BigDecimal) totals.get("totalNetPay");
                Integer currentCount = (Integer) totals.get("employeeCount");
                
                totals.put("totalGrossPay", currentGross.add(record.getGrossPay()));
                totals.put("totalNetPay", currentNet.add(record.getNetPay()));
                totals.put("employeeCount", currentCount + 1);
            }
            
            report.addAll(jobTitleTotals.values());
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating monthly pay by job title report", e);
        }
        
        return report;
    }
    
    /**
     * Generate monthly pay report by division
     */
    private List<Map<String, Object>> generateMonthlyPayByDivisionReport(LocalDate monthStart, LocalDate monthEnd) {
        // Similar implementation to job title report
        List<Map<String, Object>> report = new ArrayList<>();
        
        try {
            List<PayrollRecord> monthlyPayroll = payrollDAO.findByDateRange(monthStart, monthEnd);
            
            Map<String, Map<String, Object>> divisionTotals = new HashMap<>();
            
            for (PayrollRecord record : monthlyPayroll) {
                String division = "Information Technology"; // This would come from join query
                
                Map<String, Object> totals = divisionTotals.computeIfAbsent(division, k -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("division", k);
                    map.put("totalGrossPay", BigDecimal.ZERO);
                    map.put("totalNetPay", BigDecimal.ZERO);
                    map.put("employeeCount", 0);
                    return map;
                });
                
                BigDecimal currentGross = (BigDecimal) totals.get("totalGrossPay");
                BigDecimal currentNet = (BigDecimal) totals.get("totalNetPay");
                Integer currentCount = (Integer) totals.get("employeeCount");
                
                totals.put("totalGrossPay", currentGross.add(record.getGrossPay()));
                totals.put("totalNetPay", currentNet.add(record.getNetPay()));
                totals.put("employeeCount", currentCount + 1);
            }
            
            report.addAll(divisionTotals.values());
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating monthly pay by division report", e);
        }
        
        return report;
    }
    
    /**
     * Calculate gross pay for a pay period (simplified calculation)
     */
    private BigDecimal calculateGrossPay(BigDecimal annualSalary, LocalDate periodStart, LocalDate periodEnd) {
        // Simple calculation assuming monthly pay periods
        // In real system, this would be much more sophisticated
        return annualSalary.divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
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
     * Check if current user is authorized to view specific employee's payroll
     */
    private boolean isAuthorizedToViewPayroll(int empId) {
        if (currentUserRole == UserRole.HR_ADMIN) {
            return true;
        }
        
        if (currentUserRole == UserRole.EMPLOYEE && currentUser != null) {
            return currentUser.getEmpId() == empId;
        }
        
        return false;
    }
}