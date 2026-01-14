package com.employeemgmt.view.dashboard;

import java.io.IOException;

import com.employeemgmt.controller.AuthenticationController;
import com.employeemgmt.model.Employee;
import com.employeemgmt.util.UserRole;
import com.employeemgmt.view.employee.EmployeeCreateController;
import com.employeemgmt.view.employee.EmployeeEditController;
import com.employeemgmt.view.employee.EmployeeSearchController;
import com.employeemgmt.view.payroll.PayStatementController;
import com.employeemgmt.view.payroll.SalaryManagementController;
import com.employeemgmt.view.reports.HiringDateRangeReportController;
import com.employeemgmt.view.reports.MonthlyPayByDivisionReportController;
import com.employeemgmt.view.reports.MonthlyPayByJobTitleReportController;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main dashboard view for the Employee Management System.
 */
public class MainDashboard {

    private final Stage primaryStage;
    private final AuthenticationController authController;
    private Runnable onLogoutCallback;

    // UI Components
    private BorderPane mainLayout;
    private MenuBar menuBar;
    private Label welcomeLabel;
    private VBox contentArea;

    public MainDashboard(Stage primaryStage, AuthenticationController authController) {
        this.primaryStage = primaryStage;
        this.authController = authController;
        initializeComponents();
    }

    public void setOnLogoutCallback(Runnable callback) {
        this.onLogoutCallback = callback;
    }

