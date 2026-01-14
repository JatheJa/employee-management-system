package com.employeemgmt.view.reports;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.employeemgmt.dao.EmployeeDAO;
import com.employeemgmt.model.Employee;
import com.employeemgmt.util.UserRole;

/**
 * View-layer orchestration class for the "Hiring Date Range" report.
 *
 * Responsibilities (per SDD-style separation):
 *  - Provide a simple API for the UI/controller layer to request a hiring report
 *    for a given date range.
 *  - Enforce HR-only access (HR_ADMIN role) for this global report.
 *  - Delegate data retrieval to {@link EmployeeDAO}.
 *  - Return results wrapped in a {@link ViewResult} for easy UI consumption.
 *
 * Typical usage from a JavaFX controller (to be created separately):
 *  1. After login, call {@link #setUserContext(UserRole, Employee)}.
 *  2. When the user selects start/end dates and clicks "Run Report", call
 *     {@link #getHiringReport(LocalDate, LocalDate)}.
 *  3. Use the returned {@link ViewResult} to populate a table and show messages.
 */
public class HiringDateRangeReport {

    private static final Logger LOGGER = Logger.getLogger(HiringDateRangeReport.class.getName());

    private final EmployeeDAO employeeDAO;

    // User context (needed to enforce HR-only access)
    private UserRole currentUserRole = UserRole.HR_ADMIN;
    private Employee currentUser;

    /**
     * Primary constructor for dependency injection.
     */
    public HiringDateRangeReport(EmployeeDAO employeeDAO) {
        if (employeeDAO == null) {
            throw new IllegalArgumentException("employeeDAO cannot be null");
        }
        this.employeeDAO = employeeDAO;
    }

    /**
     * Convenience constructor that wires a default EmployeeDAO.
     */
    public HiringDateRangeReport() {
        this(new EmployeeDAO());
    }

    /**
     * Propagate the current logged-in user context into this view.
     * This allows us to enforce that only HR Admins can run global hiring reports.
     */
    public void setUserContext(UserRole role, Employee user) {
        this.currentUserRole = role;
        this.currentUser = user;
    }

    /**
     * Run the "Hiring Date Range" report.
     *
     * Returns all active employees whose hireDate falls between startDate and endDate
     * inclusive. The heavy lifting is done at the DAO level (via findAllActive), and
     * we filter in-memory by hire date, so we do not require any new DAO methods.
     *
     * Access control:
     *  - HR_ADMIN: Can run the report for all employees.
     *  - EMPLOYEE / others: Not permitted (returns failure).
     *
     * @param startDate inclusive start of hire date range
     * @param endDate   inclusive end of hire date range
     * @return ViewResult containing either a list of employees or an error message
     */
    public ViewResult<List<Employee>> getHiringReport(LocalDate startDate, LocalDate endDate) {
        try {
            // Role enforcement: HR only
            if (currentUserRole != UserRole.HR_ADMIN) {
                return ViewResult.failure("Access denied: HR Admin privileges required for hiring reports");
            }

            // Basic parameter validation
            if (startDate == null || endDate == null) {
                return ViewResult.failure("Start date and end date are required");
            }
            if (endDate.isBefore(startDate)) {
                return ViewResult.failure("End date cannot be before start date");
            }

            LOGGER.info(String.format(
                    "Running Hiring Date Range Report from %s to %s (requested by %s)",
                    startDate, endDate,
                    currentUser != null ? currentUser.getEmpNumber() : "unknown user"
            ));

            // Get all active employees and filter by hireDate in-memory.
            // This avoids needing a new DAO method while staying consistent
            // with the existing EmployeeController / EmployeeDAO capabilities.
            List<Employee> allActive = employeeDAO.findAllActive();

            List<Employee> hiredInRange = allActive.stream()
                    .filter(e -> {
                        LocalDate hireDate = e.getHireDate();
                        return hireDate != null
                                && !hireDate.isBefore(startDate)
                                && !hireDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());

            String message = String.format(
                    "Found %d employees hired between %s and %s",
                    hiredInRange.size(), startDate, endDate
            );

            LOGGER.info(message);
            return ViewResult.success(message, hiredInRange);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error running Hiring Date Range Report", ex);
            return ViewResult.failure("Database error: " + ex.getMessage());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error running Hiring Date Range Report", ex);
            return ViewResult.failure("Unexpected error: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // ViewResult DTO (same pattern as PayStatementView / SalaryManagementView)
    // -------------------------------------------------------------------------

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
