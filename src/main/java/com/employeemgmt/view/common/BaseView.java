package com.employeemgmt.view.common;

import com.employeemgmt.util.UserRole;
import com.employeemgmt.model.Employee;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.prefs.Preferences;

/**
 * Base view class providing common functionality for all JavaFX views
 * Implements standard patterns for view management, validation, user interaction, and styling
 * 
 * Features:
 * - Consistent header/content/footer layout
 * - Built-in loading states and progress indicators
 * - Validation framework with error handling
 * - Role-based access control integration
 * - State management and preferences
 * - Common UI components and styling
 * - AlertUtils integration for user feedback
 * 
 * @author Team 6
 */
public abstract class BaseView extends BorderPane {
    
    private static final Logger LOGGER = Logger.getLogger(BaseView.class.getName());
    
    // ==================== STYLING CONSTANTS ====================
    
    protected static final String BASE_STYLE_CLASS = "base-view";
    protected static final String HEADER_STYLE_CLASS = "view-header";
    protected static final String CONTENT_STYLE_CLASS = "view-content";
    protected static final String FOOTER_STYLE_CLASS = "view-footer";
    protected static final String LOADING_STYLE_CLASS = "loading-overlay";
    protected static final String REQUIRED_FIELD_STYLE = "required-field";
    protected static final String ERROR_FIELD_STYLE = "error-field";
    
    // ==================== LAYOUT CONSTANTS ====================
    
    protected static final double STANDARD_SPACING = 10.0;
    protected static final double LARGE_SPACING = 20.0;
    protected static final double SMALL_SPACING = 5.0;
    protected static final Insets STANDARD_PADDING = new Insets(10);
    protected static final Insets LARGE_PADDING = new Insets(20);
    protected static final Insets SMALL_PADDING = new Insets(5);
    protected static final double BUTTON_MIN_WIDTH = 100.0;
    
    // ==================== PROPERTIES ====================
    
    private final StringProperty viewTitle = new SimpleStringProperty("");
    private final StringProperty viewSubtitle = new SimpleStringProperty("");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty hasUnsavedChanges = new SimpleBooleanProperty(false);
    private final ObjectProperty<UserRole> userRole = new SimpleObjectProperty<>();
    private final ObjectProperty<Employee> currentUser = new SimpleObjectProperty<>();
    private final BooleanProperty viewDisabled = new SimpleBooleanProperty(false);
    
    // ==================== UI COMPONENTS ====================
    
    private VBox headerContainer;
    private Label titleLabel;
    private Label subtitleLabel;
    private StackPane contentContainer;
    private HBox footerContainer;
    private VBox loadingOverlay;
    private ProgressIndicator loadingIndicator;
    private Label loadingLabel;
    
    // ==================== STATE MANAGEMENT ====================
    
    private final Map<String, Object> viewState = new HashMap<>();
    private final List<ValidationRule> validationRules = new ArrayList<>();
    private final List<Node> requiredFields = new ArrayList<>();
    private final List<Task<?>> runningTasks = new ArrayList<>();
    private Preferences viewPreferences;
    
    // ==================== VALIDATION FRAMEWORK ====================
    
    /**
     * Validation rule functional interface
     */
    @FunctionalInterface
    public interface ValidationRule {
        ValidationResult validate();
    }
    
    /**
     * Validation result wrapper
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final Node errorField;
        
        private ValidationResult(boolean valid, String errorMessage, Node errorField) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.errorField = errorField;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null, null);
        }
        
        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message, null);
        }
        
        public static ValidationResult failure(String message, Node field) {
            return new ValidationResult(false, message, field);
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public Node getErrorField() { return errorField; }
    }
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Default constructor
     */
    public BaseView() {
        initialize();
    }
    
    /**
     * Constructor with title
     */
    public BaseView(String title) {
        this();
        setViewTitle(title);
    }
    
    /**
     * Constructor with title and subtitle
     */
    public BaseView(String title, String subtitle) {
        this();
        setViewTitle(title);
        setViewSubtitle(subtitle);
    }
    
    // ==================== INITIALIZATION ====================
    
