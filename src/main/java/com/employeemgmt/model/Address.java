package com.employeemgmt.model;

import java.time.LocalDate;

/**
 * Address entity class representing the address table in database
 * Contains employee address and demographic information
 * 
 * @author Team 6
 */
public class Address {
    private int empId;              // Foreign key to employees table
    private String street;
    private int cityId;             // Foreign key to city table
    private int stateId;            // Foreign key to state table
    private String zip;
    private Gender gender;
    private String race;
    private LocalDate dateOfBirth;
    private String phone;
    
    // Associated objects for display purposes
    private String cityName;
    private String stateName;
    private String stateCode;

    // Default constructor
    public Address() {}

    // Constructor with essential fields
    public Address(int empId, String street, int cityId, int stateId, String zip, 
                   Gender gender, String race, LocalDate dateOfBirth, String phone) {
        this.empId = empId;
        this.street = street;
        this.cityId = cityId;
        this.stateId = stateId;
        this.zip = zip;
        this.gender = gender;
        this.race = race;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
    }

    // Getters and Setters
    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Display-related getters and setters
    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    // Utility methods
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (street != null && !street.isEmpty()) {
            address.append(street);
        }
        if (cityName != null && !cityName.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(cityName);
        }
        if (stateCode != null && !stateCode.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(stateCode);
        }
        if (zip != null && !zip.isEmpty()) {
            if (address.length() > 0) address.append(" ");
            address.append(zip);
        }
        return address.toString();
    }

    public int getAge() {
        if (dateOfBirth != null) {
            return LocalDate.now().getYear() - dateOfBirth.getYear();
        }
        return 0;
    }

    public boolean isValidZip() {
        return zip != null && zip.matches("\\d{5}(-\\d{4})?");
    }

    public boolean isValidPhone() {
        return phone != null && phone.matches("\\(\\d{3}\\) \\d{3}-\\d{4}|\\d{3}-\\d{3}-\\d{4}|\\d{10}");
    }

    @Override
    public String toString() {
        return "Address{" +
                "empId=" + empId +
                ", street='" + street + '\'' +
                ", city='" + cityName + '\'' +
                ", state='" + stateCode + '\'' +
                ", zip='" + zip + '\'' +
                ", gender=" + gender +
                ", dateOfBirth=" + dateOfBirth +
                ", phone='" + phone + '\'' +
                '}';
    }

    // Gender Enum
    public enum Gender {
        M("Male"),
        F("Female"),
        OTHER("Other"),
        PREFER_NOT_TO_SAY("Prefer not to say");

        private final String displayName;

        Gender(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}