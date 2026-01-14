package com.employeemgmt.view.employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.controller.EmployeeController;
import com.employeemgmt.dao.AddressDAO;
import com.employeemgmt.dao.EmployeeDAO;
import com.employeemgmt.model.Employee;
import com.employeemgmt.service.ValidationService;
import com.employeemgmt.util.UserRole;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * JavaFX controller for the "Employee Search" screen.
 *
 * Responsibilities:
 *  - Bind search criteria fields and results table.
 *  - Build search criteria map and delegate to EmployeeController.
 *  - Respect role-based access (HR_ADMIN vs EMPLOYEE) via setUserContext.
 *  - Display results and messages to the user.
 *  - Delegate "edit employee" navigation back to the MainDashboard via a callback.
 */
public class EmployeeSearchController {

    private static final Logger LOGGER = Logger.getLogger(EmployeeSearchController.class.getName());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // -------------------------------------------------------------------------
    // FXML-bound controls (must match EmployeeSearchView.fxml)
    // -------------------------------------------------------------------------
    @FXML private TextField nameField;
    @FXML private TextField ssnField;
    @FXML private TextField empNumberField;
    @FXML private TextField empIdField;
    @FXML private DatePicker dobPicker;

    @FXML private Button searchButton;
    @FXML private Button clearButton;

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> empIdColumn;
    @FXML private TableColumn<Employee, String> empNumberColumn;
    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TableColumn<Employee, String> emailColumn;
    @FXML private TableColumn<Employee, String> hireDateColumn;
    @FXML private TableColumn<Employee, String> statusColumn;
    @FXML private TableColumn<Employee, String> salaryColumn;

    @FXML private Label messageLabel;

    // -------------------------------------------------------------------------
    // Dependencies & context
    // -------------------------------------------------------------------------
    private final EmployeeController employeeController;

    private UserRole currentUserRole = UserRole.HR_ADMIN; // default until set by dashboard
    private Employee currentUser;

    // Callback provided by MainDashboard to open the Edit view (empId -> showEmployeeEditView)
    private Consumer<Integer> onEditEmployee;

    public EmployeeSearchController() {
        this.employeeController = new EmployeeController(
                new EmployeeDAO(),
                new AddressDAO(),
                new ValidationService()
        );
    }

    @FXML
    public void initialize() {
        try {
            initTableColumns();
            initRowDoubleClick();
            clearMessage();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error initializing EmployeeSearchController", ex);
            showError("Initialization error: " + ex.getMessage());
        }
    }

    /**
     * Called by MainDashboard after login to propagate user role & identity.
     */
    public void setUserContext(UserRole role, Employee user) {
        this.currentUserRole = role;
        this.currentUser = user;
        employeeController.setUserContext(role, user);
    }

    /**
     * Called by MainDashboard to provide navigation callback for editing.
     */
    public void setOnEditEmployee(Consumer<Integer> onEditEmployee) {
        this.onEditEmployee = onEditEmployee;
    }

    // -------------------------------------------------------------------------
    // Table configuration
    // -------------------------------------------------------------------------
    private void initTableColumns() {
        if (employeeTable == null) {
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

        salaryColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(formatMoney(cd.getValue().getCurrentSalary())));
    }

    /**
     * Allow double-click on a row to trigger edit for that employee.
     */
    private void initRowDoubleClick() {
        if (employeeTable == null) {
            return;
        }

        employeeTable.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Employee rowData = row.getItem();
                    invokeEdit(rowData);
                }
            });
            return row;
        });
    }

    // -------------------------------------------------------------------------
    // Event handlers (wired in FXML)
    // -------------------------------------------------------------------------
    @FXML
    private void handleSearch() {
        clearMessage();

        Map<String, Object> criteria = new HashMap<>();

        String name = trim(nameField.getText());
        String ssn = trim(ssnField.getText());
        String empNumber = trim(empNumberField.getText());
        String empIdText = trim(empIdField.getText());
        LocalDate dob = dobPicker != null ? dobPicker.getValue() : null;

        if (!isBlank(name)) {
            criteria.put("name", name);
        }
        if (!isBlank(ssn)) {
            criteria.put("ssn", ssn);
        }
        if (!isBlank(empNumber)) {
            criteria.put("empNumber", empNumber);
        }
        if (!isBlank(empIdText)) {
            try {
                criteria.put("empId", Integer.parseInt(empIdText));
            } catch (NumberFormatException ex) {
                showError("Employee ID must be a valid number");
                return;
            }
        }
        if (dob != null) {
            criteria.put("dateOfBirth", dob);
        }

        // Delegate to EmployeeController (enforces role-based rules)
        EmployeeController.ControllerResult<List<Employee>> result =
                employeeController.searchEmployees(criteria);

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
        if (nameField != null) nameField.clear();
        if (ssnField != null) ssnField.clear();
        if (empNumberField != null) empNumberField.clear();
        if (empIdField != null) empIdField.clear();
        if (dobPicker != null) dobPicker.setValue(null);

        if (employeeTable != null) {
            employeeTable.getItems().clear();
        }

        clearMessage();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private void invokeEdit(Employee employee) {
        if (employee == null) {
            showError("No employee selected.");
            return;
        }

        if (onEditEmployee != null) {
            onEditEmployee.accept(employee.getEmpId());
        } else {
            // This indicates MainDashboard forgot to configure the callback
            showError("Edit action is not configured.");
        }
    }

    private void populateTable(List<Employee> employees) {
        if (employeeTable == null) {
            return;
        }

        ObservableList<Employee> items = FXCollections.observableArrayList();
        if (employees != null) {
            items.addAll(employees);
        }
        employeeTable.setItems(items);
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FMT);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "";
        return value.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    private String nullSafe(String v) {
        return v == null ? "" : v;
    }

    private String trim(String v) {
        return v == null ? null : v.trim();
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
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
