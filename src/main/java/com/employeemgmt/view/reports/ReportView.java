package com.employeemgmt.view.reports;

import java.util.Objects;
import java.util.logging.Logger;

import com.employeemgmt.model.Employee;
import com.employeemgmt.util.UserRole;
import com.employeemgmt.view.payroll.PayStatementView;

/**
 * Facade for report-related view logic.
 *
 * This class does NOT correspond to a specific JavaFX screen. Instead,
 * it centralizes access to the individual report view classes:
 *
 * <ul>
 *   <li>{@link HiringDateRangeReport}</li>
 *   <li>{@link MonthlyPayByDivisionReport}</li>
 *   <li>{@link MonthlyPayByJobTitleReport}</li>
 *   <li>{@link PayStatementView} (pay statement history)</li>
 * </ul>
 *
 * Responsibilities:
 * <ul>
 *   <li>Hold and propagate current user context (role + employee).</li>
 *   <li>Provide a single place where controllers or higher-level
 *       components can obtain view-layer helpers for reports.</li>
 * </ul>
 */
public class ReportView {

    private static final Logger LOGGER = Logger.getLogger(ReportView.class.getName());

    // Underlying report-specific view helpers
    private final HiringDateRangeReport hiringDateRangeReport;
    private final MonthlyPayByDivisionReport monthlyPayByDivisionReport;
    private final MonthlyPayByJobTitleReport monthlyPayByJobTitleReport;
    private final PayStatementView payStatementView;

    // Current user context
    private UserRole currentUserRole = UserRole.HR_ADMIN;
    private Employee currentUser;

    /**
     * Convenience constructor that wires up default instances of each
     * report view helper.
     */
    public ReportView() {
        this(
            new HiringDateRangeReport(),
            new MonthlyPayByDivisionReport(),
            new MonthlyPayByJobTitleReport(),
            new PayStatementView()
        );
    }

    /**
     * Primary constructor for dependency injection / testing.
     */
    public ReportView(HiringDateRangeReport hiringDateRangeReport,
                      MonthlyPayByDivisionReport monthlyPayByDivisionReport,
                      MonthlyPayByJobTitleReport monthlyPayByJobTitleReport,
                      PayStatementView payStatementView) {

        this.hiringDateRangeReport =
                Objects.requireNonNull(hiringDateRangeReport, "hiringDateRangeReport cannot be null");
        this.monthlyPayByDivisionReport =
                Objects.requireNonNull(monthlyPayByDivisionReport, "monthlyPayByDivisionReport cannot be null");
        this.monthlyPayByJobTitleReport =
                Objects.requireNonNull(monthlyPayByJobTitleReport, "monthlyPayByJobTitleReport cannot be null");
        this.payStatementView =
                Objects.requireNonNull(payStatementView, "payStatementView cannot be null");
    }

    /**
     * Set current user context and propagate it to all underlying report views.
     */
    public void setUserContext(UserRole role, Employee user) {
        this.currentUserRole = role != null ? role : UserRole.HR_ADMIN;
        this.currentUser = user;

        LOGGER.info(
            "ReportView user context updated: role=" + this.currentUserRole +
            ", user=" + (user != null ? user.getEmpId() : "null")
        );

        // Propagate to sub-views that also support user context
        hiringDateRangeReport.setUserContext(this.currentUserRole, this.currentUser);
        monthlyPayByDivisionReport.setUserContext(this.currentUserRole, this.currentUser);
        monthlyPayByJobTitleReport.setUserContext(this.currentUserRole, this.currentUser);
        payStatementView.setUserContext(this.currentUserRole, this.currentUser);
    }

    // ---------------------------------------------------------------------
    // Accessors for underlying report views
    // ---------------------------------------------------------------------

    public HiringDateRangeReport getHiringDateRangeReport() {
        return hiringDateRangeReport;
    }

    public MonthlyPayByDivisionReport getMonthlyPayByDivisionReport() {
        return monthlyPayByDivisionReport;
    }

    public MonthlyPayByJobTitleReport getMonthlyPayByJobTitleReport() {
        return monthlyPayByJobTitleReport;
    }

    public PayStatementView getPayStatementView() {
        return payStatementView;
    }

    // Optional: expose user context for consumers that need to inspect it

    public UserRole getCurrentUserRole() {
        return currentUserRole;
    }

    public Employee getCurrentUser() {
        return currentUser;
    }
}
