package com.employeemgmt.view.reports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.model.Employee;
import com.employeemgmt.util.UserRole;
import com.employeemgmt.view.reports.MonthlyPayByJobTitleReport.JobTitlePaySummary;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * JavaFX controller for the "Monthly Pay by Job Title" report screen.
 *
 * Responsibilities:
 *  - Bind date filters, report table, and message/totals labels.
 *  - Delegate business logic to {@link MonthlyPayByJobTitleReport}.
 *  - Enforce HR-only access via user context (passed from MainDashboard).
 */
public class MonthlyPayByJobTitleReportController {

    private static final Logger LOGGER =
            Logger.getLogger(MonthlyPayByJobTitleReportController.class.getName());

    // -------------------------------------------------------------------------
    // FXML-bound controls (must match MonthlyPayByJobTitleReportView.fxml)
    // -------------------------------------------------------------------------

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private TableView<JobTitlePaySummary> jobTitleTable;
    @FXML private TableColumn<JobTitlePaySummary, String> jobTitleColumn;
    @FXML private TableColumn<JobTitlePaySummary, String> totalGrossColumn;
    @FXML private TableColumn<JobTitlePaySummary, String> totalNetColumn;
    @FXML private TableColumn<JobTitlePaySummary, String> employeeCountColumn;

    @FXML private Label messageLabel;
    @FXML private Label totalsLabel;

    @FXML private Button runReportButton;
    @FXML private Button clearButton;

    // -------------------------------------------------------------------------
    // Dependencies & context
    // -------------------------------------------------------------------------

    private final MonthlyPayByJobTitleReport reportView;

    private UserRole currentUserRole = UserRole.HR_ADMIN; // default until set by dashboard
    private Employee currentUser;

    public MonthlyPayByJobTitleReportController() {
        this.reportView = new MonthlyPayByJobTitleReport();
    }

    /**
     * Called automatically by JavaFX after FXML injection.
     */
    @FXML
    public void initialize() {
        try {
            initTableColumns();
            clearMessage();
            clearTotals();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE,
                    "Error initializing MonthlyPayByJobTitleReportController", ex);
            showError("Initialization error: " + ex.getMessage());
        }
    }

    /**
     * Called by MainDashboard after login to inject HR_ADMIN context.
     */
    public void setUserContext(UserRole role, Employee user) {
        this.currentUserRole = role;
        this.currentUser = user;
        reportView.setUserContext(role, user);
    }

    // -------------------------------------------------------------------------
    // Table configuration
    // -------------------------------------------------------------------------

    private void initTableColumns() {
        if (jobTitleTable == null) {
            return;
        }

        jobTitleColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getJobTitle()));

        totalGrossColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(formatMoney(cd.getValue().getTotalGrossPay())));

        totalNetColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(formatMoney(cd.getValue().getTotalNetPay())));

        employeeCountColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(String.valueOf(cd.getValue().getEmployeeCount())));
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------

    @FXML
    private void handleRunReport() {
        clearMessage();
        clearTotals();

        LocalDate start = startDatePicker != null ? startDatePicker.getValue() : null;
        LocalDate end = endDatePicker != null ? endDatePicker.getValue() : null;

        if (start == null || end == null) {
            showError("Start date and end date are required.");
            return;
        }

        MonthlyPayByJobTitleReport.ViewResult<List<JobTitlePaySummary>> result =
                reportView.generateReport(start, end);

        if (result.isSuccess()) {
            List<JobTitlePaySummary> rows = result.getData();
            populateTable(rows);
            showSuccess(result.getMessage());
        } else {
            populateTable(null);
            showError(result.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        if (startDatePicker != null) {
            startDatePicker.setValue(null);
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(null);
        }
        if (jobTitleTable != null) {
            jobTitleTable.getItems().clear();
        }
        clearMessage();
        clearTotals();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void populateTable(List<JobTitlePaySummary> rows) {
        if (jobTitleTable == null) {
            return;
        }

        ObservableList<JobTitlePaySummary> items = FXCollections.observableArrayList();
        if (rows != null) {
            items.addAll(rows);
        }
        jobTitleTable.setItems(items);

        updateTotals(rows);
    }

    private void updateTotals(List<JobTitlePaySummary> rows) {
        if (totalsLabel == null) {
            return;
        }

        if (rows == null || rows.isEmpty()) {
            clearTotals();
            return;
        }

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        int totalEmployees = 0;

        for (JobTitlePaySummary row : rows) {
            if (row.getTotalGrossPay() != null) {
                totalGross = totalGross.add(row.getTotalGrossPay());
            }
            if (row.getTotalNetPay() != null) {
                totalNet = totalNet.add(row.getTotalNetPay());
            }
            totalEmployees += row.getEmployeeCount();
        }

        totalsLabel.setText(
                "Total Gross: " + formatMoney(totalGross) +
                " | Total Net: " + formatMoney(totalNet) +
                " | Total Employees (summed across titles): " + totalEmployees
        );
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private void clearMessage() {
        if (messageLabel == null) {
            return;
        }
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll("error-message", "success-message");
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
