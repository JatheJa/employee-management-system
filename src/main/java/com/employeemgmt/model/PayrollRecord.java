package com.employeemgmt.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * PayrollRecord entity class representing the payroll table in database.
 * Supports pay statement history and summary reporting.
 *
 * @author 
 */
public class PayrollRecord {

    private int payrollId;
    private int empId;

    private LocalDate payDate;
    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;

    private BigDecimal grossPay;
    private BigDecimal netPay;
    private BigDecimal federalTax;
    private BigDecimal stateTax;
    private BigDecimal otherDeductions;

    // Populated in joined queries
    private String employeeFirstName;
    private String employeeLastName;
    private String employeeNumber;

    public PayrollRecord() {
        this.grossPay = BigDecimal.ZERO;
        this.netPay = BigDecimal.ZERO;
        this.federalTax = BigDecimal.ZERO;
        this.stateTax = BigDecimal.ZERO;
        this.otherDeductions = BigDecimal.ZERO;
    }

    public PayrollRecord(int empId,
                         LocalDate payDate,
                         LocalDate payPeriodStart,
                         LocalDate payPeriodEnd,
                         BigDecimal grossPay,
                         BigDecimal netPay) {

        this.empId = empId;
        this.payDate = payDate;
        this.payPeriodStart = payPeriodStart;
        this.payPeriodEnd = payPeriodEnd;
        this.grossPay = (grossPay != null ? grossPay : BigDecimal.ZERO);
        this.netPay  = (netPay != null ? netPay : BigDecimal.ZERO);

        this.federalTax = BigDecimal.ZERO;
        this.stateTax = BigDecimal.ZERO;
        this.otherDeductions = BigDecimal.ZERO;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public int getPayrollId() {
        return payrollId;
    }

    public void setPayrollId(int payrollId) {
        this.payrollId = payrollId;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public LocalDate getPayDate() {
        return payDate;
    }

    public void setPayDate(LocalDate payDate) {
        this.payDate = payDate;
    }

    public LocalDate getPayPeriodStart() {
        return payPeriodStart;
    }

    public void setPayPeriodStart(LocalDate payPeriodStart) {
        this.payPeriodStart = payPeriodStart;
    }

    public LocalDate getPayPeriodEnd() {
        return payPeriodEnd;
    }

    public void setPayPeriodEnd(LocalDate payPeriodEnd) {
        this.payPeriodEnd = payPeriodEnd;
    }

    public BigDecimal getGrossPay() {
        return grossPay;
    }

    public void setGrossPay(BigDecimal grossPay) {
        this.grossPay = grossPay != null ? grossPay : BigDecimal.ZERO;
    }

    public BigDecimal getNetPay() {
        return netPay;
    }

    public void setNetPay(BigDecimal netPay) {
        this.netPay = netPay != null ? netPay : BigDecimal.ZERO;
    }

    public BigDecimal getFederalTax() {
        return federalTax;
    }

    public void setFederalTax(BigDecimal federalTax) {
        this.federalTax = federalTax != null ? federalTax : BigDecimal.ZERO;
    }

    public BigDecimal getStateTax() {
        return stateTax;
    }

    public void setStateTax(BigDecimal stateTax) {
        this.stateTax = stateTax != null ? stateTax : BigDecimal.ZERO;
    }

    public BigDecimal getOtherDeductions() {
        return otherDeductions;
    }

    public void setOtherDeductions(BigDecimal otherDeductions) {
        this.otherDeductions = otherDeductions != null ? otherDeductions : BigDecimal.ZERO;
    }

    public String getEmployeeFirstName() {
        return employeeFirstName;
    }

    public void setEmployeeFirstName(String employeeFirstName) {
        this.employeeFirstName = employeeFirstName;
    }

    public String getEmployeeLastName() {
        return employeeLastName;
    }

    public void setEmployeeLastName(String employeeLastName) {
        this.employeeLastName = employeeLastName;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    public String getEmployeeFullName() {
        if (employeeFirstName != null && employeeLastName != null) {
            return employeeFirstName + " " + employeeLastName;
        }
        return "";
    }

    public String getFormattedPayPeriod() {
        if (payPeriodStart != null && payPeriodEnd != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            return payPeriodStart.format(fmt) + " - " + payPeriodEnd.format(fmt);
        }
        return "";
    }

    public String getFormattedPayDate() {
        return payDate != null
            ? payDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            : "";
    }

    public BigDecimal getTotalDeductions() {
        return federalTax.add(stateTax).add(otherDeductions);
    }

    public double getEffectiveTaxRate() {
        if (grossPay.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalTax = federalTax.add(stateTax);
            return totalTax
                    .divide(grossPay, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100.0))
                    .doubleValue();
        }
        return 0.0;
    }

    public boolean isValid() {
        return empId > 0
            && payDate != null
            && payPeriodStart != null
            && payPeriodEnd != null
            && !payPeriodStart.isAfter(payPeriodEnd)  // allow start == end
            && grossPay.compareTo(BigDecimal.ZERO) >= 0
            && netPay.compareTo(BigDecimal.ZERO) >= 0
            && federalTax.compareTo(BigDecimal.ZERO) >= 0
            && stateTax.compareTo(BigDecimal.ZERO) >= 0
            && otherDeductions.compareTo(BigDecimal.ZERO) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayrollRecord)) return false;
        PayrollRecord that = (PayrollRecord) o;
        return payrollId == that.payrollId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(payrollId);
    }

    @Override
    public String toString() {
        return String.format(
            "PayrollRecord{id=%d, empId=%d, payDate=%s, gross=%s, net=%s}",
            payrollId, empId, payDate, grossPay, netPay
        );
    }
}
