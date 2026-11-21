package com.example.pro3.config;

import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfig {

    @Value("${app.cassandra.host:127.0.0.1}")
    private String cassandraHost;

    @Bean(destroyMethod = "stop")
    public SparkSession sparkSession() {
        return SparkSession.builder()
                .appName("Pro3WebApp")
                .master("local[*]")
                .config("spark.cassandra.connection.host", cassandraHost)
                .config("spark.ui.enabled", "false")
                .getOrCreate();
    }
}