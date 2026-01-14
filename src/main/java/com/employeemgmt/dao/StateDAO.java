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
import com.employeemgmt.model.State;

/**
 * Data Access Object for State entities
 * Handles all database operations for state lookup data
 * 
 * @author Team 6
 */
public class StateDAO extends BaseDAO<State> implements SearchableDAO<State> {
    
    private static final String TABLE_NAME = "state";
    private static final String PRIMARY_KEY = "state_id";
    
    // SQL Queries
    private static final String INSERT_STATE = 
        "INSERT INTO state (state_code, state_name) VALUES (?, ?)";
        
    private static final String SELECT_BY_ID = 
        "SELECT * FROM state WHERE state_id = ?";
        
    private static final String SELECT_ALL = 
        "SELECT * FROM state ORDER BY state_name";
        
    private static final String UPDATE_STATE = 
        "UPDATE state SET state_code = ?, state_name = ? WHERE state_id = ?";
        
    private static final String DELETE_STATE = 
        "DELETE FROM state WHERE state_id = ?";
        
    private static final String SEARCH_BY_NAME = 
        "SELECT * FROM state WHERE state_name LIKE ? ORDER BY state_name";
        
    private static final String SEARCH_BY_CODE = 
        "SELECT * FROM state WHERE state_code = ?";
        
    private static final String SELECT_WITH_CITY_COUNT = 
        "SELECT s.*, COUNT(c.city_id) as city_count " +
        "FROM state s LEFT JOIN city c ON s.state_id = c.state_id " +
        "GROUP BY s.state_id, s.state_code, s.state_name " +
        "ORDER BY s.state_name";

    // -------------------------------------------------------------------------
    // BaseDAO required methods
    // -------------------------------------------------------------------------

    @Override
    public State create(State state) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(INSERT_STATE, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, state.getStateCode().toUpperCase());
            stmt.setString(2, state.getStateName());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new SQLException("Creating state failed, no rows affected.");
            }
            
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                state.setStateId(rs.getInt(1));
            }
            
            return state;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * New: required by BaseDAO – typically same behavior as create().
     */
    @Override
    public State save(State state) throws SQLException {
        return create(state);
    }

    @Override
    public State findById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_ID);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToState(rs);
            }
            
            return null;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    @Override
    public List<State> findAll() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<State> states = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_ALL);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                states.add(mapResultSetToState(rs));
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return states;
    }

    /**
     * Updated: BaseDAO.update(T) now returns T, not boolean.
     */
    @Override
    public State update(State state) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(UPDATE_STATE);
            
            stmt.setString(1, state.getStateCode().toUpperCase());
            stmt.setString(2, state.getStateName());
            stmt.setInt(3, state.getStateId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException(
                    "Updating state failed, no rows affected. State may not exist with ID: " + state.getStateId()
                );
            }
            
            return state;
            
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return executeUpdateQuery(DELETE_STATE, id) > 0;
    }

    /**
     * New: required by BaseDAO – delegate to delete(int).
     */
    @Override
    public boolean deleteById(int id) throws SQLException {
        return delete(id);
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
    // SearchableDAO implementation
    // -------------------------------------------------------------------------

    @Override
    public List<State> searchByName(String name) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<State> states = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SEARCH_BY_NAME);
            stmt.setString(1, "%" + name + "%");
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                states.add(mapResultSetToState(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching states by name: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return states;
    }

    @Override
    public State searchById(int id) {
        try {
            return findById(id);
        } catch (SQLException e) {
            System.err.println("Error searching state by ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<State> searchByCriteria(Map<String, Object> searchCriteria) {
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
     * Find state by state code (e.g., "GA", "FL")
     * @param stateCode 2-character state code
     * @return State if found, null otherwise
     */
    public State findByStateCode(String stateCode) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SEARCH_BY_CODE);
            stmt.setString(1, stateCode.toUpperCase());
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToState(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching state by code: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return null;
    }

    /**
     * Check if state code already exists
     * @param stateCode State code to check
     * @return true if state code exists
     * @throws SQLException if database operation fails
     */
    public boolean stateCodeExists(String stateCode) throws SQLException {
        String query = "SELECT COUNT(*) FROM state WHERE state_code = ?";
        return executeCountQuery(query, stateCode.toUpperCase()) > 0;
    }

    /**
     * Check if state name already exists
     * @param stateName State name to check
     * @return true if state name exists
     * @throws SQLException if database operation fails
     */
    public boolean stateNameExists(String stateName) throws SQLException {
        String query = "SELECT COUNT(*) FROM state WHERE state_name = ?";
        return executeCountQuery(query, stateName) > 0;
    }

    /**
     * Get all states with city counts
     * @return List of states with city count information
     * @throws SQLException if database operation fails
     */
    public List<StateWithCityCount> getStatesWithCityCounts() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<StateWithCityCount> states = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_WITH_CITY_COUNT);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                State state = mapResultSetToState(rs);
                int cityCount = rs.getInt("city_count");
                states.add(new StateWithCityCount(state, cityCount));
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return states;
    }

    /**
     * Get states commonly used in the system
     * @return List of frequently used states
     * @throws SQLException if database operation fails
     */
    public List<State> getCommonStates() throws SQLException {
        String query = "SELECT DISTINCT s.* FROM state s " +
                      "JOIN address a ON s.state_id = a.state_id " +
                      "ORDER BY s.state_name";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<State> states = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                states.add(mapResultSetToState(rs));
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return states;
    }
    
    /**
     * Map ResultSet row to State object
     * @param rs ResultSet positioned at current row
     * @return State object
     * @throws SQLException if data access fails
     */
    private State mapResultSetToState(ResultSet rs) throws SQLException {
        State state = new State();
        
        state.setStateId(rs.getInt("state_id"));
        state.setStateCode(rs.getString("state_code"));
        state.setStateName(rs.getString("state_name"));
        
        return state;
    }
    
    /**
     * Helper class for states with city counts
     */
    public static class StateWithCityCount {
        private final State state;
        private final int cityCount;
        
        public StateWithCityCount(State state, int cityCount) {
            this.state = state;
            this.cityCount = cityCount;
        }
        
        public State getState() {
            return state;
        }
        
        public int getCityCount() {
            return cityCount;
        }
        
        @Override
        public String toString() {
            return state.getDisplayName() + " (" + cityCount + " cities)";
        }
    }
}
