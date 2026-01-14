package com.employeemgmt.model;

/**
 * City entity class representing the city table in database
 * Used for normalized city data with state relationship
 * 
 * @author Team 6
 */
public class City {
    private int cityId;
    private String cityName;
    private int stateId;
    
    // Associated state information for display
    private String stateName;
    private String stateCode;

    // Default constructor
    public City() {}

    // Constructor with essential fields
    public City(String cityName, int stateId) {
        this.cityName = cityName;
        this.stateId = stateId;
    }

    // Constructor with all fields
    public City(int cityId, String cityName, int stateId) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.stateId = stateId;
    }

    // Getters and Setters
    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
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
    public String getFullCityState() {
        StringBuilder result = new StringBuilder(cityName);
        if (stateCode != null && !stateCode.isEmpty()) {
            result.append(", ").append(stateCode);
        } else if (stateName != null && !stateName.isEmpty()) {
            result.append(", ").append(stateName);
        }
        return result.toString();
    }

    public boolean isValidCity() {
        return cityName != null && !cityName.trim().isEmpty() && stateId > 0;
    }

    @Override
    public String toString() {
        return getFullCityState();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        City city = (City) obj;
        return cityId == city.cityId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(cityId);
    }
}