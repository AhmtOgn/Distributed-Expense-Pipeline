package com.example.pro3.service;

import com.example.pro3.model.Dept;
import com.example.pro3.model.Emp;
import com.example.pro3.repository.DeptRepository;
import com.example.pro3.repository.EmpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpService {

    private final EmpRepository empRepository;
    private final DeptRepository deptRepository;

    public List<Emp> getAllEmps() {
        return empRepository.findAll();
    }

    @Cacheable(value = "empsByDept", key = "#deptno")
    public List<Emp> getEmpsByDept(Integer deptno) {
        return empRepository.findByDept_Deptno(deptno);
    }

    @Cacheable(value = "emp", key = "#empno")
    public Emp getEmpById(Integer empno) {
        return empRepository.findById(empno).orElse(null);
    }

    public List<Dept> getAllDepts() {
        return deptRepository.findAll();
    }
}