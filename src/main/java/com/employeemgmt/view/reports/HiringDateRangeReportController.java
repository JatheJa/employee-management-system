package com.employeemgmt.view.reports;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.model.Employee;
import com.employeemgmt.util.UserRole;
import com.employeemgmt.view.reports.HiringDateRangeReport.ViewResult;

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
 * JavaFX controller for the "Hiring Date Range Report" screen.
 *
 * Responsibilities:
 *  - Bind UI controls (date pickers, run/clear buttons, results table, message label).
 *  - Accept user context (role + current employee) from MainDashboard.
 *  - Delegate report logic to {@link HiringDateRangeReport}.
 *  - Populate the results table and display messages.
 *
 * Expected FXML (to be created separately):
 *  - fx:controller="com.employeemgmt.view.reports.HiringDateRangeReportController"
 *  - DatePickers: startDatePicker, endDatePicker
 *  - Buttons: runReportButton (onAction="#handleRunReport"), clearButton (onAction="#handleClear")
 *  - TableView<Employee>: hiringTable with columns:
 *        empIdColumn, empNumberColumn, nameColumn, emailColumn, hireDateColumn, statusColumn
 *  - Label: messageLabel
 */
public class HiringDateRangeReportController {

    private static final Logger LOGGER =
            Logger.getLogger(HiringDateRangeReportController.class.getName());

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // -------------------------------------------------------------------------
    // FXML-bound controls
    // -------------------------------------------------------------------------
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private Button runReportButton;
    @FXML private Button clearButton;

    @FXML private TableView<Employee> hiringTable;
    @FXML private TableColumn<Employee, String> empIdColumn;
    @FXML private TableColumn<Employee, String> empNumberColumn;
    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TableColumn<Employee, String> emailColumn;
    @FXML private TableColumn<Employee, String> hireDateColumn;
    @FXML private TableColumn<Employee, String> statusColumn;

    @FXML private Label messageLabel;

    // -------------------------------------------------------------------------
    // Dependencies & context
    // -------------------------------------------------------------------------
    private final HiringDateRangeReport reportService;

    private UserRole currentUserRole = UserRole.HR_ADMIN;
    private Employee currentUser;

    public HiringDateRangeReportController() {
        this.reportService = new HiringDateRangeReport();
    }

    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------
    @FXML
    public void initialize() {
        try {
            initTableColumns();
            initDefaultDates();
            clearMessage();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error initializing HiringDateRangeReportController", ex);
            showError("Initialization error: " + ex.getMessage());
        }
    }

    /**
     * Called by MainDashboard to propagate user role and identity.
     */
    public void setUserContext(UserRole role, Employee user) {
        this.currentUserRole = role;
        this.currentUser = user;
        reportService.setUserContext(role, user);
    }

    private void initTableColumns() {
        if (hiringTable == null) {
            return;
        }

        empIdColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(String.valueOf(cd.getValue().getEmpId())));

        empNumberColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(nullSafe(cd.getValue().getEmpNumber())));

        nameColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        (nullSafe(cd.getValue().getFirstName()) + " " +
                         nullSafe(cd.getValue().getLastName())).trim()
                ));

        emailColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(nullSafe(cd.getValue().getEmail())));

        hireDateColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(formatDate(cd.getValue().getHireDate())));

        statusColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getEmploymentStatus() != null
                                ? cd.getValue().getEmploymentStatus().name()
                                : ""
                ));
    }

    /**
     * Optional: set a sensible initial range (e.g., last 30 days).
     */
    private void initDefaultDates() {
        if (startDatePicker == null || endDatePicker == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        endDatePicker.setValue(today);
        startDatePicker.setValue(today.minusDays(30));
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------
    @FXML
    private void handleRunReport() {
        clearMessage();

        LocalDate startDate = startDatePicker != null ? startDatePicker.getValue() : null;
        LocalDate endDate = endDatePicker != null ? endDatePicker.getValue() : null;

        if (startDate == null || endDate == null) {
            showError("Start date and end date are required");
            return;
        }

        ViewResult<List<Employee>> result =
                reportService.getHiringReport(startDate, endDate);

        if (result.isSuccess()) {
            populateTable(result.getData());
            showSuccess(result.getMessage());
        } else {
            populateTable(null);
            showError(result.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        if (startDatePicker != null) startDatePicker.setValue(null);
        if (endDatePicker != null) endDatePicker.setValue(null);

        if (hiringTable != null) {
            hiringTable.getItems().clear();
        }

        clearMessage();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private void populateTable(List<Employee> employees) {
        if (hiringTable == null) {
            return;
        }

        ObservableList<Employee> items = FXCollections.observableArrayList();
        if (employees != null) {
            items.addAll(employees);
        }
        hiringTable.setItems(items);
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FMT);
    }

    private String nullSafe(String v) {
        return v == null ? "" : v;
    }

    private void clearMessage() {
        if (messageLabel == null) {
            return;
        }
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll("error-message", "success-message");
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
