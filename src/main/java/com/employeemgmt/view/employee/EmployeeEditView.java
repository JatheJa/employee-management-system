package com.employeemgmt.view.employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.controller.EmployeeController;
import com.employeemgmt.dao.AddressDAO;
import com.employeemgmt.dao.EmployeeDAO;
import com.employeemgmt.model.Address;
import com.employeemgmt.model.Employee;
import com.employeemgmt.model.Employee.EmploymentStatus;
import com.employeemgmt.service.ValidationService;
import com.employeemgmt.util.UserRole;

/**
 * View-layer class responsible for orchestrating the "Edit Employee" flow.
 *
 * Typical usage by a JavaFX controller:
 *  1. Call {@link #setUserContext(UserRole, Employee)} after login.
 *  2. Call {@link #loadEmployeeForEdit(int)} to get an {@link EmployeeEditForm}
 *     populated from the database; bind the UI controls to this form.
 *  3. When the user clicks "Save", pass the edited form to
 *     {@link #submitEmployeeUpdate(EmployeeEditForm)}.
 *  4. Inspect the returned {@link EmployeeController.ControllerResult} to show
 *     success or error messages.
 *
 * All business rules, validation, and authorization are handled by
 * {@link EmployeeController} and {@link ValidationService}, per the SDD.
 */
public class EmployeeEditView {

    private static final Logger LOGGER = Logger.getLogger(EmployeeEditView.class.getName());

    private final EmployeeController employeeController;

    /**
     * Primary constructor using dependency injection.
     */
    public EmployeeEditView(EmployeeController employeeController) {
        this.employeeController =
                Objects.requireNonNull(employeeController, "employeeController cannot be null");
    }

    /**
     * Convenience constructor that wires up default DAOs and ValidationService.
     *
     * In a larger application, this wiring would typically live in a central
     * composition root, but this keeps the view self-contained.
     */
    public EmployeeEditView() {
        this(new EmployeeController(
                new EmployeeDAO(),
                new AddressDAO(),
                new ValidationService()
        ));
    }

    /**
     * Propagate the current logged-in user and role into the underlying controller.
     * This ensures the HR-only edit rules in the SDD are enforced.
     */
    public void setUserContext(UserRole role, Employee user) {
        employeeController.setUserContext(role, user);
    }

    // -------------------------------------------------------------------------
    // LOAD EXISTING EMPLOYEE DATA FOR EDITING
    // -------------------------------------------------------------------------

    /**
     * Load an existing employee (and associated address) into an edit form DTO.
     *
     * @param empId employee ID to edit
     * @return a populated {@link EmployeeEditForm}
     * @throws IllegalArgumentException if the employee cannot be loaded (not found,
     *                                  or access denied)
     */
    public EmployeeEditForm loadEmployeeForEdit(int empId) {
        if (empId <= 0) {
            throw new IllegalArgumentException("Employee ID must be greater than zero");
        }

        try {
            EmployeeController.ControllerResult<Employee> result =
                    employeeController.getEmployeeById(empId);

            if (!result.isSuccess()) {
                throw new IllegalArgumentException("Unable to load employee: " + result.getMessage());
            }

            Employee employee = result.getData();
            if (employee == null) {
                throw new IllegalArgumentException("Employee not found with ID: " + empId);
            }

            Address address = employee.getAddress(); // may be null, controller sets this

            EmployeeEditForm form = new EmployeeEditForm();
            form.setEmpId(employee.getEmpId());
            form.setEmpNumber(employee.getEmpNumber());
            form.setFirstName(employee.getFirstName());
            form.setLastName(employee.getLastName());
            form.setEmail(employee.getEmail());
            form.setSsn(employee.getSsn());
            form.setHireDate(employee.getHireDate());
            form.setCurrentSalary(employee.getCurrentSalary());
            form.setEmploymentStatus(employee.getEmploymentStatus());

            if (address != null) {
                form.setStreet(address.getStreet());
                form.setCityId(address.getCityId());
                form.setStateId(address.getStateId());
                form.setZip(address.getZip());
                form.setGender(address.getGender());
                form.setRace(address.getRace());
                form.setDateOfBirth(address.getDateOfBirth());
                form.setPhone(address.getPhone());
            }

            LOGGER.info("Loaded employee for edit. ID=" + empId);
            return form;

        } catch (IllegalArgumentException ex) {
            // Re-throw as-is so UI can show a clear message
            throw ex;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error loading employee for edit. ID=" + empId, ex);
            throw new IllegalArgumentException("Error loading employee for edit: " + ex.getMessage(), ex);
        }
    }

    // -------------------------------------------------------------------------
    // SUBMIT UPDATE
    // -------------------------------------------------------------------------

