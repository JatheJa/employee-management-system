package com.employeemgmt.model;

import java.time.LocalDate;

/**
 * EmployeeDivision entity class representing the employee_division table
 * Manages the many-to-many relationship between employees and divisions
 * Includes historical tracking with start/end dates
 * 
 * @author Team 6
 */
public class EmployeeDivision {
    private int empId;
    private int divId;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrent;
    
    // Associated objects for display purposes
    private String employeeName;
    private String empNumber;
    private String divisionName;
    private String divisionCode;

    // Default constructor
    public EmployeeDivision() {
        this.isCurrent = true;
    }

    // Constructor with essential fields
    public EmployeeDivision(int empId, int divId, LocalDate startDate) {
        this();
        this.empId = empId;
        this.divId = divId;
        this.startDate = startDate;
    }

    // Constructor with all fields
    public EmployeeDivision(int empId, int divId, LocalDate startDate, LocalDate endDate, boolean isCurrent) {
        this.empId = empId;
        this.divId = divId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrent = isCurrent;
    }

    // Getters and Setters
    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public int getDivId() {
        return divId;
    }

    public void setDivId(int divId) {
        this.divId = divId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        // If end date is set, it's no longer current
        if (endDate != null) {
            this.isCurrent = false;
        }
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    // Display properties
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmpNumber() {
        return empNumber;
    }

    public void setEmpNumber(String empNumber) {
        this.empNumber = empNumber;
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

    // Utility methods
    public String getDurationDescription() {
        if (startDate == null) return "Unknown";
        
        StringBuilder duration = new StringBuilder();
        duration.append("Since ").append(startDate);
        
        if (endDate != null) {
            duration.append(" to ").append(endDate);
        } else if (isCurrent) {
            duration.append(" (Current)");
        }
        
        return duration.toString();
    }

    public boolean isActiveAssignment() {
        return isCurrent && endDate == null;
    }

    public boolean isHistoricalAssignment() {
        return !isCurrent || endDate != null;
    }

    /**
     * End the current division assignment
     * @param endDate Date when assignment ended
     */
    public void endAssignment(LocalDate endDate) {
        this.endDate = endDate;
        this.isCurrent = false;
    }

    /**
     * Check if this assignment overlaps with another assignment period
     * @param otherStart Other assignment start date
     * @param otherEnd Other assignment end date (can be null for current)
     * @return true if there's an overlap
     */
    public boolean hasDateOverlap(LocalDate otherStart, LocalDate otherEnd) {
        if (startDate == null || otherStart == null) {
            return false;
        }
        
        LocalDate thisEnd = endDate != null ? endDate : LocalDate.now();
        LocalDate compareEnd = otherEnd != null ? otherEnd : LocalDate.now();
        
        return !startDate.isAfter(compareEnd) && !thisEnd.isBefore(otherStart);
    }

    @Override
    public String toString() {
        return "EmployeeDivision{" +
                "empId=" + empId +
                ", divId=" + divId +
                ", employeeName='" + employeeName + '\'' +
                ", divisionName='" + divisionName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", isCurrent=" + isCurrent +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EmployeeDivision that = (EmployeeDivision) obj;
        return empId == that.empId && 
               divId == that.divId && 
               startDate != null && startDate.equals(that.startDate);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(empId);
        result = 31 * result + Integer.hashCode(divId);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        return result;
    }
}