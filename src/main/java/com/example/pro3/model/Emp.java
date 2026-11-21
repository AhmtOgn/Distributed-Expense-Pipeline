package com.example.pro3.model;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "emp")
public class Emp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "empno")
    private Integer empno;

    @Column(name = "ename")
    private String ename;

    @Column(name = "job")
    private String job;

    @Column(name = "mgr")
    private Integer mgr;

    @Column(name = "hiredate")
    private LocalDate hiredate;

    @Column(name = "sal")
    private BigDecimal sal;

    @Column(name = "comm")
    private BigDecimal comm;

    @Column(name = "img")
    private String img;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "deptno")
    private Dept dept;
}