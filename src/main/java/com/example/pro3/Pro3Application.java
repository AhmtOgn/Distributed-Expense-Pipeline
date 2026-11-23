package com.example.pro3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class Pro3Application {

    public static void main(String[] args) {
        SpringApplication.run(Pro3Application.class, args);
    }

}
