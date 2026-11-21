package com.example.pro3.repository;

import com.example.pro3.model.Dept;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeptRepository extends JpaRepository<Dept, Integer> {
}