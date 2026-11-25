package com.example.pro3.controller;

import com.example.pro3.model.Emp;
import com.example.pro3.model.Expense;
import com.example.pro3.service.EmpService;
import com.example.pro3.service.ExpenseService;
import com.example.pro3.service.HdfsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class EmpController {

    private final EmpService empService;
    private final ExpenseService expenseService;
    private final HdfsService hdfsService;

    @GetMapping("/")
    public String index(@RequestParam(required = false) Integer deptno, Model model) {
        List<Emp> emps = (deptno != null) ? empService.getEmpsByDept(deptno) : empService.getAllEmps();
        model.addAttribute("emps", emps);
        model.addAttribute("depts", empService.getAllDepts());
        model.addAttribute("selectedDept", deptno);
        return "index";
    }

    @GetMapping("/emp/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Emp emp = empService.getEmpById(id);
        if (emp == null) {
            return "redirect:/";
        }

        List<Expense> expenses = expenseService.getExpensesByUserId(id);
        double totalPayment = expenses.stream().mapToDouble(Expense::getPayment).sum();

        Map<String, Double> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getType() != null ? e.getType() : "Other",
                        TreeMap::new,
                        Collectors.summingDouble(Expense::getPayment)
                ));

        model.addAttribute("emp", emp);
        model.addAttribute("expenses", expenses);
        model.addAttribute("totalPayment", totalPayment);
        model.addAttribute("categoryLabels", new ArrayList<>(categoryTotals.keySet()));
        model.addAttribute("categoryValues", new ArrayList<>(categoryTotals.values()));
        return "detail";
    }

    @GetMapping("/image/{filename}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws IOException {
        byte[] img = hdfsService.getImage(filename);
        if (img == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(img);
    }
}