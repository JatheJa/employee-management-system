package com.employeemgmt.view.reports;

import java.math.BigDecimal;
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
 * View-layer class for the "Monthly Pay by Division" report.
 *
 * Responsibilities (per SDD and MVC separation):
 *  - Provide a simple, UI-friendly API for generating the report.
 *  - Enforce HR_ADMIN-only authorization at the view layer.
 *  - Delegate data access and aggregation to {@link PayrollDAO}.
 *  - Map raw DAO results (List&lt;Map&lt;String,Object&gt;&gt;) to a typed DTO
 *    {@link DivisionPaySummary} for easy binding in JavaFX controllers.
 *
 * Typical usage from a JavaFX controller:
 *  1. After login, call {@link #setUserContext(UserRole, Employee)}.
 *  2. When user selects a month/year (or date range), compute periodStart/end.
 *  3. Call {@link #getMonthlyPayByDivision(LocalDate, LocalDate)}.
 *  4. Bind the returned {@link DivisionPaySummary} list to a TableView.
 */
public class MonthlyPayByDivisionReport {

    private static final Logger LOGGER =
            Logger.getLogger(MonthlyPayByDivisionReport.class.getName());

    private final PayrollDAO payrollDAO;

    private UserRole currentUserRole = UserRole.HR_ADMIN; // default
    private Employee currentUser;                         // optional, for logging / future use

    /**
     * Primary constructor for dependency injection.
     */
    public MonthlyPayByDivisionReport(PayrollDAO payrollDAO) {
        this.payrollDAO = Objects.requireNonNull(payrollDAO, "payrollDAO cannot be null");
    }

    /**
     * Convenience constructor using a default {@link PayrollDAO}.
     */
    public MonthlyPayByDivisionReport() {
        this(new PayrollDAO());
    }

    /**
     * Propagate current logged-in user and role.
     * This is used to enforce that only HR_ADMIN can run this report.
     */
    public void setUserContext(UserRole role, Employee user) {
        this.currentUserRole = role;
        this.currentUser = user;
    }

    // -------------------------------------------------------------------------
    // Public API for controllers
    // -------------------------------------------------------------------------

    /**
     * Generate a "Monthly Pay by Division" report for the given period.
     *
     * The PayrollDAO supports any date range; the UI will typically pass
     * the first and last day of a given month, but this method is generic.
     *
     * @param periodStart inclusive start of the pay period range
     * @param periodEnd   inclusive end of the pay period range
     * @return ViewResult containing either a list of DivisionPaySummary rows,
     *         or an error message if validation/authorization fails
     */
    public ViewResult<List<DivisionPaySummary>> getMonthlyPayByDivision(
            LocalDate periodStart, LocalDate periodEnd) {

        // Authorization: HR_ADMIN only
        if (currentUserRole != UserRole.HR_ADMIN) {
            return ViewResult.failure("Access denied: HR Admin privileges required to run this report");
        }

        // Basic validation
        if (periodStart == null || periodEnd == null) {
            return ViewResult.failure("Start date and end date are required");
        }
        if (periodEnd.isBefore(periodStart)) {
            return ViewResult.failure("End date cannot be before start date");
        }

        try {
            LOGGER.info(String.format(
                    "Generating Monthly Pay by Division report for period %s to %s (requested by empId=%s, role=%s)",
                    periodStart,
                    periodEnd,
                    currentUser != null ? currentUser.getEmpId() : "null",
                    currentUserRole
            ));

            List<Map<String, Object>> rawRows =
                    payrollDAO.getMonthlyPayByDivision(periodStart, periodEnd);

            List<DivisionPaySummary> summaries = new ArrayList<>();
            for (Map<String, Object> row : rawRows) {
                summaries.add(mapRowToSummary(row));
            }

            String message = String.format(
                    "Report generated for %s to %s. Divisions: %d",
                    periodStart, periodEnd, summaries.size()
            );

            LOGGER.info(message);
            return ViewResult.success(message, summaries);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error generating Monthly Pay by Division report", ex);
            return ViewResult.failure("Error generating report: " + ex.getMessage());
        }
    }

    /**
     * Convenience method for a specific month/year.
     * Calculates the first and last day of the month and delegates to
     * {@link #getMonthlyPayByDivision(LocalDate, LocalDate)}.
     */
    public ViewResult<List<DivisionPaySummary>> getMonthlyPayByDivision(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return getMonthlyPayByDivision(start, end);
    }

    // -------------------------------------------------------------------------
    // Internal mapping helpers
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private DivisionPaySummary mapRowToSummary(Map<String, Object> row) {
        if (row == null) {
            return new DivisionPaySummary();
        }

        DivisionPaySummary summary = new DivisionPaySummary();

        Object divNameObj = row.get("divisionName");
        summary.setDivisionName(divNameObj != null ? divNameObj.toString() : "Unknown");

        Object grossObj = row.get("totalGrossPay");
        if (grossObj instanceof BigDecimal) {
            summary.setTotalGrossPay((BigDecimal) grossObj);
        }

        Object netObj = row.get("totalNetPay");
        if (netObj instanceof BigDecimal) {
            summary.setTotalNetPay((BigDecimal) netObj);
        }

        Object countObj = row.get("employeeCount");
        if (countObj instanceof Number) {
            summary.setEmployeeCount(((Number) countObj).intValue());
        }

        return summary;
    }

    // -------------------------------------------------------------------------
    // DTOs and result wrapper
    // -------------------------------------------------------------------------

    /**
     * Typed DTO representing one row of the "Monthly Pay by Division" report.
     *
     * This is designed to be easily bound to a JavaFX TableView:
     *  - divisionName       → String column
     *  - totalGrossPay      → formatted currency column
     *  - totalNetPay        → formatted currency column
     *  - employeeCount      → integer column
     */
    public static class DivisionPaySummary {
        private String divisionName;
        private BigDecimal totalGrossPay = BigDecimal.ZERO;
        private BigDecimal totalNetPay = BigDecimal.ZERO;
        private int employeeCount;

        public String getDivisionName() {
            return divisionName;
        }

        public void setDivisionName(String divisionName) {
            this.divisionName = divisionName;
        }

        public BigDecimal getTotalGrossPay() {
            return totalGrossPay;
        }

        public void setTotalGrossPay(BigDecimal totalGrossPay) {
            this.totalGrossPay = totalGrossPay != null ? totalGrossPay : BigDecimal.ZERO;
        }

        public BigDecimal getTotalNetPay() {
            return totalNetPay;
        }

        public void setTotalNetPay(BigDecimal totalNetPay) {
            this.totalNetPay = totalNetPay != null ? totalNetPay : BigDecimal.ZERO;
        }

        public int getEmployeeCount() {
            return employeeCount;
        }

        public void setEmployeeCount(int employeeCount) {
            this.employeeCount = employeeCount;
        }
    }

    /**
     * Simple result wrapper for view-layer methods, analogous to
     * EmployeeController.ControllerResult but scoped to this view.
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
