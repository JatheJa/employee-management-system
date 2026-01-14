package com.employeemgmt.view.auth;

import com.employeemgmt.controller.AuthenticationController;
import com.employeemgmt.controller.AuthenticationController.AuthenticationResult;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Login view for user authentication
 * Provides GUI interface for username/password login
 * 
 * @author Team 6
 */
public class LoginView {
    
    private Stage primaryStage;
    private AuthenticationController authController;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label messageLabel;
    private Button loginButton;
    private Button exitButton;
    
    // Callback for successful login
    private Runnable onLoginSuccessCallback;
    
    public LoginView(Stage primaryStage, AuthenticationController authController) {
        this.primaryStage = primaryStage;
        this.authController = authController;
        initializeComponents();
    }
    
    /**
     * Set callback to execute when login is successful
     * @param callback Callback to execute
     */
    public void setOnLoginSuccessCallback(Runnable callback) {
        this.onLoginSuccessCallback = callback;
    }
    
    /**
     * Initialize JavaFX components
     */
    private void initializeComponents() {
        // Create main container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setStyle("-fx-background-color: #f8f9fa;");
        
        // Title
        Label titleLabel = new Label("Employee Management System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        // Subtitle
        Label subtitleLabel = new Label("Please login to continue");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        // Login form container
        VBox formContainer = createLoginForm();
        
        // Add all components to main container
        mainContainer.getChildren().addAll(titleLabel, subtitleLabel, formContainer);
        
        // Create scene
        Scene scene = new Scene(mainContainer, 400, 350);
        
        // Set stage properties
        primaryStage.setTitle("Employee Management System - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        
        // Center stage on screen
        primaryStage.centerOnScreen();
        
        // Set close operation
        primaryStage.setOnCloseRequest(e -> Platform.exit());
        
        // Focus on username field when shown
        Platform.runLater(() -> usernameField.requestFocus());
    }
    
    /**
     * Create the login form
     * @return VBox containing login form elements
     */
    private VBox createLoginForm() {
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(30));
        formContainer.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        formContainer.setMaxWidth(300);
        
        // Username field
        VBox usernameContainer = new VBox(5);
        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(35);
        usernameField.setStyle("-fx-font-size: 14;");
        
        usernameContainer.getChildren().addAll(usernameLabel, usernameField);
        
        // Password field
        VBox passwordContainer = new VBox(5);
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(35);
        passwordField.setStyle("-fx-font-size: 14;");
        
        passwordContainer.getChildren().addAll(passwordLabel, passwordField);
        
        // Message label for feedback
        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(280);
        messageLabel.setAlignment(Pos.CENTER);
        
        // Buttons container
        HBox buttonContainer = createButtonContainer();
        
        // Add enter key support for login
        passwordField.setOnAction(e -> performLogin());
        usernameField.setOnAction(e -> performLogin());
        
        formContainer.getChildren().addAll(
            usernameContainer,
            passwordContainer,
            messageLabel,
            buttonContainer
        );
        
        return formContainer;
    }
    
    /**
     * Create button container with login and exit buttons
     * @return HBox containing buttons
     */
    private HBox createButtonContainer() {
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);
        
        // Login button
        loginButton = new Button("Login");
        loginButton.setPrefWidth(100);
        loginButton.setPrefHeight(35);
        loginButton.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5;"
        );
        loginButton.setOnAction(e -> performLogin());
        
        // Hover effect for login button
        loginButton.setOnMouseEntered(e -> 
            loginButton.setStyle(
                "-fx-background-color: #2980b9; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5;"
            )
        );
        loginButton.setOnMouseExited(e -> 
            loginButton.setStyle(
                "-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5;"
            )
        );
        
        // Exit button
        exitButton = new Button("Exit");
        exitButton.setPrefWidth(100);
        exitButton.setPrefHeight(35);
        exitButton.setStyle(
            "-fx-background-color: #95a5a6; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5;"
        );
        exitButton.setOnAction(e -> Platform.exit());
        
        // Hover effect for exit button
        exitButton.setOnMouseEntered(e -> 
            exitButton.setStyle(
                "-fx-background-color: #7f8c8d; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5;"
            )
        );
        exitButton.setOnMouseExited(e -> 
            exitButton.setStyle(
                "-fx-background-color: #95a5a6; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5;"
            )
        );
        
        buttonContainer.getChildren().addAll(loginButton, exitButton);
        
        return buttonContainer;
    }
    
    /**
     * Perform login authentication
     */
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Basic validation
        if (username.isEmpty()) {
            showMessage("Please enter a username.", false);
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showMessage("Please enter a password.", false);
            passwordField.requestFocus();
            return;
        }
        
        // Disable login button during authentication
        loginButton.setDisable(true);
        loginButton.setText("Logging in...");
        
        // Clear previous messages
        showMessage("", true);
        
        // Perform authentication in background thread to avoid blocking UI
        Thread authThread = new Thread(() -> {
            try {
                AuthenticationResult result = authController.authenticateUser(username, password);
                
                Platform.runLater(() -> {
                    // Re-enable login button
                    loginButton.setDisable(false);
                    loginButton.setText("Login");
                    
                    if (result.isSuccess()) {
                        showMessage("Login successful! Welcome, " + 
                                  authController.getCurrentUserName(), true);
                        
                        // Execute success callback if set
                        if (onLoginSuccessCallback != null) {
                            // Add small delay to show success message
                            Timeline timeline = new Timeline(
                                new KeyFrame(Duration.millis(1000), e -> onLoginSuccessCallback.run())
                            );
                            timeline.play();
                        }
                        
                    } else {
                        showMessage(result.getMessage(), false);
                        passwordField.clear();
                        passwordField.requestFocus();
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    loginButton.setText("Login");
                    showMessage("An error occurred during login. Please try again.", false);
                    System.err.println("Login error: " + e.getMessage());
                });
            }
        });
        
        authThread.setDaemon(true);
        authThread.start();
    }
    
    /**
     * Show message to user
     * @param message Message to display
     * @param isSuccess true for success message, false for error message
     */
    private void showMessage(String message, boolean isSuccess) {
        messageLabel.setText(message);
        
        if (message.isEmpty()) {
            messageLabel.setStyle("");
        } else if (isSuccess) {
            messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }
    
    /**
     * Clear all form fields
     */
    public void clearForm() {
        usernameField.clear();
        passwordField.clear();
        messageLabel.setText("");
        usernameField.requestFocus();
    }
    
    /**
     * Show the login window
     */
    public void show() {
        primaryStage.show();
    }
    
    /**
     * Hide the login window
     */
    public void hide() {
        primaryStage.hide();
    }
}
