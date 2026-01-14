package com.employeemgmt.model;

import java.time.LocalDateTime;

/**
 * Division entity class representing the division table in database
 * Contains organizational division information
 * 
 * @author Team 6
 */
public class Division {
    private int divId;
    private String divisionName;
    private String divisionCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for reporting and analytics
    private int employeeCount;
    
    /**
     * Default constructor
     */
    public Division() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with basic fields
     */
    public Division(String divisionName, String divisionCode) {
        this();
        this.divisionName = divisionName;
        this.divisionCode = divisionCode;
    }
    
    /**
     * Constructor with all fields
     */
    public Division(int divId, String divisionName, String divisionCode) {
        this(divisionName, divisionCode);
        this.divId = divId;
    }
    
    // Getters and Setters
    public int getDivId() {
        return divId;
    }

    public void setDivId(int divId) {
        this.divId = divId;
    }

    public String getDivisionName() {
        return divisionName;
    }

    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public String getDivisionCode() {
        return divisionCode;
    }

    public void setDivisionCode(String divisionCode) {
        this.divisionCode = divisionCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }
    
    // Utility methods
    
    /**
     * Get display name for UI purposes
     */
    public String getDisplayName() {
        if (divisionCode != null && !divisionCode.isEmpty()) {
            return String.format("%s (%s)", divisionName, divisionCode);
        }
        return divisionName != null ? divisionName : "Unknown Division";
    }
    
    /**
     * Validate division data
     */
    public boolean isValid() {
        return divisionName != null && !divisionName.trim().isEmpty() &&
               divisionName.length() <= 100 &&
               divisionCode != null && !divisionCode.trim().isEmpty() &&
               divisionCode.length() <= 20;
    }
    
    /**
     * Check if this is a new division (not yet saved to database)
     */
    public boolean isNew() {
        return divId <= 0;
    }
    
    /**
     * Create a copy of this division
     */
    public Division copy() {
        Division copy = new Division(divisionName, divisionCode);
        copy.divId = this.divId;
        copy.createdAt = this.createdAt;
        copy.updatedAt = this.updatedAt;
        copy.employeeCount = this.employeeCount;
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Division division = (Division) obj;
        
        // If both have IDs, compare by ID
        if (divId > 0 && division.divId > 0) {
            return divId == division.divId;
        }
        
        // Otherwise compare by name and code
        return divisionName != null && divisionName.equals(division.divisionName) &&
               divisionCode != null && divisionCode.equals(division.divisionCode);
    }

    @Override
    public int hashCode() {
        if (divId > 0) {
            return Integer.hashCode(divId);
        }
        
        int result = divisionName != null ? divisionName.hashCode() : 0;
        result = 31 * result + (divisionCode != null ? divisionCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("Division{id=%d, name='%s', code='%s', employees=%d}", 
                           divId, divisionName, divisionCode, employeeCount);
    }
}