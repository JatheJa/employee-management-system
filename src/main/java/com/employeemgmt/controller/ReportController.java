package com.employeemgmt.controller;

import com.employeemgmt.dao.ReportDAO;
import com.employeemgmt.dao.EmployeeDAO;
import com.employeemgmt.model.Employee;
import com.employeemgmt.service.ValidationService;
import com.employeemgmt.util.UserRole;
import com.employeemgmt.controller.EmployeeController.ControllerResult;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.math.BigDecimal;

/**
 * Controller class for Report generation and coordination
 * Handles various organizational and analytical reports
 * Coordinates with other controllers for comprehensive reporting
 * 
 * @author Team 6
 */
public class ReportController {
    
    private static final Logger LOGGER = Logger.getLogger(ReportController.class.getName());
    
    // DAOs for data access
    private final ReportDAO reportDAO;
    private final EmployeeDAO employeeDAO;
    
    // Other controllers for report coordination
    private PayrollController payrollController;
    
    // Services for validation
    private final ValidationService validationService;
    
    // Current user context for authorization
    private UserRole currentUserRole;
    private Employee currentUser;

    /**
     * Constructor with dependency injection
     * 
     * @param reportDAO Report data access object
     * @param employeeDAO Employee data access object
     * @param validationService Validation service
     */
    public ReportController(ReportDAO reportDAO, EmployeeDAO employeeDAO, ValidationService validationService) {
        if (reportDAO == null || employeeDAO == null || validationService == null) {
            throw new IllegalArgumentException("Dependencies cannot be null");
        }
        
        this.reportDAO = reportDAO;
        this.employeeDAO = employeeDAO;
        this.validationService = validationService;
        
        LOGGER.info("ReportController initialized");
    }
    
