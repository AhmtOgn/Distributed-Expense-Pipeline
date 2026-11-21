package com.example.pro3.repository;

import com.example.pro3.model.Emp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmpRepository extends JpaRepository<Emp, Integer> {
    List<Emp> findByDept_Deptno(Integer deptno);
}