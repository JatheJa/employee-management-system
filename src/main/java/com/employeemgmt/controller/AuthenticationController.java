package com.employeemgmt.controller;

import java.sql.SQLException;

import com.employeemgmt.dao.EmployeeDAO;
import com.employeemgmt.model.Employee;
import com.employeemgmt.service.SecurityService;
import com.employeemgmt.util.UserRole;

/**
 * Controller for handling user authentication and authorization
 * Manages login, logout, and permission checking
 * 
 * @author Team 6
 */
public class AuthenticationController {

    private final SecurityService securityService;
    private UserSession currentUserSession;
    private final EmployeeDAO employeeDAO;

    public AuthenticationController() {
        this.securityService = new SecurityService();
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * Authenticate user with username and password
     */
    public AuthenticationResult authenticateUser(String username, String password) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return new AuthenticationResult(false, "Username cannot be empty", null);
            }
            if (password == null || password.trim().isEmpty()) {
                return new AuthenticationResult(false, "Password cannot be empty", null);
            }

            UserSession session = securityService.authenticateUser(username, password);

            if (session != null) {
                this.currentUserSession = session;
                return new AuthenticationResult(true, "Login successful", session);
            } else {
                return new AuthenticationResult(false, "Invalid username or password", null);
            }

        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
            return new AuthenticationResult(false, "System error. Please try again.", null);
        } catch (Exception e) {
            System.err.println("Unexpected error during authentication: " + e.getMessage());
            return new AuthenticationResult(false, "Unexpected error occurred", null);
        }
    }

    /**
     * Logout the current user
     */
    public void logout() {
        if (currentUserSession != null) {
            try {
                securityService.logoutUser(currentUserSession.getUserId());
            } catch (SQLException e) {
                System.err.println("Error updating logout time: " + e.getMessage());
            }
            currentUserSession = null;
        }
    }

    public boolean isUserLoggedIn() {
        return currentUserSession != null;
    }

    public UserSession getCurrentUserSession() {
        return currentUserSession;
    }

    public boolean isCurrentUserAdmin() {
        return currentUserSession != null &&
               currentUserSession.getUserRole() == UserRole.HR_ADMIN;
    }

    public boolean canCurrentUserModifyData() {
        return isCurrentUserAdmin();
    }

    public boolean canCurrentUserViewEmployeeData(int employeeId) {
        if (currentUserSession == null) {
            return false;
        }
        if (currentUserSession.getUserRole() == UserRole.HR_ADMIN) {
            return true;
        }
        return currentUserSession.getEmployeeId() != null &&
               currentUserSession.getEmployeeId() == employeeId;
    }

    /**
     * NEW — Return the Employee model of the logged-in account.
     * Used by MainDashboard and PayStatement features.
     */
    public Employee getCurrentUser() {
        try {
            if (currentUserSession == null || currentUserSession.getEmployeeId() == null) {
                return null;
            }
            return employeeDAO.findById(currentUserSession.getEmployeeId());
        } catch (SQLException e) {
            System.err.println("Error loading current employee profile: " + e.getMessage());
            return null;
        }
    }

    public boolean hasPermission(String action) {
        if (currentUserSession == null) {
            return false;
        }

        UserRole role = currentUserSession.getUserRole();

        switch (action) {
            case "CREATE_EMPLOYEE":
            case "UPDATE_EMPLOYEE":
            case "DELETE_EMPLOYEE":
            case "UPDATE_SALARY":
            case "GENERATE_REPORTS":
            case "VIEW_ALL_EMPLOYEES":
                return role == UserRole.HR_ADMIN;

            case "VIEW_OWN_DATA":
            case "VIEW_PAY_STATEMENTS":
                return true;

            default:
                return false;
        }
    }

    public String getCurrentUserName() {
        if (currentUserSession != null) {
            return currentUserSession.getFullName();
        }
        return "Unknown User";
    }

    public String getCurrentUserRoleDisplayName() {
        if (currentUserSession != null) {
            return currentUserSession.getUserRole().getDisplayName();
        }
        return "No Role";
    }

    /**
     * Authentication result class
     */
    public static class AuthenticationResult {
        private final boolean success;
        private final String message;
        private final UserSession userSession;

        public AuthenticationResult(boolean success, String message, UserSession userSession) {
            this.success = success;
            this.message = message;
            this.userSession = userSession;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public UserSession getUserSession() { return userSession; }
    }

    /**
     * Session descriptor class
     */
    public static class UserSession {
        private final int userId;
        private final String username;
        private final UserRole userRole;
        private final Integer employeeId;
        private final String fullName;
        private final long loginTime;

        public UserSession(int userId, String username, UserRole userRole,
                           Integer employeeId, String fullName) {
            this.userId = userId;
            this.username = username;
            this.userRole = userRole;
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.loginTime = System.currentTimeMillis();
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public UserRole getUserRole() { return userRole; }
        public Integer getEmployeeId() { return employeeId; }
        public String getFullName() { return fullName; }
        public long getLoginTime() { return loginTime; }

        @Override
        public String toString() {
            return "UserSession{" +
                    "userId=" + userId +
                    ", username='" + username + '\'' +
                    ", userRole=" + userRole +
                    ", employeeId=" + employeeId +
                    ", fullName='" + fullName + '\'' +
                    '}';
        }
    }
}
