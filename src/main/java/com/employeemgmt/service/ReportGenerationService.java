package com.employeemgmt.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.employeemgmt.dao.DivisionDAO;
import com.employeemgmt.dao.EmployeeDAO;
import com.employeemgmt.dao.JobTitleDAO;
import com.employeemgmt.dao.PayrollDAO;
import com.employeemgmt.dao.ReportDAO;
import com.employeemgmt.model.Division;
import com.employeemgmt.model.Employee;
import com.employeemgmt.model.PayrollRecord;
import com.employeemgmt.model.Report;

/**
 * Service class for advanced report generation utilities
 * Provides comprehensive report generation, formatting, and export capabilities
 * Coordinates between multiple data sources for complex reporting
 * 
 * @author Team 6
 */
public class ReportGenerationService {
    
    private static final Logger LOGGER = Logger.getLogger(ReportGenerationService.class.getName());
    
    // DAOs for data access
    private final ReportDAO reportDAO;
    private final EmployeeDAO employeeDAO;
    private final PayrollDAO payrollDAO;
    private final DivisionDAO divisionDAO;
    private final JobTitleDAO jobTitleDAO;
    
    // Services for additional functionality
    private final ValidationService validationService;
    
    // Thread pool for async report generation
    private final ExecutorService reportExecutor;
    
    // Report caching
    private final Map<String, CachedReport> reportCache;
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes
    
    /**
     * Constructor with dependency injection
     * 
     * @param reportDAO Report data access object
     * @param employeeDAO Employee data access object
     * @param payrollDAO Payroll data access object
     * @param divisionDAO Division data access object
     * @param jobTitleDAO Job title data access object
     * @param validationService Validation service
     */
    public ReportGenerationService(ReportDAO reportDAO, EmployeeDAO employeeDAO, 
                                 PayrollDAO payrollDAO, DivisionDAO divisionDAO,
                                 JobTitleDAO jobTitleDAO, ValidationService validationService) {
        if (reportDAO == null || employeeDAO == null || payrollDAO == null || 
            divisionDAO == null || jobTitleDAO == null || validationService == null) {
            throw new IllegalArgumentException("Dependencies cannot be null");
        }
        
        this.reportDAO = reportDAO;
        this.employeeDAO = employeeDAO;
        this.payrollDAO = payrollDAO;
        this.divisionDAO = divisionDAO;
        this.jobTitleDAO = jobTitleDAO;
        this.validationService = validationService;
        
        // Initialize thread pool for async operations
        this.reportExecutor = Executors.newFixedThreadPool(3);
        
        // Initialize report cache
        this.reportCache = new HashMap<>();
        
        LOGGER.info("ReportGenerationService initialized");
    }
    
    /**
     * Generate comprehensive employee report with full details
     * 
     * @param startDate Start date for hire date filter (optional)
     * @param endDate End date for hire date filter (optional)
     * @param includeSalaryInfo Whether to include salary information
     * @param generatedBy User generating the report
     * @return Report object with employee data
     */
    public Report generateEmployeeReport(LocalDate startDate, LocalDate endDate, 
                                       boolean includeSalaryInfo, String generatedBy) {
        LOGGER.info("Generating comprehensive employee report");
        
        try {
            Report report = new Report.Builder("Employee Report", Report.ReportType.ORGANIZATIONAL_SUMMARY, generatedBy)
                .withParameter("startDate", startDate)
                .withParameter("endDate", endDate)
                .withParameter("includeSalaryInfo", includeSalaryInfo)
                .withStatus(Report.ReportStatus.GENERATING)
                .build();
            
            List<Map<String, Object>> reportData = new ArrayList<>();
            List<Employee> employees;
            
            // Get employees based on date filter
            if (startDate != null && endDate != null) {
                employees = getEmployeesByHireDateRange(startDate, endDate);
            } else {
                employees = employeeDAO.findAllActive();
            }
            
            // Enhance employee data with additional information
            for (Employee employee : employees) {
                Map<String, Object> employeeData = buildEmployeeReportData(employee, includeSalaryInfo);
                reportData.add(employeeData);
            }
            
            // Sort by last name, then first name
            reportData.sort((a, b) -> {
                String lastNameA = (String) a.get("lastName");
                String lastNameB = (String) b.get("lastName");
                int result = lastNameA.compareTo(lastNameB);
                if (result == 0) {
                    String firstNameA = (String) a.get("firstName");
                    String firstNameB = (String) b.get("firstName");
                    return firstNameA.compareTo(firstNameB);
                }
                return result;
            });
            
            // Calculate summary statistics
            Map<String, Object> summary = calculateEmployeeSummary(employees, includeSalaryInfo);
            
            report.setData(reportData);
            report.setSummary(summary);
            report.setStatus(Report.ReportStatus.COMPLETED);
            
            LOGGER.info(String.format("Employee report completed: %d employees", employees.size()));
            return report;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating employee report", e);
            return new Report.Builder("Employee Report", Report.ReportType.ORGANIZATIONAL_SUMMARY, generatedBy)
                .withStatus(Report.ReportStatus.FAILED)
                .build();
        }
    }
    
