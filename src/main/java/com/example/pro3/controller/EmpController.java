package com.example.pro3.controller;

import com.example.pro3.model.Emp;
import com.example.pro3.model.ExpenseReport;
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

        // Manager name comes from MySQL (emp.mgr -> emp.ename)
        String managerName = "-";
        if (emp.getMgr() != null) {
            Emp manager = empService.getEmpById(emp.getMgr());
            if (manager != null) {
                managerName = manager.getEname();
            }
        }

        // Expenses, total and per-category totals are all calculated by Spark
        ExpenseReport report = expenseService.getReport(id);

        model.addAttribute("emp", emp);
        model.addAttribute("managerName", managerName);
        model.addAttribute("expenses", report.getExpenses());
        model.addAttribute("totalPayment", report.getTotal());
        model.addAttribute("categoryLabels", new ArrayList<>(report.getCategoryTotals().keySet()));
        model.addAttribute("categoryValues", new ArrayList<>(report.getCategoryTotals().values()));
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