    private void initializeComponents() {
        System.out.println("[MainDashboard] Initializing components");

        mainLayout = new BorderPane();
        createMenuBar();
        VBox header = createHeader();
        createContentArea();

        mainLayout.setTop(new VBox(menuBar, header));
        mainLayout.setCenter(contentArea);

        Scene scene = new Scene(mainLayout, 1000, 700);
        primaryStage.setTitle("Employee Management System - Dashboard");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);

        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            handleLogout();
        });

        System.out.println("[MainDashboard] Initialization complete");
    }

    private void createMenuBar() {
        System.out.println("[MainDashboard] Creating menu bar");
        menuBar = new MenuBar();

        // FILE MENU
        Menu fileMenu = new Menu("File");
        MenuItem homeItem = new MenuItem("Home");
        homeItem.setOnAction(e -> showDashboardHome());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> handleLogout());
        fileMenu.getItems().addAll(homeItem, new SeparatorMenuItem(), exitItem);

        // EMPLOYEE MENU (Admin only)
        if (authController.isCurrentUserAdmin()) {
            Menu employeeMenu = new Menu("Employee");

            MenuItem addItem = new MenuItem("Add Employee");
            addItem.setOnAction(e -> showEmployeeCreateView());

            MenuItem searchItem = new MenuItem("Search Employees");
            searchItem.setOnAction(e -> showEmployeeSearchView());

            MenuItem manageItem = new MenuItem("Manage Employees");
            // For now, reuse search screen for "Manage Employees"
            manageItem.setOnAction(e -> showEmployeeSearchView());

            employeeMenu.getItems().addAll(addItem, searchItem, manageItem);
            menuBar.getMenus().add(employeeMenu);
        }

        // PAYROLL MENU (Admin only)
        if (authController.isCurrentUserAdmin()) {
            Menu payrollMenu = new Menu("Payroll");

            // Salary Management screen
            MenuItem salaryMgmtItem = new MenuItem("Salary Management");
            salaryMgmtItem.setOnAction(e -> showSalaryManagementView());

            MenuItem reportsItem = new MenuItem("Pay Reports");
            reportsItem.setOnAction(e -> showPayStatementHistoryViewForAdmin());

            payrollMenu.getItems().addAll(salaryMgmtItem, reportsItem);
            menuBar.getMenus().add(payrollMenu);
        }

        // REPORTS MENU
        Menu reportsMenu = new Menu("Reports");
        if (authController.isCurrentUserAdmin()) {
            MenuItem payReports = new MenuItem("Pay Reports");
            payReports.setOnAction(e -> showPayStatementHistoryViewForAdmin());

            // Hiring Date Range Report
            MenuItem hiringReport = new MenuItem("Hiring Date Range Report");
            hiringReport.setOnAction(e -> showHiringDateRangeReportView());

            // Monthly Pay by Division Report
            MenuItem monthlyDivisionReport = new MenuItem("Monthly Pay by Division");
            monthlyDivisionReport.setOnAction(e -> showMonthlyPayByDivisionReportView());

            // Monthly Pay by Job Title Report
            MenuItem monthlyJobTitleReport = new MenuItem("Monthly Pay by Job Title");
            monthlyJobTitleReport.setOnAction(e -> showMonthlyPayByJobTitleReportView());

            reportsMenu.getItems().addAll(
                payReports,
                hiringReport,
                monthlyDivisionReport,
                monthlyJobTitleReport
            );
        } else {
            MenuItem myPayItem = new MenuItem("My Pay Statements");
            myPayItem.setOnAction(e -> showMyPayStatementsView());
            reportsMenu.getItems().add(myPayItem);
        }

        // HELP MENU
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAbout());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, reportsMenu, helpMenu);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");

        welcomeLabel = new Label("Welcome, " + authController.getCurrentUserName());
        welcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");

        Label roleLabel = new Label("Role: " + authController.getCurrentUserRoleDisplayName());
        roleLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 12;");

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> handleLogout());

        HBox headerContent = new HBox(10);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox userInfoBox = new VBox(5, welcomeLabel, roleLabel);
        headerContent.getChildren().addAll(userInfoBox, spacer, logoutButton);
        header.getChildren().add(headerContent);

        return header;
    }

    private void createContentArea() {
        contentArea = new VBox(20);
        contentArea.setPadding(new Insets(20));
        showDashboardHome();
    }

    private void showDashboardHome() {
        System.out.println("[MainDashboard] Showing home dashboard");
        contentArea.getChildren().clear();

        Label welcomeTitle = new Label("Employee Management System");
        welcomeTitle.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        Label message = new Label("Welcome to your employee management dashboard.");

        contentArea.getChildren().addAll(welcomeTitle, message);
    }

    private void showEmployeeCreateView() {
        System.out.println("[MainDashboard] Loading EmployeeCreateView.fxml");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EmployeeCreateView.fxml"));
            Parent root = loader.load();

            // We currently do NOT pass user context here because EmployeeCreateController
            // does not define setUserContext(UserRole, Employee). The menu itself is
            // admin-only, so this screen is already restricted at the UI level.
            @SuppressWarnings("unused")
            EmployeeCreateController controller = loader.getController();

            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Load Error (Employee Create)", e.getMessage());
        }
    }

    /**
     * Show the Employee Search screen (EmployeeSearchView.fxml),
     * and push user context into EmployeeSearchController.
     */
    private void showEmployeeSearchView() {
        System.out.println("[MainDashboard] Loading EmployeeSearchView.fxml");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EmployeeSearchView.fxml"));
            Parent root = loader.load();

            EmployeeSearchController controller = loader.getController();
            Employee current = authController.getCurrentUser();
            controller.setUserContext(resolveCurrentUserRole(), current);

            // Wire the edit callback so double-clicking a row opens the edit screen
            controller.setOnEditEmployee(empId -> {
                System.out.println("[MainDashboard] Edit requested for empId=" + empId);
                showEmployeeEditView(empId);
            });

            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Load Error (Employee Search)", e.getMessage());
        }
    }

    private void showPayStatementHistoryViewForAdmin() {
        System.out.println("[MainDashboard] Loading PayStatementView.fxml for HR_ADMIN");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PayStatementView.fxml"));
            Parent root = loader.load();

            PayStatementController controller = loader.getController();
            controller.setUserContext(UserRole.HR_ADMIN, authController.getCurrentUser());

            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Load Error (Pay Statements - Admin)", e.getMessage());
        }
    }

    private void showMyPayStatementsView() {
        System.out.println("[MainDashboard] Loading PayStatementView.fxml for EMPLOYEE");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PayStatementView.fxml"));
            Parent root = loader.load();

            PayStatementController controller = loader.getController();
            controller.setUserContext(UserRole.EMPLOYEE, authController.getCurrentUser());

            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Load Error (My Pay Statements)", e.getMessage());
        }
    }

    /**
     * Show the Employee Edit screen for the given employee ID.
     * Intended to be called from EmployeeSearchController.
     */
    private void showEmployeeEditView(int empId) {
        System.out.println("[MainDashboard] Loading EmployeeEditView.fxml for empId=" + empId);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EmployeeEditView.fxml"));
            Parent root = loader.load();

            EmployeeEditController controller = loader.getController();
            Employee current = authController.getCurrentUser();
            controller.setUserContext(resolveCurrentUserRole(), current);
            controller.loadEmployeeForEdit(empId);

            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Load Error (Employee Edit)", e.getMessage());
        }
    }

    /**
     * Show the Salary Management screen (SalaryManagementView.fxml),
     * and inject HR_ADMIN context into SalaryManagementController.
     */
    private void showSalaryManagementView() {
        System.out.println("[MainDashboard] Loading SalaryManagementView.fxml");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SalaryManagementView.fxml"));
            Parent root = loader.load();

            SalaryManagementController controller = loader.getController();
            Employee current = authController.getCurrentUser();
            controller.setUserContext(UserRole.HR_ADMIN, current);

            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Load Error (Salary Management)", e.getMessage());
        }
    }

    /**
     * Show the Hiring Date Range Report screen
     * (HiringDateRangeReportView.fxml) and inject HR_ADMIN context.
     */
    private void showHiringDateRangeReportView() {
        System.out.println("[MainDashboard] Loading HiringDateRangeReportView.fxml");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HiringDateRangeReportView.fxml"));
            Parent root = loader.load();

            HiringDateRangeReportController controller = loader.getController();
            Employee current = authController.getCurrentUser();
            controller.setUserContext(UserRole.HR_ADMIN, current);

            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Load Error (Hiring Date Range Report)", e.getMessage());
        }
    }

    /**
 * Show the Monthly Pay by Division Report screen
 * (MonthlyPayByDivisionReportView.fxml) and inject HR_ADMIN context.
 */
