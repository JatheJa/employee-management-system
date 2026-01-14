package com.employeemgmt.view.payroll;

import com.employeemgmt.model.Employee;
import com.employeemgmt.model.PayrollRecord;
import com.employeemgmt.util.UserRole;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX controller for the "Pay Statement History" screen.
 *
 * Responsibilities (per SDD and MVC separation):
 *  - Bind UI controls (table, filters, buttons).
 *  - Resolve which employee's pay history to show (HR Admin vs Employee).
 *  - Delegate business logic / data access to {@link PayStatementView}.
 *  - Render results and error messages to the user.
 *
 * Typical usage:
 *  - HR Admin: enters an Employee ID in empIdField, then clicks "Load All"
 *    or "Filter by Date".
 *  - Employee: when integrated with Login/MainDashboard, the hosting code
 *    calls {@link #setUserContext(UserRole, Employee)}; this controller then
 *    uses the current user's empId automatically and can ignore empIdField.
 */
public class PayStatementController {

    private static final Logger LOGGER = Logger.getLogger(PayStatementController.class.getName());

    // -------------------------------------------------------------------------
    // FXML-bound UI controls (must match the PayStatementView.fxml)
    // -------------------------------------------------------------------------

    // Filters
    @FXML private TextField empIdField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    // Table and columns
    @FXML private TableView<PayrollRecord> payTable;
    @FXML private TableColumn<PayrollRecord, String> payDateColumn;
    @FXML private TableColumn<PayrollRecord, String> periodColumn;
    @FXML private TableColumn<PayrollRecord, String> grossPayColumn;
    @FXML private TableColumn<PayrollRecord, String> netPayColumn;
    @FXML private TableColumn<PayrollRecord, String> federalTaxColumn;
    @FXML private TableColumn<PayrollRecord, String> stateTaxColumn;
    @FXML private TableColumn<PayrollRecord, String> otherDeductionsColumn;

    // Status / totals
    @FXML private Label messageLabel;
    @FXML private Label totalsLabel;

    // Buttons (for wiring onAction in FXML, even if not used directly here)
    @FXML private Button loadAllButton;
    @FXML private Button filterButton;
    @FXML private Button myStatementsButton;

    // -------------------------------------------------------------------------
    // Dependencies & context
    // -------------------------------------------------------------------------

    private final PayStatementView payStatementView;

    // Optional: store user context so we can adjust behavior (EMPLOYEE vs HR_ADMIN)
    private UserRole currentUserRole = UserRole.HR_ADMIN; // default until wired with login
    private Employee currentUser;

    public PayStatementController() {
        this.payStatementView = new PayStatementView();
        // Default context: HR admin, can see any employee.
        this.payStatementView.setUserContext(UserRole.HR_ADMIN, null);
    }

    /**
     * Called automatically by JavaFX after FXML injection.
     * Initializes table columns, default states, etc.
     */
    @FXML
    public void initialize() {
        try {
            initTableColumns();
            clearMessage();
            clearTotals();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error initializing PayStatementController", ex);
            showError("Initialization error: " + ex.getMessage());
        }
    }

    /**
     * Allows hosting code (e.g., MainDashboard after login) to inject current
     * user context. This keeps role-based behavior aligned with the SDD.
     */
    public void setUserContext(UserRole role, Employee user) {
        this.currentUserRole = role;
        this.currentUser = user;
        this.payStatementView.setUserContext(role, user);

        // For an EMPLOYEE, we typically ignore empIdField and always show their own statements.
        if (role == UserRole.EMPLOYEE && user != null) {
            if (empIdField != null) {
                empIdField.setText(String.valueOf(user.getEmpId()));
                empIdField.setEditable(false);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Table configuration
    // -------------------------------------------------------------------------

    private void initTableColumns() {
        if (payTable == null) {
            return; // defensive: FXML not wired yet
        }

        payDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedPayDate()));

        periodColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedPayPeriod()));

        grossPayColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatMoney(cellData.getValue().getGrossPay())));

        netPayColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatMoney(cellData.getValue().getNetPay())));

        federalTaxColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatMoney(cellData.getValue().getFederalTax())));

        stateTaxColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatMoney(cellData.getValue().getStateTax())));

        otherDeductionsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatMoney(cellData.getValue().getOtherDeductions())));
    }

    // -------------------------------------------------------------------------
    // Event handlers (wire via onAction in FXML)
    // -------------------------------------------------------------------------

    /**
     * Load all pay statements for the target employee.
     */
    @FXML
    private void handleLoadAll() {
        clearMessage();
        clearTotals();

        Integer targetEmpId = resolveTargetEmpId();
        if (targetEmpId == null) {
            return; // error already shown
        }

        PayStatementView.ViewResult<List<PayrollRecord>> result =
                payStatementView.getPayHistoryForEmployee(targetEmpId);

        if (result.isSuccess()) {
            List<PayrollRecord> records = result.getData();
            populateTable(records);
            showSuccess(result.getMessage());
        } else {
            populateTable(null);
            showError(result.getMessage());
        }
    }

    /**
     * Load pay statements filtered by pay date range for the target employee.
     */
    @FXML
    private void handleFilterByDate() {
        clearMessage();
        clearTotals();

        Integer targetEmpId = resolveTargetEmpId();
        if (targetEmpId == null) {
            return; // error already shown
        }

        LocalDate startDate = startDatePicker != null ? startDatePicker.getValue() : null;
        LocalDate endDate = endDatePicker != null ? endDatePicker.getValue() : null;

        if (startDate == null || endDate == null) {
            showError("Start date and end date are required");
            return;
        }

        PayStatementView.ViewResult<List<PayrollRecord>> result =
                payStatementView.getPayHistoryForEmployeeByDateRange(targetEmpId, startDate, endDate);

        if (result.isSuccess()) {
            List<PayrollRecord> records = result.getData();
            populateTable(records);
            showSuccess(result.getMessage());
        } else {
            populateTable(null);
            showError(result.getMessage());
        }
    }

    /**
     * Convenience action: for logged-in employees to view their own pay statements
     * without entering an Employee ID.
     */
    @FXML
    private void handleLoadMyStatements() {
        clearMessage();
        clearTotals();

        if (currentUserRole != UserRole.EMPLOYEE || currentUser == null) {
            showError("This action is only available for logged-in employees");
            return;
        }

        PayStatementView.ViewResult<List<PayrollRecord>> result =
                payStatementView.getPayHistoryForCurrentUser();

        if (result.isSuccess()) {
            List<PayrollRecord> records = result.getData();
            populateTable(records);
            showSuccess(result.getMessage());
        } else {
            populateTable(null);
            showError(result.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Determine which employee's statements to load, based on role:
     *  - EMPLOYEE: always use currentUser.empId.
     *  - HR_ADMIN: parse from empIdField.
     */
    private Integer resolveTargetEmpId() {
        if (currentUserRole == UserRole.EMPLOYEE && currentUser != null) {
            return currentUser.getEmpId();
        }

        // HR Admin (or other roles) must supply an empId in the text field
        if (empIdField == null) {
            showError("Employee ID field is not available");
            return null;
        }

        String text = empIdField.getText();
        if (text == null || text.trim().isEmpty()) {
            showError("Employee ID is required");
            return null;
        }

        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            showError("Employee ID must be a valid number");
            return null;
        }
    }

    private void populateTable(List<PayrollRecord> records) {
        if (payTable == null) {
            return;
        }

        ObservableList<PayrollRecord> items = FXCollections.observableArrayList();
        if (records != null) {
            items.addAll(records);
        }
        payTable.setItems(items);

        updateTotals(records);
    }

    private void updateTotals(List<PayrollRecord> records) {
        if (totalsLabel == null) {
            return;
        }

        if (records == null || records.isEmpty()) {
            clearTotals();
            return;
        }

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;

        for (PayrollRecord r : records) {
            if (r.getGrossPay() != null) {
                totalGross = totalGross.add(r.getGrossPay());
            }
            if (r.getNetPay() != null) {
                totalNet = totalNet.add(r.getNetPay());
            }
            totalDeductions = totalDeductions.add(r.getTotalDeductions());
        }

        totalsLabel.setText(
                "Total Gross: " + formatMoney(totalGross) +
                " | Total Net: " + formatMoney(totalNet) +
                " | Total Deductions: " + formatMoney(totalDeductions)
        );
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void clearMessage() {
        if (messageLabel != null) {
            messageLabel.setText("");
            messageLabel.getStyleClass().removeAll("error-message", "success-message");
        }
    }

    private void clearTotals() {
        if (totalsLabel != null) {
            totalsLabel.setText("");
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
}
