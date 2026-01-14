package com.employeemgmt.model;

import java.time.LocalDate;

/**
 * EmployeeJobTitle entity class representing the employee_job_titles table
 * Manages the many-to-many relationship between employees and job titles
 * Includes historical tracking with start/end dates for promotions/role changes
 * 
 * @author Team 6
 */
public class EmployeeJobTitle {
    private int empId;
    private int jobTitleId;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrent;
    
    // Associated objects for display purposes
    private String employeeName;
    private String empNumber;
    private String jobTitle;

    // Default constructor
    public EmployeeJobTitle() {
        this.isCurrent = true;
    }

    // Constructor with essential fields
    public EmployeeJobTitle(int empId, int jobTitleId, LocalDate startDate) {
        this();
        this.empId = empId;
        this.jobTitleId = jobTitleId;
        this.startDate = startDate;
    }

    // Constructor with all fields
    public EmployeeJobTitle(int empId, int jobTitleId, LocalDate startDate, LocalDate endDate, boolean isCurrent) {
        this.empId = empId;
        this.jobTitleId = jobTitleId;
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

    public int getJobTitleId() {
        return jobTitleId;
    }

    public void setJobTitleId(int jobTitleId) {
        this.jobTitleId = jobTitleId;
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

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
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

    public boolean isActivePosition() {
        return isCurrent && endDate == null;
    }

    public boolean isHistoricalPosition() {
        return !isCurrent || endDate != null;
    }

    /**
     * End the current job title assignment (promotion or role change)
     * @param endDate Date when job title assignment ended
     */
    public void endPosition(LocalDate endDate) {
        this.endDate = endDate;
        this.isCurrent = false;
    }

    /**
     * Check if this position overlaps with another position period
     * @param otherStart Other position start date
     * @param otherEnd Other position end date (can be null for current)
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

    /**
     * Calculate tenure in this position
     * @return Number of days in this position
     */
    public long getTenureInDays() {
        if (startDate == null) return 0;
        
        LocalDate compareDate = endDate != null ? endDate : LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, compareDate);
    }

    /**
     * Get formatted tenure string
     * @return Formatted tenure (e.g., "2 years, 3 months")
     */
    public String getFormattedTenure() {
        if (startDate == null) return "Unknown";
        
        LocalDate compareDate = endDate != null ? endDate : LocalDate.now();
        
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, compareDate);
        long months = java.time.temporal.ChronoUnit.MONTHS.between(startDate, compareDate);
        long years = java.time.temporal.ChronoUnit.YEARS.between(startDate, compareDate);
        
        if (years > 0) {
            long remainingMonths = months - (years * 12);
            if (remainingMonths > 0) {
                return years + " year" + (years > 1 ? "s" : "") + 
                       ", " + remainingMonths + " month" + (remainingMonths > 1 ? "s" : "");
            } else {
                return years + " year" + (years > 1 ? "s" : "");
            }
        } else if (months > 0) {
            return months + " month" + (months > 1 ? "s" : "");
        } else {
            return days + " day" + (days > 1 ? "s" : "");
        }
    }

    @Override
    public String toString() {
        return "EmployeeJobTitle{" +
                "empId=" + empId +
                ", jobTitleId=" + jobTitleId +
                ", employeeName='" + employeeName + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", isCurrent=" + isCurrent +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EmployeeJobTitle that = (EmployeeJobTitle) obj;
        return empId == that.empId && 
               jobTitleId == that.jobTitleId && 
               startDate != null && startDate.equals(that.startDate);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(empId);
        result = 31 * result + Integer.hashCode(jobTitleId);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        return result;
    }
}