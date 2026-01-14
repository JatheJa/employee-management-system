package com.employeemgmt.view.payroll;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.model.Employee;
import com.employeemgmt.util.UserRole;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * JavaFX controller for the "Salary Management" screen.
 *
 * Responsibilities:
 *  - Bind UI controls (min/max salary, percentage, buttons, labels).
 *  - Parse and validate user input.
 *  - Delegate salary update operation to {@link SalaryManagementView},
 *    which in turn uses {@link com.employeemgmt.controller.EmployeeController}.
 *  - Display success / error messages and summary of updates.
 *
 * Per SDD, this feature is HR Admin–only. The hosting code (MainDashboard)
 * should call {@link #setUserContext(UserRole, Employee)} and only expose this
 * screen to HR_ADMIN users.
 */
public class SalaryManagementController {

    private static final Logger LOGGER = Logger.getLogger(SalaryManagementController.class.getName());

    // -------------------------------------------------------------------------
    // FXML-bound controls (must match SalaryManagementView.fxml)
    // -------------------------------------------------------------------------
    @FXML private TextField minSalaryField;
    @FXML private TextField maxSalaryField;
    @FXML private TextField percentageField;

    @FXML private Button applyButton;
    @FXML private Button clearButton;

    @FXML private Label messageLabel;
    @FXML private Label summaryLabel;

    // -------------------------------------------------------------------------
    // Dependencies & context
    // -------------------------------------------------------------------------
    private final SalaryManagementView salaryManagementView;

    private UserRole currentUserRole = UserRole.HR_ADMIN; // default
    private Employee currentUser;

    public SalaryManagementController() {
        this.salaryManagementView = new SalaryManagementView();
        // Default context until dashboard wires a real user
        this.salaryManagementView.setUserContext(UserRole.HR_ADMIN, null);
    }

    @FXML
    public void initialize() {
        try {
            clearMessage();
            clearSummary();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error initializing SalaryManagementController", ex);
            showError("Initialization error: " + ex.getMessage());
        }
    }

    /**
     * Called by MainDashboard after login to propagate user role & identity.
     * Per SDD, only HR_ADMIN should reach this screen.
     */
    public void setUserContext(UserRole role, Employee user) {
        this.currentUserRole = role;
        this.currentUser = user;
        salaryManagementView.setUserContext(role, user);
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------

    /**
     * Handle "Apply Increase" button.
     * Reads min/max salary and percentage from the UI, validates them,
     * and delegates to SalaryManagementView.
     */
    @FXML
    private void handleApplyIncrease() {
        clearMessage();
        clearSummary();

        // Basic role check for sanity (UI should already restrict access)
        if (currentUserRole != UserRole.HR_ADMIN) {
            showError("Access denied: only HR Admin can manage salaries.");
            return;
        }

        // Parse inputs
        String minText = trim(minSalaryField != null ? minSalaryField.getText() : null);
        String maxText = trim(maxSalaryField != null ? maxSalaryField.getText() : null);
        String pctText = trim(percentageField != null ? percentageField.getText() : null);

        if (isBlank(minText) || isBlank(maxText) || isBlank(pctText)) {
            showError("Minimum salary, maximum salary, and percentage increase are all required.");
            return;
        }

        BigDecimal minSalary;
        BigDecimal maxSalary;
        double percentage;

        try {
            minSalary = new BigDecimal(minText);
        } catch (NumberFormatException ex) {
            showError("Minimum salary must be a valid number.");
            return;
        }

        try {
            maxSalary = new BigDecimal(maxText);
        } catch (NumberFormatException ex) {
            showError("Maximum salary must be a valid number.");
            return;
        }

        try {
            percentage = Double.parseDouble(pctText);
        } catch (NumberFormatException ex) {
            showError("Percentage increase must be a valid number (e.g. 3.5).");
            return;
        }

        // Delegate to view / controller layer
        try {
            SalaryManagementView.ViewResult<Map<String, Object>> result =
                    salaryManagementView.applySalaryAdjustment(minSalary, maxSalary, percentage);

            if (result.isSuccess()) {
                showSuccess(result.getMessage());
                updateSummary(result.getData());
            } else {
                showError(result.getMessage());
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error applying salary increase", ex);
            showError("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Handle "Clear" button.
     */
    @FXML
    private void handleClear() {
        if (minSalaryField != null) minSalaryField.clear();
        if (maxSalaryField != null) maxSalaryField.clear();
        if (percentageField != null) percentageField.clear();

        clearMessage();
        clearSummary();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void updateSummary(Map<String, Object> data) {
        if (summaryLabel == null || data == null) {
            return;
        }

        Object updatedCount = data.get("employeesUpdated");
        Object totalOldSalary = data.get("totalOldSalary");
        Object totalNewSalary = data.get("totalNewSalary");
        Object totalIncrease = data.get("totalIncrease");
        Object pctApplied = data.get("percentageApplied");

        StringBuilder sb = new StringBuilder();

        sb.append("Employees Updated: ").append(updatedCount != null ? updatedCount : "0");

        sb.append("\nTotal Old Salary: ")
          .append(formatMoneyOrRaw(totalOldSalary));

        sb.append("\nTotal New Salary: ")
          .append(formatMoneyOrRaw(totalNewSalary));

        sb.append("\nTotal Increase: ")
          .append(formatMoneyOrRaw(totalIncrease));

        sb.append("\nPercentage Applied: ")
          .append(pctApplied != null ? pctApplied.toString() + "%" : "");

        summaryLabel.setText(sb.toString());
    }

    private String formatMoneyOrRaw(Object value) {
        if (value instanceof BigDecimal) {
            return formatMoney((BigDecimal) value);
        }
        return value != null ? value.toString() : "0.00";
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private void clearMessage() {
        if (messageLabel != null) {
            messageLabel.setText("");
            messageLabel.getStyleClass().removeAll("error-message", "success-message");
        }
    }

    private void clearSummary() {
        if (summaryLabel != null) {
            summaryLabel.setText("");
        }
    }

    private void showError(String msg) {
        if (messageLabel == null) {
            return;
        }
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("success-message");
        if (!messageLabel.getStyleClass().contains("error-message")) {
            messageLabel.getStyleClass().add("error-message");
        }
    }

    private void showSuccess(String msg) {
        if (messageLabel == null) {
            return;
        }
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("error-message");
        if (!messageLabel.getStyleClass().contains("success-message")) {
            messageLabel.getStyleClass().add("success-message");
        }
    }

    private String trim(String v) {
        return v == null ? null : v.trim();
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }
}
