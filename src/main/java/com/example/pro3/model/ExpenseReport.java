package com.example.pro3.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Result of the Spark job for one employee.
 * Everything in here is computed by Spark; the controller only displays it.
 */
@Data
@AllArgsConstructor
public class ExpenseReport {
    /** All expenses of the employee, newest first. */
    private List<Expense> expenses;
    /** SUM(payment) for the employee. */
    private double total;
    /** SUM(payment) grouped by expense type. */
    private Map<String, Double> categoryTotals;
}
