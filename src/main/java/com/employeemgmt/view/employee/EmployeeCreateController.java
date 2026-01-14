package com.employeemgmt.view.employee;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.controller.EmployeeController;
import com.employeemgmt.dao.CityDAO;
import com.employeemgmt.dao.StateDAO;
import com.employeemgmt.model.Address;
import com.employeemgmt.model.City;
import com.employeemgmt.model.Employee;
import com.employeemgmt.model.State;
import com.employeemgmt.util.UserRole;
import com.employeemgmt.view.employee.EmployeeCreateView.EmployeeForm;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * JavaFX controller backing the Create Employee screen.
 * Handles UI bindings, collects input, populates the EmployeeForm DTO,
 * and delegates create operations to EmployeeCreateView.
 */
public class EmployeeCreateController {

    private static final Logger LOGGER = Logger.getLogger(EmployeeCreateController.class.getName());

    // -------------------------------------------------------------------------
    // FXML-bound UI controls (MUST MATCH FXML EXACTLY)
    // -------------------------------------------------------------------------
    @FXML private TextField empNumberField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField ssnField;
    @FXML private DatePicker hireDatePicker;
    @FXML private TextField salaryField;

    // NEW – matches corrected FXML
    @FXML private ComboBox<Employee.EmploymentStatus> employmentStatusComboBox;

    @FXML private TextField streetField;
    @FXML private ComboBox<State> stateComboBox;
    @FXML private ComboBox<City> cityComboBox;

    @FXML private TextField zipField;
    @FXML private ComboBox<Address.Gender> genderComboBox;
    @FXML private TextField raceField;
    @FXML private DatePicker dobPicker;
    @FXML private TextField phoneField;

    @FXML private Label messageLabel;
    @FXML private Button saveButton;
    @FXML private Button clearButton;

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    private final EmployeeCreateView employeeCreateView;
    private final CityDAO cityDAO;
    private final StateDAO stateDAO;

    private ObservableList<State> stateItems = FXCollections.observableArrayList();
    private ObservableList<City> cityItems = FXCollections.observableArrayList();

    public EmployeeCreateController() {
        this.employeeCreateView = new EmployeeCreateView();
        this.cityDAO = new CityDAO();
        this.stateDAO = new StateDAO();
    }

    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------
    @FXML
    public void initialize() {
        try {
            employeeCreateView.setUserContext(UserRole.HR_ADMIN, null); // temp until login is implemented

            initEmploymentStatusComboBox();
            initGenderComboBox();
            initStateComboBox();
            initCityComboBox();

            clearMessage();

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Initialization error", ex);
            showError("Initialization error: " + ex.getMessage());
        }
    }

    private void initEmploymentStatusComboBox() {
        employmentStatusComboBox.setItems(
                FXCollections.observableArrayList(Employee.EmploymentStatus.values())
        );
        employmentStatusComboBox.getSelectionModel().select(Employee.EmploymentStatus.ACTIVE);
    }

    private void initGenderComboBox() {
        genderComboBox.setItems(FXCollections.observableArrayList(Address.Gender.values()));
        genderComboBox.getSelectionModel().clearSelection();
    }

    private void initStateComboBox() throws SQLException {
        List<State> states = stateDAO.findAll();
        stateItems = FXCollections.observableArrayList(states);
        stateComboBox.setItems(stateItems);

        // On state change → reload cities
        stateComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            try {
                onStateChanged(newVal);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to load cities", e);
                showError("Could not load cities for selected state.");
            }
        });
    }

    private void initCityComboBox() {
        cityComboBox.setItems(cityItems);
    }

    private void onStateChanged(State selectedState) throws SQLException {
        cityItems.clear();
        cityComboBox.getSelectionModel().clearSelection();
        if (selectedState != null) {
            cityItems.addAll(cityDAO.findByStateId(selectedState.getStateId()));
        }
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------
    @FXML
    private void handleSave() {
        clearMessage();
        try {
            EmployeeForm form = buildFormFromUI();
            EmployeeController.ControllerResult<Employee> result =
                    employeeCreateView.submitNewEmployee(form);

            if (result.isSuccess()) {
                Employee created = result.getData();
                String msg = result.getMessage();
                if (created != null) {
                    msg += " (ID: " + created.getEmpId() + ")";
                }
                showSuccess(msg);
                clearForm();

            } else {
                showError(result.getMessage());
            }

        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "Validation error", ex);
            showError(ex.getMessage());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error in save", ex);
            showError("Unexpected error: " + ex.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
        clearMessage();
    }

    // -------------------------------------------------------------------------
    // Map UI → Form DTO
    // -------------------------------------------------------------------------
    private EmployeeForm buildFormFromUI() {
        EmployeeForm form = new EmployeeForm();

        form.setEmpNumber(trim(empNumberField.getText()));
        form.setFirstName(trim(firstNameField.getText()));
        form.setLastName(trim(lastNameField.getText()));
        form.setEmail(trim(emailField.getText()));
        form.setSsn(trim(ssnField.getText()));
        form.setHireDate(hireDatePicker.getValue());

        String salaryText = trim(salaryField.getText());
        if (!isBlank(salaryText)) {
            try {
                form.setCurrentSalary(new BigDecimal(salaryText));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Salary must be a valid number");
            }
        }

        // NEW: Employment status from ComboBox
        form.setEmploymentStatus(
                employmentStatusComboBox.getSelectionModel().getSelectedItem()
        );

        // Address fields
        form.setStreet(trim(streetField.getText()));

        State st = stateComboBox.getSelectionModel().getSelectedItem();
        City ct = cityComboBox.getSelectionModel().getSelectedItem();
        if (st != null) form.setStateId(st.getStateId());
        if (ct != null) form.setCityId(ct.getCityId());

        form.setZip(trim(zipField.getText()));
        form.setGender(genderComboBox.getSelectionModel().getSelectedItem());
        form.setRace(trim(raceField.getText()));
        form.setDateOfBirth(dobPicker.getValue());
        form.setPhone(trim(phoneField.getText()));

        return form;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private void clearForm() {
        empNumberField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        ssnField.clear();
        hireDatePicker.setValue(null);
        salaryField.clear();
        employmentStatusComboBox.getSelectionModel().select(Employee.EmploymentStatus.ACTIVE);

        streetField.clear();
        stateComboBox.getSelectionModel().clearSelection();
        cityComboBox.getSelectionModel().clearSelection();
        cityItems.clear();

        zipField.clear();
        genderComboBox.getSelectionModel().clearSelection();
        raceField.clear();
        dobPicker.setValue(null);
        phoneField.clear();
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll("error-message", "success-message");
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("success-message");
        if (!messageLabel.getStyleClass().contains("error-message")) {
            messageLabel.getStyleClass().add("error-message");
        }
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("error-message");
        if (!messageLabel.getStyleClass().contains("success-message")) {
            messageLabel.getStyleClass().add("success-message");
        }
    }

    private String trim(String v) { return v == null ? null : v.trim(); }

    private boolean isBlank(String v) { return v == null || v.trim().isEmpty(); }
}
