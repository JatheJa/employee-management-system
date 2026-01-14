package com.employeemgmt.view.employee;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.dao.CityDAO;
import com.employeemgmt.dao.StateDAO;
import com.employeemgmt.model.Address;
import com.employeemgmt.model.City;
import com.employeemgmt.model.Employee;
import com.employeemgmt.model.Employee.EmploymentStatus;
import com.employeemgmt.model.State;
import com.employeemgmt.util.UserRole;
import com.employeemgmt.view.employee.EmployeeEditView.EmployeeEditForm;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * JavaFX controller backing the "Edit Employee" screen.
 *
 * Responsibilities:
 *  - Bind UI controls to FXML.
 *  - Load an employee into an {@link EmployeeEditForm} via {@link EmployeeEditView}.
 *  - Populate the UI from that form.
 *  - Collect edited values back into an EmployeeEditForm and submit update.
 */
public class EmployeeEditController {

    private static final Logger LOGGER = Logger.getLogger(EmployeeEditController.class.getName());

    // -------------------------------------------------------------------------
    // FXML-bound UI controls (must match EmployeeEditView.fxml IDs)
    // -------------------------------------------------------------------------

    // Header / identity
    @FXML private Label empIdLabel;

    // Employee fields
    @FXML private TextField empNumberField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField ssnField;
    @FXML private DatePicker hireDatePicker;
    @FXML private TextField salaryField;
    @FXML private ComboBox<EmploymentStatus> employmentStatusComboBox;

    // Address & demographics
    @FXML private TextField streetField;
    @FXML private ComboBox<State> stateComboBox;
    @FXML private ComboBox<City> cityComboBox;
    @FXML private TextField zipField;
    @FXML private ComboBox<Address.Gender> genderComboBox;
    @FXML private TextField raceField;
    @FXML private DatePicker dobPicker;
    @FXML private TextField phoneField;

    // Status / buttons
    @FXML private Label messageLabel;
    @FXML private Button saveButton;
    @FXML private Button reloadButton;

    // -------------------------------------------------------------------------
    // Dependencies & context
    // -------------------------------------------------------------------------

    private final EmployeeEditView employeeEditView;
    private final CityDAO cityDAO;
    private final StateDAO stateDAO;

    private ObservableList<State> stateItems = FXCollections.observableArrayList();
    private ObservableList<City> cityItems = FXCollections.observableArrayList();

    private int currentEmpId = -1;

