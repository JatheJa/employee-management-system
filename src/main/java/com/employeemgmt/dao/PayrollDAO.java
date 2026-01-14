package com.employeemgmt.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.employeemgmt.dao.base.BaseDAO;
import com.employeemgmt.model.PayrollRecord;

/**
 * Data Access Object for PayrollRecord entities
 * Handles all database operations for payroll data.
 *
 * Responsibilities per SDD:
 * - Pay history queries for individual employees
 * - Monthly pay summaries by job title and division
 */
public class PayrollDAO extends BaseDAO<PayrollRecord> {

    private static final Logger LOGGER = Logger.getLogger(PayrollDAO.class.getName());

    private static final String TABLE_NAME = "payroll";
    private static final String PRIMARY_KEY = "payroll_id";

    // -------------------------------------------------------------------------
    // SQL Queries
    // -------------------------------------------------------------------------

    private static final String INSERT_PAYROLL =
        "INSERT INTO payroll (empid, pay_date, pay_period_start, pay_period_end, " +
        "gross_pay, net_pay, federal_tax, state_tax, other_deductions) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID =
        "SELECT p.*, e.first_name, e.last_name, e.emp_number " +
        "FROM payroll p " +
        "JOIN employees e ON p.empid = e.empid " +
        "WHERE p.payroll_id = ?";

    private static final String SELECT_ALL =
        "SELECT p.*, e.first_name, e.last_name, e.emp_number " +
        "FROM payroll p " +
        "JOIN employees e ON p.empid = e.empid " +
        "ORDER BY p.pay_date DESC";

    private static final String SELECT_BY_EMPLOYEE_ID =
        "SELECT p.*, e.first_name, e.last_name, e.emp_number " +
        "FROM payroll p " +
        "JOIN employees e ON p.empid = e.empid " +
        "WHERE p.empid = ? " +
        "ORDER BY p.pay_date DESC";

    private static final String SELECT_BY_DATE_RANGE =
        "SELECT p.*, e.first_name, e.last_name, e.emp_number " +
        "FROM payroll p " +
        "JOIN employees e ON p.empid = e.empid " +
        "WHERE p.pay_date BETWEEN ? AND ? " +
        "ORDER BY p.pay_date DESC";

    private static final String SELECT_BY_PAY_PERIOD =
        "SELECT p.*, e.first_name, e.last_name, e.emp_number " +
        "FROM payroll p " +
        "JOIN employees e ON p.empid = e.empid " +
        "WHERE p.pay_period_start >= ? AND p.pay_period_end <= ? " +
        "ORDER BY p.pay_date DESC";

    // Summary queries supporting Monthly Pay by Job Title / Division reports (per SDD)
    private static final String SELECT_MONTHLY_PAY_BY_JOB_TITLE =
        "SELECT jt.title_name AS job_title, " +               // fixed: use title_name with alias
        "       SUM(p.gross_pay) AS total_gross_pay, " +
        "       SUM(p.net_pay)   AS total_net_pay, " +
        "       COUNT(*)         AS employee_count " +
        "FROM payroll p " +
        "JOIN employees e ON p.empid = e.empid " +
        "JOIN employee_job_titles ejt ON e.empid = ejt.empid AND ejt.is_current = 1 " +
        "JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id " +
        "WHERE p.pay_period_start >= ? AND p.pay_period_end <= ? " +
        "GROUP BY jt.job_title_id, jt.title_name " +          // fixed: group by title_name
        "ORDER BY total_gross_pay DESC";

    private static final String SELECT_MONTHLY_PAY_BY_DIVISION =
        "SELECT d.division_name, " +
        "       SUM(p.gross_pay) AS total_gross_pay, " +
        "       SUM(p.net_pay)   AS total_net_pay, " +
        "       COUNT(*)         AS employee_count " +
        "FROM payroll p " +
        "JOIN employees e ON p.empid = e.empid " +
        "JOIN employee_division ed ON e.empid = ed.empid AND ed.is_current = 1 " +
        "JOIN division d ON ed.div_id = d.div_id " +
        "WHERE p.pay_period_start >= ? AND p.pay_period_end <= ? " +
        "GROUP BY d.div_id, d.division_name " +
        "ORDER BY total_gross_pay DESC";

    private static final String UPDATE_PAYROLL =
        "UPDATE payroll SET empid = ?, pay_date = ?, pay_period_start = ?, pay_period_end = ?, " +
        "gross_pay = ?, net_pay = ?, federal_tax = ?, state_tax = ?, other_deductions = ? " +
        "WHERE payroll_id = ?";

    // FIXED: proper parameterized DELETE
    private static final String DELETE_PAYROLL =
        "DELETE FROM payroll WHERE payroll_id = ?";

