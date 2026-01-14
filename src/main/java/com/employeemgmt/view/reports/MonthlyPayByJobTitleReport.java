package com.employeemgmt.view.reports;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.dao.PayrollDAO;
import com.employeemgmt.model.Employee;
import com.employeemgmt.util.UserRole;

/**
 * View-layer helper for the "Monthly Pay by Job Title" report.
 *
 * Responsibilities (per SDD and layering):
 *  - Expose a simple API to the JavaFX controller for generating the report.
 *  - Enforce basic authorization (HR Admin only).
 *  - Perform light input validation (date range checks).
 *  - Delegate data retrieval to {@link PayrollDAO#getMonthlyPayByJobTitle(LocalDate, LocalDate)}.
 *  - Map DAO result sets into a type-safe DTO for UI binding.
 *
 * This class does NOT know about any JavaFX controls – that is the responsibility
 * of {@code MonthlyPayByJobTitleReportController}.
 */
public class MonthlyPayByJobTitleReport {

    private static final Logger LOGGER =
            Logger.getLogger(MonthlyPayByJobTitleReport.class.getName());

    private final PayrollDAO payrollDAO;

    // Simple user context, primarily for enforcing HR-only access.
    private UserRole currentUserRole = UserRole.EMPLOYEE;
    private Employee currentUser;

    /**
     * Primary constructor with explicit dependency injection.
     */
    public MonthlyPayByJobTitleReport(PayrollDAO payrollDAO) {
        this.payrollDAO = Objects.requireNonNull(payrollDAO, "payrollDAO cannot be null");
    }

    /**
     * Convenience constructor that wires the default {@link PayrollDAO}.
     */
    public MonthlyPayByJobTitleReport() {
        this(new PayrollDAO());
    }

    /**
     * Set the current user context (role + employee) so we can enforce
     * HR-only access as required by the SDD.
     *
     * @param role current user's role
     * @param user current logged-in employee (may be null for system/admin users)
     */
    public void setUserContext(UserRole role, Employee user) {
        this.currentUserRole = role;
        this.currentUser = user;
    }

    /**
     * Generate the "Monthly Pay by Job Title" report for the given period.
     *
     * @param periodStart inclusive period start date (typically first day of month)
     * @param periodEnd   inclusive period end date (typically last day of month)
     * @return {@link ViewResult} wrapping a list of {@link JobTitlePaySummary}
     */
    public ViewResult<List<JobTitlePaySummary>> generateReport(LocalDate periodStart,
                                                               LocalDate periodEnd) {
        // Authorization: HR Admin only.
        if (currentUserRole != UserRole.HR_ADMIN) {
            return ViewResult.failure("Access denied: HR Admin privileges required");
        }

        // Basic input validation.
        if (periodStart == null || periodEnd == null) {
            return ViewResult.failure("Start date and end date are required");
        }
        if (periodEnd.isBefore(periodStart)) {
            return ViewResult.failure("End date cannot be before start date");
        }

        try {
            List<Map<String, Object>> rawRows =
                    payrollDAO.getMonthlyPayByJobTitle(periodStart, periodEnd);

            List<JobTitlePaySummary> summaries = mapToSummaries(rawRows);

            String message;
            if (summaries.isEmpty()) {
                message = "No payroll data found for the selected period.";
            } else {
                message = "Retrieved " + summaries.size() + " job title rows.";
            }

            LOGGER.info(String.format(
                    "MonthlyPayByJobTitle report generated for %s to %s, rows=%d",
                    periodStart, periodEnd, summaries.size()
            ));

            return ViewResult.success(message, summaries);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "Database error generating MonthlyPayByJobTitle report", ex);
            return ViewResult.failure("Database error: " + ex.getMessage());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE,
                    "Unexpected error generating MonthlyPayByJobTitle report", ex);
            return ViewResult.failure("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Map DAO result rows (Map<String, Object>) into strongly-typed DTOs
     * for easier JavaFX binding.
     */
    private List<JobTitlePaySummary> mapToSummaries(List<Map<String, Object>> rows) {
        List<JobTitlePaySummary> summaries = new ArrayList<>();
        if (rows == null) {
            return summaries;
        }

        for (Map<String, Object> row : rows) {
            if (row == null) {
                continue;
            }

            String jobTitle = (String) row.getOrDefault("jobTitle", "");
            BigDecimal totalGross =
                    safeBigDecimal(row.get("totalGrossPay"));
            BigDecimal totalNet =
                    safeBigDecimal(row.get("totalNetPay"));
            int employeeCount =
                    safeInt(row.get("employeeCount"));

            summaries.add(new JobTitlePaySummary(jobTitle, totalGross, totalNet, employeeCount));
        }

        return summaries;
    }

    private BigDecimal safeBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value == null) {
            return BigDecimal.ZERO;
        }
        // Fallback: try to parse from string
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private int safeInt(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // ---------------------------------------------------------------------
    // DTO + simple result wrapper
    // ---------------------------------------------------------------------

    /**
     * DTO representing aggregated pay information for a single job title.
     *
     * Fields correspond directly to the SELECT aliases in
     * {@link PayrollDAO#getMonthlyPayByJobTitle(LocalDate, LocalDate)}:
     *  - jobTitle
     *  - totalGrossPay
     *  - totalNetPay
     *  - employeeCount
     */
    public static class JobTitlePaySummary {
        private final String jobTitle;
        private final BigDecimal totalGrossPay;
        private final BigDecimal totalNetPay;
        private final int employeeCount;

        public JobTitlePaySummary(String jobTitle,
                                  BigDecimal totalGrossPay,
                                  BigDecimal totalNetPay,
                                  int employeeCount) {
            this.jobTitle = jobTitle != null ? jobTitle : "";
            this.totalGrossPay = totalGrossPay != null ? totalGrossPay : BigDecimal.ZERO;
            this.totalNetPay = totalNetPay != null ? totalNetPay : BigDecimal.ZERO;
            this.employeeCount = employeeCount;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public BigDecimal getTotalGrossPay() {
            return totalGrossPay;
        }

        public BigDecimal getTotalNetPay() {
            return totalNetPay;
        }

        public int getEmployeeCount() {
            return employeeCount;
        }
    }

    /**
     * Simple view-layer result wrapper, similar to the pattern used in other
     * view helper classes.
     */
    public static class ViewResult<T> {
        private final boolean success;
        private final String message;
        private final T data;

        private ViewResult(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static <T> ViewResult<T> success(String message, T data) {
            return new ViewResult<>(true, message, data);
        }

        public static <T> ViewResult<T> failure(String message) {
            return new ViewResult<>(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public T getData() {
            return data;
        }
    }
}