    /**
     * Set PayrollController for integrated reporting
     * 
     * @param payrollController PayrollController instance
     */
    public void setPayrollController(PayrollController payrollController) {
        this.payrollController = payrollController;
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
     * Generate employees hired within date range report (Requirement 6d)
     * HR Admin only
     * 
     * @param startDate Start date for hire date range
     * @param endDate End date for hire date range
     * @return ControllerResult containing employees hired in date range
     */
    public ControllerResult<List<Map<String, Object>>> getEmployeesHiredByDateRange(LocalDate startDate, LocalDate endDate) {
        LOGGER.info(String.format("Generating employees hired report for date range: %s to %s", startDate, endDate));
        
        try {
            // Authorization check
            if (!isAuthorizedForReports()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required for reports");
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
            
            // Generate report
            List<Map<String, Object>> employees = reportDAO.getEmployeesHiredByDateRange(startDate, endDate);
            
            // Add calculated fields
            for (Map<String, Object> employee : employees) {
                LocalDate hireDate = (LocalDate) employee.get("hireDate");
                employee.put("formattedHireDate", hireDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                employee.put("yearsOfService", calculateYearsOfService(hireDate));
                
                BigDecimal salary = (BigDecimal) employee.get("currentSalary");
                employee.put("formattedSalary", formatCurrency(salary));
            }
            
            String message = String.format("Generated hiring report for %s to %s: %d employees found", 
                                         startDate, endDate, employees.size());
            LOGGER.info(message);
            return ControllerResult.success(message, employees);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error generating hiring date range report", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error generating hiring date range report", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Generate organizational summary report
     * Includes employee counts by division, job title, demographics
     * 
     * @return ControllerResult containing organizational summary
     */
    public ControllerResult<Map<String, Object>> getOrganizationalSummary() {
        LOGGER.info("Generating organizational summary report");
        
        try {
            // Authorization check
            if (!isAuthorizedForReports()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required for reports");
            }
            
            Map<String, Object> summary = new HashMap<>();
            
            // Get employee counts by division
            List<Map<String, Object>> divisionCounts = reportDAO.getEmployeeCountByDivision();
            summary.put("employeesByDivision", divisionCounts);
            
            // Get employee counts by job title
            List<Map<String, Object>> jobTitleCounts = reportDAO.getEmployeeCountByJobTitle();
            summary.put("employeesByJobTitle", jobTitleCounts);
            
            // Get demographics summary
            Map<String, Object> demographics = reportDAO.getDemographicsSummary();
            summary.put("demographics", demographics);
            
            // Calculate summary statistics
            int totalEmployees = (Integer) demographics.get("totalEmployees");
            summary.put("totalActiveEmployees", totalEmployees);
            summary.put("totalDivisions", divisionCounts.size());
            summary.put("totalJobTitles", jobTitleCounts.size());
            
            // Generate timestamp
            summary.put("reportGeneratedDate", LocalDate.now());
            summary.put("reportGeneratedBy", currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "System");
            
            String message = String.format("Generated organizational summary: %d employees across %d divisions and %d job titles", 
                                         totalEmployees, divisionCounts.size(), jobTitleCounts.size());
            LOGGER.info(message);
            return ControllerResult.success(message, summary);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error generating organizational summary", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error generating organizational summary", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Generate salary analysis report
     * Includes salary distribution and statistics
     * 
     * @return ControllerResult containing salary analysis
     */
    public ControllerResult<Map<String, Object>> getSalaryAnalysisReport() {
        LOGGER.info("Generating salary analysis report");
        
        try {
            // Authorization check
            if (!isAuthorizedForReports()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required for reports");
            }
            
            Map<String, Object> analysis = new HashMap<>();
            
            // Get salary distribution
            List<Map<String, Object>> salaryDistribution = reportDAO.getSalaryDistribution();
            analysis.put("salaryDistribution", salaryDistribution);
            
            // Get employee counts by division with salary stats
            List<Map<String, Object>> divisionSalaryStats = reportDAO.getEmployeeCountByDivision();
            analysis.put("salaryByDivision", divisionSalaryStats);
            
            // Get employee counts by job title with salary stats
            List<Map<String, Object>> jobTitleSalaryStats = reportDAO.getEmployeeCountByJobTitle();
            analysis.put("salaryByJobTitle", jobTitleSalaryStats);
            
            // Calculate overall statistics
            Map<String, Object> demographics = reportDAO.getDemographicsSummary();
            BigDecimal avgSalary = (BigDecimal) demographics.get("avgSalary");
            
            analysis.put("overallAvgSalary", avgSalary);
            analysis.put("formattedAvgSalary", formatCurrency(avgSalary));
            
            // Calculate salary range statistics
            BigDecimal minSalary = BigDecimal.ZERO;
            BigDecimal maxSalary = BigDecimal.ZERO;
            int totalEmployeesInRanges = 0;
            
            for (Map<String, Object> range : salaryDistribution) {
                BigDecimal rangeMin = (BigDecimal) range.get("minSalary");
                BigDecimal rangeMax = (BigDecimal) range.get("maxSalary");
                int employeeCount = (Integer) range.get("employeeCount");
                
                if (rangeMin != null && (minSalary.equals(BigDecimal.ZERO) || rangeMin.compareTo(minSalary) < 0)) {
                    minSalary = rangeMin;
                }
                
                if (rangeMax != null && rangeMax.compareTo(maxSalary) > 0) {
                    maxSalary = rangeMax;
                }
                
                totalEmployeesInRanges += employeeCount;
            }
            
            analysis.put("overallMinSalary", minSalary);
            analysis.put("overallMaxSalary", maxSalary);
            analysis.put("formattedMinSalary", formatCurrency(minSalary));
            analysis.put("formattedMaxSalary", formatCurrency(maxSalary));
            
            // Generate timestamp
            analysis.put("reportGeneratedDate", LocalDate.now());
            analysis.put("reportGeneratedBy", currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "System");
            
            String message = String.format("Generated salary analysis: %d employees, avg salary %s", 
                                         totalEmployeesInRanges, formatCurrency(avgSalary));
            LOGGER.info(message);
            return ControllerResult.success(message, analysis);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error generating salary analysis", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error generating salary analysis", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Generate employee tenure analysis report
     * 
     * @return ControllerResult containing tenure analysis
     */
    public ControllerResult<List<Map<String, Object>>> getTenureAnalysisReport() {
        LOGGER.info("Generating employee tenure analysis report");
        
        try {
            // Authorization check
            if (!isAuthorizedForReports()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required for reports");
            }
            
            List<Map<String, Object>> tenureAnalysis = reportDAO.getTenureAnalysis();
            
            // Add formatted data
            for (Map<String, Object> tenure : tenureAnalysis) {
                BigDecimal avgSalary = (BigDecimal) tenure.get("avgSalary");
                tenure.put("formattedAvgSalary", formatCurrency(avgSalary));
            }
            
            String message = String.format("Generated tenure analysis: %d tenure ranges analyzed", tenureAnalysis.size());
            LOGGER.info(message);
            return ControllerResult.success(message, tenureAnalysis);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error generating tenure analysis", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error generating tenure analysis", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Generate comprehensive dashboard summary
     * Combines data from multiple sources for dashboard display
     * 
     * @return ControllerResult containing dashboard data
     */
    public ControllerResult<Map<String, Object>> getDashboardSummary() {
        LOGGER.info("Generating dashboard summary");
        
        try {
            // Authorization check
            if (!isAuthorizedForReports()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required for reports");
            }
            
            Map<String, Object> dashboard = new HashMap<>();
            
            // Get basic demographics
            Map<String, Object> demographics = reportDAO.getDemographicsSummary();
            dashboard.put("totalActiveEmployees", demographics.get("totalEmployees"));
            dashboard.put("averageAge", demographics.get("avgAge"));
            dashboard.put("averageSalary", formatCurrency((BigDecimal) demographics.get("avgSalary")));
            
            // Get division count
            List<Map<String, Object>> divisions = reportDAO.getEmployeeCountByDivision();
            dashboard.put("totalDivisions", divisions.size());
            
            // Get job title count
            List<Map<String, Object>> jobTitles = reportDAO.getEmployeeCountByJobTitle();
            dashboard.put("totalJobTitles", jobTitles.size());
            
            // Calculate recent hires (last 90 days)
            LocalDate ninetyDaysAgo = LocalDate.now().minusDays(90);
            List<Map<String, Object>> recentHires = reportDAO.getEmployeesHiredByDateRange(ninetyDaysAgo, LocalDate.now());
            dashboard.put("recentHires", recentHires.size());
            
            // Get largest division
            String largestDivision = "N/A";
            int maxEmployeesInDivision = 0;
            for (Map<String, Object> division : divisions) {
                int count = (Integer) division.get("employeeCount");
                if (count > maxEmployeesInDivision) {
                    maxEmployeesInDivision = count;
                    largestDivision = (String) division.get("divisionName");
                }
            }
            dashboard.put("largestDivision", largestDivision);
            dashboard.put("largestDivisionCount", maxEmployeesInDivision);
            
            // Generate timestamp
            dashboard.put("lastUpdated", LocalDate.now());
            dashboard.put("lastUpdatedFormatted", LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            
            String message = String.format("Generated dashboard summary: %s employees, %d divisions, %d recent hires", 
                                         demographics.get("totalEmployees"), divisions.size(), recentHires.size());
            LOGGER.info(message);
            return ControllerResult.success(message, dashboard);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error generating dashboard summary", e);
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error generating dashboard summary", e);
            return ControllerResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Get comprehensive payroll reports by coordinating with PayrollController
     * 
     * @param year Report year
     * @param month Report month (1-12)
     * @return ControllerResult containing all payroll reports for the month
     */
    public ControllerResult<Map<String, Object>> getComprehensivePayrollReports(int year, int month) {
        LOGGER.info(String.format("Generating comprehensive payroll reports for %d/%d", month, year));
        
        try {
            // Authorization check
            if (!isAuthorizedForReports()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required for reports");
            }
            
            if (payrollController == null) {
                return ControllerResult.failure("PayrollController not configured for integrated reporting");
            }
            
            Map<String, Object> comprehensiveReport = new HashMap<>();
            
            // Set payroll controller context
            payrollController.setUserContext(currentUserRole, currentUser);
            
            // Get monthly pay by job title
            ControllerResult<List<Map<String, Object>>> jobTitleResult = 
                payrollController.getMonthlyPayByJobTitle(year, month);
            
            if (jobTitleResult.isSuccess()) {
                comprehensiveReport.put("monthlyPayByJobTitle", jobTitleResult.getData());
            } else {
                comprehensiveReport.put("monthlyPayByJobTitle", new ArrayList<>());
                comprehensiveReport.put("jobTitleReportError", jobTitleResult.getMessage());
            }
            
            // Get monthly pay by division
            ControllerResult<List<Map<String, Object>>> divisionResult = 
                payrollController.getMonthlyPayByDivision(year, month);
            
            if (divisionResult.isSuccess()) {
                comprehensiveReport.put("monthlyPayByDivision", divisionResult.getData());
            } else {
                comprehensiveReport.put("monthlyPayByDivision", new ArrayList<>());
                comprehensiveReport.put("divisionReportError", divisionResult.getMessage());
            }
            
            // Add report metadata
            comprehensiveReport.put("reportYear", year);
            comprehensiveReport.put("reportMonth", month);
            comprehensiveReport.put("reportGeneratedDate", LocalDate.now());
            comprehensiveReport.put("reportGeneratedBy", currentUser != null ? 
                currentUser.getFirstName() + " " + currentUser.getLastName() : "System");
            
            String message = String.format("Generated comprehensive payroll reports for %d/%d", month, year);
            LOGGER.info(message);
            return ControllerResult.success(message, comprehensiveReport);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating comprehensive payroll reports", e);
            return ControllerResult.failure("Error generating reports: " + e.getMessage());
        }
    }
    
    /**
     * Export report data to CSV format (string representation)
     * 
     * @param reportData List of maps representing report data
     * @param headers List of column headers
     * @return ControllerResult containing CSV string
     */
    public ControllerResult<String> exportToCsv(List<Map<String, Object>> reportData, List<String> headers) {
        LOGGER.info("Exporting report data to CSV format");
        
        try {
            // Authorization check
            if (!isAuthorizedForReports()) {
                return ControllerResult.failure("Access denied: HR Admin privileges required for reports");
            }
            
            if (reportData == null || reportData.isEmpty()) {
                return ControllerResult.failure("No data to export");
            }
            
            if (headers == null || headers.isEmpty()) {
                return ControllerResult.failure("Headers are required for CSV export");
            }
            
            StringBuilder csv = new StringBuilder();
            
            // Add headers
            csv.append(String.join(",", headers)).append("\n");
            
            // Add data rows
            for (Map<String, Object> row : reportData) {
                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    Object value = row.get(header);
                    String stringValue = value != null ? value.toString() : "";
                    
                    // Escape commas and quotes in CSV
                    if (stringValue.contains(",") || stringValue.contains("\"")) {
                        stringValue = "\"" + stringValue.replace("\"", "\"\"") + "\"";
                    }
                    
                    values.add(stringValue);
                }
                csv.append(String.join(",", values)).append("\n");
            }
            
            String message = String.format("Exported %d rows to CSV format", reportData.size());
            LOGGER.info(message);
            return ControllerResult.success(message, csv.toString());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting to CSV", e);
            return ControllerResult.failure("Error exporting to CSV: " + e.getMessage());
        }
    }
    
    // Private helper methods
    
    /**
     * Calculate years of service from hire date
     */
    private double calculateYearsOfService(LocalDate hireDate) {
        if (hireDate == null) return 0.0;
        
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(hireDate, LocalDate.now());
        return daysBetween / 365.25; // Account for leap years
    }
    
    /**
     * Format currency for display
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        
        return String.format("$%,.2f", amount);
    }
    
    // Authorization helper methods
    
    /**
     * Check if current user is authorized for reports
     */
    private boolean isAuthorizedForReports() {
        return currentUserRole == UserRole.HR_ADMIN;
    }
}