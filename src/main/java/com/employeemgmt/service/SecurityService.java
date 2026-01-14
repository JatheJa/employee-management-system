package com.employeemgmt.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;

import com.employeemgmt.controller.AuthenticationController.UserSession;
import com.employeemgmt.dao.base.DatabaseConnection;
import com.employeemgmt.util.PasswordUtils;
import com.employeemgmt.util.UserRole;

/**
 * Security service for user authentication and authorization
 * Handles password verification, user validation, and security logging
 * 
 * @author Team 6
 */
public class SecurityService {
    
    private DatabaseConnection dbConnection;
    
    // SQL queries
    private static final String AUTHENTICATE_USER = 
        "SELECT u.user_id, u.username, u.user_role, u.empid, u.is_active, " +
        "       COALESCE(CONCAT(e.first_name, ' ', e.last_name), u.username) as full_name, " +
        "       u.password_hash " +
        "FROM users u " +
        "LEFT JOIN employees e ON u.empid = e.empid " +
        "WHERE u.username = ? AND u.is_active = 1";
    
    private static final String UPDATE_LAST_LOGIN = 
        "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
    
    private static final String CREATE_USER = 
        "INSERT INTO users (username, password_hash, user_role, empid, is_active) " +
        "VALUES (?, ?, ?, ?, 1)";
    
    private static final String CHANGE_PASSWORD = 
        "UPDATE users SET password_hash = ? WHERE user_id = ?";
    
    public SecurityService() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Authenticate user with username and password
     * @param username User's username
     * @param password User's plain text password
     * @return UserSession if authentication successful, null otherwise
     * @throws SQLException if database error occurs
     */
    public UserSession authenticateUser(String username, String password) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(AUTHENTICATE_USER);
            stmt.setString(1, username.toLowerCase().trim()); // Normalize username
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPasswordHash = rs.getString("password_hash");
                
                // Verify password
                if (PasswordUtils.verifyPassword(password, storedPasswordHash)) {
                    // Create user session
                    int userId = rs.getInt("user_id");
                    UserRole userRole = UserRole.valueOf(rs.getString("user_role"));
                    Integer empId = rs.getObject("empid", Integer.class);
                    String fullName = rs.getString("full_name");
                    
                    // Update last login time
                    updateLastLogin(userId);
                    
                    return new UserSession(userId, username, userRole, empId, fullName);
                }
            }
            
            // Authentication failed
            return null;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Create a new user account
     * @param username Unique username
     * @param password Plain text password
     * @param userRole User role
     * @param employeeId Associated employee ID (can be null for HR admin)
     * @return true if user created successfully
     * @throws SQLException if database error occurs
     */
    public boolean createUser(String username, String password, UserRole userRole, Integer employeeId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username cannot be empty");
            }
            
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be empty");
            }
            
            // Hash password
            String passwordHash = PasswordUtils.hashPassword(password);
            
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(CREATE_USER);
            stmt.setString(1, username.toLowerCase().trim());
            stmt.setString(2, passwordHash);
            stmt.setString(3, userRole.name());
            
            if (employeeId != null) {
                stmt.setInt(4, employeeId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            
            return stmt.executeUpdate() > 0;
            
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Change user's password
     * @param userId User ID
     * @param oldPassword Current password
     * @param newPassword New password
     * @return true if password changed successfully
     * @throws SQLException if database error occurs
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws SQLException {
        // First verify old password
        if (!verifyUserPassword(userId, oldPassword)) {
            return false;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            String newPasswordHash = PasswordUtils.hashPassword(newPassword);
            
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(CHANGE_PASSWORD);
            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
            
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Verify user's current password
     * @param userId User ID
     * @param password Password to verify
     * @return true if password is correct
     * @throws SQLException if database error occurs
     */
    public boolean verifyUserPassword(int userId, String password) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement("SELECT password_hash FROM users WHERE user_id = ? AND is_active = 1");
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPasswordHash = rs.getString("password_hash");
                return PasswordUtils.verifyPassword(password, storedPasswordHash);
            }
            
            return false;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Update user's last login time
     * @param userId User ID
     * @throws SQLException if database error occurs
     */
    public void logoutUser(int userId) throws SQLException {
        // Could add logout logging here if needed
        System.out.println("User " + userId + " logged out at " + LocalDateTime.now());
    }
    
    /**
     * Check if username is available
     * @param username Username to check
     * @return true if username is available
     * @throws SQLException if database error occurs
     */
    public boolean isUsernameAvailable(String username) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
            stmt.setString(1, username.toLowerCase().trim());
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
            
            return true;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Deactivate user account
     * @param userId User ID to deactivate
     * @return true if deactivated successfully
     * @throws SQLException if database error occurs
     */
    public boolean deactivateUser(int userId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement("UPDATE users SET is_active = 0 WHERE user_id = ?");
            stmt.setInt(1, userId);
            
            return stmt.executeUpdate() > 0;
            
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Validate password strength
     * @param password Password to validate
     * @return validation message, null if password is strong
     */
    public String validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one digit";
        }
        
        // Additional checks can be added here
        return null; // Password is strong
    }
    
    /**
     * Update last login timestamp
     */
    private void updateLastLogin(int userId) {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LAST_LOGIN)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating last login time: " + e.getMessage());
        }
    }
    
    /**
     * Close database resources safely
     */
    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
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
}