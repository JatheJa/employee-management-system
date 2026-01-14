package com.employeemgmt.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity class representing the employees table in database
 * 
 * @author Team 6
 */
public class Employee {
    private int empId;
    private String empNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String ssn;
    private LocalDate hireDate;
    private BigDecimal currentSalary;
    private EmploymentStatus employmentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Associated objects
    private Address address;
    private Division division;
    private JobTitle jobTitle;

    // Default constructor
    public Employee() {
        this.employmentStatus = EmploymentStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with essential fields
    public Employee(String firstName, String lastName, String email, String ssn, LocalDate hireDate, BigDecimal currentSalary) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.ssn = ssn;
        this.hireDate = hireDate;
        this.currentSalary = currentSalary;
    }

    // Getters and Setters
    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public String getEmpNumber() {
        return empNumber;
    }

    public void setEmpNumber(String empNumber) {
        this.empNumber = empNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public BigDecimal getCurrentSalary() {
        return currentSalary;
    }

    public void setCurrentSalary(BigDecimal currentSalary) {
        this.currentSalary = currentSalary;
        this.updatedAt = LocalDateTime.now();
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
        this.updatedAt = LocalDateTime.now();
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public JobTitle getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(JobTitle jobTitle) {
        this.jobTitle = jobTitle;
    }

    // Utility methods
    public boolean isActive() {
        return employmentStatus == EmploymentStatus.ACTIVE;
    }

    public void updateSalaryByPercentage(double percentage) {
        if (currentSalary != null && percentage > 0) {
            BigDecimal multiplier = BigDecimal.valueOf(1 + (percentage / 100));
            this.currentSalary = currentSalary.multiply(multiplier).setScale(2, BigDecimal.ROUND_HALF_UP);
            this.updatedAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "Employee{" +
                "empId=" + empId +
                ", empNumber='" + empNumber + '\'' +
                ", name='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", hireDate=" + hireDate +
                ", currentSalary=" + currentSalary +
                ", status=" + employmentStatus +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Employee employee = (Employee) obj;
        return empId == employee.empId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(empId);
    }

    // Employment Status Enum
    public enum EmploymentStatus {
        ACTIVE, TERMINATED
    }
}