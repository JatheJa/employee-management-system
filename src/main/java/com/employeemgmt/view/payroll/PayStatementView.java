package com.employeemgmt.view.payroll;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.employeemgmt.dao.PayrollDAO;
import com.employeemgmt.model.Employee;
import com.employeemgmt.model.PayrollRecord;
import com.employeemgmt.util.UserRole;

/**
 * View-layer class responsible for orchestrating the "Pay Statement History"
 * use case as described in the SDD.
 *
 * This class is designed to be called from a JavaFX controller or other UI layer:
 *  - UI sets the current user context (role + employee).
 *  - UI requests pay history via one of the exposed methods.
 *  - UI inspects the {@link PayStatementView.ViewResult} and renders data / errors.
 *
 * All persistence is delegated to {@link PayrollDAO}. Role-based access rules are
 * implemented here in line with the SDD:
 *  - HR_ADMIN can view pay history for any employee.
 *  - EMPLOYEE can only view their own pay history.
 */
public class PayStatementView {

    private static final Logger LOGGER = Logger.getLogger(PayStatementView.class.getName());

    private final PayrollDAO payrollDAO;

    // Current user context (mirrors EmployeeController pattern)
    private UserRole currentUserRole;
    private Employee currentUser;

    /**
     * Primary constructor using dependency injection.
     */
    public PayStatementView(PayrollDAO payrollDAO) {
        this.payrollDAO = Objects.requireNonNull(payrollDAO, "payrollDAO cannot be null");
    }

    /**
     * Convenience constructor that creates a default PayrollDAO.
     */
    public PayStatementView() {
        this(new PayrollDAO());
    }

    /**
     * Propagate the current logged-in user and role into the view so that
     * role-based authorization behaves as specified in the SDD.
     *
     * Typically called once after login.
     *
     * @param role current user's role
     * @param user current logged-in employee (may be null for admin accounts
     *             not tied to an employee record)
     */
    public void setUserContext(UserRole role, Employee user) {
        this.currentUserRole = role;
        this.currentUser = user;

        LOGGER.info(() -> "Payroll user context set: role=" + role +
                          ", empId=" + (user != null ? user.getEmpId() : "null"));
    }

    // -------------------------------------------------------------------------
    // Public API for UI controllers
    // -------------------------------------------------------------------------

    /**
     * Get pay statement history for a specific employee, subject to role rules:
     *  - HR_ADMIN can request any empId.
     *  - EMPLOYEE can only request their own empId.
     *
     * @param empId employee ID whose pay history is requested
     * @return ViewResult with either the list of PayrollRecord or an error message
     */
    public ViewResult<List<PayrollRecord>> getPayHistoryForEmployee(int empId) {
        try {
            if (!isAuthorizedToViewEmployee(empId)) {
                return ViewResult.failure("Access denied: not authorized to view this employee's pay history");
            }

            List<PayrollRecord> records = payrollDAO.findByEmployeeId(empId);

            if (records == null || records.isEmpty()) {
                return ViewResult.success("No pay statements found for this employee", records);
            }

            String msg = "Retrieved " + records.size() + " pay statements";
            return ViewResult.success(msg, records);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error retrieving pay history for employee " + empId, ex);
            return ViewResult.failure("Unexpected error retrieving pay history: " + ex.getMessage());
        }
    }

    /**
     * Convenience method for an employee viewing their own pay statements.
     * Fails if no currentUser is set in context.
     *
     * @return ViewResult with the current user's pay history
     */
    public ViewResult<List<PayrollRecord>> getPayHistoryForCurrentUser() {
        if (currentUser == null || currentUser.getEmpId() <= 0) {
            return ViewResult.failure("User context is not set; cannot determine current employee");
        }
        return getPayHistoryForEmployee(currentUser.getEmpId());
    }

    /**
     * Get pay history for an employee constrained by a pay date range.
     * Applies the same role rules as {@link #getPayHistoryForEmployee(int)}.
     *
     * @param empId     employee ID
     * @param startDate inclusive start pay_date
     * @param endDate   inclusive end pay_date
     * @return ViewResult with filtered list of PayrollRecord
     */
    public ViewResult<List<PayrollRecord>> getPayHistoryForEmployeeByDateRange(
            int empId, LocalDate startDate, LocalDate endDate) {

        // Basic input checks (UI-level validation)
        if (startDate == null || endDate == null) {
            return ViewResult.failure("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            return ViewResult.failure("End date cannot be before start date");
        }

        try {
            if (!isAuthorizedToViewEmployee(empId)) {
                return ViewResult.failure("Access denied: not authorized to view this employee's pay history");
            }

            // Pull all history for the employee and filter in-memory.
            // Alternatively, you could add a DAO method for (empId + date range).
            List<PayrollRecord> allRecords = payrollDAO.findByEmployeeId(empId);
            if (allRecords == null || allRecords.isEmpty()) {
                return ViewResult.success("No pay statements found for this employee", allRecords);
            }

            List<PayrollRecord> filtered = allRecords.stream()
                    .filter(r -> r.getPayDate() != null
                               && !r.getPayDate().isBefore(startDate)
                               && !r.getPayDate().isAfter(endDate))
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) {
                return ViewResult.success("No pay statements in selected date range", filtered);
            }

            String msg = "Retrieved " + filtered.size() + " pay statements in selected date range";
            return ViewResult.success(msg, filtered);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error retrieving pay history for employee " + empId
                    + " between " + startDate + " and " + endDate, ex);
            return ViewResult.failure("Unexpected error retrieving pay history: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Authorization helpers (aligned with EmployeeController semantics)
    // -------------------------------------------------------------------------

    private boolean isAuthorizedToViewEmployee(int empId) {
        if (currentUserRole == UserRole.HR_ADMIN) {
            return true;
        }

        if (currentUserRole == UserRole.EMPLOYEE && currentUser != null) {
            return currentUser.getEmpId() == empId;
        }

        return false;
    }

    // -------------------------------------------------------------------------
    // Result wrapper for the view layer
    // -------------------------------------------------------------------------

    /**
     * Simple result wrapper for view operations.
     * Mirrors the pattern of EmployeeController.ControllerResult but is scoped to this view.
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

        public static <T> ViewResult<T> success(String message) {
            return new ViewResult<>(true, message, null);
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
