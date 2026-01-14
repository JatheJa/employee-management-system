package com.employeemgmt.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.employeemgmt.dao.base.BaseDAO;
import com.employeemgmt.model.Employee;

/**
 * Data Access Object for Report-specific queries
 * Handles complex database operations for report generation
 * 
 * @author Team 6
 */
public class ReportDAO extends BaseDAO<Employee> {
    
    private static final Logger LOGGER = Logger.getLogger(ReportDAO.class.getName());
    
    // SQL Queries for Reports
    
    // Employees hired within date range
    private static final String SELECT_EMPLOYEES_HIRED_BY_DATE_RANGE =
        "SELECT e.*, a.street, a.zip, a.phone, " +
        "c.city_name, s.state_name, s.state_code, " +
        "d.division_name, jt.job_title " +
        "FROM employees e " +
        "LEFT JOIN address a ON e.empid = a.empid " +
        "LEFT JOIN city c ON a.city_id = c.city_id " +
        "LEFT JOIN state s ON a.state_id = s.state_id " +
        "LEFT JOIN employee_division ed ON e.empid = ed.empid AND ed.is_current = 1 " +
        "LEFT JOIN division d ON ed.div_id = d.div_id " +
        "LEFT JOIN employee_job_titles ejt ON e.empid = ejt.empid AND ejt.is_current = 1 " +
        "LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id " +
        "WHERE e.hire_date BETWEEN ? AND ? " +
        "ORDER BY e.hire_date ASC, e.last_name ASC, e.first_name ASC";
    
    // Employee count by division
    private static final String SELECT_EMPLOYEE_COUNT_BY_DIVISION =
        "SELECT d.division_name, d.division_code, COUNT(e.empid) as employee_count, " +
        "AVG(e.current_salary) as avg_salary, MIN(e.current_salary) as min_salary, MAX(e.current_salary) as max_salary " +
        "FROM division d " +
        "LEFT JOIN employee_division ed ON d.div_id = ed.div_id AND ed.is_current = 1 " +
        "LEFT JOIN employees e ON ed.empid = e.empid AND e.employment_status = 'ACTIVE' " +
        "GROUP BY d.div_id, d.division_name, d.division_code " +
        "ORDER BY employee_count DESC";
    
    // Employee count by job title
    private static final String SELECT_EMPLOYEE_COUNT_BY_JOB_TITLE =
        "SELECT jt.job_title, COUNT(e.empid) as employee_count, " +
        "AVG(e.current_salary) as avg_salary, MIN(e.current_salary) as min_salary, MAX(e.current_salary) as max_salary " +
        "FROM job_titles jt " +
        "LEFT JOIN employee_job_titles ejt ON jt.job_title_id = ejt.job_title_id AND ejt.is_current = 1 " +
        "LEFT JOIN employees e ON ejt.empid = e.empid AND e.employment_status = 'ACTIVE' " +
        "GROUP BY jt.job_title_id, jt.job_title " +
        "ORDER BY employee_count DESC";
    
    // Employee demographics summary
    private static final String SELECT_DEMOGRAPHICS_SUMMARY =
        "SELECT " +
        "COUNT(*) as total_employees, " +
        "COUNT(CASE WHEN a.gender = 'M' THEN 1 END) as male_count, " +
        "COUNT(CASE WHEN a.gender = 'F' THEN 1 END) as female_count, " +
        "COUNT(CASE WHEN a.gender = 'Other' THEN 1 END) as other_gender_count, " +
        "COUNT(CASE WHEN a.gender = 'Prefer not to say' THEN 1 END) as prefer_not_to_say_count, " +
        "AVG(YEAR(CURDATE()) - YEAR(a.date_of_birth)) as avg_age, " +
        "MIN(e.hire_date) as earliest_hire_date, " +
        "MAX(e.hire_date) as latest_hire_date, " +
        "AVG(e.current_salary) as avg_salary " +
        "FROM employees e " +
        "LEFT JOIN address a ON e.empid = a.empid " +
        "WHERE e.employment_status = 'ACTIVE'";
    
