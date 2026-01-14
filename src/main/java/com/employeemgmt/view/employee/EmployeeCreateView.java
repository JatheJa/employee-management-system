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
 * View-layer class responsible for orchestrating the "Create Employee" flow.
 *
 * This class is designed to be used by a JavaFX controller or other UI layer:
 *  - UI populates an {@link EmployeeCreateView.EmployeeForm} from user input.
 *  - UI calls {@link #submitNewEmployee(EmployeeForm)}.
 *  - UI inspects the {@link EmployeeController.ControllerResult} to display
 *    success or validation/error messages to the user.
 *
 * All business rules and persistence are delegated to {@link EmployeeController}
 * and the underlying DAO layer, in line with the SDD's MVC separation.
 */
public class EmployeeCreateView {

    private static final Logger LOGGER = Logger.getLogger(EmployeeCreateView.class.getName());

    private final EmployeeController employeeController;

    /**
     * Primary constructor using dependency injection.
     * Allows tests or other code to provide pre-configured controller instances.
     */
    public EmployeeCreateView(EmployeeController employeeController) {
        this.employeeController = Objects.requireNonNull(employeeController, "employeeController cannot be null");
    }

    /**
     * Convenience constructor for simple use-cases.
     * Creates a default {@link EmployeeController} with concrete DAO and service implementations.
     *
     * In a larger app, this wiring would usually live in a central "composition root"
     * (e.g. an Application or DI configuration), but this keeps the view self-contained.
     */
    public EmployeeCreateView() {
        this(new EmployeeController(
                new EmployeeDAO(),
                new AddressDAO(),
                new ValidationService()
        ));
    }

    /**
     * Propagate the current logged-in user and role into the underlying controller,
     * so that role-based authorization checks (HR_ADMIN vs EMPLOYEE) behave as
     * specified in the SDD.
     *
     * Typically called once after login, before invoking submitNewEmployee.
     *
     * @param role current user's role
     * @param user current logged-in employee (may be null for admin accounts not tied to an employee record)
     */
    public void setUserContext(UserRole role, Employee user) {
        employeeController.setUserContext(role, user);
    }

    /**
     * Submit a new employee creation request using the data from the UI form.
     *
     * This method:
     *  1. Performs minimal null/consistency checks on the form (UI-level validation).
     *  2. Maps form fields to domain objects ({@link Employee}, {@link Address}).
     *  3. Delegates to {@link EmployeeController#createEmployee(Employee, Address)}.
     *  4. Returns the controller's {@link EmployeeController.ControllerResult} so the UI
     *     can render success/failure outcome and messages.
     *
     * The heavy business validation (e.g., required fields, formats, ranges) is handled
     * in the controller/validation service per the SDD.
     *
     * @param form data collected from the "Add New Employee" screen
     * @return result of the create operation
     */
    public EmployeeController.ControllerResult<Employee> submitNewEmployee(EmployeeForm form) {
        if (form == null) {
            return EmployeeController.ControllerResult.failure("Form data cannot be null");
        }

        try {
            // Basic UI-level sanity checks for required fields
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

            if (form.getCurrentSalary() == null || form.getCurrentSalary().compareTo(BigDecimal.ZERO) <= 0) {
                return EmployeeController.ControllerResult.failure("Current salary must be greater than zero");
            }

            // Build domain objects from the form
            Employee employee = buildEmployeeFromForm(form);
            Address address = buildAddressFromForm(form);

            // Delegate to controller (which performs full validation + persistence)
            EmployeeController.ControllerResult<Employee> result =
                    employeeController.createEmployee(employee, address);

            logResult(result);

            return result;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error while creating employee from view", ex);
            return EmployeeController.ControllerResult.failure("Unexpected error: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Internal mapping helpers
    // -------------------------------------------------------------------------

    private Employee buildEmployeeFromForm(EmployeeForm form) {
        Employee employee = new Employee();

        employee.setEmpNumber(form.getEmpNumber());
        employee.setFirstName(form.getFirstName());
        employee.setLastName(form.getLastName());
        employee.setEmail(form.getEmail());
        employee.setSsn(form.getSsn());
        employee.setHireDate(form.getHireDate());
        employee.setCurrentSalary(form.getCurrentSalary());

        // Optional: if the UI provided an explicit employment status, apply it;
        // otherwise the database default (ACTIVE) will be used.
        if (form.getEmploymentStatus() != null) {
            employee.setEmploymentStatus(form.getEmploymentStatus());
        }

        return employee;
    }

    /**
     * Build an Address from the form if there is at least some address data.
     * If the user did not enter any address info, this returns null, and the
     * controller will create the Employee without an Address record.
     */
    private Address buildAddressFromForm(EmployeeForm form) {
        // Detect whether the form actually contains address data
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
            return null; // No address to create
        }

        Address address = new Address();

        // empId is assigned after the Employee is created; the controller/DAO
        // will set it before persisting the address.
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

    private void logResult(EmployeeController.ControllerResult<Employee> result) {
        if (result == null) {
            return;
        }

        if (result.isSuccess()) {
            Employee emp = result.getData();
            int id = (emp != null ? emp.getEmpId() : -1);
            LOGGER.info("Employee created successfully via view. ID=" + id +
                        ", message=" + result.getMessage());
        } else {
            LOGGER.warning("Employee creation failed via view. Reason=" + result.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // -------------------------------------------------------------------------
    // Form DTO for the "Create Employee" screen
    // -------------------------------------------------------------------------

    /**
     * Simple data holder for all fields on the "Add New Employee" screen.
     *
     * A JavaFX controller (or any other UI layer) should:
     *  - Bind user input controls to an instance of this class.
     *  - Populate the fields.
     *  - Pass the instance to {@link #submitNewEmployee(EmployeeForm)}.
     *
     * This keeps the view layer aligned with the SDD: the view collects data,
     * the controller applies business rules and talks to the DAOs.
     */
    public static class EmployeeForm {
        // Employee fields
        private String empNumber;
        private String firstName;
        private String lastName;
        private String email;
        private String ssn;
        private LocalDate hireDate;
        private BigDecimal currentSalary;
        private EmploymentStatus employmentStatus; // optional, default ACTIVE if null

        // Address fields (optional but recommended per SDD use case)
        private String street;
        private Integer cityId;
        private Integer stateId;
        private String zip;
        private Address.Gender gender;
        private String race;
        private LocalDate dateOfBirth;
        private String phone;

        // --- Getters & setters ---

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
