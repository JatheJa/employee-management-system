package com.employeemgmt.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.dao.base.BaseDAO;
import com.employeemgmt.dao.base.SearchableDAO;
import com.employeemgmt.model.Division;

/**
 * Data Access Object for Division entities
 * Handles all database operations for organizational division data
 * 
 * @author Team 6
 */
public class DivisionDAO extends BaseDAO<Division> implements SearchableDAO<Division> {
    
    private static final Logger LOGGER = Logger.getLogger(DivisionDAO.class.getName());
    
    private static final String TABLE_NAME = "division";
    private static final String PRIMARY_KEY = "div_id";
    
    // SQL Queries
    private static final String INSERT_DIVISION = 
        "INSERT INTO division (division_name, division_code) VALUES (?, ?)";
        
    private static final String SELECT_BY_ID = 
        "SELECT * FROM division WHERE div_id = ?";
        
    private static final String SELECT_ALL = 
        "SELECT * FROM division ORDER BY division_name ASC";
        
    private static final String SELECT_BY_NAME = 
        "SELECT * FROM division WHERE division_name = ?";
        
    private static final String SELECT_BY_CODE = 
        "SELECT * FROM division WHERE division_code = ?";
        
    private static final String SEARCH_BY_NAME_PATTERN = 
        "SELECT * FROM division WHERE division_name LIKE ? OR division_code LIKE ? " +
        "ORDER BY division_name ASC";
        
    private static final String UPDATE_DIVISION = 
        "UPDATE division SET division_name = ?, division_code = ? WHERE div_id = ?";
        
    private static final String DELETE_DIVISION = 
        "DELETE FROM division WHERE div_id = ?";
        
    private static final String CHECK_DIVISION_IN_USE = 
        "SELECT COUNT(*) FROM employee_division WHERE div_id = ? AND is_current = 1";
        
    private static final String SELECT_WITH_EMPLOYEE_COUNT = 
        "SELECT d.*, COUNT(ed.empid) as employee_count " +
        "FROM division d " +
        "LEFT JOIN employee_division ed ON d.div_id = ed.div_id AND ed.is_current = 1 " +
        "LEFT JOIN employees e ON ed.empid = e.empid AND e.employment_status = 'ACTIVE' " +
        "GROUP BY d.div_id, d.division_name, d.division_code " +
        "ORDER BY d.division_name ASC";
        
    private static final String SELECT_ACTIVE_DIVISIONS = 
        "SELECT DISTINCT d.* FROM division d " +
        "INNER JOIN employee_division ed ON d.div_id = ed.div_id AND ed.is_current = 1 " +
        "INNER JOIN employees e ON ed.empid = e.empid AND e.employment_status = 'ACTIVE' " +
        "ORDER BY d.division_name ASC";

    @Override
    public Division save(Division division) throws SQLException {
        if (division == null) {
            throw new IllegalArgumentException("Division cannot be null");
        }
        
        if (!division.isValid()) {
            throw new IllegalArgumentException("Division data is not valid");
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(INSERT_DIVISION, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, division.getDivisionName());
            stmt.setString(2, division.getDivisionCode());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating division failed, no rows affected");
            }
            
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                division.setDivId(rs.getInt(1));
            }
            
            LOGGER.info("Division created successfully with ID: " + division.getDivId());
            return division;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    @Override
    public Division findById(int divId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_ID);
            stmt.setInt(1, divId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToDivision(rs);
            }
            