    /**
     * Generate organizational hierarchy report
     * Shows structure by division and job title with statistics
     * 
     * @param includeInactive Whether to include inactive divisions/job titles
     * @param generatedBy User generating the report
     * @return Report object with organizational hierarchy
     */
    public Report generateOrganizationalHierarchyReport(boolean includeInactive, String generatedBy) {
        LOGGER.info("Generating organizational hierarchy report");
        
        try {
            Report report = new Report.Builder("Organizational Hierarchy", 
                                             Report.ReportType.ORGANIZATIONAL_SUMMARY, generatedBy)
                .withParameter("includeInactive", includeInactive)
                .withStatus(Report.ReportStatus.GENERATING)
                .build();
            
            List<Map<String, Object>> hierarchyData = new ArrayList<>();
            
            // Get divisions with employee counts
            List<Division> divisions = includeInactive ? 
                divisionDAO.findAllWithEmployeeCount() : 
                divisionDAO.findActiveDivisions();
            
            for (Division division : divisions) {
                Map<String, Object> divisionData = new HashMap<>();
                divisionData.put("type", "DIVISION");
                divisionData.put("id", division.getDivId());
                divisionData.put("name", division.getDivisionName());
                divisionData.put("code", division.getDivisionCode());
                divisionData.put("displayName", division.getDisplayName());
                
                // Get job titles in this division
                List<Map<String, Object>> jobTitlesInDivision = getJobTitlesByDivision(division.getDivId());
                divisionData.put("jobTitles", jobTitlesInDivision);
                divisionData.put("totalEmployees", calculateTotalEmployeesInDivision(division.getDivId()));
                
                hierarchyData.add(divisionData);
            }
            
            // Calculate organizational summary
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalDivisions", divisions.size());
            summary.put("totalJobTitles", getTotalJobTitlesCount(includeInactive));
            summary.put("totalEmployees", getTotalActiveEmployeesCount());
            
            report.setData(hierarchyData);
            report.setSummary(summary);
            report.setStatus(Report.ReportStatus.COMPLETED);
            
            LOGGER.info("Organizational hierarchy report completed");
            return report;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating organizational hierarchy report", e);
            return new Report.Builder("Organizational Hierarchy", 
                                    Report.ReportType.ORGANIZATIONAL_SUMMARY, generatedBy)
                .withStatus(Report.ReportStatus.FAILED)
                .build();
        }
    }
    
