package com.employeemgmt.view.reports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.model.Employee;
import com.employeemgmt.util.UserRole;
import com.employeemgmt.view.reports.MonthlyPayByDivisionReport.DivisionPaySummary;
import com.employeemgmt.view.reports.MonthlyPayByDivisionReport.ViewResult;

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
 * JavaFX controller for the "Monthly Pay by Division" report screen.
 *
 * Responsibilities:
 *  - Bind UI controls (date pickers, table, buttons, labels).
 *  - Accept current user context (HR_ADMIN only) from MainDashboard.
 *  - Delegate report generation to {@link MonthlyPayByDivisionReport}.
 *  - Render results and error/success messages.
 */
public class MonthlyPayByDivisionReportController {

    private static final Logger LOGGER =
            Logger.getLogger(MonthlyPayByDivisionReportController.class.getName());

    // -------------------------------------------------------------------------
    // FXML-bound controls (must match MonthlyPayByDivisionReportView.fxml)
    // -------------------------------------------------------------------------

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private TableView<DivisionPaySummary> reportTable;
    @FXML private TableColumn<DivisionPaySummary, String> divisionColumn;
    @FXML private TableColumn<DivisionPaySummary, String> totalGrossColumn;
    @FXML private TableColumn<DivisionPaySummary, String> totalNetColumn;
    @FXML private TableColumn<DivisionPaySummary, String> employeeCountColumn;

    @FXML private Label messageLabel;
    @FXML private Label totalsLabel;

    @FXML private Button generateButton;
    @FXML private Button clearButton;

    // -------------------------------------------------------------------------
    // Dependencies & context
    // -------------------------------------------------------------------------

    private final MonthlyPayByDivisionReport reportView;

    private UserRole currentUserRole = UserRole.HR_ADMIN; // default until injected
    private Employee currentUser;

    public MonthlyPayByDivisionReportController() {
        this.reportView = new MonthlyPayByDivisionReport();
        // default user context; real one will be set by dashboard
        this.reportView.setUserContext(UserRole.HR_ADMIN, null);
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
            LOGGER.log(Level.SEVERE, "Error initializing MonthlyPayByDivisionReportController", ex);
            showError("Initialization error: " + ex.getMessage());
        }
    }

    /**
     * Called by MainDashboard after login to propagate role & identity.
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
        if (reportTable == null) {
            return;
        }

        divisionColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getDivisionName() != null
                                ? cd.getValue().getDivisionName()
                                : ""
                ));

        totalGrossColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(formatMoney(cd.getValue().getTotalGrossPay())));

        totalNetColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(formatMoney(cd.getValue().getTotalNetPay())));

        employeeCountColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(String.valueOf(cd.getValue().getEmployeeCount())));
    }

    // -------------------------------------------------------------------------
    // Event handlers (wire via onAction in FXML)
    // -------------------------------------------------------------------------

    /**
     * Run the report for the selected date range.
     */
    @FXML
    private void handleGenerateReport() {
        clearMessage();
        clearTotals();

        LocalDate start = startDatePicker != null ? startDatePicker.getValue() : null;
        LocalDate end = endDatePicker != null ? endDatePicker.getValue() : null;

        if (start == null || end == null) {
            showError("Start date and end date are required");
            return;
        }

        ViewResult<List<DivisionPaySummary>> result =
                reportView.getMonthlyPayByDivision(start, end);

        if (result.isSuccess()) {
            List<DivisionPaySummary> rows = result.getData();
            populateTable(rows);
            updateTotals(rows);
            showSuccess(result.getMessage());
        } else {
            populateTable(null);
            showError(result.getMessage());
        }
    }

    /**
     * Clear criteria and results.
     */
    @FXML
    private void handleClear() {
        if (startDatePicker != null) {
            startDatePicker.setValue(null);
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(null);
        }
        if (reportTable != null) {
            reportTable.getItems().clear();
        }
        clearMessage();
        clearTotals();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void populateTable(List<DivisionPaySummary> rows) {
        if (reportTable == null) {
            return;
        }

        ObservableList<DivisionPaySummary> items = FXCollections.observableArrayList();
        if (rows != null) {
            items.addAll(rows);
        }
        reportTable.setItems(items);
    }

    private void updateTotals(List<DivisionPaySummary> rows) {
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

        for (DivisionPaySummary row : rows) {
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
                " | Total Employees: " + totalEmployees
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
        if (totalsLabel == null) {
            return;
        }
        totalsLabel.setText("");
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
