package com.example.pro3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    private Integer userId;
    private String dateTime;
    private String description;
    private String type;
    private Integer count;
    private Double payment;
}