package com.employeemgmt.tools;

import java.sql.SQLException;

import com.employeemgmt.dao.EmployeeDAO;
import com.employeemgmt.model.Employee;
import com.employeemgmt.service.SecurityService;
import com.employeemgmt.util.UserRole;

/**
 * One-time bootstrap tool to create sample user accounts in the `users` table.
 *
 * Run this AFTER:
 *  - The schema has been created, and
 *  - The sample data script has been loaded (so employees like E1001 exist).
 *
 * This uses SecurityService so password hashes are generated with PasswordUtils.
 */
public class BootstrapUsers {

    public static void main(String[] args) {
        System.out.println("=== Employee Management System User Bootstrap ===");

        SecurityService securityService = new SecurityService();
        EmployeeDAO employeeDAO = new EmployeeDAO();

        try {
            // 1) Seed an HR Admin not linked to any specific employee
            createUserIfMissing(
                    securityService,
                    "hradmin",
                    "Password123!",
                    UserRole.HR_ADMIN,
                    null
            );

            // 2) Seed an EMPLOYEE login for Alice (E1001)
            createEmployeeUserIfMissing(
                    securityService,
                    employeeDAO,
                    "E1001",           // emp_number from sample SQL
                    "alice",
                    "Password123!"
            );

            // 3) Optionally, seed an EMPLOYEE login for another employee, e.g., Emma (E1005)
            createEmployeeUserIfMissing(
                    securityService,
                    employeeDAO,
                    "E1005",
                    "emma",
                    "Password123!"
            );

            System.out.println("Bootstrap complete.");
        } catch (Exception ex) {
            System.err.println("Bootstrap failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Create a user only if the username does not already exist.
     */
    private static void createUserIfMissing(SecurityService securityService,
                                            String username,
                                            String password,
                                            UserRole role,
                                            Integer empId) throws SQLException {

        if (!securityService.isUsernameAvailable(username)) {
            System.out.println("User '" + username + "' already exists. Skipping.");
            return;
        }

        boolean created = securityService.createUser(username, password, role, empId);
        if (created) {
            System.out.println("Created user '" + username + "' with role " + role +
                               (empId != null ? " (empid=" + empId + ")" : ""));
        } else {
            System.out.println("Failed to create user '" + username + "'.");
        }
    }

    /**
     * Look up an employee by emp_number and create an EMPLOYEE user for them.
     */
    private static void createEmployeeUserIfMissing(SecurityService securityService,
                                                    EmployeeDAO employeeDAO,
                                                    String empNumber,
                                                    String username,
                                                    String password) throws SQLException {

        if (!securityService.isUsernameAvailable(username)) {
            System.out.println("User '" + username + "' already exists. Skipping.");
            return;
        }

        Employee employee = employeeDAO.searchByEmpNumber(empNumber);
        if (employee == null) {
            System.out.println("No employee found with emp_number '" + empNumber +
                               "'. Cannot create user '" + username + "'.");
            return;
        }

        Integer empId = employee.getEmpId();
        boolean created = securityService.createUser(username, password, UserRole.EMPLOYEE, empId);
        if (created) {
            System.out.println("Created EMPLOYEE user '" + username +
                               "' for emp_number " + empNumber + " (empid=" + empId + ").");
        } else {
            System.out.println("Failed to create user '" + username + "'.");
        }
    }
}
