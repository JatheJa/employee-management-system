package com.employeemgmt.dao.base;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Abstract base class for all DAO implementations
 * Provides common database operations and connection management
 *
 * @param <T> The entity type
 * @author Team 6
 */
public abstract class BaseDAO<T> {

    protected DatabaseConnection dbConnection;

    public BaseDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    // -------------------------------------------------------------------------
    // Preferred CRUD contract for all DAOs
    // -------------------------------------------------------------------------

    /**
     * Persist a new entity in the database.
     *
     * @param entity The entity to persist
     * @return The persisted entity with any generated fields populated
     * @throws SQLException if database operation fails
     */
    public abstract T save(T entity) throws SQLException;

    /**
     * Find an entity by its ID.
     *
     * @param id The entity ID
     * @return The entity if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public abstract T findById(int id) throws SQLException;

    /**
     * Get all entities.
     *
     * @return List of all entities
     * @throws SQLException if database operation fails
     */
    public abstract List<T> findAll() throws SQLException;

    /**
     * Update an existing entity.
     *
     * @param entity The entity to update
     * @return The updated entity
     * @throws SQLException if database operation fails
     */
    public abstract T update(T entity) throws SQLException;

    /**
     * Delete an entity by ID.
     *
     * @param id The ID of the entity to delete
     * @return true if deletion was successful
     * @throws SQLException if database operation fails
     */
    public abstract boolean deleteById(int id) throws SQLException;

    /**
     * Get the table name for this DAO.
     *
     * @return The database table name
     */
    protected abstract String getTableName();

    /**
     * Get the primary key column name for this table.
     *
     * @return Primary key column name
     */
    protected abstract String getPrimaryKeyColumn();

    // -------------------------------------------------------------------------
    // Legacy compatibility helpers (optional for older code paths)
    // -------------------------------------------------------------------------

    /**
     * Legacy alias for {@link #save(Object)}.
     * Only needed if older code still calls create(...).
     */
    @Deprecated
    public T create(T entity) throws SQLException {
        return save(entity);
    }

    /**
     * Legacy alias for {@link #deleteById(int)}.
     * Only needed if older code still calls delete(int).
     */
    @Deprecated
    public boolean delete(int id) throws SQLException {
        return deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Common utility methods
    // -------------------------------------------------------------------------

    /**
     * Get a database connection.
     *
     * @return Database connection
     * @throws SQLException if connection cannot be established
     */
    protected Connection getConnection() throws SQLException {
        return dbConnection.getConnection();
    }

    /**
     * Close database resources safely.
     *
     * @param conn Connection to close
     * @param stmt Statement to close
     * @param rs   ResultSet to close
     */
    protected void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            System.err.println("Error closing ResultSet: " + e.getMessage());
        }

        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            System.err.println("Error closing PreparedStatement: " + e.getMessage());
        }

        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing Connection: " + e.getMessage());
        }
    }

    /**
     * Execute a count query.
     *
     * @param query  The SQL query
     * @param params Parameters for the query
     * @return The count result
     * @throws SQLException if query execution fails
     */
    protected int executeCountQuery(String query, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Execute an update/insert/delete query.
     *
     * @param query  The SQL query
     * @param params Parameters for the query
     * @return Number of rows affected
     * @throws SQLException if query execution fails
     */
    protected int executeUpdateQuery(String query, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeUpdate();

        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * Check if entity exists by ID.
     *
     * @param id The entity ID
     * @return true if entity exists
     * @throws SQLException if database operation fails
     */
    public boolean exists(int id) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + getTableName() +
                       " WHERE " + getPrimaryKeyColumn() + " = ?";
        return executeCountQuery(query, id) > 0;
    }

    /**
     * Get count of all entities in the table.
     *
     * @return Total number of entities
     * @throws SQLException if database operation fails
     */
    public int getCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM " + getTableName();
        return executeCountQuery(query);
    }
}
