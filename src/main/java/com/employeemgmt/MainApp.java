package com.employeemgmt;

import com.employeemgmt.controller.AuthenticationController;
import com.employeemgmt.dao.base.DatabaseConnection;
import com.employeemgmt.view.auth.LoginView;
import com.employeemgmt.view.dashboard.MainDashboard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Main application entry point for Employee Management System
 * Handles application initialization, login flow, and main dashboard
 * 
 * @author Team 6
 */
public class MainApp extends Application {
    
    private AuthenticationController authController;
    private LoginView loginView;
    private MainDashboard mainDashboard;
    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try {
            // Initialize application
            initializeApplication();
            
            // Show login window
            showLogin();
            
        } catch (Exception e) {
            showError("Application Startup Error", 
                      "Failed to initialize the application: " + e.getMessage());
            Platform.exit();
        }
    }
    
    /**
     * Initialize application components and dependencies
     */
    private void initializeApplication() {
        System.out.println("Initializing Employee Management System...");
        
        // Test database connection
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        if (!dbConnection.testConnection()) {
            throw new RuntimeException("Could not connect to database. Please check your database configuration.");
        }
        
        System.out.println("Database connection successful");
        System.out.println("Database URL: " + dbConnection.getDatabaseUrl());
        
        // Initialize authentication controller
        authController = new AuthenticationController();
        
        System.out.println("Application initialization complete");
    }
    
    /**
     * Show login window
     */
    private void showLogin() {
        loginView = new LoginView(primaryStage, authController);
        
        // Set callback for successful login
        loginView.setOnLoginSuccessCallback(this::showMainDashboard);
        
        // Show login window
        loginView.show();
        
        System.out.println("Login window displayed");
    }
    
    /**
     * Show main dashboard after successful login
     */
    private void showMainDashboard() {
        try {
            // Hide login window
            loginView.hide();
            
            // Create and show main dashboard
            mainDashboard = new MainDashboard(primaryStage, authController);
            
            // Set logout callback
            mainDashboard.setOnLogoutCallback(this::handleLogout);
            
            // Show main dashboard
            mainDashboard.show();
            
            System.out.println("Main dashboard displayed for user: " + 
                               authController.getCurrentUserName());
            
        } catch (Exception e) {
            showError("Dashboard Error", 
                      "Failed to load main dashboard: " + e.getMessage());
            handleLogout(); // Return to login
        }
    }
    
    /**
     * Handle user logout
     */
    private void handleLogout() {
        try {
            // Logout user
            if (authController != null) {
                System.out.println("Logging out user: " + authController.getCurrentUserName());
                authController.logout();
            }
            
            // Hide main dashboard if open
            if (mainDashboard != null) {
                mainDashboard.hide();
                mainDashboard = null;
            }
            
            // Clear and show login window
            if (loginView != null) {
                loginView.clearForm();
                loginView.show();
            } else {
                showLogin(); // Recreate if necessary
            }
            
            System.out.println("User logged out successfully");
            
        } catch (Exception e) {
            showError("Logout Error", "Error during logout: " + e.getMessage());
        }
    }
    
    /**
     * Show error dialog
     * @param title Dialog title
     * @param message Error message
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Make dialog stay on top
        alert.initOwner(primaryStage);
        
        alert.showAndWait();
    }
    
    /**
     * Show confirmation dialog
     * @param title Dialog title
     * @param message Confirmation message
     * @return true if user confirmed, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    /**
     * Show information dialog
     * @param title Dialog title
     * @param message Information message
     */
    public static void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void stop() {
        try {
            // Logout user if still logged in
            if (authController != null && authController.isUserLoggedIn()) {
                authController.logout();
            }
            
            // Close database connections
            DatabaseConnection.getInstance().closeDataSource();
            
            System.out.println("Application shutdown complete");
            
        } catch (Exception e) {
            System.err.println("Error during application shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Main method to launch JavaFX application
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set system properties for JavaFX before launch (optional tuning)
        System.setProperty("javafx.animation.fullspeed", "true");
        System.setProperty("prism.lcdtext", "false");
        
        // Print startup information
        System.out.println("========================================");
        System.out.println("Employee Management System");
        System.out.println("Version: 1.0.0");
        System.out.println("Team 6 - CSc3350 Fall 2025");
        System.out.println("========================================");
        
        try {
            // Launch JavaFX application ONCE
            launch(args);
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
