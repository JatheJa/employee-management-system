package com.employeemgmt.service;

import com.employeemgmt.model.Employee;
import com.employeemgmt.model.Address;
import com.employeemgmt.controller.EmployeeController.ValidationResult;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.math.BigDecimal;

/**
 * Service class for validating employee and related data
 * Provides comprehensive validation rules for business logic
 * 
 * @author Team 6
 */
public class ValidationService {
    
    // Regular expression patterns for validation
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\+?1?[-.\\s]?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$");
    
    private static final Pattern SSN_PATTERN = 
        Pattern.compile("^\\d{3}-?\\d{2}-?\\d{4}$");
    
    private static final Pattern ZIP_PATTERN = 
        Pattern.compile("^\\d{5}(-\\d{4})?$");
    
    private static final Pattern NAME_PATTERN = 
        Pattern.compile("^[a-zA-Z\\s'-]{2,50}$");
    
    // Constants for validation rules
    private static final int MIN_AGE = 16;
    private static final int MAX_AGE = 100;
    private static final BigDecimal MIN_SALARY = new BigDecimal("15000");
    private static final BigDecimal MAX_SALARY = new BigDecimal("1000000");
    
    /**
     * Validate employee data for creation
     * 
     * @param employee Employee object to validate
     * @return ValidationResult indicating success/failure
     */
    public ValidationResult validateEmployeeForCreation(Employee employee) {
        if (employee == null) {
            return new ValidationResult(false, "Employee cannot be null");
        }
        
        // Validate required fields
        ValidationResult requiredFieldsResult = validateRequiredFields(employee);
        if (!requiredFieldsResult.isValid()) {
            return requiredFieldsResult;
        }
        
        // Validate business rules
        ValidationResult businessRulesResult = validateBusinessRules(employee);
        if (!businessRulesResult.isValid()) {
            return businessRulesResult;
        }
        
        return new ValidationResult(true, "Validation passed");
    }
    
    /**
     * Validate employee data for update
     * 
     * @param employee Employee object to validate
     * @return ValidationResult indicating success/failure
     */
    public ValidationResult validateEmployeeForUpdate(Employee employee) {
        if (employee == null) {
            return new ValidationResult(false, "Employee cannot be null");
        }
        
        if (employee.getEmpId() <= 0) {
            return new ValidationResult(false, "Employee ID is required for updates");
        }
        
        // Same validation as creation, but ID is required
        ValidationResult requiredFieldsResult = validateRequiredFields(employee);
        if (!requiredFieldsResult.isValid()) {
            return requiredFieldsResult;
        }
        
        ValidationResult businessRulesResult = validateBusinessRules(employee);
        if (!businessRulesResult.isValid()) {
            return businessRulesResult;
        }
        
        return new ValidationResult(true, "Validation passed");
    }
    
    /**
     * Validate address data
     * 
     * @param address Address object to validate
     * @return ValidationResult indicating success/failure
     */
    public ValidationResult validateAddress(Address address) {
        if (address == null) {
            return new ValidationResult(false, "Address cannot be null");
        }
        
        // Validate street address
        if (isNullOrEmpty(address.getStreet())) {
            return new ValidationResult(false, "Street address is required");
        }
        
        if (address.getStreet().length() > 100) {
            return new ValidationResult(false, "Street address cannot exceed 100 characters");
        }
        
        // Validate city
        if (address.getCityId() <= 0) {
            return new ValidationResult(false, "Valid city is required");
        }
        
        // Validate state
        if (address.getStateId() <= 0) {
            return new ValidationResult(false, "Valid state is required");
        }
        
        // Validate ZIP code
        if (!isNullOrEmpty(address.getZip()) && !ZIP_PATTERN.matcher(address.getZip()).matches()) {
            return new ValidationResult(false, "Invalid ZIP code format");
        }
        
        // Validate phone
        if (!isNullOrEmpty(address.getPhone()) && !PHONE_PATTERN.matcher(address.getPhone()).matches()) {
            return new ValidationResult(false, "Invalid phone number format");
        }
        
        // Validate date of birth
        if (address.getDateOfBirth() == null) {
            return new ValidationResult(false, "Date of birth is required");
        }
        
        ValidationResult dobResult = validateDateOfBirth(address.getDateOfBirth());
        if (!dobResult.isValid()) {
            return dobResult;
        }
        
        return new ValidationResult(true, "Address validation passed");
    }
    