    /**
     * Generate comprehensive payroll analysis report
     * 
     * @param year Report year
     * @param month Report month (1-12, or null for entire year)
     * @param breakdownBy Breakdown type: "DIVISION", "JOB_TITLE", or "BOTH"
     * @param generatedBy User generating the report
     * @return Report object with payroll analysis
     */
    public Report generatePayrollAnalysisReport(int year, Integer month, String breakdownBy, String generatedBy) {
        LOGGER.info(String.format("Generating payroll analysis report for %d/%s", year, month));
        
        try {
            String reportName = month != null ? 
                String.format("Payroll Analysis - %s %d", YearMonth.of(year, month).getMonth(), year) :
                String.format("Payroll Analysis - %d", year);
                
            Report report = new Report.Builder(reportName, Report.ReportType.COMPREHENSIVE_PAYROLL, generatedBy)
                .withParameter("year", year)
                .withParameter("month", month)
                .withParameter("breakdownBy", breakdownBy)
                .withStatus(Report.ReportStatus.GENERATING)
                .build();
            
            // Determine date range
            LocalDate startDate, endDate;
            if (month != null) {
                YearMonth yearMonth = YearMonth.of(year, month);
                startDate = yearMonth.atDay(1);
                endDate = yearMonth.atEndOfMonth();
            } else {
                startDate = LocalDate.of(year, 1, 1);
                endDate = LocalDate.of(year, 12, 31);
            }
            
            List<Map<String, Object>> analysisData = new ArrayList<>();
            Map<String, Object> summary = new HashMap<>();
            
            // Get payroll data for the period
            List<PayrollRecord> payrollRecords = payrollDAO.findByDateRange(startDate, endDate);
            
            if ("DIVISION".equals(breakdownBy) || "BOTH".equals(breakdownBy)) {
                List<Map<String, Object>> divisionAnalysis = analyzePayrollByDivision(payrollRecords);
                analysisData.addAll(divisionAnalysis);
            }
            
            if ("JOB_TITLE".equals(breakdownBy) || "BOTH".equals(breakdownBy)) {
                List<Map<String, Object>> jobTitleAnalysis = analyzePayrollByJobTitle(payrollRecords);
                analysisData.addAll(jobTitleAnalysis);
            }
            
            // Calculate overall summary
            BigDecimal totalGrossPay = payrollRecords.stream()
                .map(PayrollRecord::getGrossPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalNetPay = payrollRecords.stream()
                .map(PayrollRecord::getNetPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            summary.put("totalGrossPay", totalGrossPay);
            summary.put("totalNetPay", totalNetPay);
            summary.put("totalDeductions", totalGrossPay.subtract(totalNetPay));
            summary.put("payrollRecordsCount", payrollRecords.size());
            summary.put("reportPeriod", month != null ? 
                String.format("%s %d", YearMonth.of(year, month).getMonth(), year) :
                String.valueOf(year));
            
            report.setData(analysisData);
            report.setSummary(summary);
            report.setStatus(Report.ReportStatus.COMPLETED);
            
            LOGGER.info("Payroll analysis report completed");
            return report;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating payroll analysis report", e);
            return new Report.Builder("Payroll Analysis", Report.ReportType.COMPREHENSIVE_PAYROLL, generatedBy)
                .withStatus(Report.ReportStatus.FAILED)
                .build();
        }
    }
    
    /**
     * Generate executive dashboard report with key metrics
     * 
     * @param generatedBy User generating the report
     * @return Report object with dashboard metrics
     */
    public Report generateExecutiveDashboard(String generatedBy) {
        LOGGER.info("Generating executive dashboard report");
        
        String cacheKey = "executive_dashboard";
        CachedReport cached = reportCache.get(cacheKey);
        
        if (cached != null && !cached.isExpired()) {
            LOGGER.info("Returning cached executive dashboard");
            return cached.getReport();
        }
        
        try {
            Report report = new Report.Builder("Executive Dashboard", 
                                             Report.ReportType.DASHBOARD_SUMMARY, generatedBy)
                .withStatus(Report.ReportStatus.GENERATING)
                .build();
            
            Map<String, Object> dashboardMetrics = new HashMap<>();
            
            // Employee metrics
            int totalEmployees = getTotalActiveEmployeesCount();
            dashboardMetrics.put("totalEmployees", totalEmployees);
            
            // Recent hiring metrics (last 30, 90 days)
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            LocalDate ninetyDaysAgo = LocalDate.now().minusDays(90);
            
            int hiredLast30Days = getEmployeesByHireDateRange(thirtyDaysAgo, LocalDate.now()).size();
            int hiredLast90Days = getEmployeesByHireDateRange(ninetyDaysAgo, LocalDate.now()).size();
            
            dashboardMetrics.put("hiredLast30Days", hiredLast30Days);
            dashboardMetrics.put("hiredLast90Days", hiredLast90Days);
            
            // Organizational structure metrics
            dashboardMetrics.put("totalDivisions", divisionDAO.findAll().size());
            dashboardMetrics.put("totalJobTitles", jobTitleDAO.findAll().size());
            dashboardMetrics.put("activeDivisions", divisionDAO.findActiveDivisions().size());
            dashboardMetrics.put("activeJobTitles", jobTitleDAO.findActiveJobTitles().size());
            
            // Payroll metrics (current month)
            LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);
            LocalDate currentMonthEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            
            List<PayrollRecord> currentMonthPayroll = payrollDAO.findByDateRange(currentMonthStart, currentMonthEnd);
            BigDecimal currentMonthGrossPay = currentMonthPayroll.stream()
                .map(PayrollRecord::getGrossPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            dashboardMetrics.put("currentMonthGrossPay", currentMonthGrossPay);
            dashboardMetrics.put("currentMonthPayrollRecords", currentMonthPayroll.size());
            
            // Top divisions by employee count
            List<Division> divisionsWithCount = divisionDAO.findAllWithEmployeeCount();
            List<Map<String, Object>> topDivisions = divisionsWithCount.stream()
                .sorted((a, b) -> Integer.compare(b.getEmployeeCount(), a.getEmployeeCount()))
                .limit(5)
                .map(div -> {
                    Map<String, Object> divMap = new HashMap<>();
                    divMap.put("name", div.getDivisionName());
                    divMap.put("code", div.getDivisionCode());
                    divMap.put("employeeCount", div.getEmployeeCount());
                    return divMap;
                })
                .collect(Collectors.toList());
            
            dashboardMetrics.put("topDivisions", topDivisions);
            
            // Average tenure calculation
            double averageTenure = calculateAverageTenure();
            dashboardMetrics.put("averageTenureYears", Math.round(averageTenure * 100.0) / 100.0);
            
            // Growth metrics
            Map<String, Object> growthMetrics = calculateGrowthMetrics();
            dashboardMetrics.putAll(growthMetrics);
            
            List<Map<String, Object>> dashboardData = new ArrayList<>();
            dashboardData.add(dashboardMetrics);
            
            report.setData(dashboardData);
            report.setSummary(dashboardMetrics);
            report.setStatus(Report.ReportStatus.COMPLETED);
            
            // Cache the report
            reportCache.put(cacheKey, new CachedReport(report));
            
            LOGGER.info("Executive dashboard report completed");
            return report;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating executive dashboard", e);
            return new Report.Builder("Executive Dashboard", Report.ReportType.DASHBOARD_SUMMARY, generatedBy)
                .withStatus(Report.ReportStatus.FAILED)
                .build();
        }
    }
    
    /**
     * Generate report asynchronously
     * 
     * @param reportType Type of report to generate
     * @param parameters Report parameters
     * @param generatedBy User generating the report
     * @return CompletableFuture with the generated report
     */
    public CompletableFuture<Report> generateReportAsync(Report.ReportType reportType, 
                                                        Map<String, Object> parameters, 
                                                        String generatedBy) {
        LOGGER.info("Starting async report generation: " + reportType);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                switch (reportType) {
                    case ORGANIZATIONAL_SUMMARY:
                        LocalDate startDate = (LocalDate) parameters.get("startDate");
                        LocalDate endDate = (LocalDate) parameters.get("endDate");
                        Boolean includeSalary = (Boolean) parameters.getOrDefault("includeSalaryInfo", false);
                        return generateEmployeeReport(startDate, endDate, includeSalary, generatedBy);
                        
                    case DASHBOARD_SUMMARY:
                        return generateExecutiveDashboard(generatedBy);
                        
                    case COMPREHENSIVE_PAYROLL:
                        Integer year = (Integer) parameters.get("year");
                        Integer month = (Integer) parameters.get("month");
                        String breakdown = (String) parameters.getOrDefault("breakdownBy", "BOTH");
                        return generatePayrollAnalysisReport(year, month, breakdown, generatedBy);
                        
                    default:
                        throw new IllegalArgumentException("Unsupported report type: " + reportType);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in async report generation", e);
                return new Report.Builder("Failed Report", reportType, generatedBy)
                    .withStatus(Report.ReportStatus.FAILED)
                    .build();
            }
        }, reportExecutor);
    }
    