    /**
     * Submit an employee update using the data from the edit form.
     *
     * This performs light UI-level checks, maps the form to domain objects, and
     * delegates to {@link EmployeeController#updateEmployee(int, Employee, Address)}.
     *
     * @param form data collected from the "Edit Employee" screen
     * @return controller result describing success or failure
     */
    public EmployeeController.ControllerResult<Employee> submitEmployeeUpdate(EmployeeEditForm form) {
        if (form == null) {
            return EmployeeController.ControllerResult.failure("Form data cannot be null");
        }
        if (form.getEmpId() <= 0) {
            return EmployeeController.ControllerResult.failure("Employee ID is required for update");
        }

        try {
            // Basic UI-level sanity checks
            if (isBlank(form.getFirstName())
                    || isBlank(form.getLastName())
                    || isBlank(form.getEmpNumber())
                    || isBlank(form.getEmail())
                    || isBlank(form.getSsn())) {
                return EmployeeController.ControllerResult.failure(
                        "First name, last name, employee number, email, and SSN are required");
            }

            if (form.getHireDate() == null) {
                return EmployeeController.ControllerResult.failure("Hire date is required");
            }

            if (form.getCurrentSalary() == null
                    || form.getCurrentSalary().compareTo(BigDecimal.ZERO) <= 0) {
                return EmployeeController.ControllerResult.failure("Current salary must be greater than zero");
            }

            // Map form to domain objects
            Employee updatedEmployee = buildEmployeeFromForm(form);
            Address updatedAddress = buildAddressFromForm(form);

            // Delegate to controller (per SDD all validation/authorization is there)
            EmployeeController.ControllerResult<Employee> result =
                    employeeController.updateEmployee(form.getEmpId(), updatedEmployee, updatedAddress);

            logResult(result, form.getEmpId());
            return result;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error while updating employee from view", ex);
            return EmployeeController.ControllerResult.failure("Unexpected error: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Internal mapping helpers
    // -------------------------------------------------------------------------

    private Employee buildEmployeeFromForm(EmployeeEditForm form) {
        Employee employee = new Employee();

        employee.setEmpId(form.getEmpId());
        employee.setEmpNumber(form.getEmpNumber());
        employee.setFirstName(form.getFirstName());
        employee.setLastName(form.getLastName());
        employee.setEmail(form.getEmail());
        employee.setSsn(form.getSsn());
        employee.setHireDate(form.getHireDate());
        employee.setCurrentSalary(form.getCurrentSalary());

        if (form.getEmploymentStatus() != null) {
            employee.setEmploymentStatus(form.getEmploymentStatus());
        }

        return employee;
    }

    /**
     * Build an Address from the form if there is at least some address data.
     * If no address fields are present, returns null, meaning "do not change address".
     */
    private Address buildAddressFromForm(EmployeeEditForm form) {
        boolean hasAnyAddressField =
                !isBlank(form.getStreet()) ||
                !isBlank(form.getZip()) ||
                !isBlank(form.getPhone()) ||
                form.getCityId() != null ||
                form.getStateId() != null ||
                form.getGender() != null ||
                !isBlank(form.getRace()) ||
                form.getDateOfBirth() != null;

        if (!hasAnyAddressField) {
            return null;
        }

        Address address = new Address();
        address.setEmpId(form.getEmpId());
        address.setStreet(form.getStreet());

        if (form.getCityId() != null) {
            address.setCityId(form.getCityId());
        }
        if (form.getStateId() != null) {
            address.setStateId(form.getStateId());
        }

        address.setZip(form.getZip());
        address.setGender(form.getGender());
        address.setRace(form.getRace());
        address.setDateOfBirth(form.getDateOfBirth());
        address.setPhone(form.getPhone());

        return address;
    }

    private void logResult(EmployeeController.ControllerResult<Employee> result, int empId) {
        if (result == null) {
            return;
        }

        if (result.isSuccess()) {
            LOGGER.info("Employee updated successfully via view. ID=" + empId +
                        ", message=" + result.getMessage());
        } else {
            LOGGER.warning("Employee update failed via view. ID=" + empId +
                           ", reason=" + result.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // -------------------------------------------------------------------------
    // Form DTO for the "Edit Employee" screen
    // -------------------------------------------------------------------------

    /**
     * Data Transfer Object backing the "Edit Employee" screen.
     *
     * UI pattern:
     *  - Call {@link EmployeeEditView#loadEmployeeForEdit(int)} to get an instance.
     *  - Bind controls to its fields.
     *  - On save, pass the edited instance to {@link #submitEmployeeUpdate(EmployeeEditForm)}.
     */
    public static class EmployeeEditForm {

        // Identity
        private int empId;

        // Employee fields
        private String empNumber;
        private String firstName;
        private String lastName;
        private String email;
        private String ssn;
        private LocalDate hireDate;
        private BigDecimal currentSalary;
        private EmploymentStatus employmentStatus;

        // Address fields
        private String street;
        private Integer cityId;
        private Integer stateId;
        private String zip;
        private Address.Gender gender;
        private String race;
        private LocalDate dateOfBirth;
        private String phone;

        // --- Getters & setters ---

        public int getEmpId() {
            return empId;
        }

        public void setEmpId(int empId) {
            this.empId = empId;
        }

        public String getEmpNumber() {
            return empNumber;
        }

        public void setEmpNumber(String empNumber) {
            this.empNumber = empNumber;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getSsn() {
            return ssn;
        }

        public void setSsn(String ssn) {
            this.ssn = ssn;
        }

        public LocalDate getHireDate() {
            return hireDate;
        }

        public void setHireDate(LocalDate hireDate) {
            this.hireDate = hireDate;
        }

        public BigDecimal getCurrentSalary() {
            return currentSalary;
        }

        public void setCurrentSalary(BigDecimal currentSalary) {
            this.currentSalary = currentSalary;
        }

        public EmploymentStatus getEmploymentStatus() {
            return employmentStatus;
        }

        public void setEmploymentStatus(EmploymentStatus employmentStatus) {
            this.employmentStatus = employmentStatus;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public Integer getCityId() {
            return cityId;
        }

        public void setCityId(Integer cityId) {
            this.cityId = cityId;
        }

        public Integer getStateId() {
            return stateId;
        }

        public void setStateId(Integer stateId) {
            this.stateId = stateId;
        }

        public String getZip() {
            return zip;
        }

        public void setZip(String zip) {
            this.zip = zip;
        }

        public Address.Gender getGender() {
            return gender;
        }

        public void setGender(Address.Gender gender) {
            this.gender = gender;
        }

        public String getRace() {
            return race;
        }

        public void setRace(String race) {
            this.race = race;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