    /**
     * Validate required fields for employee
     */
    private ValidationResult validateRequiredFields(Employee employee) {
        // First name
        if (isNullOrEmpty(employee.getFirstName())) {
            return new ValidationResult(false, "First name is required");
        }
        
        if (!NAME_PATTERN.matcher(employee.getFirstName()).matches()) {
            return new ValidationResult(false, "First name contains invalid characters or is too long");
        }
        
        // Last name
        if (isNullOrEmpty(employee.getLastName())) {
            return new ValidationResult(false, "Last name is required");
        }
        
        if (!NAME_PATTERN.matcher(employee.getLastName()).matches()) {
            return new ValidationResult(false, "Last name contains invalid characters or is too long");
        }
        
        // Email
        if (isNullOrEmpty(employee.getEmail())) {
            return new ValidationResult(false, "Email is required");
        }
        
        if (!EMAIL_PATTERN.matcher(employee.getEmail()).matches()) {
            return new ValidationResult(false, "Invalid email format");
        }
        
        // Employee number
        if (isNullOrEmpty(employee.getEmpNumber())) {
            return new ValidationResult(false, "Employee number is required");
        }
        
        if (employee.getEmpNumber().length() > 20) {
            return new ValidationResult(false, "Employee number cannot exceed 20 characters");
        }
        
        // SSN
        if (isNullOrEmpty(employee.getSsn())) {
            return new ValidationResult(false, "SSN is required");
        }
        
        if (!SSN_PATTERN.matcher(employee.getSsn()).matches()) {
            return new ValidationResult(false, "Invalid SSN format (use XXX-XX-XXXX)");
        }
        
        // Hire date
        if (employee.getHireDate() == null) {
            return new ValidationResult(false, "Hire date is required");
        }
        
        // Current salary
        if (employee.getCurrentSalary() == null) {
            return new ValidationResult(false, "Current salary is required");
        }
        
        return new ValidationResult(true, "Required fields validation passed");
    }
    
    /**
     * Validate business rules for employee
     */
    private ValidationResult validateBusinessRules(Employee employee) {
        // Validate hire date (cannot be in future, not too far in past)
        LocalDate hireDate = employee.getHireDate();
        LocalDate today = LocalDate.now();
        LocalDate maxPastDate = today.minusYears(50); // Reasonable limit
        
        if (hireDate.isAfter(today)) {
            return new ValidationResult(false, "Hire date cannot be in the future");
        }
        
        if (hireDate.isBefore(maxPastDate)) {
            return new ValidationResult(false, "Hire date cannot be more than 50 years ago");
        }
        
        // Validate salary range
        BigDecimal salary = employee.getCurrentSalary();
        if (salary.compareTo(MIN_SALARY) < 0) {
            return new ValidationResult(false, "Salary cannot be less than $" + MIN_SALARY);
        }
        
        if (salary.compareTo(MAX_SALARY) > 0) {
            return new ValidationResult(false, "Salary cannot exceed $" + MAX_SALARY);
        }
        
        // Validate email uniqueness constraints (business rule)
        if (employee.getEmail().length() > 100) {
            return new ValidationResult(false, "Email address cannot exceed 100 characters");
        }
        
        return new ValidationResult(true, "Business rules validation passed");
    }
    
    /**
     * Validate date of birth
     */
    private ValidationResult validateDateOfBirth(LocalDate dateOfBirth) {
        LocalDate today = LocalDate.now();
        
        if (dateOfBirth.isAfter(today)) {
            return new ValidationResult(false, "Date of birth cannot be in the future");
        }
        
        int age = Period.between(dateOfBirth, today).getYears();
        
        if (age < MIN_AGE) {
            return new ValidationResult(false, "Employee must be at least " + MIN_AGE + " years old");
        }
        
        if (age > MAX_AGE) {
            return new ValidationResult(false, "Employee age cannot exceed " + MAX_AGE + " years");
        }
        
        return new ValidationResult(true, "Date of birth validation passed");
    }
    
    /**
     * Validate salary update parameters
     * 
     * @param minSalary Minimum salary in range
     * @param maxSalary Maximum salary in range
     * @param percentage Percentage increase/decrease
     * @return ValidationResult indicating success/failure
     */
    public ValidationResult validateSalaryUpdate(BigDecimal minSalary, BigDecimal maxSalary, double percentage) {
        if (minSalary == null || maxSalary == null) {
            return new ValidationResult(false, "Salary range values cannot be null");
        }
        
        if (minSalary.compareTo(BigDecimal.ZERO) < 0 || maxSalary.compareTo(BigDecimal.ZERO) < 0) {
            return new ValidationResult(false, "Salary values cannot be negative");
        }
        
        if (minSalary.compareTo(maxSalary) > 0) {
            return new ValidationResult(false, "Minimum salary cannot be greater than maximum salary");
        }
        
        if (percentage < -50.0 || percentage > 100.0) {
            return new ValidationResult(false, "Percentage must be between -50% and 100%");
        }
        
        return new ValidationResult(true, "Salary update validation passed");
    }
    
    /**
     * Utility method to check if string is null or empty
     */
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}