            return null;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    @Override
    public List<Division> findAll() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Division> divisions = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_ALL);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                divisions.add(mapResultSetToDivision(rs));
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return divisions;
    }
    
    /**
     * Find all divisions with employee counts
     * 
     * @return List of divisions with employee count information
     * @throws SQLException if database operation fails
     */
    public List<Division> findAllWithEmployeeCount() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Division> divisions = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_WITH_EMPLOYEE_COUNT);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Division division = mapResultSetToDivision(rs);
                division.setEmployeeCount(rs.getInt("employee_count"));
                divisions.add(division);
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return divisions;
    }
    
    /**
     * Find only divisions that have active employees
     * 
     * @return List of divisions with active employees
     * @throws SQLException if database operation fails
     */
    public List<Division> findActiveDivisions() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Division> divisions = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_ACTIVE_DIVISIONS);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                divisions.add(mapResultSetToDivision(rs));
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return divisions;
    }
    
    /**
     * Find division by name
     * 
     * @param divisionName Division name
     * @return Division if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public Division findByName(String divisionName) throws SQLException {
        if (divisionName == null || divisionName.trim().isEmpty()) {
            return null;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_NAME);
            stmt.setString(1, divisionName);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToDivision(rs);
            }
            
            return null;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Find division by division code
     * 
     * @param divisionCode Division code
     * @return Division if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public Division findByCode(String divisionCode) throws SQLException {
        if (divisionCode == null || divisionCode.trim().isEmpty()) {
            return null;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_CODE);
            stmt.setString(1, divisionCode);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToDivision(rs);
            }
            
            return null;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    @Override
    public Division update(Division division) throws SQLException {
        if (division == null) {
            throw new IllegalArgumentException("Division cannot be null");
        }
        
        if (division.getDivId() <= 0) {
            throw new IllegalArgumentException("Division ID is required for update");
        }
        
        if (!division.isValid()) {
            throw new IllegalArgumentException("Division data is not valid");
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(UPDATE_DIVISION);
            
            stmt.setString(1, division.getDivisionName());
            stmt.setString(2, division.getDivisionCode());
            stmt.setInt(3, division.getDivId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating division failed, no rows affected. Division may not exist.");
            }
            
            LOGGER.info("Division updated successfully: " + division.getDivId());
            return division;
            
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    @Override
    public boolean deleteById(int divId) throws SQLException {
        // First check if division is in use
        if (isDivisionInUse(divId)) {
            throw new SQLException("Cannot delete division: it is currently assigned to active employees");
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(DELETE_DIVISION);
            stmt.setInt(1, divId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.info("Division deleted successfully: " + divId);
                return true;
            }
            
            return false;
            
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Check if division is currently assigned to any active employees
     * 
     * @param divId Division ID
     * @return true if division is in use, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean isDivisionInUse(int divId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(CHECK_DIVISION_IN_USE);
            stmt.setInt(1, divId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getPrimaryKeyColumn() {
        return PRIMARY_KEY;
    }

    // SearchableDAO implementation
    @Override
    public List<Division> searchByName(String namePattern) {
        if (namePattern == null || namePattern.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Division> divisions = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SEARCH_BY_NAME_PATTERN);
            String searchPattern = "%" + namePattern + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                divisions.add(mapResultSetToDivision(rs));
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching divisions by name pattern: " + namePattern, e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return divisions;
    }

    @Override
    public Division searchById(int id) {
        try {
            return findById(id);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching division by ID: " + id, e);
            return null;
        }
    }

    @Override
    public List<Division> searchByCriteria(Map<String, Object> searchCriteria) {
        List<Division> results = new ArrayList<>();
        
        if (searchCriteria == null || searchCriteria.isEmpty()) {
            return results;
        }
        
        try {
            // Handle different search criteria
            String name = (String) searchCriteria.get("name");
            String code = (String) searchCriteria.get("code");
            Boolean activeOnly = (Boolean) searchCriteria.get("activeOnly");
            
            if (name != null && !name.trim().isEmpty()) {
                results = searchByName(name);
            } else if (code != null && !code.trim().isEmpty()) {
                Division division = findByCode(code);
                if (division != null) {
                    results.add(division);
                }
            } else if (activeOnly != null && activeOnly) {
                results = findActiveDivisions();
            } else {
                results = findAll();
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching divisions by criteria", e);
        }
        
        return results;
    }

    @Override
    public int getSearchCount(Map<String, Object> searchCriteria) {
        List<Division> results = searchByCriteria(searchCriteria);
        return results.size();
    }
    
    /**
     * Map ResultSet row to Division object
     * 
     * @param rs ResultSet positioned at current row
     * @return Division object
     * @throws SQLException if data access fails
     */
    private Division mapResultSetToDivision(ResultSet rs) throws SQLException {
        Division division = new Division();
        
        division.setDivId(rs.getInt("div_id"));
        division.setDivisionName(rs.getString("division_name"));
        division.setDivisionCode(rs.getString("division_code"));
        
        return division;
    }
}