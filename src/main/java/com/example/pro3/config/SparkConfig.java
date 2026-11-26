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
                // One employee has only a few hundred rows. The default of 200 shuffle
                // partitions makes every orderBy/groupBy start 200 tasks and slows each
                // page refresh by seconds. One partition is enough for this data size.
                .config("spark.sql.shuffle.partitions", "1")
                .getOrCreate();
    }
}
