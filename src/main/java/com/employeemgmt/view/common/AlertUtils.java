package com.employeemgmt.view.common;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.geometry.Insets;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Utility class for standardized alert and dialog management in JavaFX
 * Provides consistent styling, behavior, and user experience across the application
 * 
 * @author Team 6
 */
public class AlertUtils {
    
    private static final Logger LOGGER = Logger.getLogger(AlertUtils.class.getName());
    
    // Application styling constants
    private static final String ALERT_STYLE_CLASS = "employee-mgmt-alert";
    private static final String ERROR_STYLE_CLASS = "error-alert";
    private static final String WARNING_STYLE_CLASS = "warning-alert";
    private static final String SUCCESS_STYLE_CLASS = "success-alert";
    private static final String CONFIRM_STYLE_CLASS = "confirm-alert";
    
    // Default alert properties
    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 200;
    private static String applicationTitle = "Employee Management System";
    private static Window defaultOwner = null;
    
    /**
     * Alert result for async operations
     */
    public enum AlertResult {
        OK, CANCEL, YES, NO, CLOSE
    }
    
    /**
     * Set the application title for alerts
     * 
     * @param title Application title
     */
    public static void setApplicationTitle(String title) {
        applicationTitle = title;
    }
    
    /**
     * Set the default owner window for alerts
     * 
     * @param owner Default owner window
     */
    public static void setDefaultOwner(Window owner) {
        defaultOwner = owner;
    }
    
    // ==================== SUCCESS ALERTS ====================
    
    /**
     * Show success message
     * 
     * @param message Success message
     */
    public static void showSuccess(String message) {
        showSuccess("Success", message);
    }
    
