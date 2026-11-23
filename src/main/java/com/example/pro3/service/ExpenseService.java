package com.example.pro3.service;

import com.example.pro3.model.Expense;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.apache.spark.sql.functions.col;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final SparkSession sparkSession;

    @Value("${app.cassandra.keyspace:test}")
    private String keyspace;

    @Value("${app.cassandra.table:expenses}")
    private String table;

    public List<Expense> getExpensesByUserId(Integer empno) {
        Dataset<Row> df = sparkSession.read()
                .format("org.apache.spark.sql.cassandra")
                .option("keyspace", keyspace)
                .option("table", table)
                .load();

        List<Row> rows = df.filter(col("user_id").equalTo(empno))
                .orderBy(col("date_time").desc())
                .collectAsList();

        List<Expense> expenses = new ArrayList<>();
        for (Row row : rows) {
            expenses.add(new Expense(
                    row.getAs("user_id"),
                    row.getAs("date_time"),
                    row.getAs("description"),
                    row.getAs("type"),
                    row.getAs("count"),
                    ((Number) row.getAs("payment")).doubleValue()
            ));
        }
        return expenses;
    }
}