    // -------------------------------------------------------------------------
    // BaseDAO implementation
    // -------------------------------------------------------------------------

    /**
     * Create a new payroll record.
     */
    @Override
    public PayrollRecord create(PayrollRecord payrollRecord) throws SQLException {
        if (payrollRecord == null) {
            throw new IllegalArgumentException("PayrollRecord cannot be null");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(INSERT_PAYROLL, Statement.RETURN_GENERATED_KEYS);

            setPayrollParameters(stmt, payrollRecord);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating payroll record failed, no rows affected");
            }

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                payrollRecord.setPayrollId(rs.getInt(1));
            }

            LOGGER.info("Payroll record created successfully with ID: " + payrollRecord.getPayrollId());
            return payrollRecord;

        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Convenience alias.
     */
    public PayrollRecord save(PayrollRecord payrollRecord) throws SQLException {
        return create(payrollRecord);
    }

    @Override
    public PayrollRecord findById(int payrollId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_ID);
            stmt.setInt(1, payrollId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPayrollRecord(rs);
            }
            return null;

        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    @Override
    public List<PayrollRecord> findAll() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<PayrollRecord> payrollRecords = new ArrayList<>();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_ALL);
            rs = stmt.executeQuery();

            while (rs.next()) {
                payrollRecords.add(mapResultSetToPayrollRecord(rs));
            }

        } finally {
            closeResources(conn, stmt, rs);
        }

        return payrollRecords;
    }

    /**
     * Pay Statement History: find payroll records by employee ID, sorted by pay date.
     *
     * Supports "Pay Statement History Report" in the SDD.
     */
    public List<PayrollRecord> findByEmployeeId(int empId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<PayrollRecord> payrollRecords = new ArrayList<>();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_EMPLOYEE_ID);
            stmt.setInt(1, empId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                payrollRecords.add(mapResultSetToPayrollRecord(rs));
            }

        } finally {
            closeResources(conn, stmt, rs);
        }

        return payrollRecords;
    }

    /**
     * Find payroll records within a date range (all employees).
     */
    public List<PayrollRecord> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<PayrollRecord> payrollRecords = new ArrayList<>();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_DATE_RANGE);
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            rs = stmt.executeQuery();

            while (rs.next()) {
                payrollRecords.add(mapResultSetToPayrollRecord(rs));
            }

        } finally {
            closeResources(conn, stmt, rs);
        }

        return payrollRecords;
    }

    /**
     * Find payroll records for a specific pay period (all employees).
     */
    public List<PayrollRecord> findByPayPeriod(LocalDate periodStart, LocalDate periodEnd) throws SQLException {
        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("Period start and end dates are required");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException("Period end date cannot be before start date");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<PayrollRecord> payrollRecords = new ArrayList<>();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_PAY_PERIOD);
            stmt.setDate(1, Date.valueOf(periodStart));
            stmt.setDate(2, Date.valueOf(periodEnd));
            rs = stmt.executeQuery();

            while (rs.next()) {
                payrollRecords.add(mapResultSetToPayrollRecord(rs));
            }

        } finally {
            closeResources(conn, stmt, rs);
        }

        return payrollRecords;
    }

    /**
     * Monthly Pay by Job Title Report.
     *
     * Returns aggregated payroll totals for the given period grouped by job title:
     * - jobTitle
     * - totalGrossPay
     * - totalNetPay
     * - employeeCount
     *
     * This directly supports the SDD's "Monthly Pay by Job Title Report".
     */
    public List<Map<String, Object>> getMonthlyPayByJobTitle(LocalDate periodStart, LocalDate periodEnd)
            throws SQLException {

        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("Period start and end dates are required");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException("Period end date cannot be before start date");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_MONTHLY_PAY_BY_JOB_TITLE);
            stmt.setDate(1, Date.valueOf(periodStart));
            stmt.setDate(2, Date.valueOf(periodEnd));
            rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("jobTitle", rs.getString("job_title"));
                row.put("totalGrossPay", rs.getBigDecimal("total_gross_pay"));
                row.put("totalNetPay", rs.getBigDecimal("total_net_pay"));
                row.put("employeeCount", rs.getInt("employee_count"));
                results.add(row);
            }

            LOGGER.info("Monthly pay by job title computed for period "
                        + periodStart + " to " + periodEnd + ", rows: " + results.size());
        } finally {
            closeResources(conn, stmt, rs);
        }

        return results;
    }

    /**
     * Monthly Pay by Division Report.
     *
     * Returns aggregated payroll totals for the given period grouped by division:
     * - divisionName
     * - totalGrossPay
     * - totalNetPay
     * - employeeCount
     *
     * This directly supports the SDD's "Monthly Pay by Division Report".
     */
    public List<Map<String, Object>> getMonthlyPayByDivision(LocalDate periodStart, LocalDate periodEnd)
            throws SQLException {

        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("Period start and end dates are required");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException("Period end date cannot be before start date");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_MONTHLY_PAY_BY_DIVISION);
            stmt.setDate(1, Date.valueOf(periodStart));
            stmt.setDate(2, Date.valueOf(periodEnd));
            rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("divisionName", rs.getString("division_name"));
                row.put("totalGrossPay", rs.getBigDecimal("total_gross_pay"));
                row.put("totalNetPay", rs.getBigDecimal("total_net_pay"));
                row.put("employeeCount", rs.getInt("employee_count"));
                results.add(row);
            }

            LOGGER.info("Monthly pay by division computed for period "
                        + periodStart + " to " + periodEnd + ", rows: " + results.size());
        } finally {
            closeResources(conn, stmt, rs);
        }

        return results;
    }

    /**
     * Update existing payroll record.
     */
    @Override
    public PayrollRecord update(PayrollRecord payrollRecord) throws SQLException {
        if (payrollRecord == null) {
            throw new IllegalArgumentException("PayrollRecord cannot be null");
        }

        if (payrollRecord.getPayrollId() <= 0) {
            throw new IllegalArgumentException("Payroll ID is required for update");
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(UPDATE_PAYROLL);

            setPayrollParameters(stmt, payrollRecord);
            stmt.setInt(10, payrollRecord.getPayrollId()); // WHERE clause parameter

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException(
                    "Updating payroll record failed, no rows affected. Record may not exist: "
                    + payrollRecord.getPayrollId()
                );
            }

            LOGGER.info("Payroll record updated successfully: " + payrollRecord.getPayrollId());
            return payrollRecord;

        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * Override BaseDAO.delete(int) – delete by primary key.
     */
    @Override
    public boolean delete(int payrollId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(DELETE_PAYROLL);
            stmt.setInt(1, payrollId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.info("Payroll record deleted successfully: " + payrollId);
                return true;
            }

            LOGGER.info("No payroll record found to delete for ID: " + payrollId);
            return false;

        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * Convenience alias matching common DAO naming.
     */
    public boolean deleteById(int payrollId) throws SQLException {
        return delete(payrollId);
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getPrimaryKeyColumn() {
        return PRIMARY_KEY;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Set parameters for PreparedStatement using PayrollRecord object.
     */
    private void setPayrollParameters(PreparedStatement stmt, PayrollRecord record) throws SQLException {
        stmt.setInt(1, record.getEmpId());
        stmt.setDate(2, Date.valueOf(record.getPayDate()));
        stmt.setDate(3, Date.valueOf(record.getPayPeriodStart()));
        stmt.setDate(4, Date.valueOf(record.getPayPeriodEnd()));
        stmt.setBigDecimal(5, record.getGrossPay());
        stmt.setBigDecimal(6, record.getNetPay());
        stmt.setBigDecimal(7, record.getFederalTax());
        stmt.setBigDecimal(8, record.getStateTax());
        stmt.setBigDecimal(9, record.getOtherDeductions());
    }

    /**
     * Map ResultSet row to PayrollRecord object.
     */
    private PayrollRecord mapResultSetToPayrollRecord(ResultSet rs) throws SQLException {
        PayrollRecord record = new PayrollRecord();

        record.setPayrollId(rs.getInt("payroll_id"));
        record.setEmpId(rs.getInt("empid"));

        Date payDate = rs.getDate("pay_date");
        if (payDate != null) {
            record.setPayDate(payDate.toLocalDate());
        }

        Date periodStart = rs.getDate("pay_period_start");
        if (periodStart != null) {
            record.setPayPeriodStart(periodStart.toLocalDate());
        }

        Date periodEnd = rs.getDate("pay_period_end");
        if (periodEnd != null) {
            record.setPayPeriodEnd(periodEnd.toLocalDate());
        }

        record.setGrossPay(rs.getBigDecimal("gross_pay"));
        record.setNetPay(rs.getBigDecimal("net_pay"));
        record.setFederalTax(rs.getBigDecimal("federal_tax"));
        record.setStateTax(rs.getBigDecimal("state_tax"));
        record.setOtherDeductions(rs.getBigDecimal("other_deductions"));

        // Set employee information if available
        record.setEmployeeFirstName(rs.getString("first_name"));
        record.setEmployeeLastName(rs.getString("last_name"));
        record.setEmployeeNumber(rs.getString("emp_number"));

        return record;
    }
}