    /**
     * Show success message with custom title
     * 
     * @param title Alert title
     * @param message Success message
     */
    public static void showSuccess(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = createAlert(AlertType.INFORMATION, title, message);
            alert.getDialogPane().getStyleClass().add(SUCCESS_STYLE_CLASS);
            setSuccessIcon(alert);
            alert.showAndWait();
        });
    }
    
    /**
     * Show success message asynchronously
     * 
     * @param message Success message
     * @return CompletableFuture that completes when alert is closed
     */
    public static CompletableFuture<AlertResult> showSuccessAsync(String message) {
        return showSuccessAsync("Success", message);
    }
    
    /**
     * Show success message asynchronously with custom title
     * 
     * @param title Alert title
     * @param message Success message
     * @return CompletableFuture that completes when alert is closed
     */
    public static CompletableFuture<AlertResult> showSuccessAsync(String title, String message) {
        CompletableFuture<AlertResult> future = new CompletableFuture<>();
        
        Platform.runLater(() -> {
            Alert alert = createAlert(AlertType.INFORMATION, title, message);
            alert.getDialogPane().getStyleClass().add(SUCCESS_STYLE_CLASS);
            setSuccessIcon(alert);
            
            Optional<ButtonType> result = alert.showAndWait();
            future.complete(result.isPresent() ? AlertResult.OK : AlertResult.CLOSE);
        });
        
        return future;
    }
    
    // ==================== ERROR ALERTS ====================
    
    /**
     * Show error message
     * 
     * @param message Error message
     */
    public static void showError(String message) {
        showError("Error", message);
    }
    
    /**
     * Show error message with custom title
     * 
     * @param title Alert title
     * @param message Error message
     */
    public static void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = createAlert(AlertType.ERROR, title, message);
            alert.getDialogPane().getStyleClass().add(ERROR_STYLE_CLASS);
            alert.showAndWait();
        });
    }
    
    /**
     * Show error message with exception details
     * 
     * @param message Error message
     * @param exception Exception to display
     */
    public static void showError(String message, Throwable exception) {
        showError("Error", message, exception);
    }
    
    /**
     * Show error message with exception details and custom title
     * 
     * @param title Alert title
     * @param message Error message
     * @param exception Exception to display
     */
    public static void showError(String title, String message, Throwable exception) {
        Platform.runLater(() -> {
            Alert alert = createAlert(AlertType.ERROR, title, message);
            alert.getDialogPane().getStyleClass().add(ERROR_STYLE_CLASS);
            
            // Add expandable exception details
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            String exceptionText = sw.toString();
            
            Label label = new Label("Exception Details:");
            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.setPadding(new Insets(10));
            expContent.setHgap(10);
            expContent.setVgap(10);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);
            
            alert.getDialogPane().setExpandableContent(expContent);
            
            // Log the exception
            LOGGER.log(Level.SEVERE, "Error shown to user: " + message, exception);
            
            alert.showAndWait();
        });
    }
    
    /**
     * Show database error with standard message
     * 
     * @param operation Operation that failed
     * @param exception Database exception
     */
    public static void showDatabaseError(String operation, Throwable exception) {
        String message = String.format("Database error occurred while %s. Please try again or contact your administrator.", operation);
        showError("Database Error", message, exception);
    }
    
    /**
     * Show validation error
     * 
     * @param fieldName Field that failed validation
     * @param validationMessage Validation error message
     */
    public static void showValidationError(String fieldName, String validationMessage) {
        String message = String.format("Validation Error in %s:\n%s", fieldName, validationMessage);
        showError("Validation Error", message);
    }
    
    // ==================== WARNING ALERTS ====================
    
    /**
     * Show warning message
     * 
     * @param message Warning message
     */
    public static void showWarning(String message) {
        showWarning("Warning", message);
    }
    
    /**
     * Show warning message with custom title
     * 
     * @param title Alert title
     * @param message Warning message
     */
    public static void showWarning(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = createAlert(AlertType.WARNING, title, message);
            alert.getDialogPane().getStyleClass().add(WARNING_STYLE_CLASS);
            alert.showAndWait();
        });
    }
    
    /**
     * Show access denied warning
     * 
     * @param operation Operation that was denied
     */
    public static void showAccessDenied(String operation) {
        String message = String.format("Access Denied: You do not have sufficient privileges to %s.", operation);
        showWarning("Access Denied", message);
    }
    
    // ==================== INFORMATION ALERTS ====================
    
    /**
     * Show information message
     * 
     * @param message Information message
     */
    public static void showInformation(String message) {
        showInformation("Information", message);
    }
    
    /**
     * Show information message with custom title
     * 
     * @param title Alert title
     * @param message Information message
     */
    public static void showInformation(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = createAlert(AlertType.INFORMATION, title, message);
            alert.showAndWait();
        });
    }
    
    // ==================== CONFIRMATION ALERTS ====================
    
    /**
     * Show confirmation dialog with Yes/No buttons
     * 
     * @param message Confirmation message
     * @return true if user clicked Yes, false otherwise
     */
    public static boolean showConfirmation(String message) {
        return showConfirmation("Confirm", message);
    }
    
    /**
     * Show confirmation dialog with custom title and Yes/No buttons
     * 
     * @param title Alert title
     * @param message Confirmation message
     * @return true if user clicked Yes, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = createAlert(AlertType.CONFIRMATION, title, message);
        alert.getDialogPane().getStyleClass().add(CONFIRM_STYLE_CLASS);
        
        // Set Yes/No buttons
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }
    
    /**
     * Show confirmation dialog with OK/Cancel buttons
     * 
     * @param message Confirmation message
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showOkCancel(String message) {
        return showOkCancel("Confirm", message);
    }
    
    /**
     * Show confirmation dialog with custom title and OK/Cancel buttons
     * 
     * @param title Alert title
     * @param message Confirmation message
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showOkCancel(String title, String message) {
        Alert alert = createAlert(AlertType.CONFIRMATION, title, message);
        alert.getDialogPane().getStyleClass().add(CONFIRM_STYLE_CLASS);
        
        // Set OK/Cancel buttons
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Show delete confirmation dialog
     * 
     * @param itemType Type of item being deleted (e.g., "employee", "division")
     * @param itemName Name of the specific item being deleted
     * @return true if user confirmed deletion, false otherwise
     */
    public static boolean showDeleteConfirmation(String itemType, String itemName) {
        String message = String.format(
            "Are you sure you want to delete this %s?\n\n" +
            "Item: %s\n\n" +
            "This action cannot be undone.",
            itemType, itemName
        );
        
        Alert alert = createAlert(AlertType.CONFIRMATION, "Confirm Deletion", message);
        alert.getDialogPane().getStyleClass().addAll(CONFIRM_STYLE_CLASS, "delete-confirmation");
        
        // Custom buttons for delete confirmation
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(deleteButton, cancelButton);
        
        // Style the delete button as dangerous
        Button deleteBtn = (Button) alert.getDialogPane().lookupButton(deleteButton);
        deleteBtn.getStyleClass().add("danger-button");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == deleteButton;
    }
    
    /**
     * Show unsaved changes confirmation
     * 
     * @return true if user wants to discard changes, false otherwise
     */
    public static boolean showUnsavedChangesConfirmation() {
        String message = "You have unsaved changes. Do you want to discard them?";
        
        Alert alert = createAlert(AlertType.CONFIRMATION, "Unsaved Changes", message);
        alert.getDialogPane().getStyleClass().add(CONFIRM_STYLE_CLASS);
        
        ButtonType discardButton = new ButtonType("Discard Changes", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(discardButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == discardButton;
    }
    
    // ==================== ASYNC CONFIRMATION ====================
    
    /**
     * Show confirmation dialog asynchronously
     * 
     * @param title Alert title
     * @param message Confirmation message
     * @return CompletableFuture with the user's choice
     */
    public static CompletableFuture<Boolean> showConfirmationAsync(String title, String message) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        Platform.runLater(() -> {
            boolean result = showConfirmation(title, message);
            future.complete(result);
        });
        
        return future;
    }
    
    // ==================== INPUT DIALOGS ====================
    
    /**
     * Show text input dialog
     * 
     * @param title Dialog title
     * @param headerText Header text
     * @param promptText Prompt text
     * @return User input or null if cancelled
     */
    public static String showTextInput(String title, String headerText, String promptText) {
        return showTextInput(title, headerText, promptText, "");
    }
    
    /**
     * Show text input dialog with default value
     * 
     * @param title Dialog title
     * @param headerText Header text
     * @param promptText Prompt text
     * @param defaultValue Default input value
     * @return User input or null if cancelled
     */
    public static String showTextInput(String title, String headerText, String promptText, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(promptText);
        
        configureDialog(dialog);
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    /**
     * Show password input dialog
     * 
     * @param title Dialog title
     * @param headerText Header text
     * @return User input or null if cancelled
     */
    public static String showPasswordInput(String title, String headerText) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        
        configureDialog(dialog);
        
        // Set button types
        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        
        // Create password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Password:"), 0, 0);
        grid.add(passwordField, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        
        // Focus on password field
        Platform.runLater(passwordField::requestFocus);
        
        // Convert result to password when OK is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return passwordField.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    // ==================== PROGRESS DIALOGS ====================
    
    /**
     * Show progress dialog (non-modal)
     * 
     * @param title Dialog title
     * @param message Progress message
     * @return ProgressIndicator for updating progress
     */
    public static Alert showProgress(String title, String message) {
        Alert alert = new Alert(AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(message);
        
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);
        
        alert.getDialogPane().setContent(progressIndicator);
        alert.getDialogPane().getButtonTypes().clear();
        
        configureAlert(alert);
        
        // Show non-modal
        alert.show();
        
        return alert;
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Create a basic alert with standard configuration
     * 
     * @param alertType Type of alert
     * @param title Alert title
     * @param message Alert message
     * @return Configured Alert
     */
    private static Alert createAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        configureAlert(alert);
        
        return alert;
    }
    
    /**
     * Configure common alert properties
     * 
     * @param alert Alert to configure
     */
    private static void configureAlert(Alert alert) {
        // Set owner window
        if (defaultOwner != null) {
            alert.initOwner(defaultOwner);
        }
        
        // Set modality
        alert.initModality(Modality.APPLICATION_MODAL);
        
        // Set minimum size
        alert.getDialogPane().setMinHeight(DEFAULT_HEIGHT);
        alert.getDialogPane().setPrefWidth(DEFAULT_WIDTH);
        
        // Add common style class
        alert.getDialogPane().getStyleClass().add(ALERT_STYLE_CLASS);
        
        // Set icon if running in a Stage
        if (alert.getDialogPane().getScene() != null && 
            alert.getDialogPane().getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            // You can set custom icons here
            // stage.getIcons().add(new Image("/path/to/icon.png"));
        }
    }
    
    /**
     * Configure dialog properties (for non-Alert dialogs)
     * 
     * @param dialog Dialog to configure
     */
    private static void configureDialog(Dialog<?> dialog) {
        // Set owner window
        if (defaultOwner != null) {
            dialog.initOwner(defaultOwner);
        }
        
        // Set modality
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        // Add common style class
        dialog.getDialogPane().getStyleClass().add(ALERT_STYLE_CLASS);
    }
    
    /**
     * Set success icon for alert
     * 
     * @param alert Alert to set icon for
     */
    private static void setSuccessIcon(Alert alert) {
        try {
            // You can add custom success icon here
            // ImageView icon = new ImageView(new Image("/icons/success.png"));
            // icon.setFitHeight(48);
            // icon.setFitWidth(48);
            // alert.getDialogPane().setGraphic(icon);
        } catch (Exception e) {
            // Ignore if icon not found
        }
    }
    
    /**
     * Check if running on JavaFX Application Thread
     * 
     * @return true if on FX thread, false otherwise
     */
    public static boolean isFxApplicationThread() {
        return Platform.isFxApplicationThread();
    }
    
    /**
     * Run on JavaFX Application Thread if not already on it
     * 
     * @param runnable Code to run on FX thread
     */
    public static void runOnFxThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
}