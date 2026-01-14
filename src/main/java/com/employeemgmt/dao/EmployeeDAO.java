package com.employeemgmt.dao;

import com.employeemgmt.dao.base.BaseDAO;
import com.employeemgmt.dao.base.SearchableDAO;
import com.employeemgmt.model.Employee;
import com.employeemgmt.model.Employee.EmploymentStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Employee entities
 * Handles all database operations for employee data
 *
 * @author Team 6
 */
public class EmployeeDAO extends BaseDAO<Employee> implements SearchableDAO<Employee> {

    private static final String TABLE_NAME = "employees";
    private static final String PRIMARY_KEY = "empid";

    // SQL Queries
    private static final String INSERT_EMPLOYEE =
        "INSERT INTO employees (emp_number, first_name, last_name, email, ssn, hire_date, current_salary, employment_status) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID =
        "SELECT * FROM employees WHERE empid = ?";

    private static final String SELECT_ALL =
        "SELECT * FROM employees ORDER BY last_name, first_name";

    private static final String UPDATE_EMPLOYEE =
        "UPDATE employees SET emp_number = ?, first_name = ?, last_name = ?, email = ?, " +
        "ssn = ?, hire_date = ?, current_salary = ?, employment_status = ?, updated_at = CURRENT_TIMESTAMP " +
        "WHERE empid = ?";

    // Soft delete – mark as TERMINATED instead of physical delete
    private static final String DELETE_EMPLOYEE =
        "UPDATE employees SET employment_status = 'TERMINATED', updated_at = CURRENT_TIMESTAMP WHERE empid = ?";

    private static final String SEARCH_BY_NAME =
        "SELECT * FROM employees WHERE (first_name LIKE ? OR last_name LIKE ?) " +
        "ORDER BY last_name, first_name";

    private static final String SEARCH_BY_SSN =
        "SELECT * FROM employees WHERE ssn = ?";

    private static final String SEARCH_BY_EMP_NUMBER =
        "SELECT * FROM employees WHERE emp_number = ?";

    private static final String UPDATE_SALARY_BY_RANGE =
        "UPDATE employees SET current_salary = current_salary * (1 + ? / 100), updated_at = CURRENT_TIMESTAMP " +
        "WHERE current_salary >= ? AND current_salary <= ? AND employment_status = 'ACTIVE'";

    private static final String SELECT_BY_HIRE_DATE_RANGE =
        "SELECT * FROM employees WHERE hire_date >= ? AND hire_date <= ? " +
        "ORDER BY hire_date DESC";

    private static final String SELECT_ACTIVE_EMPLOYEES =
        "SELECT * FROM employees WHERE employment_status = 'ACTIVE' ORDER BY last_name, first_name";

    // NEW: used by EmployeeController.updateSalariesByPercentage(...)
    private static final String SELECT_BY_SALARY_RANGE =
        "SELECT * FROM employees " +
        "WHERE current_salary >= ? AND current_salary <= ? " +
        "AND employment_status = 'ACTIVE' " +
        "ORDER BY current_salary";

    // -------------------------------------------------------------------------
    // BaseDAO implementation
    // -------------------------------------------------------------------------