    public EmployeeEditController() {
        this.employeeEditView = new EmployeeEditView();
        this.cityDAO = new CityDAO();
        this.stateDAO = new StateDAO();
    }

    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        try {
            // Default context: HR_ADMIN until wired from dashboard/login
            employeeEditView.setUserContext(UserRole.HR_ADMIN, null);

            initEmploymentStatusComboBox();
            initGenderComboBox();
            initStateComboBox();
            initCityComboBox();

            clearMessage();

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error initializing EmployeeEditController", ex);
            showError("Initialization error: " + ex.getMessage());
        }
    }

    private void initEmploymentStatusComboBox() {
        employmentStatusComboBox.setItems(
                FXCollections.observableArrayList(EmploymentStatus.values())
        );
        // For edit, we leave it unselected until a specific employee is loaded.
    }

    private void initGenderComboBox() {
        genderComboBox.setItems(FXCollections.observableArrayList(Address.Gender.values()));
        genderComboBox.getSelectionModel().clearSelection();
    }

    private void initStateComboBox() {
        try {
            List<State> states = stateDAO.findAll();
            stateItems = FXCollections.observableArrayList(states);
            stateComboBox.setItems(stateItems);

            stateComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldState, newState) -> {
                try {
                    onStateChanged(newState);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Failed to load cities for state", e);
                    showError("Could not load cities for selected state.");
                }
            });
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error loading states", ex);
            showError("Could not load states: " + ex.getMessage());
        }
    }

    private void initCityComboBox() {
        cityComboBox.setItems(cityItems);
    }

    private void onStateChanged(State selectedState) throws SQLException {
        cityItems.clear();
        cityComboBox.getSelectionModel().clearSelection();

        if (selectedState == null) {
            return;
        }

        List<City> cities = cityDAO.findByStateId(selectedState.getStateId());
        cityItems.addAll(cities);
    }

    // -------------------------------------------------------------------------
    // Public context / load API (used by dashboard/search)
    // -------------------------------------------------------------------------

    /**
     * Injects the current user context (HR Admin vs Employee).
     * This is called from MainDashboard / search results wiring.
     */
    public void setUserContext(UserRole role, Employee currentUser) {
        employeeEditView.setUserContext(role, currentUser);
    }

    /**
     * Load an employee by ID into the edit form and populate the UI.
     * Intended to be called from search results or dashboard.
     */
    public void loadEmployeeForEdit(int empId) {
        clearMessage();

        if (empId <= 0) {
            showError("Invalid employee ID for edit.");
            return;
        }

        try {
            EmployeeEditForm form = employeeEditView.loadEmployeeForEdit(empId);
            this.currentEmpId = form.getEmpId();
            populateUIFromForm(form);
            showSuccess("Employee loaded for editing.");

        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "Unable to load employee for edit: " + empId, ex);
            showError(ex.getMessage());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error loading employee for edit: " + empId, ex);
            showError("Error loading employee for edit: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------

    @FXML
    private void handleSave() {
        clearMessage();

        if (currentEmpId <= 0) {
            showError("No employee selected for editing.");
            return;
        }

        try {
            EmployeeEditForm form = buildFormFromUI();
            form.setEmpId(currentEmpId);

            var result = employeeEditView.submitEmployeeUpdate(form);

            if (result.isSuccess()) {
                showSuccess(result.getMessage());
            } else {
                showError(result.getMessage());
            }

        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "Validation error in Edit Employee screen", ex);
            showError(ex.getMessage());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error saving employee", ex);
            showError("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Reload the employee from the database, discarding unsaved changes.
     */
    @FXML
    private void handleReload() {
        clearMessage();
        if (currentEmpId <= 0) {
            showError("No employee selected to reload.");
            return;
        }
        loadEmployeeForEdit(currentEmpId);
    }

    // -------------------------------------------------------------------------
    // Mapping: Form → UI and UI → Form
    // -------------------------------------------------------------------------

    private void populateUIFromForm(EmployeeEditForm form) {
        // Identity
        if (empIdLabel != null) {
            empIdLabel.setText(String.valueOf(form.getEmpId()));
        }

        // Employee fields
        empNumberField.setText(form.getEmpNumber());
        firstNameField.setText(form.getFirstName());
        lastNameField.setText(form.getLastName());
        emailField.setText(form.getEmail());
        ssnField.setText(form.getSsn());
        hireDatePicker.setValue(form.getHireDate());

        if (form.getCurrentSalary() != null) {
            salaryField.setText(form.getCurrentSalary().toPlainString());
        } else {
            salaryField.clear();
        }

        if (form.getEmploymentStatus() != null) {
            employmentStatusComboBox.getSelectionModel().select(form.getEmploymentStatus());
        } else {
            employmentStatusComboBox.getSelectionModel().clearSelection();
        }

        // Address fields
        streetField.setText(form.getStreet());
        zipField.setText(form.getZip());
        raceField.setText(form.getRace());
        phoneField.setText(form.getPhone());
        dobPicker.setValue(form.getDateOfBirth());

        if (form.getGender() != null) {
            genderComboBox.getSelectionModel().select(form.getGender());
        } else {
            genderComboBox.getSelectionModel().clearSelection();
        }

        // State & city by ID
        try {
            if (form.getStateId() != null) {
                State state = findStateById(form.getStateId());
                if (state != null) {
                    stateComboBox.getSelectionModel().select(state);
                    // Force load of cities for this state
                    onStateChanged(state);

                    if (form.getCityId() != null) {
                        City city = findCityById(form.getCityId());
                        if (city != null) {
                            cityComboBox.getSelectionModel().select(city);
                        }
                    }
                } else {
                    stateComboBox.getSelectionModel().clearSelection();
                    cityComboBox.getSelectionModel().clearSelection();
                    cityItems.clear();
                }
            } else {
                stateComboBox.getSelectionModel().clearSelection();
                cityComboBox.getSelectionModel().clearSelection();
                cityItems.clear();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error loading city list for employee edit", ex);
            showError("Could not load cities for selected state.");
        }
    }

    private EmployeeEditForm buildFormFromUI() {
        EmployeeEditForm form = new EmployeeEditForm();

        // empId is set separately when saving
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
                throw new IllegalArgumentException("Current salary must be a valid number", ex);
            }
        } else {
            form.setCurrentSalary(null);
        }

        form.setEmploymentStatus(
                employmentStatusComboBox.getSelectionModel().getSelectedItem()
        );

        // Address section
        form.setStreet(trim(streetField.getText()));

        State st = stateComboBox.getSelectionModel().getSelectedItem();
        City ct = cityComboBox.getSelectionModel().getSelectedItem();
        if (st != null) {
            form.setStateId(st.getStateId());
        }
        if (ct != null) {
            form.setCityId(ct.getCityId());
        }

        form.setZip(trim(zipField.getText()));
        form.setGender(genderComboBox.getSelectionModel().getSelectedItem());
        form.setRace(trim(raceField.getText()));
        form.setDateOfBirth(dobPicker.getValue());
        form.setPhone(trim(phoneField.getText()));

        return form;
    }

    // -------------------------------------------------------------------------
    // Lookup helpers
    // -------------------------------------------------------------------------

    private State findStateById(Integer stateId) {
        if (stateId == null) {
            return null;
        }
        for (State s : stateItems) {
            if (s.getStateId() == stateId) {
                return s;
            }
        }
        return null;
    }

    private City findCityById(Integer cityId) {
        if (cityId == null) {
            return null;
        }
        for (City c : cityItems) {
            if (c.getCityId() == cityId) {
                return c;
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // UI feedback helpers
    // -------------------------------------------------------------------------

    private void clearMessage() {
        if (messageLabel != null) {
            messageLabel.setText("");
            messageLabel.getStyleClass().removeAll("error-message", "success-message");
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

    // -------------------------------------------------------------------------
    // Small string helpers
    // -------------------------------------------------------------------------

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
