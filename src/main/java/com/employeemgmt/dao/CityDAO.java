package com.employeemgmt.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.employeemgmt.dao.base.BaseDAO;
import com.employeemgmt.dao.base.SearchableDAO;
import com.employeemgmt.model.City;

/**
 * Data Access Object for City entities
 * Handles all database operations for city lookup data
 * 
 * @author Team 6
 */
public class CityDAO extends BaseDAO<City> implements SearchableDAO<City> {
    
    private static final String TABLE_NAME = "city";
    private static final String PRIMARY_KEY = "city_id";
    
    // SQL Queries
    private static final String INSERT_CITY = 
        "INSERT INTO city (city_name, state_id) VALUES (?, ?)";
        
    private static final String SELECT_BY_ID = 
        "SELECT c.city_id, c.city_name, c.state_id, s.state_name, s.state_code " +
        "FROM city c LEFT JOIN state s ON c.state_id = s.state_id " +
        "WHERE c.city_id = ?";
        
    private static final String SELECT_ALL = 
        "SELECT c.city_id, c.city_name, c.state_id, s.state_name, s.state_code " +
        "FROM city c LEFT JOIN state s ON c.state_id = s.state_id " +
        "ORDER BY s.state_name, c.city_name";
        
    private static final String UPDATE_CITY = 
        "UPDATE city SET city_name = ?, state_id = ? WHERE city_id = ?";
        
    private static final String DELETE_CITY = 
        "DELETE FROM city WHERE city_id = ?";
        
    private static final String SEARCH_BY_NAME = 
        "SELECT c.city_id, c.city_name, c.state_id, s.state_name, s.state_code " +
        "FROM city c LEFT JOIN state s ON c.state_id = s.state_id " +
        "WHERE c.city_name LIKE ? " +
        "ORDER BY c.city_name";
        
    private static final String SELECT_BY_STATE = 
        "SELECT c.city_id, c.city_name, c.state_id, s.state_name, s.state_code " +
        "FROM city c LEFT JOIN state s ON c.state_id = s.state_id " +
        "WHERE c.state_id = ? " +
        "ORDER BY c.city_name";

    // -------------------------------------------------------------------------
    // BaseDAO implementation
    // -------------------------------------------------------------------------

    /**
     * Insert a new City.
     */
    @Override
    public City save(City city) throws SQLException {
        if (city == null) {
            throw new IllegalArgumentException("City cannot be null");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(INSERT_CITY, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, city.getCityName());
            stmt.setInt(2, city.getStateId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new SQLException("Creating city failed, no rows affected.");
            }
            
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                city.setCityId(rs.getInt(1));
            }
            
            return city;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Compatibility helper if any legacy code still calls create(...).
     */
    public City create(City city) throws SQLException {
        return save(city);
    }

    @Override
    public City findById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_ID);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCity(rs);
            }
            
            return null;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    @Override
    public List<City> findAll() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<City> cities = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_ALL);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                cities.add(mapResultSetToCity(rs));
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return cities;
    }

    /**
     * Update an existing City, returning the updated entity.
     */
    @Override
    public City update(City city) throws SQLException {
        if (city == null) {
            throw new IllegalArgumentException("City cannot be null");
        }
        if (city.getCityId() <= 0) {
            throw new IllegalArgumentException("City ID is required for update");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(UPDATE_CITY);
            
            stmt.setString(1, city.getCityName());
            stmt.setInt(2, city.getStateId());
            stmt.setInt(3, city.getCityId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException(
                    "Updating city failed, no rows affected. City may not exist with ID: " + city.getCityId()
                );
            }
            
            return city;
            
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * Optional compatibility method if any old code expects a boolean.
     */
    public boolean updateCity(City city) throws SQLException {
        return update(city) != null;
    }

    /**
     * Delete City by ID.
     */
    @Override
    public boolean deleteById(int id) throws SQLException {
        return executeUpdateQuery(DELETE_CITY, id) > 0;
    }

    /**
     * BaseDAO already has a default delete(int) delegating to deleteById,
     * so no need to override unless you want custom behavior.
     */

    // -------------------------------------------------------------------------
    // SearchableDAO implementation
    // -------------------------------------------------------------------------

    @Override
    public List<City> searchByName(String name) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<City> cities = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SEARCH_BY_NAME);
            stmt.setString(1, "%" + name + "%");
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                cities.add(mapResultSetToCity(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching cities by name: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return cities;
    }

    @Override
    public City searchById(int id) {
        try {
            return findById(id);
        } catch (SQLException e) {
            System.err.println("Error searching city by ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<City> searchByCriteria(Map<String, Object> searchCriteria) {
        // TODO: Implement dynamic search criteria
        return new ArrayList<>();
    }

    @Override
    public int getSearchCount(Map<String, Object> searchCriteria) {
        // TODO: Implement count for search criteria
        return 0;
    }

    // -------------------------------------------------------------------------
    // Extra helpers
    // -------------------------------------------------------------------------

    /**
     * Find cities by state ID
     * @param stateId State ID to filter by
     * @return List of cities in the specified state
     * @throws SQLException if database operation fails
     */
    public List<City> findByStateId(int stateId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<City> cities = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_STATE);
            stmt.setInt(1, stateId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                cities.add(mapResultSetToCity(rs));
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return cities;
    }

    /**
     * Check if city name exists in a specific state
     * @param cityName City name to check
     * @param stateId State ID to check within
     * @return true if city exists in the state
     * @throws SQLException if database operation fails
     */
    public boolean cityExistsInState(String cityName, int stateId) throws SQLException {
        String query = "SELECT COUNT(*) FROM city WHERE city_name = ? AND state_id = ?";
        return executeCountQuery(query, cityName, stateId) > 0;
    }

    /**
     * Get count of cities by state
     * @param stateId State ID
     * @return Number of cities in the state
     * @throws SQLException if database operation fails
     */
    public int getCityCountByState(int stateId) throws SQLException {
        String query = "SELECT COUNT(*) FROM city WHERE state_id = ?";
        return executeCountQuery(query, stateId);
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
     * Map ResultSet row to City object
     * @param rs ResultSet positioned at current row
     * @return City object
     * @throws SQLException if data access fails
     */
    private City mapResultSetToCity(ResultSet rs) throws SQLException {
        City city = new City();
        
        city.setCityId(rs.getInt("city_id"));
        city.setCityName(rs.getString("city_name"));
        city.setStateId(rs.getInt("state_id"));
        
        // Set associated state information
        String stateName = rs.getString("state_name");
        if (stateName != null) {
            city.setStateName(stateName);
        }
        
        String stateCode = rs.getString("state_code");
        if (stateCode != null) {
            city.setStateCode(stateCode);
        }
        
        return city;
    }
}