    /**
     * Initialize the base view structure
     */
    private void initialize() {
        try {
            // Set up preferences
            viewPreferences = Preferences.userNodeForPackage(this.getClass());
            
            // Add base styling
            getStyleClass().add(BASE_STYLE_CLASS);
            
            // Create UI structure
            createHeader();
            createContent();
            createFooter();
            createLoadingOverlay();
            
            // Set up bindings and listeners
            setupBindings();
            setupEventHandlers();
            
            // Initialize view in next frame
            Platform.runLater(() -> {
                try {
                    initializeView();
                    restoreViewState();
                    LOGGER.info("View initialized: " + this.getClass().getSimpleName());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error initializing view", e);
                    AlertUtils.showError("Failed to initialize view", e);
                }
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal error during BaseView initialization", e);
            throw new RuntimeException("Failed to initialize BaseView", e);
        }
    }
    
    /**
     * Create header section with title and subtitle
     */
    private void createHeader() {
        headerContainer = new VBox(SMALL_SPACING);
        headerContainer.getStyleClass().add(HEADER_STYLE_CLASS);
        headerContainer.setPadding(STANDARD_PADDING);
        
        // Title label
        titleLabel = new Label();
        titleLabel.getStyleClass().addAll("view-title", "h1");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.textProperty().bind(viewTitle);
        
        // Subtitle label
        subtitleLabel = new Label();
        subtitleLabel.getStyleClass().addAll("view-subtitle", "h3");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.GRAY);
        subtitleLabel.textProperty().bind(viewSubtitle);
        
        headerContainer.getChildren().addAll(titleLabel, subtitleLabel);
        
        // Hide subtitle if empty
        subtitleLabel.visibleProperty().bind(viewSubtitle.isNotEmpty());
        subtitleLabel.managedProperty().bind(subtitleLabel.visibleProperty());
        
        setTop(headerContainer);
    }
    
    /**
     * Create main content container
     */
    private void createContent() {
        contentContainer = new StackPane();
        contentContainer.getStyleClass().add(CONTENT_STYLE_CLASS);
        contentContainer.setPadding(STANDARD_PADDING);
        
        setCenter(contentContainer);
    }
    
    /**
     * Create footer section for buttons
     */
    private void createFooter() {
        footerContainer = new HBox(STANDARD_SPACING);
        footerContainer.getStyleClass().add(FOOTER_STYLE_CLASS);
        footerContainer.setPadding(STANDARD_PADDING);
        footerContainer.setAlignment(Pos.CENTER_RIGHT);
        
        // Initially hidden
        footerContainer.setVisible(false);
        footerContainer.setManaged(false);
        
        setBottom(footerContainer);
    }
    
    /**
     * Create loading overlay
     */
    private void createLoadingOverlay() {
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(60, 60);
        
        loadingLabel = new Label("Loading...");
        loadingLabel.getStyleClass().add("loading-label");
        loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        
        loadingOverlay = new VBox(STANDARD_SPACING);
        loadingOverlay.getChildren().addAll(loadingIndicator, loadingLabel);
        loadingOverlay.setAlignment(Pos.CENTER);
        loadingOverlay.getStyleClass().add(LOADING_STYLE_CLASS);
        loadingOverlay.setStyle("-fx-background-color: rgba(240, 240, 240, 0.9);");
        loadingOverlay.setVisible(false);
        
        contentContainer.getChildren().add(loadingOverlay);
    }
    
    /**
     * Set up property bindings
     */
    private void setupBindings() {
        // Bind loading overlay visibility
        loadingOverlay.visibleProperty().bind(loading);
        loadingOverlay.managedProperty().bind(loading);
        
        // Bind header visibility
        headerContainer.visibleProperty().bind(
            viewTitle.isNotEmpty().or(viewSubtitle.isNotEmpty())
        );
        headerContainer.managedProperty().bind(headerContainer.visibleProperty());
        
        // Bind view disabled state
        contentContainer.disableProperty().bind(viewDisabled);
    }
    
    /**
     * Set up event handlers
     */
    private void setupEventHandlers() {
        // Handle window closing to save state
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() != null) {
                newScene.getWindow().setOnCloseRequest(e -> {
                    if (!handleViewClosing()) {
                        e.consume();
                    }
                });
            }
        });
    }
    
    // ==================== ABSTRACT METHODS ====================
    
    /**
     * Initialize view-specific content
     * Called after base initialization is complete
     */
    protected abstract void initializeView();
    
    /**
     * Refresh view data
     * Should reload data from services/database
     */
    public abstract void refreshView();
    
    // ==================== CONTENT MANAGEMENT ====================
    
    /**
     * Set the main content of the view
     */
    protected void setViewContent(Node content) {
        contentContainer.getChildren().clear();
        if (content != null) {
            contentContainer.getChildren().add(content);
        }
        contentContainer.getChildren().add(loadingOverlay);
    }
    
    /**
     * Add content to the view
     */
    protected void addViewContent(Node content) {
        if (content != null) {
            int insertIndex = contentContainer.getChildren().size() - 1; // Before loading overlay
            contentContainer.getChildren().add(insertIndex, content);
        }
    }
    
    /**
     * Clear all content
     */
    protected void clearViewContent() {
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(loadingOverlay);
    }
    
    // ==================== FOOTER MANAGEMENT ====================
    
    /**
     * Set footer buttons
     */
    protected void setFooterButtons(Button... buttons) {
        footerContainer.getChildren().clear();
        if (buttons.length > 0) {
            footerContainer.getChildren().addAll(buttons);
            showFooter();
        } else {
            hideFooter();
        }
    }
    
    /**
     * Add button to footer
     */
    protected void addFooterButton(Button button) {
        footerContainer.getChildren().add(button);
        showFooter();
    }
    
    /**
     * Show footer
     */
    protected void showFooter() {
        footerContainer.setVisible(true);
        footerContainer.setManaged(true);
    }
    
    /**
     * Hide footer
     */
    protected void hideFooter() {
        footerContainer.setVisible(false);
        footerContainer.setManaged(false);
    }
    
    // ==================== LOADING STATE ====================
    
    /**
     * Show loading with default message
     */
    protected void showLoading() {
        showLoading("Loading...");
    }
    
    /**
     * Show loading with custom message
     */
    protected void showLoading(String message) {
        loadingLabel.setText(message);
        setLoading(true);
    }
    
    /**
     * Hide loading
     */
    protected void hideLoading() {
        setLoading(false);
    }
    
    /**
     * Execute task with loading indicator
     */
    protected <T> void executeWithLoading(Task<T> task, String loadingMessage) {
        executeWithLoading(task, null, null, loadingMessage);
    }
    
    /**
     * Execute task with loading indicator and callbacks
     */
    protected <T> void executeWithLoading(Task<T> task, 
                                        Consumer<T> onSuccess,
                                        Consumer<Throwable> onError,
                                        String loadingMessage) {
        
        showLoading(loadingMessage);
        runningTasks.add(task);
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            runningTasks.remove(task);
            hideLoading();
            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
        }));
        
        task.setOnFailed(e -> Platform.runLater(() -> {
            runningTasks.remove(task);
            hideLoading();
            Throwable exception = task.getException();
            if (onError != null) {
                onError.accept(exception);
            } else {
                AlertUtils.showError("Operation failed", exception);
            }
        }));
        
        task.setOnCancelled(e -> Platform.runLater(() -> {
            runningTasks.remove(task);
            hideLoading();
        }));
        
        Thread taskThread = new Thread(task);
        taskThread.setDaemon(true);
        taskThread.start();
    }
    
    // ==================== VALIDATION FRAMEWORK ====================
    
    /**
     * Add validation rule
     */
    protected void addValidationRule(ValidationRule rule) {
        validationRules.add(rule);
    }
    
    /**
     * Add field validation
     */
    protected void addFieldValidation(Node field, java.util.function.Supplier<Boolean> validator, String errorMessage) {
        addValidationRule(() -> {
            boolean isValid = validator.get();
            return isValid ? ValidationResult.success() : ValidationResult.failure(errorMessage, field);
        });
    }
    
    /**
     * Add required field validation
     */
    protected void addRequiredFieldValidation(TextField field, String fieldName) {
        markFieldAsRequired(field);
        addFieldValidation(field, 
            () -> field.getText() != null && !field.getText().trim().isEmpty(),
            fieldName + " is required");
    }
    
    /**
     * Mark field as required
     */
    protected void markFieldAsRequired(Node field) {
        requiredFields.add(field);
        field.getStyleClass().add(REQUIRED_FIELD_STYLE);
    }
    
    /**
     * Validate all rules
     */
    protected boolean validateAll() {
        clearFieldErrors();
        
        for (ValidationRule rule : validationRules) {
            ValidationResult result = rule.validate();
            if (!result.isValid()) {
                handleValidationError(result);
                return false;
            }
        }
        return true;
    }
    
    /**
     * Handle validation error
     */
    private void handleValidationError(ValidationResult result) {
        if (result.getErrorField() != null) {
            // Highlight error field
            result.getErrorField().getStyleClass().add(ERROR_FIELD_STYLE);
            result.getErrorField().requestFocus();
            
            String fieldName = getFieldDisplayName(result.getErrorField());
            AlertUtils.showValidationError(fieldName, result.getErrorMessage());
        } else {
            AlertUtils.showError("Validation Error", result.getErrorMessage());
        }
    }
    
    /**
     * Clear field error styling
     */
    protected void clearFieldErrors() {
        for (Node field : requiredFields) {
            field.getStyleClass().remove(ERROR_FIELD_STYLE);
        }
    }
    
    /**
     * Get display name for field
     */
    private String getFieldDisplayName(Node field) {
        // Try to get from userData, id, or prompt text
        if (field.getUserData() instanceof String) {
            return (String) field.getUserData();
        }
        
        if (field.getId() != null) {
            return field.getId().replaceAll("([a-z])([A-Z])", "$1 $2")
                                .replace("Field", "")
                                .replace("field", "")
                                .trim();
        }
        
        if (field instanceof TextInputControl) {
            String promptText = ((TextInputControl) field).getPromptText();
            if (promptText != null && !promptText.isEmpty()) {
                return promptText.replace(":", "");
            }
        }
        
        return "Field";
    }
    
    // ==================== USER CONTEXT & SECURITY ====================
    
    /**
     * Set user context for role-based access control
     */
    public void setUserContext(UserRole role, Employee user) {
        setUserRole(role);
        setCurrentUser(user);
        onUserContextChanged(role, user);
    }
    
    /**
     * Called when user context changes
     */
    protected void onUserContextChanged(UserRole role, Employee user) {
        updateUIForRole(role);
    }
    
    /**
     * Update UI based on user role
     */
    protected void updateUIForRole(UserRole role) {
        // Default implementation - can be overridden
        boolean isAdmin = (role == UserRole.HR_ADMIN);
        setViewDisabled(!isAdmin);
    }
    
    /**
     * Check if current user is admin
     */
    protected boolean isCurrentUserAdmin() {
        return getUserRole() == UserRole.HR_ADMIN;
    }
    
    /**
     * Check if user can perform action
     */
    protected boolean canPerformAction(String action) {
        UserRole role = getUserRole();
        if (role == null) return false;
        
        switch (action.toLowerCase()) {
            case "create":
            case "update": 
            case "delete":
                return role == UserRole.HR_ADMIN;
            case "read":
            case "view":
                return true; // Both roles can view
            default:
                return role == UserRole.HR_ADMIN;
        }
    }
    
    // ==================== BUTTON FACTORY METHODS ====================
    
    /**
     * Create standard button
     */
    protected Button createButton(String text, EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().add("standard-button");
        button.setMinWidth(BUTTON_MIN_WIDTH);
        if (action != null) {
            button.setOnAction(action);
        }
        return button;
    }
    
    /**
     * Create primary button (emphasized)
     */
    protected Button createPrimaryButton(String text, EventHandler<ActionEvent> action) {
        Button button = createButton(text, action);
        button.getStyleClass().add("primary-button");
        button.setDefaultButton(true);
        return button;
    }
    
    /**
     * Create secondary button
     */
    protected Button createSecondaryButton(String text, EventHandler<ActionEvent> action) {
        Button button = createButton(text, action);
        button.getStyleClass().add("secondary-button");
        return button;
    }
    
    /**
     * Create success button
     */
    protected Button createSuccessButton(String text, EventHandler<ActionEvent> action) {
        Button button = createButton(text, action);
        button.getStyleClass().add("success-button");
        return button;
    }
    
    /**
     * Create danger button (for destructive actions)
     */
    protected Button createDangerButton(String text, EventHandler<ActionEvent> action) {
        Button button = createButton(text, action);
        button.getStyleClass().add("danger-button");
        return button;
    }
    
    /**
     * Create cancel button
     */
    protected Button createCancelButton(EventHandler<ActionEvent> action) {
        Button button = createSecondaryButton("Cancel", action);
        button.setCancelButton(true);
        return button;
    }
    
    // ==================== STATE MANAGEMENT ====================
    
    /**
     * Save view state
     */
    protected void saveViewState() {
        try {
            onSaveViewState();
            viewPreferences.flush();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to save view state", e);
        }
    }
    
    /**
     * Restore view state
     */
    protected void restoreViewState() {
        try {
            onRestoreViewState();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to restore view state", e);
        }
    }
    
    /**
     * Override to save custom view state
     */
    protected void onSaveViewState() {
        // Override in subclasses
    }
    
    /**
     * Override to restore custom view state
     */
    protected void onRestoreViewState() {
        // Override in subclasses
    }
    
    /**
     * Handle view closing
     */
    protected boolean handleViewClosing() {
        if (!handleUnsavedChanges()) {
            return false;
        }
        
        saveViewState();
        cancelRunningTasks();
        disposeView();
        return true;
    }
    
    /**
     * Handle unsaved changes
     */
    protected boolean handleUnsavedChanges() {
        if (getHasUnsavedChanges()) {
            return AlertUtils.showUnsavedChangesConfirmation();
        }
        return true;
    }
    
    /**
     * Cancel running tasks
     */
    protected void cancelRunningTasks() {
        for (Task<?> task : runningTasks) {
            if (task.isRunning()) {
                task.cancel();
            }
        }
        runningTasks.clear();
    }
    
    /**
     * Dispose view resources
     */
    protected void disposeView() {
        LOGGER.info("Disposing view: " + this.getClass().getSimpleName());
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Run action on JavaFX thread
     */
    protected void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
    
    /**
     * Get or create preference value
     */
    protected String getPreference(String key, String defaultValue) {
        return viewPreferences.get(key, defaultValue);
    }
    
    /**
     * Set preference value
     */
    protected void setPreference(String key, String value) {
        viewPreferences.put(key, value);
    }
    
    /**
     * Get view state
     */
    protected Object getViewState(String key) {
        return viewState.get(key);
    }
    
    /**
     * Set view state
     */
    protected void setViewState(String key, Object value) {
        viewState.put(key, value);
    }
    
    // ==================== PROPERTY ACCESSORS ====================
    
    public String getViewTitle() {
        return viewTitle.get();
    }
    
    public void setViewTitle(String title) {
        this.viewTitle.set(title);
    }
    
    public StringProperty viewTitleProperty() {
        return viewTitle;
    }
    
    public String getViewSubtitle() {
        return viewSubtitle.get();
    }
    
    public void setViewSubtitle(String subtitle) {
        this.viewSubtitle.set(subtitle);
    }
    
    public StringProperty viewSubtitleProperty() {
        return viewSubtitle;
    }
    
    public boolean getLoading() {
        return loading.get();
    }
    
    public void setLoading(boolean loading) {
        this.loading.set(loading);
    }
    
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    public boolean getHasUnsavedChanges() {
        return hasUnsavedChanges.get();
    }
    
    public void setHasUnsavedChanges(boolean hasChanges) {
        this.hasUnsavedChanges.set(hasChanges);
    }
    
    public BooleanProperty hasUnsavedChangesProperty() {
        return hasUnsavedChanges;
    }
    
    public UserRole getUserRole() {
        return userRole.get();
    }
    
    public void setUserRole(UserRole role) {
        this.userRole.set(role);
    }
    
    public ObjectProperty<UserRole> userRoleProperty() {
        return userRole;
    }
    
    public Employee getCurrentUser() {
        return currentUser.get();
    }
    
    public void setCurrentUser(Employee user) {
        this.currentUser.set(user);
    }
    
    public ObjectProperty<Employee> currentUserProperty() {
        return currentUser;
    }
    
    public boolean getViewDisabled() {
        return viewDisabled.get();
    }
    
    public void setViewDisabled(boolean disabled) {
        this.viewDisabled.set(disabled);
    }
    
    public BooleanProperty viewDisabledProperty() {
        return viewDisabled;
    }
}