    // Salary distribution report
    private static final String SELECT_SALARY_DISTRIBUTION =
        "SELECT " +
        "CASE " +
        "WHEN current_salary < 40000 THEN 'Under $40K' " +
        "WHEN current_salary < 60000 THEN '$40K - $60K' " +
        "WHEN current_salary < 80000 THEN '$60K - $80K' " +
        "WHEN current_salary < 100000 THEN '$80K - $100K' " +
        "WHEN current_salary < 120000 THEN '$100K - $120K' " +
        "ELSE '$120K+' " +
        "END as salary_range, " +
        "COUNT(*) as employee_count, " +
        "MIN(current_salary) as min_salary, " +
        "MAX(current_salary) as max_salary, " +
        "AVG(current_salary) as avg_salary " +
        "FROM employees " +
        "WHERE employment_status = 'ACTIVE' " +
        "GROUP BY salary_range " +
        "ORDER BY MIN(current_salary)";
    
    // Tenure analysis
    private static final String SELECT_TENURE_ANALYSIS =
        "SELECT " +
        "CASE " +
        "WHEN DATEDIFF(CURDATE(), hire_date) < 365 THEN 'Less than 1 year' " +
        "WHEN DATEDIFF(CURDATE(), hire_date) < 1095 THEN '1-3 years' " +
        "WHEN DATEDIFF(CURDATE(), hire_date) < 1825 THEN '3-5 years' " +
        "WHEN DATEDIFF(CURDATE(), hire_date) < 3650 THEN '5-10 years' " +
        "ELSE '10+ years' " +
        "END as tenure_range, " +
        "COUNT(*) as employee_count, " +
        "AVG(current_salary) as avg_salary " +
        "FROM employees " +
        "WHERE employment_status = 'ACTIVE' " +
        "GROUP BY tenure_range " +
        "ORDER BY MIN(DATEDIFF(CURDATE(), hire_date))";

