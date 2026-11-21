package com.example.pro3.model;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "dept")
public class Dept implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "deptno")
    private Integer deptno;

    @Column(name = "dname")
    private String dname;

    @Column(name = "loc")
    private String loc;
}