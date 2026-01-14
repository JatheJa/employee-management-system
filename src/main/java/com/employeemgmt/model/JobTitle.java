package com.employeemgmt.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JobTitle entity class representing the job_titles table in database
 * Contains job classification information
 *
 * SDD alignment:
 *  - jobTitleId
 *  - titleName
 *  - baseSalary
 *
 * Existing code compatibility:
 *  - Keeps jobTitle field and getJobTitle()/setJobTitle() as aliases for titleName
 *
 * @author Team 6
 */
public class JobTitle {

    private int jobTitleId;

    /**
     * Canonical title field for persistence / business logic.
     * For SDD compatibility this is exposed as "titleName".
     */
    private String jobTitle;

    /**
     * SDD-required field: base salary associated with this job title.
     */
    private BigDecimal baseSalary;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields for reporting and analytics
    private int employeeCount;

    /**
     * Default constructor
     */
    public JobTitle() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.baseSalary = null; // can be set explicitly by callers
    }

    /**
     * Constructor with job title (titleName in SDD terms)
     */
    public JobTitle(String jobTitle) {
        this();
        this.jobTitle = jobTitle;
    }

    /**
     * Constructor with id and title
     */
    public JobTitle(int jobTitleId, String jobTitle) {
        this(jobTitle);
        this.jobTitleId = jobTitleId;
    }

    /**
     * Constructor with SDD fields.
     */
    public JobTitle(int jobTitleId, String titleName, BigDecimal baseSalary) {
        this(jobTitleId, titleName);
        this.baseSalary = baseSalary;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public int getJobTitleId() {
        return jobTitleId;
    }

    public void setJobTitleId(int jobTitleId) {
        this.jobTitleId = jobTitleId;
    }

    /**
     * Existing name used throughout the codebase.
     * Semantically equivalent to SDD's "titleName".
     */
    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     * SDD-aligned getter for titleName (alias for jobTitle).
     */
    public String getTitleName() {
        return jobTitle;
    }

    /**
     * SDD-aligned setter for titleName (alias for jobTitle).
     */
    public void setTitleName(String titleName) {
        this.jobTitle = titleName;
    }

    /**
     * SDD-required base salary for this job title.
     */
    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
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

    // -------------------------------------------------------------------------
    // Utility methods
    // -------------------------------------------------------------------------

    /**
     * Get display name for UI purposes (same as title name)
     */
    public String getDisplayName() {
        return getTitleName() != null ? getTitleName() : "Unknown Job Title";
    }

    /**
     * Validate job title data based on SDD expectations.
     */
    public boolean isValid() {
        String title = getTitleName();
        return title != null && !title.trim().isEmpty() && title.length() <= 100;
    }

    /**
     * Check if this is a new job title (not yet saved to database)
     */
    public boolean isNew() {
        return jobTitleId <= 0;
    }

    /**
     * Create a copy of this job title
     */
    public JobTitle copy() {
        JobTitle copy = new JobTitle(getTitleName());
        copy.jobTitleId = this.jobTitleId;
        copy.createdAt = this.createdAt;
        copy.updatedAt = this.updatedAt;
        copy.employeeCount = this.employeeCount;
        copy.baseSalary = this.baseSalary;
        return copy;
    }

    /**
     * Get formatted job title for specific contexts
     */
    public String getFormattedTitle() {
        String title = getTitleName();
        if (title == null) return "Unknown";

        // Capitalize first letter of each word
        String[] words = title.toLowerCase().split("\\s+");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) formatted.append(" ");
            if (words[i].length() > 0) {
                formatted.append(Character.toUpperCase(words[i].charAt(0)));
                if (words[i].length() > 1) {
                    formatted.append(words[i].substring(1));
                }
            }
        }

        return formatted.toString();
    }

    /**
     * Check if this is a management position
     */
    public boolean isManagementRole() {
        String title = getTitleName();
        if (title == null) return false;

        String lowerTitle = title.toLowerCase();
        return lowerTitle.contains("manager") ||
               lowerTitle.contains("director") ||
               lowerTitle.contains("supervisor") ||
               lowerTitle.contains("lead") ||
               lowerTitle.contains("chief") ||
               lowerTitle.contains("head") ||
               lowerTitle.contains("vp") ||
               lowerTitle.contains("vice president");
    }

    /**
     * Check if this is a senior level position
     */
    public boolean isSeniorRole() {
        String title = getTitleName();
        if (title == null) return false;

        String lowerTitle = title.toLowerCase();
        return lowerTitle.contains("senior") ||
               lowerTitle.contains("principal") ||
               lowerTitle.contains("architect") ||
               lowerTitle.contains("specialist") ||
               isManagementRole();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        JobTitle jobTitleObj = (JobTitle) obj;

        // If both have IDs, compare by ID
        if (jobTitleId > 0 && jobTitleObj.jobTitleId > 0) {
            return jobTitleId == jobTitleObj.jobTitleId;
        }

        // Otherwise compare by title name
        String thisTitle = getTitleName();
        String otherTitle = jobTitleObj.getTitleName();
        return thisTitle != null && thisTitle.equals(otherTitle);
    }

    @Override
    public int hashCode() {
        if (jobTitleId > 0) {
            return Integer.hashCode(jobTitleId);
        }

        String title = getTitleName();
        return title != null ? title.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format(
            "JobTitle{id=%d, title='%s', baseSalary=%s, employees=%d}",
            jobTitleId,
            getTitleName(),
            baseSalary,
            employeeCount
        );
    }
}
