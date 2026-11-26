package com.example.pro3.service;

import com.example.pro3.model.Expense;
import com.example.pro3.model.ExpenseReport;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.spark.sql.functions.coalesce;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.lit;
import static org.apache.spark.sql.functions.round;
import static org.apache.spark.sql.functions.sum;

/**
 * Expense report for one employee.
 * Spark fetches and filters the data from Cassandra and does all the calculations
 * (total and per-category totals). The web app never talks to Cassandra directly.
 */
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final SparkSession sparkSession;

    @Value("${app.cassandra.keyspace:test}")
    private String keyspace;

    @Value("${app.cassandra.table:expenses}")
    private String table;

    public ExpenseReport getReport(Integer empno) {
        // Spark reads Cassandra through the Spark-Cassandra connector.
        // user_id is the partition key, so this filter is pushed down to Cassandra.
        Dataset<Row> userExpenses = sparkSession.read()
                .format("org.apache.spark.sql.cassandra")
                .option("keyspace", keyspace)
                .option("table", table)
                .load()
                .filter(col("user_id").equalTo(empno))
                .cache(); // read Cassandra once, reuse for the 3 Spark actions below

        try {
            // 1) Expense list, newest first (Spark)
            List<Expense> expenses = new ArrayList<>();
            for (Row row : userExpenses.orderBy(col("date_time").desc()).collectAsList()) {
                Object payment = row.getAs("payment");
                expenses.add(new Expense(
                        row.getAs("user_id"),
                        row.getAs("date_time"),
                        row.getAs("description"),
                        row.getAs("type"),
                        row.getAs("count"),
                        payment == null ? 0.0 : ((Number) payment).doubleValue()
                ));
            }

            // 2) Total expense: SUM(payment) (Spark)
            Row totalRow = userExpenses.agg(round(sum("payment"), 2)).first();
            double total = totalRow.isNullAt(0) ? 0.0 : ((Number) totalRow.get(0)).doubleValue();

            // 3) Total per expense type: GROUP BY type (Spark)
            Map<String, Double> categoryTotals = new TreeMap<>();
            List<Row> categoryRows = userExpenses
                    .groupBy(coalesce(col("type"), lit("Other")).as("type"))
                    .agg(round(sum("payment"), 2).as("total"))
                    .collectAsList();
            for (Row r : categoryRows) {
                categoryTotals.put(r.getString(0), r.isNullAt(1) ? 0.0 : ((Number) r.get(1)).doubleValue());
            }

            return new ExpenseReport(expenses, total, categoryTotals);
        } finally {
            userExpenses.unpersist();
        }
    }
}