    @Override
    public Employee save(Employee employee) throws SQLException {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(INSERT_EMPLOYEE, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, employee.getEmpNumber());
            stmt.setString(2, employee.getFirstName());
            stmt.setString(3, employee.getLastName());
            stmt.setString(4, employee.getEmail());
            stmt.setString(5, employee.getSsn());
            stmt.setDate(6, Date.valueOf(employee.getHireDate()));
            stmt.setBigDecimal(7, employee.getCurrentSalary());
            stmt.setString(8, employee.getEmploymentStatus().name());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating employee failed, no rows affected.");
            }

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                employee.setEmpId(rs.getInt(1));
            }

            return employee;

        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Compatibility helper if any legacy code still calls create(...)
     */
    public Employee create(Employee employee) throws SQLException {
        return save(employee);
    }

    @Override
    public Employee findById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_ID);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            }

            return null;

        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    @Override
    public List<Employee> findAll() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Employee> employees = new ArrayList<>();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_ALL);
            rs = stmt.executeQuery();

            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }

        } finally {
            closeResources(conn, stmt, rs);
        }

        return employees;
    }

    /**
     * Get all active employees
     *
     * @return List of active employees
     * @throws SQLException if database operation fails
     */
    public List<Employee> findAllActive() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Employee> employees = new ArrayList<>();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_ACTIVE_EMPLOYEES);
            rs = stmt.executeQuery();

            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }

        } finally {
            closeResources(conn, stmt, rs);
        }

        return employees;
    }

    @Override
    public Employee update(Employee employee) throws SQLException {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (employee.getEmpId() <= 0) {
            throw new IllegalArgumentException("Employee ID is required for update");
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(UPDATE_EMPLOYEE);

            stmt.setString(1, employee.getEmpNumber());
            stmt.setString(2, employee.getFirstName());
            stmt.setString(3, employee.getLastName());
            stmt.setString(4, employee.getEmail());
            stmt.setString(5, employee.getSsn());
            stmt.setDate(6, Date.valueOf(employee.getHireDate()));
            stmt.setBigDecimal(7, employee.getCurrentSalary());
            stmt.setString(8, employee.getEmploymentStatus().name());
            stmt.setInt(9, employee.getEmpId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException(
                    "Updating employee failed, no rows affected. Employee may not exist with ID: " + employee.getEmpId()
                );
            }

            return employee;

        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * Compatibility helper if any old code expects a boolean update result.
     */
    public boolean updateEmployee(Employee employee) throws SQLException {
        return update(employee) != null;
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        // Soft delete - mark as terminated instead of actual deletion
        return executeUpdateQuery(DELETE_EMPLOYEE, id) > 0;
    }

    /**
     * Compatibility helper if any old code still calls delete(int).
     */
    public boolean delete(int id) throws SQLException {
        return deleteById(id);
    }

    // -------------------------------------------------------------------------
    // SearchableDAO implementation
    // -------------------------------------------------------------------------

    @Override
    public List<Employee> searchByName(String name) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Employee> employees = new ArrayList<>();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SEARCH_BY_NAME);
            String searchPattern = "%" + name + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            rs = stmt.executeQuery();

            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error searching employees by name: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }

        return employees;
    }

    @Override
    public Employee searchById(int id) {
        try {
            return findById(id);
        } catch (SQLException e) {
            System.err.println("Error searching employee by ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Employee> searchByCriteria(Map<String, Object> searchCriteria) {
        // TODO: Implement dynamic query building based on criteria if needed
        return new ArrayList<>();
    }

    @Override
    public int getSearchCount(Map<String, Object> searchCriteria) {
        // TODO: Implement count query for search criteria
        return 0;
    }

    // -------------------------------------------------------------------------
    // Extra query helpers used by controllers/services
    // -------------------------------------------------------------------------

    /**
     * Search employee by SSN
     *
     * @param ssn Social Security Number
     * @return Employee if found, null otherwise
     */
    public Employee searchBySsn(String ssn) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SEARCH_BY_SSN);
            stmt.setString(1, ssn);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error searching employee by SSN: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }

        return null;
    }

    /**
     * Search employee by employee number
     *
     * @param empNumber Employee number
     * @return Employee if found, null otherwise
     */
    public Employee searchByEmpNumber(String empNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SEARCH_BY_EMP_NUMBER);
            stmt.setString(1, empNumber);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error searching employee by employee number: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }

        return null;
    }

    /**
     * Update salary for employees within a specified range by percentage
     * (kept in case you still want the pure SQL batch update)
     *
     * @param percentage Percentage increase (e.g., 3.2 for 3.2%)
     * @param minSalary  Minimum salary in range
     * @param maxSalary  Maximum salary in range
     * @return Number of employees affected
     * @throws SQLException if database operation fails
     */
    public int updateSalaryByRange(double percentage, BigDecimal minSalary, BigDecimal maxSalary) throws SQLException {
        return executeUpdateQuery(UPDATE_SALARY_BY_RANGE, percentage, minSalary, maxSalary);
    }

    /**
     * Find employees hired within a date range
     *
     * @param startDate Start date of range
     * @param endDate   End date of range
     * @return List of employees hired in the date range
     * @throws SQLException if database operation fails
     */
    public List<Employee> findByHireDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Employee> employees = new ArrayList<>();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_HIRE_DATE_RANGE);
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            rs = stmt.executeQuery();

            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }

        } finally {
            closeResources(conn, stmt, rs);
        }

        return employees;
    }

    /**
     * Used by EmployeeController.updateSalariesByPercentage(...)
     *
     * @param minSalary minimum current_salary (inclusive)
     * @param maxSalary maximum current_salary (inclusive)
     * @return list of ACTIVE employees in that salary range
     * @throws SQLException if database operation fails
     */
    public List<Employee> findEmployeesBySalaryRange(BigDecimal minSalary, BigDecimal maxSalary) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Employee> employees = new ArrayList<>();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_SALARY_RANGE);
            stmt.setBigDecimal(1, minSalary);
            stmt.setBigDecimal(2, maxSalary);
            rs = stmt.executeQuery();

            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }

        } finally {
            closeResources(conn, stmt, rs);
        }

        return employees;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getPrimaryKeyColumn() {
        return PRIMARY_KEY;
    }

    /**
     * Map ResultSet row to Employee object
     *
     * @param rs ResultSet positioned at current row
     * @return Employee object
     * @throws SQLException if data access fails
     */
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();

        employee.setEmpId(rs.getInt("empid"));
        employee.setEmpNumber(rs.getString("emp_number"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setEmail(rs.getString("email"));
        employee.setSsn(rs.getString("ssn"));

        Date hireDate = rs.getDate("hire_date");
        if (hireDate != null) {
            employee.setHireDate(hireDate.toLocalDate());
        }

        employee.setCurrentSalary(rs.getBigDecimal("current_salary"));

        String statusStr = rs.getString("employment_status");
        if (statusStr != null) {
            employee.setEmploymentStatus(EmploymentStatus.valueOf(statusStr));
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            employee.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            employee.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return employee;
    }
}
