package com.employeemgmt.view.employee;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.controller.EmployeeController;
import com.employeemgmt.dao.AddressDAO;
import com.employeemgmt.dao.EmployeeDAO;
import com.employeemgmt.model.Employee;
import com.employeemgmt.service.ValidationService;
import com.employeemgmt.util.UserRole;

/**
 * View-layer class responsible for orchestrating employee search flows.
 *
 * Typical usage from a JavaFX controller:
 *  - After login, call {@link #setUserContext(UserRole, Employee)}.
 *  - Bind UI search controls (text fields, date picker) to an instance of
 *    {@link EmployeeSearchView.SearchForm}.
 *  - Pass the populated form to {@link #searchEmployees(SearchForm)}.
 *  - Use the returned {@link EmployeeController.ControllerResult} to display
 *    results or error messages to the user.
 *
 * All business rules, role-based access control, and validation for search
 * are implemented in {@link EmployeeController#searchEmployees(Map)} in line
 * with the SDD; this class only does light input sanity checks and mapping.
 */
public class EmployeeSearchView {

    private static final Logger LOGGER = Logger.getLogger(EmployeeSearchView.class.getName());

    private final EmployeeController employeeController;

    /**
     * Constructor with injected controller (useful for tests or custom wiring).
     */
    public EmployeeSearchView(EmployeeController employeeController) {
        this.employeeController = Objects.requireNonNull(employeeController, "employeeController cannot be null");
    }

    /**
     * Convenience constructor that wires up default DAOs and ValidationService.
     */
    public EmployeeSearchView() {
        this(new EmployeeController(
                new EmployeeDAO(),
                new AddressDAO(),
                new ValidationService()
        ));
    }

    /**
     * Propagate current user and role into the controller so that all search
     * behavior complies with SDD rules (HR vs EMPLOYEE visibility).
     */
    public void setUserContext(UserRole role, Employee currentUser) {
        employeeController.setUserContext(role, currentUser);
    }

    // -------------------------------------------------------------------------
    // Primary search entry point
    // -------------------------------------------------------------------------

    /**
     * Execute a search using the given form.
     *
     * Behavior (matching EmployeeController.searchEmployees):
     *  - HR_ADMIN:
     *      - If specific criteria provided, search by that.
     *      - If no criteria, returns all active employees.
     *  - EMPLOYEE:
     *      - Can only see their own record (controller enforces this).
     *
     * @param form search criteria filled from the UI
     * @return controller result holding a list of employees or an error message
     */
    public EmployeeController.ControllerResult<List<Employee>> searchEmployees(SearchForm form) {
        if (form == null) {
            return EmployeeController.ControllerResult.failure("Search form cannot be null");
        }

        try {
            Map<String, Object> criteria = buildCriteriaMap(form);

            // Optional UI-level sanity: if everything is empty and UI does not
            // want "show all", you could enforce at least one criterion.
            // For now we allow empty criteria so HR can get all active employees,
            // which matches the SDD behavior in EmployeeController.
            logCriteria(criteria);

            return employeeController.searchEmployees(criteria);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error during employee search", ex);
            return EmployeeController.ControllerResult.failure("Unexpected error during search: " + ex.getMessage());
        }
    }

    /**
     * Convenience method: get all active employees (delegates directly to controller).
     *
     * HR-only operation; authorization rules are enforced inside the controller.
     */
    public EmployeeController.ControllerResult<List<Employee>> getAllActiveEmployees() {
        try {
            EmployeeController.ControllerResult<List<Employee>> result =
                    employeeController.getAllActiveEmployees();

            if (result.isSuccess()) {
                LOGGER.info("Retrieved " + (result.getData() != null ? result.getData().size() : 0)
                        + " active employees via search view");
            } else {
                LOGGER.warning("Failed to retrieve active employees: " + result.getMessage());
            }
            return result;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error getting all active employees", ex);
            return EmployeeController.ControllerResult.failure("Unexpected error: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Convenience search helpers (optional but handy for other layers)
    // -------------------------------------------------------------------------

    public EmployeeController.ControllerResult<List<Employee>> searchByEmpId(int empId) {
        SearchForm form = new SearchForm();
        form.setEmpId(empId);
        return searchEmployees(form);
    }

    public EmployeeController.ControllerResult<List<Employee>> searchByEmpNumber(String empNumber) {
        SearchForm form = new SearchForm();
        form.setEmpNumber(trim(empNumber));
        return searchEmployees(form);
    }

    public EmployeeController.ControllerResult<List<Employee>> searchBySsn(String ssn) {
        SearchForm form = new SearchForm();
        form.setSsn(trim(ssn));
        return searchEmployees(form);
    }

    public EmployeeController.ControllerResult<List<Employee>> searchByName(String name) {
        SearchForm form = new SearchForm();
        form.setName(trim(name));
        return searchEmployees(form);
    }

    public EmployeeController.ControllerResult<List<Employee>> searchByDateOfBirth(LocalDate dob) {
        SearchForm form = new SearchForm();
        form.setDateOfBirth(dob);
        return searchEmployees(form);
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    /**
     * Build the criteria map expected by EmployeeController.searchEmployees.
     *
     * Keys used by controller:
     *  - "name"       : String
     *  - "ssn"        : String
     *  - "empNumber"  : String
     *  - "empId"      : Integer
     *  - "dateOfBirth": LocalDate
     */
    private Map<String, Object> buildCriteriaMap(SearchForm form) {
        Map<String, Object> criteria = new HashMap<>();

        if (form.getEmpId() != null && form.getEmpId() > 0) {
            criteria.put("empId", form.getEmpId());
        }

        if (!isBlank(form.getEmpNumber())) {
            criteria.put("empNumber", form.getEmpNumber().trim());
        }

        if (!isBlank(form.getSsn())) {
            criteria.put("ssn", form.getSsn().trim());
        }

        if (!isBlank(form.getName())) {
            criteria.put("name", form.getName().trim());
        }

        if (form.getDateOfBirth() != null) {
            criteria.put("dateOfBirth", form.getDateOfBirth());
        }

        return criteria;
    }

    private void logCriteria(Map<String, Object> criteria) {
        try {
            LOGGER.info("Executing employee search with criteria: " + criteria);
        } catch (Exception ignored) {
            // Avoid any logging-related issues blowing up the search.
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // -------------------------------------------------------------------------
    // Form DTO for the "Search Employee" screen
    // -------------------------------------------------------------------------

    /**
     * Simple DTO used by UI layers to collect search criteria.
     * Any combination of fields may be supplied; the underlying controller
     * decides how to interpret them according to the SDD.
     */
    public static class SearchForm {

        // Search by specific employee id
        private Integer empId;

        // Search by employee number
        private String empNumber;

        // Search by SSN
        private String ssn;

        // Search by name (first, last, or both – DAO decides)
        private String name;

        // Search by date of birth (via AddressDAO)
        private LocalDate dateOfBirth;

        // --- Getters & setters ---

        public Integer getEmpId() {
            return empId;
        }

        public void setEmpId(Integer empId) {
            this.empId = empId;
        }

        public String getEmpNumber() {
            return empNumber;
        }

        public void setEmpNumber(String empNumber) {
            this.empNumber = empNumber;
        }

        public String getSsn() {
            return ssn;
        }

        public void setSsn(String ssn) {
            this.ssn = ssn;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }
    }
}
