package com.employeemgmt.dao.base;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Database connection manager using connection pooling
 * Implements Singleton pattern for efficient resource management
 * 
 * @author Team 6
 */
public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private HikariDataSource dataSource;
    private Properties dbProperties;
    
    // Database configuration constants
    private static final String PROPERTIES_FILE = "database.properties";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/employee_management_system";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";
    
    // Connection pool settings
    private static final int MINIMUM_IDLE = 5;
    private static final int MAXIMUM_POOL_SIZE = 20;
    private static final long CONNECTION_TIMEOUT = 30000; // 30 seconds
    private static final long IDLE_TIMEOUT = 600000; // 10 minutes
    
    /**
     * Private constructor for Singleton pattern
     */
    private DatabaseConnection() {
        loadProperties();
        initializeDataSource();
    }
    
    /**
     * Get the singleton instance of DatabaseConnection
     * @return DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Load database properties from configuration file
     */
    private void loadProperties() {
        dbProperties = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input != null) {
                dbProperties.load(input);
                System.out.println("Database properties loaded successfully");
            } else {
                System.out.println("Properties file not found, using default values");
                setDefaultProperties();
            }
        } catch (IOException e) {
            System.err.println("Error loading database properties: " + e.getMessage());
            setDefaultProperties();
        }
    }
    
    /**
     * Set default database properties if configuration file is not found
     */
    private void setDefaultProperties() {
        dbProperties.setProperty("db.url", DEFAULT_URL);
        dbProperties.setProperty("db.username", DEFAULT_USERNAME);
        dbProperties.setProperty("db.password", DEFAULT_PASSWORD);
        dbProperties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }
    
    /**
     * Initialize HikariCP connection pool
     */
    private void initializeDataSource() {
        try {
            // Load MySQL driver
            Class.forName(dbProperties.getProperty("db.driver"));
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbProperties.getProperty("db.url"));
            config.setUsername(dbProperties.getProperty("db.username"));
            config.setPassword(dbProperties.getProperty("db.password"));
            
            // Connection pool configuration
            config.setMinimumIdle(MINIMUM_IDLE);
            config.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
            config.setConnectionTimeout(CONNECTION_TIMEOUT);
            config.setIdleTimeout(IDLE_TIMEOUT);
            config.setPoolName("EmployeeManagementPool");
            
            // MySQL specific settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            dataSource = new HikariDataSource(config);
            
            System.out.println("Database connection pool initialized successfully");
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found: " + e.getMessage());
            throw new RuntimeException("Failed to load database driver", e);
        } catch (Exception e) {
            System.err.println("Failed to initialize database connection pool: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Get a connection from the pool
     * @return Database connection
     * @throws SQLException if connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true); // Default to auto-commit
        return connection;
    }
    
    /**
     * Get a connection with specified auto-commit mode
     * @param autoCommit auto-commit mode
     * @return Database connection
     * @throws SQLException if connection cannot be established
     */
    public Connection getConnection(boolean autoCommit) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(autoCommit);
        return connection;
    }
    
    /**
     * Test database connectivity
     * @return true if connection is successful
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get database URL
     * @return database URL
     */
    public String getDatabaseUrl() {
        return dbProperties.getProperty("db.url");
    }
    
    /**
     * Get connection pool statistics
     * @return connection pool info as string
     */
    public String getPoolStats() {
        if (dataSource != null) {
            return String.format("Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getIdleConnections(),
                    dataSource.getHikariPoolMXBean().getTotalConnections(),
                    dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        }
        return "DataSource not initialized";
    }
    
    /**
     * Close the data source and clean up resources
     */
    public void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Database connection pool closed");
        }
    }
    
    /**
     * Shutdown hook to ensure proper cleanup
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (instance != null) {
                instance.closeDataSource();
            }
        }));
    }
}