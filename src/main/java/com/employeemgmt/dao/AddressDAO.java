package com.employeemgmt.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.dao.base.BaseDAO;
import com.employeemgmt.model.Address;

/**
 * Data Access Object for Address entities
 * Handles all database operations for employee address data
 * 
 * @author Team 6
 */
public class AddressDAO extends BaseDAO<Address> {
    
    private static final Logger LOGGER = Logger.getLogger(AddressDAO.class.getName());
    
    private static final String TABLE_NAME = "address";
    private static final String PRIMARY_KEY = "empid";
    
    // SQL Queries
    private static final String INSERT_ADDRESS = 
        "INSERT INTO address (empid, street, city_id, state_id, zip, gender, race, date_of_birth, phone) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
    private static final String SELECT_BY_EMPLOYEE_ID = 
        "SELECT a.*, c.city_name, s.state_name, s.state_code " +
        "FROM address a " +
        "LEFT JOIN city c ON a.city_id = c.city_id " +
        "LEFT JOIN state s ON a.state_id = s.state_id " +
        "WHERE a.empid = ?";
        
    private static final String SELECT_ALL =
        "SELECT a.*, c.city_name, s.state_name, s.state_code " +
        "FROM address a " +
        "LEFT JOIN city c ON a.city_id = c.city_id " +
        "LEFT JOIN state s ON a.state_id = s.state_id";
        
    private static final String UPDATE_ADDRESS = 
        "UPDATE address SET street = ?, city_id = ?, state_id = ?, zip = ?, " +
        "gender = ?, race = ?, date_of_birth = ?, phone = ? WHERE empid = ?";
        
    private static final String DELETE_ADDRESS = 
        "DELETE FROM address WHERE empid = ?";

    // NEW: search employee IDs by Date of Birth
    private static final String SELECT_EMP_IDS_BY_DOB =
        "SELECT empid FROM address WHERE date_of_birth = ?";

    // -------------------------------------------------------------------------
    // BaseDAO implementation
    // -------------------------------------------------------------------------

    /**
     * Insert a new address record.
     */
    @Override
    public Address save(Address address) throws SQLException {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(INSERT_ADDRESS);
            
            setAddressParameters(stmt, address);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating address failed, no rows affected");
            }
            
            LOGGER.info("Address created successfully for employee ID: " + address.getEmpId());
            return address;
            
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * BaseDAO.create(...) is already implemented to delegate to save(...),
     * so any old code calling create(address) will still work.
     */

    @Override
    public Address findById(int empId) throws SQLException {
        return findByEmployeeId(empId);
    }
    
    /**
     * Find address by employee ID.
     * 
     * @param empId Employee ID
     * @return Address object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public Address findByEmployeeId(int empId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_EMPLOYEE_ID);
            stmt.setInt(1, empId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToAddress(rs);
            }
            
            return null;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    @Override
    public List<Address> findAll() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Address> addresses = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_ALL);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return addresses;
    }

    /**
     * Update an existing address.
     */
    @Override
    public Address update(Address address) throws SQLException {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        
        if (address.getEmpId() <= 0) {
            throw new IllegalArgumentException("Employee ID is required for address update");
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(UPDATE_ADDRESS);
            
            setAddressParameters(stmt, address);
            stmt.setInt(9, address.getEmpId()); // WHERE clause parameter
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException(
                    "Updating address failed, no rows affected. Address may not exist for employee ID: "
                    + address.getEmpId()
                );
            }
            
            LOGGER.info("Address updated successfully for employee ID: " + address.getEmpId());
            return address;
            
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * Delete address by employee ID.
     */
    @Override
    public boolean deleteById(int empId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(DELETE_ADDRESS);
            stmt.setInt(1, empId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.info("Address deleted successfully for employee ID: " + empId);
                return true;
            }
            
            LOGGER.info("No address record found to delete for employee ID: " + empId);
            return false;
            
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    // BaseDAO.delete(int) already delegates to deleteById(int),
    // so any old code calling delete(empId) will still work.

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getPrimaryKeyColumn() {
        return PRIMARY_KEY;
    }
    
    // -------------------------------------------------------------------------
    // Extra query helpers
    // -------------------------------------------------------------------------

    /**
     * Find employee IDs by Date of Birth.
     * Used by EmployeeController.searchEmployees when searching by DOB.
     *
     * @param dateOfBirth date of birth to search for
     * @return list of empid values for employees with that DOB
     * @throws SQLException if database operation fails
     */
    public List<Integer> findEmployeeIdsByDateOfBirth(LocalDate dateOfBirth) throws SQLException {
        List<Integer> empIds = new ArrayList<>();
        
        if (dateOfBirth == null) {
            return empIds;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_EMP_IDS_BY_DOB);
            stmt.setDate(1, Date.valueOf(dateOfBirth));
            rs = stmt.executeQuery();

            while (rs.next()) {
                empIds.add(rs.getInt("empid"));
            }

        } finally {
            closeResources(conn, stmt, rs);
        }

        return empIds;
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------
    
    /**
     * Set parameters for PreparedStatement using Address object
     * 
     * @param stmt PreparedStatement to set parameters on
     * @param address Address object containing data
     * @throws SQLException if setting parameters fails
     */
    private void setAddressParameters(PreparedStatement stmt, Address address) throws SQLException {
        stmt.setInt(1, address.getEmpId());
        stmt.setString(2, address.getStreet());
        stmt.setInt(3, address.getCityId());
        stmt.setInt(4, address.getStateId());
        stmt.setString(5, address.getZip());
        stmt.setString(6, address.getGender() != null ? address.getGender().toString() : null);
        stmt.setString(7, address.getRace());
        
        if (address.getDateOfBirth() != null) {
            stmt.setDate(8, Date.valueOf(address.getDateOfBirth()));
        } else {
            stmt.setNull(8, Types.DATE);
        }
        
        stmt.setString(9, address.getPhone());
    }
    
    /**
     * Map ResultSet row to Address object
     * 
     * @param rs ResultSet positioned at current row
     * @return Address object
     * @throws SQLException if data access fails
     */
    private Address mapResultSetToAddress(ResultSet rs) throws SQLException {
        Address address = new Address();
        
        address.setEmpId(rs.getInt("empid"));
        address.setStreet(rs.getString("street"));
        address.setCityId(rs.getInt("city_id"));
        address.setStateId(rs.getInt("state_id"));
        address.setZip(rs.getString("zip"));
        
        String genderStr = rs.getString("gender");
        if (genderStr != null && !genderStr.isEmpty()) {
            try {
                address.setGender(Address.Gender.valueOf(genderStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Invalid gender value in database: " + genderStr, e);
            }
        }
        
        address.setRace(rs.getString("race"));
        
        Date dateOfBirth = rs.getDate("date_of_birth");
        if (dateOfBirth != null) {
            address.setDateOfBirth(dateOfBirth.toLocalDate());
        }
        
        address.setPhone(rs.getString("phone"));
        
        // Set additional lookup data if available
        address.setCityName(rs.getString("city_name"));
        address.setStateName(rs.getString("state_name"));
        address.setStateCode(rs.getString("state_code"));
        
        return address;
    }
}
