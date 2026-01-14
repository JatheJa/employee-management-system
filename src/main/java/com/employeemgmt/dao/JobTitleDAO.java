package com.employeemgmt.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.employeemgmt.dao.base.BaseDAO;
import com.employeemgmt.dao.base.SearchableDAO;
import com.employeemgmt.model.JobTitle;

/**
 * Data Access Object for JobTitle entities
 * Handles all database operations for job title data
 * 
 * Table (per SDD): job_titles(job_title_id, title_name, base_salary)
 * 
 * @author Team 6
 */
public class JobTitleDAO extends BaseDAO<JobTitle> implements SearchableDAO<JobTitle> {
    
    private static final Logger LOGGER = Logger.getLogger(JobTitleDAO.class.getName());
    
    private static final String TABLE_NAME = "job_titles";
    private static final String PRIMARY_KEY = "job_title_id";
    
    // SQL Queries
    private static final String INSERT_JOB_TITLE = 
        "INSERT INTO job_titles (title_name, base_salary) VALUES (?, ?)";
        
    private static final String SELECT_BY_ID = 
        "SELECT job_title_id, title_name, base_salary " +
        "FROM job_titles WHERE job_title_id = ?";
        
    private static final String SELECT_ALL = 
        "SELECT job_title_id, title_name, base_salary " +
        "FROM job_titles " +
        "ORDER BY title_name ASC";
        
    private static final String SELECT_BY_TITLE = 
        "SELECT job_title_id, title_name, base_salary " +
        "FROM job_titles WHERE title_name = ?";
        
    private static final String SEARCH_BY_TITLE_PATTERN = 
        "SELECT job_title_id, title_name, base_salary " +
        "FROM job_titles " +
        "WHERE title_name LIKE ? " +
        "ORDER BY title_name ASC";
        
    private static final String UPDATE_JOB_TITLE = 
        "UPDATE job_titles SET title_name = ?, base_salary = ? " +
        "WHERE job_title_id = ?";
        
    private static final String DELETE_JOB_TITLE = 
        "DELETE FROM job_titles WHERE job_title_id = ?";
        
    private static final String CHECK_JOB_TITLE_IN_USE = 
        "SELECT COUNT(*) FROM employee_job_titles " +
        "WHERE job_title_id = ? AND is_current = 1";
        
    private static final String SELECT_WITH_EMPLOYEE_COUNT = 
        "SELECT jt.job_title_id, jt.title_name, jt.base_salary, " +
        "       COUNT(ejt.empid) AS employee_count " +
        "FROM job_titles jt " +
        "LEFT JOIN employee_job_titles ejt " +
        "       ON jt.job_title_id = ejt.job_title_id AND ejt.is_current = 1 " +
        "LEFT JOIN employees e " +
        "       ON ejt.empid = e.empid AND e.employment_status = 'ACTIVE' " +
        "GROUP BY jt.job_title_id, jt.title_name, jt.base_salary " +
        "ORDER BY jt.title_name ASC";
        
    private static final String SELECT_ACTIVE_JOB_TITLES = 
        "SELECT DISTINCT jt.job_title_id, jt.title_name, jt.base_salary " +
        "FROM job_titles jt " +
        "INNER JOIN employee_job_titles ejt " +
        "        ON jt.job_title_id = ejt.job_title_id AND ejt.is_current = 1 " +
        "INNER JOIN employees e " +
        "        ON ejt.empid = e.empid AND e.employment_status = 'ACTIVE' " +
        "ORDER BY jt.title_name ASC";
        
    private static final String SELECT_MANAGEMENT_TITLES = 
        "SELECT job_title_id, title_name, base_salary " +
        "FROM job_titles " +
        "WHERE title_name LIKE '%manager%' " +
        "   OR title_name LIKE '%director%' " +
        "   OR title_name LIKE '%supervisor%' " +
        "   OR title_name LIKE '%lead%' " +
        "   OR title_name LIKE '%chief%' " +
        "   OR title_name LIKE '%head%' " +
        "   OR title_name LIKE '%vp%' " +
        "   OR title_name LIKE '%vice president%' " +
        "ORDER BY title_name ASC";

    // -------------------------------------------------------------------------
    // BaseDAO implementation
    // -------------------------------------------------------------------------

