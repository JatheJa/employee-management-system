package com.employeemgmt.util;

/**
 * Enumeration for user roles in the employee management system
 * Defines different access levels for users
 * 
 * @author Team 6
 */
public enum UserRole {
    /**
     * HR Administrator - full CRUD access to all employee data
     * Can create, read, update, delete employee records
     * Can update salaries, generate reports
     * Has access to all system functionality
     */
    HR_ADMIN("HR_ADMIN", "HR Administrator"),
    
    /**
     * General Employee - read-only access to their own data
     * Can view their own employee information
     * Can view their pay statement history
     * Cannot modify any data
     */
    EMPLOYEE("EMPLOYEE", "Employee");
    
    private final String code;
    private final String displayName;
    
    /**
     * Constructor for UserRole enum
     * 
     * @param code Role code for database storage
     * @param displayName Human-readable role name
     */
    UserRole(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    /**
     * Get the role code (for database storage)
     * 
     * @return Role code string
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Get the display name (for UI display)
     * 
     * @return Display name string
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Check if this role has admin privileges
     * 
     * @return true if HR_ADMIN, false otherwise
     */
    public boolean isAdmin() {
        return this == HR_ADMIN;
    }
    
    /**
     * Check if this role can perform write operations
     * 
     * @return true if can write, false if read-only
     */
    public boolean canWrite() {
        return this == HR_ADMIN;
    }
    
    /**
     * Check if this role can read all employee data
     * 
     * @return true if can read all data, false if only own data
     */
    public boolean canReadAllData() {
        return this == HR_ADMIN;
    }
    
    /**
     * Get UserRole from code string
     * 
     * @param code Role code
     * @return UserRole enum value
     * @throws IllegalArgumentException if code is invalid
     */
    public static UserRole fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Role code cannot be null");
        }
        
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        
        throw new IllegalArgumentException("Invalid role code: " + code);
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}