    /**
     * Get employees hired within a specific date range
     */
    public List<Map<String, Object>> getEmployeesHiredByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> employees = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_EMPLOYEES_HIRED_BY_DATE_RANGE);
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> employee = new HashMap<>();
                
                // Employee basic info
                employee.put("empId", rs.getInt("empid"));
                employee.put("empNumber", rs.getString("emp_number"));
                employee.put("firstName", rs.getString("first_name"));
                employee.put("lastName", rs.getString("last_name"));
                employee.put("fullName", rs.getString("first_name") + " " + rs.getString("last_name"));
                employee.put("email", rs.getString("email"));
                employee.put("hireDate", rs.getDate("hire_date").toLocalDate());
                employee.put("currentSalary", rs.getBigDecimal("current_salary"));
                employee.put("employmentStatus", rs.getString("employment_status"));
                
                // Address info
                employee.put("street", rs.getString("street"));
                employee.put("cityName", rs.getString("city_name"));
                employee.put("stateName", rs.getString("state_name"));
                employee.put("stateCode", rs.getString("state_code"));
                employee.put("zip", rs.getString("zip"));
                employee.put("phone", rs.getString("phone"));
                
                // Organizational info
                employee.put("divisionName", rs.getString("division_name"));
                employee.put("jobTitle", rs.getString("job_title"));
                
                employees.add(employee);
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return employees;
    }
    
    /**
     * Get employee count and statistics by division
     */
    public List<Map<String, Object>> getEmployeeCountByDivision() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> divisions = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_EMPLOYEE_COUNT_BY_DIVISION);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> division = new HashMap<>();
                division.put("divisionName", rs.getString("division_name"));
                division.put("divisionCode", rs.getString("division_code"));
                division.put("employeeCount", rs.getInt("employee_count"));
                division.put("avgSalary", rs.getBigDecimal("avg_salary"));
                division.put("minSalary", rs.getBigDecimal("min_salary"));
                division.put("maxSalary", rs.getBigDecimal("max_salary"));
                
                divisions.add(division);
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return divisions;
    }
    
    /**
     * Get employee count and statistics by job title
     */
    public List<Map<String, Object>> getEmployeeCountByJobTitle() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> jobTitles = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_EMPLOYEE_COUNT_BY_JOB_TITLE);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> jobTitle = new HashMap<>();
                jobTitle.put("jobTitle", rs.getString("job_title"));
                jobTitle.put("employeeCount", rs.getInt("employee_count"));
                jobTitle.put("avgSalary", rs.getBigDecimal("avg_salary"));
                jobTitle.put("minSalary", rs.getBigDecimal("min_salary"));
                jobTitle.put("maxSalary", rs.getBigDecimal("max_salary"));
                
                jobTitles.add(jobTitle);
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return jobTitles;
    }
    
    /**
     * Get employee demographics summary
     */
    public Map<String, Object> getDemographicsSummary() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, Object> demographics = new HashMap<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_DEMOGRAPHICS_SUMMARY);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                demographics.put("totalEmployees", rs.getInt("total_employees"));
                demographics.put("maleCount", rs.getInt("male_count"));
                demographics.put("femaleCount", rs.getInt("female_count"));
                demographics.put("otherGenderCount", rs.getInt("other_gender_count"));
                demographics.put("preferNotToSayCount", rs.getInt("prefer_not_to_say_count"));
                demographics.put("avgAge", rs.getDouble("avg_age"));
                
                Date earliestHire = rs.getDate("earliest_hire_date");
                if (earliestHire != null) {
                    demographics.put("earliestHireDate", earliestHire.toLocalDate());
                }
                
                Date latestHire = rs.getDate("latest_hire_date");
                if (latestHire != null) {
                    demographics.put("latestHireDate", latestHire.toLocalDate());
                }
                
                demographics.put("avgSalary", rs.getBigDecimal("avg_salary"));
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return demographics;
    }
    
    /**
     * Get salary distribution statistics
     */
    public List<Map<String, Object>> getSalaryDistribution() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> salaryRanges = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_SALARY_DISTRIBUTION);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> salaryRange = new HashMap<>();
                salaryRange.put("salaryRange", rs.getString("salary_range"));
                salaryRange.put("employeeCount", rs.getInt("employee_count"));
                salaryRange.put("minSalary", rs.getBigDecimal("min_salary"));
                salaryRange.put("maxSalary", rs.getBigDecimal("max_salary"));
                salaryRange.put("avgSalary", rs.getBigDecimal("avg_salary"));
                
                salaryRanges.add(salaryRange);
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return salaryRanges;
    }
    
    /**
     * Get employee tenure analysis
     */
    public List<Map<String, Object>> getTenureAnalysis() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> tenureRanges = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_TENURE_ANALYSIS);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> tenureRange = new HashMap<>();
                tenureRange.put("tenureRange", rs.getString("tenure_range"));
                tenureRange.put("employeeCount", rs.getInt("employee_count"));
                tenureRange.put("avgSalary", rs.getBigDecimal("avg_salary"));
                
                tenureRanges.add(tenureRange);
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return tenureRanges;
    }

    // -------------------------------------------------------------------------
    // BaseDAO implementation (read-only)
    // -------------------------------------------------------------------------

    @Override
    public Employee create(Employee entity) throws SQLException {
        throw new UnsupportedOperationException("ReportDAO is read-only; use EmployeeDAO for create operations");
    }

    @Override
    public Employee save(Employee entity) throws SQLException {
        throw new UnsupportedOperationException("ReportDAO is read-only; use EmployeeDAO for save operations");
    }

    @Override
    public Employee findById(int id) throws SQLException {
        throw new UnsupportedOperationException("Use EmployeeDAO for individual employee operations");
    }

    @Override
    public List<Employee> findAll() throws SQLException {
        throw new UnsupportedOperationException("Use EmployeeDAO for employee list operations");
    }

    @Override
    public Employee update(Employee entity) throws SQLException {
        throw new UnsupportedOperationException("ReportDAO is read-only; use EmployeeDAO for update operations");
    }

    @Override
    public boolean delete(int id) throws SQLException {
        throw new UnsupportedOperationException("ReportDAO is read-only; use EmployeeDAO for delete operations");
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        throw new UnsupportedOperationException("ReportDAO is read-only; use EmployeeDAO for delete operations");
    }

    @Override
    protected String getTableName() {
        return "employees"; // Not used for report queries
    }

    @Override
    protected String getPrimaryKeyColumn() {
        return "empid"; // Not used for report queries
    }
}
