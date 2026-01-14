package com.employeemgmt.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report entity class for standardized report generation
 * Provides a common structure for all reports in the system
 * 
 * @author Team 6
 */
public class Report {
    private String reportId;
    private String reportName;
    private String reportDescription;
    private ReportType reportType;
    private LocalDate generatedDate;
    private String generatedBy;
    private Map<String, Object> parameters;
    private List<Map<String, Object>> data;
    private Map<String, Object> summary;
    private ReportStatus status;
    
    /**
     * Enumeration for different types of reports
     */
    public enum ReportType {
        EMPLOYEE_HIRING_DATE_RANGE("Employee Hiring Date Range", "Employees hired within specified date range"),
        ORGANIZATIONAL_SUMMARY("Organizational Summary", "Overview of organizational structure and statistics"),
        SALARY_ANALYSIS("Salary Analysis", "Salary distribution and compensation analysis"),
        TENURE_ANALYSIS("Tenure Analysis", "Employee tenure and retention analysis"),
        MONTHLY_PAY_BY_JOB_TITLE("Monthly Pay by Job Title", "Monthly payroll summary by job classification"),
        MONTHLY_PAY_BY_DIVISION("Monthly Pay by Division", "Monthly payroll summary by organizational division"),
        DASHBOARD_SUMMARY("Dashboard Summary", "Key metrics for management dashboard"),
        PAY_STATEMENT_HISTORY("Pay Statement History", "Individual employee pay statement history"),
        COMPREHENSIVE_PAYROLL("Comprehensive Payroll", "Combined payroll reports for specified period");
        
        private final String displayName;
        private final String description;
        
        ReportType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Enumeration for report generation status
     */
    public enum ReportStatus {
        PENDING("Pending"),
        GENERATING("Generating"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        EXPORTED("Exported");
        
        private final String displayName;
        
        ReportStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    /**
     * Default constructor
     */
    public Report() {
        this.parameters = new HashMap<>();
        this.summary = new HashMap<>();
        this.generatedDate = LocalDate.now();
        this.status = ReportStatus.PENDING;
    }
    
    /**
     * Constructor with basic information
     */
    public Report(String reportName, ReportType reportType, String generatedBy) {
        this();
        this.reportName = reportName;
        this.reportType = reportType;
        this.generatedBy = generatedBy;
        this.reportDescription = reportType.getDescription();
        this.reportId = generateReportId();
    }
    
    // Getters and Setters
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportDescription() {
        return reportDescription;
    }

    public void setReportDescription(String reportDescription) {
        this.reportDescription = reportDescription;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
        if (reportType != null && this.reportDescription == null) {
            this.reportDescription = reportType.getDescription();
        }
    }

    public LocalDate getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDate generatedDate) {
        this.generatedDate = generatedDate;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters != null ? parameters : new HashMap<>();
    }
    
    /**
     * Add a parameter to the report
     */
    public void addParameter(String key, Object value) {
        this.parameters.put(key, value);
    }
    
    /**
     * Get a parameter value
     */
    public Object getParameter(String key) {
        return this.parameters.get(key);
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public Map<String, Object> getSummary() {
        return summary;
    }

    public void setSummary(Map<String, Object> summary) {
        this.summary = summary != null ? summary : new HashMap<>();
    }
    
    /**
     * Add a summary statistic
     */
    public void addSummaryStatistic(String key, Object value) {
        this.summary.put(key, value);
    }
    
    /**
     * Get a summary statistic
     */
    public Object getSummaryStatistic(String key) {
        return this.summary.get(key);
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }
    
    // Utility methods
    
    /**
     * Get formatted generation date
     */
    public String getFormattedGeneratedDate() {
        if (generatedDate != null) {
            return generatedDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        }
        return "";
    }
    
    /**
     * Get formatted generation date and time
     */
    public String getFormattedGeneratedDateTime() {
        if (generatedDate != null) {
            return generatedDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' HH:mm"));
        }
        return "";
    }
    
    /**
     * Get data row count
     */
    public int getDataRowCount() {
        return data != null ? data.size() : 0;
    }
    
    /**
     * Check if report has data
     */
    public boolean hasData() {
        return data != null && !data.isEmpty();
    }
    
    /**
     * Check if report generation was successful
     */
    public boolean isSuccess() {
        return status == ReportStatus.COMPLETED || status == ReportStatus.EXPORTED;
    }
    
    /**
     * Get report title for display
     */
    public String getDisplayTitle() {
        if (reportType != null) {
            return reportType.getDisplayName() + 
                   (generatedDate != null ? " - " + getFormattedGeneratedDate() : "");
        }
        return reportName != null ? reportName : "Untitled Report";
    }
    
    /**
     * Generate a unique report ID
     */
    private String generateReportId() {
        String typePrefix = reportType != null ? 
            reportType.name().substring(0, Math.min(3, reportType.name().length())) : "RPT";
        
        String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timeString = String.valueOf(System.currentTimeMillis() % 10000);
        
        return String.format("%s-%s-%s", typePrefix, dateString, timeString);
    }
    
    /**
     * Create a copy of this report with updated status
     */
    public Report withStatus(ReportStatus newStatus) {
        Report copy = new Report();
        copy.reportId = this.reportId;
        copy.reportName = this.reportName;
        copy.reportDescription = this.reportDescription;
        copy.reportType = this.reportType;
        copy.generatedDate = this.generatedDate;
        copy.generatedBy = this.generatedBy;
        copy.parameters = new HashMap<>(this.parameters);
        copy.data = this.data;
        copy.summary = new HashMap<>(this.summary);
        copy.status = newStatus;
        return copy;
    }
    
    /**
     * Builder pattern for creating reports
     */
    public static class Builder {
        private Report report;
        
        public Builder(String reportName, ReportType reportType, String generatedBy) {
            report = new Report(reportName, reportType, generatedBy);
        }
        
        public Builder withDescription(String description) {
            report.setReportDescription(description);
            return this;
        }
        
        public Builder withParameter(String key, Object value) {
            report.addParameter(key, value);
            return this;
        }
        
        public Builder withData(List<Map<String, Object>> data) {
            report.setData(data);
            return this;
        }
        
        public Builder withSummary(Map<String, Object> summary) {
            report.setSummary(summary);
            return this;
        }
        
        public Builder withStatus(ReportStatus status) {
            report.setStatus(status);
            return this;
        }
        
        public Report build() {
            return report;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Report report = (Report) obj;
        return reportId != null ? reportId.equals(report.reportId) : report.reportId == null;
    }

    @Override
    public int hashCode() {
        return reportId != null ? reportId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("Report{id='%s', name='%s', type=%s, status=%s, rows=%d}", 
                           reportId, reportName, reportType, status, getDataRowCount());
    }
}