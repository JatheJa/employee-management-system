package com.employeemgmt.view.payroll;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.controller.EmployeeController;
import com.employeemgmt.dao.AddressDAO;
import com.employeemgmt.dao.EmployeeDAO;
import com.employeemgmt.model.Employee;
import com.employeemgmt.service.ValidationService;
import com.employeemgmt.util.UserRole;

/**
 * View-layer class responsible for orchestrating the "Salary Management" flow.
 *
 * Responsibilities (per SDD and MVC separation):
 *  - Provide a simple API for the JavaFX controller to invoke salary updates
 *    by percentage, within a given salary range.
 *  - Propagate user context (role + logged-in employee) into the underlying
 *    {@link EmployeeController}, which enforces authorization rules
 *    (HR_ADMIN vs EMPLOYEE).
 *  - Wrap {@link EmployeeController#updateSalariesByPercentage(BigDecimal, BigDecimal, double)}
 *    in a UI-friendly {@link ViewResult} that the UI can use to display messages.
 *
 * Typical usage by a JavaFX controller:
 *  1. Call {@link #setUserContext(UserRole, Employee)} after login.
 *  2. When the user submits the form (min salary, max salary, % increase),
 *     parse the inputs to {@link BigDecimal} / {@link Double}.
 *  3. Call {@link #applySalaryAdjustment(BigDecimal, BigDecimal, double)}.
 *  4. Inspect the returned {@link ViewResult} to show success/failure and
 *     summary details (employees updated, totals, etc.).
 */
public class SalaryManagementView {

    private static final Logger LOGGER = Logger.getLogger(SalaryManagementView.class.getName());

    private final EmployeeController employeeController;

    /**
     * Primary constructor for dependency injection.
     */
    public SalaryManagementView(EmployeeController employeeController) {
        this.employeeController = Objects.requireNonNull(employeeController, "employeeController cannot be null");
    }

    /**
     * Convenience constructor.
     * Wires a default EmployeeController with concrete DAO and service implementations.
     *
     * In a larger app, this wiring would typically live in an application-level
     * composition root or DI framework, but this keeps the view self-contained.
     */
    public SalaryManagementView() {
        this(new EmployeeController(
                new EmployeeDAO(),
                new AddressDAO(),
                new ValidationService()
        ));
    }

    /**
     * Propagate the current logged-in user and role into the underlying controller,
     * so that authorization (HR_ADMIN vs EMPLOYEE) is enforced per the SDD.
     *
     * @param role current user's role
     * @param user current logged-in employee (nullable, for admin accounts not
     *             tied to a specific Employee record)
     */
    public void setUserContext(UserRole role, Employee user) {
        employeeController.setUserContext(role, user);
    }

    // -------------------------------------------------------------------------
    // Salary Update Operation
    // -------------------------------------------------------------------------

    /**
     * Apply a percentage-based salary adjustment for employees within the given
     * salary range. This is the main operation needed by the Salary Management UI.
     *
     * Delegates to {@link EmployeeController#updateSalariesByPercentage(BigDecimal, BigDecimal, double)}.
     *
     * @param minSalary          minimum salary in range (inclusive)
     * @param maxSalary          maximum salary in range (inclusive)
     * @param percentageIncrease percentage to apply (e.g. 3.5 for +3.5%)
     * @return ViewResult containing success flag, message, and a summary map
     *         with keys such as:
     *         <ul>
     *             <li>"employeesUpdated" (Integer)</li>
     *             <li>"totalOldSalary" (BigDecimal)</li>
     *             <li>"totalNewSalary" (BigDecimal)</li>
     *             <li>"totalIncrease" (BigDecimal)</li>
     *             <li>"percentageApplied" (Double)</li>
     *         </ul>
     */
    public ViewResult<Map<String, Object>> applySalaryAdjustment(
            BigDecimal minSalary,
            BigDecimal maxSalary,
            double percentageIncrease) {

        if (minSalary == null || maxSalary == null) {
            return ViewResult.failure("Minimum and maximum salary are required");
        }

        try {
            EmployeeController.ControllerResult<Map<String, Object>> controllerResult =
                    employeeController.updateSalariesByPercentage(minSalary, maxSalary, percentageIncrease);

            if (controllerResult.isSuccess()) {
                LOGGER.info("Salary adjustment succeeded: " + controllerResult.getMessage());
                return ViewResult.success(controllerResult.getMessage(), controllerResult.getData());
            } else {
                LOGGER.warning("Salary adjustment failed: " + controllerResult.getMessage());
                return ViewResult.failure(controllerResult.getMessage());
            }

        } catch (IllegalArgumentException ex) {
            // UI-level feedback for parameter issues
            LOGGER.log(Level.WARNING, "Invalid salary adjustment parameters", ex);
            return ViewResult.failure(ex.getMessage());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error during salary adjustment", ex);
            return ViewResult.failure("Unexpected error: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // ViewResult wrapper (UI-friendly)
    // -------------------------------------------------------------------------

    /**
     * Simple result wrapper for view-layer operations, similar to the pattern
     * used in {@link com.employeemgmt.view.payroll.PayStatementView}.
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
