package com.employeemgmt.dao.base;

import java.util.List;

/**
 * Interface for DAOs that support search functionality
 * Provides contract for search operations across different entity types
 * 
 * @param <T> The entity type
 * @author Team 6
 */
public interface SearchableDAO<T> {
    
    /**
     * Search entities by name
     * @param name The name to search for (can be partial)
     * @return List of matching entities
     */
    List<T> searchByName(String name);
    
    /**
     * Search entities by ID
     * @param id The ID to search for
     * @return The entity with matching ID, or null if not found
     */
    T searchById(int id);
    
    /**
     * Search entities using multiple criteria
     * @param searchCriteria Map of field names and values to search for
     * @return List of matching entities
     */
    List<T> searchByCriteria(java.util.Map<String, Object> searchCriteria);
    
    /**
     * Get count of entities matching search criteria
     * @param searchCriteria Map of field names and values to search for
     * @return Number of matching entities
     */
    int getSearchCount(java.util.Map<String, Object> searchCriteria);
}