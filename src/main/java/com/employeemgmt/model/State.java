package com.employeemgmt.model;

/**
 * State entity class representing the state table in database
 * Used for normalized state data with unique codes
 * 
 * @author Team 6
 */
public class State {
    private int stateId;
    private String stateCode;    // 2-character state code (e.g., "GA", "FL")
    private String stateName;    // Full state name (e.g., "Georgia", "Florida")

    // Default constructor
    public State() {}

    // Constructor with essential fields
    public State(String stateCode, String stateName) {
        this.stateCode = stateCode;
        this.stateName = stateName;
    }

    // Constructor with all fields
    public State(int stateId, String stateCode, String stateName) {
        this.stateId = stateId;
        this.stateCode = stateCode;
        this.stateName = stateName;
    }

    // Getters and Setters
    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    // Utility methods
    public String getDisplayName() {
        return stateName + " (" + stateCode + ")";
    }

    public boolean isValidState() {
        return stateCode != null && stateCode.length() == 2 &&
               stateName != null && !stateName.trim().isEmpty();
    }

    /**
     * Check if state code is valid US state code format
     * @return true if code is 2 uppercase letters
     */
    public boolean isValidStateCode() {
        return stateCode != null && stateCode.matches("[A-Z]{2}");
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        State state = (State) obj;
        return stateId == state.stateId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(stateId);
    }

    // Common US states as static methods for convenience
    public static State[] getCommonStates() {
        return new State[] {
            new State("GA", "Georgia"),
            new State("FL", "Florida"),
            new State("AL", "Alabama"),
            new State("TN", "Tennessee"),
            new State("NC", "North Carolina"),
            new State("SC", "South Carolina")
        };
    }
}