private void showMonthlyPayByDivisionReportView() {
    System.out.println("[MainDashboard] Loading MonthlyPayByDivisionReportView.fxml");
    try {
        var fxmlUrl = getClass().getResource("/fxml/MonthlyPayByDivisionReportView.fxml");
        System.out.println("[MainDashboard] FXML URL (MonthlyPayByDivision): " + fxmlUrl);

        if (fxmlUrl == null) {
            showErrorDialog(
                "Load Error (Monthly Pay by Division)",
                "FXML resource not found at /fxml/MonthlyPayByDivisionReportView.fxml.\n" +
                "Verify the file is located under src/main/resources/fxml with that exact name."
            );
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        MonthlyPayByDivisionReportController controller = loader.getController();
        Employee current = authController.getCurrentUser();
        controller.setUserContext(UserRole.HR_ADMIN, current);

        contentArea.getChildren().setAll(root);
    } catch (Exception e) {
        e.printStackTrace();
        showErrorDialog("Load Error (Monthly Pay by Division)", e.getMessage());
    }
}


    /**
     * Show the Monthly Pay by Job Title Report screen
     * (MonthlyPayByJobTitleReportView.fxml) and inject HR_ADMIN context.
     */
    private void showMonthlyPayByJobTitleReportView() {
        System.out.println("[MainDashboard] Loading MonthlyPayByJobTitleReportView.fxml");
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/MonthlyPayByJobTitleReportView.fxml"));
            Parent root = loader.load();

            MonthlyPayByJobTitleReportController controller = loader.getController();
            Employee current = authController.getCurrentUser();
            controller.setUserContext(UserRole.HR_ADMIN, current);

            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Load Error (Monthly Pay by Job Title)", e.getMessage());
        }
    }

    private void handleLogout() {
        System.out.println("[MainDashboard] Logout requested");
        if (onLogoutCallback != null) {
            onLogoutCallback.run();
        } else {
            System.out.println("[MainDashboard] No logout callback set");
        }
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About System");
        alert.setHeaderText("Employee Management System v1.0");
        alert.setContentText("Developed by Team 6");
        alert.showAndWait();
    }

    private void showErrorDialog(String title, String message) {
        System.err.println("[MainDashboard] " + title + ": " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Resolve current user's role as UserRole enum for passing into sub-views.
     */
    private UserRole resolveCurrentUserRole() {
        return authController.isCurrentUserAdmin() ? UserRole.HR_ADMIN : UserRole.EMPLOYEE;
    }

    public void show() {
        System.out.println("[MainDashboard] Showing primary stage");
        primaryStage.show();
    }

    public void hide() {
        System.out.println("[MainDashboard] Hiding primary stage");
        primaryStage.hide();
    }
}