    /**
     * Export report to CSV format
     * 
     * @param report Report to export
     * @param includeHeaders Whether to include column headers
     * @return CSV string representation
     */
    public String exportReportToCsv(Report report, boolean includeHeaders) {
        if (report == null || !report.hasData()) {
            return "";
        }
        
        StringBuilder csv = new StringBuilder();
        List<Map<String, Object>> data = report.getData();
        
        if (data.isEmpty()) {
            return "";
        }
        
        // Get headers from first row
        List<String> headers = new ArrayList<>(data.get(0).keySet());
        headers.sort(String::compareTo); // Sort for consistent order
        
        // Add headers if requested
        if (includeHeaders) {
            csv.append(headers.stream()
                .map(this::escapeCsvValue)
                .collect(Collectors.joining(",")))
                .append("\n");
        }
        
        // Add data rows
        for (Map<String, Object> row : data) {
            csv.append(headers.stream()
                .map(header -> {
                    Object value = row.get(header);
                    return escapeCsvValue(value != null ? value.toString() : "");
                })
                .collect(Collectors.joining(",")))
                .append("\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Format currency values for display
     * 
     * @param amount BigDecimal amount
     * @return Formatted currency string
     */
    public String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "$0.00";
        }
        return String.format("$%,.2f", amount.setScale(2, RoundingMode.HALF_UP));
    }
    
    /**
     * Format percentage values for display
     * 
     * @param value Decimal value (0.15 for 15%)
     * @param decimalPlaces Number of decimal places
     * @return Formatted percentage string
     */
    public String formatPercentage(double value, int decimalPlaces) {
        return String.format("%." + decimalPlaces + "f%%", value * 100);
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        if (reportExecutor != null && !reportExecutor.isShutdown()) {
            reportExecutor.shutdown();
            LOGGER.info("Report generation service shutdown");
        }
    }
    
    // Private helper methods
    
    /**
     * Build comprehensive employee report data
     */
    private Map<String, Object> buildEmployeeReportData(Employee employee, boolean includeSalaryInfo) throws SQLException {
        Map<String, Object> data = new HashMap<>();
        
        // Basic employee information
        data.put("empId", employee.getEmpId());
        data.put("empNumber", employee.getEmpNumber());
        data.put("firstName", employee.getFirstName());
        data.put("lastName", employee.getLastName());
        data.put("fullName", employee.getFirstName() + " " + employee.getLastName());
        data.put("email", employee.getEmail());
        data.put("hireDate", employee.getHireDate());
        data.put("formattedHireDate", employee.getHireDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        data.put("employmentStatus", employee.getEmploymentStatus());
        
        // Calculate tenure
        long daysSinceHire = java.time.temporal.ChronoUnit.DAYS.between(employee.getHireDate(), LocalDate.now());
        double yearsOfService = daysSinceHire / 365.25;
        data.put("yearsOfService", Math.round(yearsOfService * 100.0) / 100.0);
        
        // Salary information (if authorized)
        if (includeSalaryInfo) {
            data.put("currentSalary", employee.getCurrentSalary());
            data.put("formattedSalary", formatCurrency(employee.getCurrentSalary()));
        }
        
        return data;
    }
    
    /**
     * Calculate employee summary statistics
     */
    private Map<String, Object> calculateEmployeeSummary(List<Employee> employees, boolean includeSalaryInfo) {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("totalEmployees", employees.size());
        
        if (!employees.isEmpty()) {
            // Calculate average tenure
            double avgTenure = employees.stream()
                .mapToDouble(emp -> {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(emp.getHireDate(), LocalDate.now());
                    return days / 365.25;
                })
                .average()
                .orElse(0.0);
            
            summary.put("averageTenure", Math.round(avgTenure * 100.0) / 100.0);
            
            // Find hire date range
            LocalDate earliestHire = employees.stream()
                .map(Employee::getHireDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
            
            LocalDate latestHire = employees.stream()
                .map(Employee::getHireDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
            
            summary.put("earliestHireDate", earliestHire);
            summary.put("latestHireDate", latestHire);
            
            // Salary statistics (if authorized)
            if (includeSalaryInfo) {
                BigDecimal avgSalary = employees.stream()
                    .map(Employee::getCurrentSalary)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(employees.size()), 2, RoundingMode.HALF_UP);
                
                BigDecimal minSalary = employees.stream()
                    .map(Employee::getCurrentSalary)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
                
                BigDecimal maxSalary = employees.stream()
                    .map(Employee::getCurrentSalary)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
                
                summary.put("averageSalary", avgSalary);
                summary.put("formattedAverageSalary", formatCurrency(avgSalary));
                summary.put("minSalary", minSalary);
                summary.put("maxSalary", maxSalary);
                summary.put("salaryRange", formatCurrency(minSalary) + " - " + formatCurrency(maxSalary));
            }
        }
        
        return summary;
    }
    
    /**
     * Get employees by hire date range
     */
    private List<Employee> getEmployeesByHireDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Employee> allEmployees = employeeDAO.findAllActive();
        return allEmployees.stream()
            .filter(emp -> !emp.getHireDate().isBefore(startDate) && !emp.getHireDate().isAfter(endDate))
            .collect(Collectors.toList());
    }
    
    /**
     * Get job titles by division
     */
    private List<Map<String, Object>> getJobTitlesByDivision(int divisionId) {
        // This would require a more complex query or service method
        // For now, return a placeholder
        return new ArrayList<>();
    }
    
    /**
     * Calculate total employees in division
     */
    private int calculateTotalEmployeesInDivision(int divisionId) {
        try {
            Division division = divisionDAO.findById(divisionId);
            return division != null ? division.getEmployeeCount() : 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error calculating employees in division: " + divisionId, e);
            return 0;
        }
    }
    
    /**
     * Get total job titles count
     */
    private int getTotalJobTitlesCount(boolean includeInactive) throws SQLException {
        return includeInactive ? 
            jobTitleDAO.findAll().size() : 
            jobTitleDAO.findActiveJobTitles().size();
    }
    
    /**
     * Get total active employees count
     */
    private int getTotalActiveEmployeesCount() {
        try {
            return employeeDAO.findAllActive().size();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error getting total employee count", e);
            return 0;
        }
    }
    
    /**
     * Analyze payroll by division
     */
    private List<Map<String, Object>> analyzePayrollByDivision(List<PayrollRecord> payrollRecords) {
        // Group payroll by division and calculate statistics
        // This would require joining with employee_division table
        return new ArrayList<>();
    }
    
    /**
     * Analyze payroll by job title
     */
    private List<Map<String, Object>> analyzePayrollByJobTitle(List<PayrollRecord> payrollRecords) {
        // Group payroll by job title and calculate statistics
        // This would require joining with employee_job_titles table
        return new ArrayList<>();
    }
    
    /**
     * Calculate average tenure across all employees
     */
    private double calculateAverageTenure() {
        try {
            List<Employee> employees = employeeDAO.findAllActive();
            if (employees.isEmpty()) {
                return 0.0;
            }
            
            double totalTenure = employees.stream()
                .mapToDouble(emp -> {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(emp.getHireDate(), LocalDate.now());
                    return days / 365.25;
                })
                .sum();
            
            return totalTenure / employees.size();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error calculating average tenure", e);
            return 0.0;
        }
    }
    
    /**
     * Calculate growth metrics
     */
    private Map<String, Object> calculateGrowthMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            LocalDate oneYearAgo = LocalDate.now().minusYears(1);
            List<Employee> hiredLastYear = getEmployeesByHireDateRange(oneYearAgo, LocalDate.now());
            
            metrics.put("hiredLastYear", hiredLastYear.size());
            
            // Calculate growth rate (simplified)
            int totalEmployees = getTotalActiveEmployeesCount();
            double growthRate = totalEmployees > 0 ? 
                (double) hiredLastYear.size() / totalEmployees : 0.0;
            
            metrics.put("yearOverYearGrowthRate", formatPercentage(growthRate, 1));
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error calculating growth metrics", e);
            metrics.put("hiredLastYear", 0);
            metrics.put("yearOverYearGrowthRate", "0.0%");
        }
        
        return metrics;
    }
    
    /**
     * Escape CSV values properly
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    /**
     * Cache wrapper for reports
     */
    private static class CachedReport {
        private final Report report;
        private final long timestamp;
        
        public CachedReport(Report report) {
            this.report = report;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Report getReport() {
            return report;
        }
        
        public boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > CACHE_DURATION_MS;
        }
    }
}