    /**
     * Implementation of BaseDAO.save(T): insert a new JobTitle.
     */
    @Override
    public JobTitle save(JobTitle jobTitle) throws SQLException {
        if (jobTitle == null) {
            throw new IllegalArgumentException("JobTitle cannot be null");
        }
        
        if (!jobTitle.isValid()) {
            throw new IllegalArgumentException("JobTitle data is not valid");
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(INSERT_JOB_TITLE, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, jobTitle.getTitleName());
            stmt.setBigDecimal(2, jobTitle.getBaseSalary());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating job title failed, no rows affected");
            }
            
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                jobTitle.setJobTitleId(rs.getInt(1));
            }
            
            LOGGER.info("Job title created successfully with ID: " + jobTitle.getJobTitleId());
            return jobTitle;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Convenience alias for legacy code that still calls create().
     * BaseDAO.create(T) will usually delegate to save(T), but we keep this explicit.
     */
    public JobTitle create(JobTitle jobTitle) throws SQLException {
        return save(jobTitle);
    }

    @Override
    public JobTitle findById(int jobTitleId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_ID);
            stmt.setInt(1, jobTitleId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToJobTitle(rs);
            }
            
            return null;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    @Override
    public List<JobTitle> findAll() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<JobTitle> jobTitles = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_ALL);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                jobTitles.add(mapResultSetToJobTitle(rs));
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return jobTitles;
    }
    
    /**
     * Find all job titles with employee counts.
     */
    public List<JobTitle> findAllWithEmployeeCount() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<JobTitle> jobTitles = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_WITH_EMPLOYEE_COUNT);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                JobTitle jobTitle = mapResultSetToJobTitle(rs);
                jobTitle.setEmployeeCount(rs.getInt("employee_count"));
                jobTitles.add(jobTitle);
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return jobTitles;
    }
    
    /**
     * Find only job titles that have active employees.
     */
    public List<JobTitle> findActiveJobTitles() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<JobTitle> jobTitles = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_ACTIVE_JOB_TITLES);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                jobTitles.add(mapResultSetToJobTitle(rs));
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return jobTitles;
    }
    
    /**
     * Find management job titles.
     */
    public List<JobTitle> findManagementTitles() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<JobTitle> jobTitles = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_MANAGEMENT_TITLES);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                jobTitles.add(mapResultSetToJobTitle(rs));
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return jobTitles;
    }
    
    /**
     * Find job title by exact title name.
     */
    public JobTitle findByTitle(String title) throws SQLException {
        if (title == null || title.trim().isEmpty()) {
            return null;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_TITLE);
            stmt.setString(1, title);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToJobTitle(rs);
            }
            
            return null;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Implementation of BaseDAO.update(T) – returns the updated JobTitle.
     */
    @Override
    public JobTitle update(JobTitle jobTitle) throws SQLException {
        if (jobTitle == null) {
            throw new IllegalArgumentException("JobTitle cannot be null");
        }
        
        if (jobTitle.getJobTitleId() <= 0) {
            throw new IllegalArgumentException("Job Title ID is required for update");
        }
        
        if (!jobTitle.isValid()) {
            throw new IllegalArgumentException("JobTitle data is not valid");
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(UPDATE_JOB_TITLE);
            
            stmt.setString(1, jobTitle.getTitleName());
            stmt.setBigDecimal(2, jobTitle.getBaseSalary());
            stmt.setInt(3, jobTitle.getJobTitleId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException(
                    "Updating job title failed, no rows affected. Job title may not exist: " 
                    + jobTitle.getJobTitleId()
                );
            }
            
            LOGGER.info("Job title updated successfully: " + jobTitle.getJobTitleId());
            return jobTitle;
            
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * Optional compatibility helper if any code still expects a boolean update result.
     */
    public boolean updateJobTitle(JobTitle jobTitle) throws SQLException {
        return update(jobTitle) != null;
    }

    /**
     * Implementation of BaseDAO.deleteById(int).
     */
    @Override
    public boolean deleteById(int jobTitleId) throws SQLException {
        // First check if job title is in use
        if (isJobTitleInUse(jobTitleId)) {
            throw new SQLException("Cannot delete job title: it is currently assigned to active employees");
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(DELETE_JOB_TITLE);
            stmt.setInt(1, jobTitleId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.info("Job title deleted successfully: " + jobTitleId);
                return true;
            }
            
            LOGGER.info("No job title record found to delete for ID: " + jobTitleId);
            return false;
            
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    // Note: BaseDAO.delete(int) already delegates to deleteById(int),
    // so callers using delete(id) will still work without an explicit override.

    /**
     * Check if job title is currently assigned to any active employees.
     */
    public boolean isJobTitleInUse(int jobTitleId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(CHECK_JOB_TITLE_IN_USE);
            stmt.setInt(1, jobTitleId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getPrimaryKeyColumn() {
        return PRIMARY_KEY;
    }

    // -------------------------------------------------------------------------
    // SearchableDAO implementation
    // -------------------------------------------------------------------------

    @Override
    public List<JobTitle> searchByName(String titlePattern) {
        if (titlePattern == null || titlePattern.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<JobTitle> jobTitles = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SEARCH_BY_TITLE_PATTERN);
            String searchPattern = "%" + titlePattern + "%";
            stmt.setString(1, searchPattern);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                jobTitles.add(mapResultSetToJobTitle(rs));
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching job titles by title pattern: " + titlePattern, e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return jobTitles;
    }

    @Override
    public JobTitle searchById(int id) {
        try {
            return findById(id);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching job title by ID: " + id, e);
            return null;
        }
    }

    @Override
    public List<JobTitle> searchByCriteria(Map<String, Object> searchCriteria) {
        List<JobTitle> results = new ArrayList<>();
        
        if (searchCriteria == null || searchCriteria.isEmpty()) {
            return results;
        }
        
        try {
            // Handle different search criteria
            String title = (String) searchCriteria.get("title");
            Boolean activeOnly = (Boolean) searchCriteria.get("activeOnly");
            Boolean managementOnly = (Boolean) searchCriteria.get("managementOnly");
            
            if (title != null && !title.trim().isEmpty()) {
                results = searchByName(title);
            } else if (managementOnly != null && managementOnly) {
                results = findManagementTitles();
            } else if (activeOnly != null && activeOnly) {
                results = findActiveJobTitles();
            } else {
                results = findAll();
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching job titles by criteria", e);
        }
        
        return results;
    }

    @Override
    public int getSearchCount(Map<String, Object> searchCriteria) {
        List<JobTitle> results = searchByCriteria(searchCriteria);
        return results.size();
    }
    
    /**
     * Get job titles categorized by level (Entry, Mid, Senior, Management)
     * 
     * @return Map with categories as keys and lists of job titles as values
     * @throws SQLException if database operation fails
     */
    public Map<String, List<JobTitle>> getJobTitlesByLevel() throws SQLException {
        Map<String, List<JobTitle>> categorized = new HashMap<>();
        categorized.put("Management", new ArrayList<>());
        categorized.put("Senior", new ArrayList<>());
        categorized.put("Mid", new ArrayList<>());
        categorized.put("Entry", new ArrayList<>());
        
        List<JobTitle> allTitles = findAll();
        
        for (JobTitle title : allTitles) {
            if (title.isManagementRole()) {
                categorized.get("Management").add(title);
            } else if (title.isSeniorRole()) {
                categorized.get("Senior").add(title);
            } else {
                String lowerTitle = title.getTitleName() != null
                        ? title.getTitleName().toLowerCase()
                        : "";
                if (lowerTitle.contains("junior") || lowerTitle.contains("entry") || 
                    lowerTitle.contains("associate") || lowerTitle.contains("trainee")) {
                    categorized.get("Entry").add(title);
                } else {
                    categorized.get("Mid").add(title);
                }
            }
        }
        
        return categorized;
    }
    
    /**
     * Map ResultSet row to JobTitle object.
     */
    private JobTitle mapResultSetToJobTitle(ResultSet rs) throws SQLException {
        JobTitle jobTitle = new JobTitle();
        
        jobTitle.setJobTitleId(rs.getInt("job_title_id"));
        jobTitle.setTitleName(rs.getString("title_name"));
        jobTitle.setBaseSalary(rs.getBigDecimal("base_salary"));
        
        return jobTitle;
    }
}
