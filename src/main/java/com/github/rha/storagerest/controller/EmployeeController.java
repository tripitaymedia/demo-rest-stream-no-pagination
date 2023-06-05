package com.github.rha.storagerest.controller;

import com.github.rha.storagerest.model.Employee;
import com.github.rha.storagerest.service.EmployeeService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class EmployeeController {
    private Map<String, Employee> employeeMap = new HashMap<>();

    @Autowired
    EmployeeService employeeService;

    @PostConstruct
    public void init() throws Exception {
        // Load Cache
        AtomicInteger i = new AtomicInteger();
        employeeService.forEachObject(employee -> {
            i.getAndIncrement();
            employeeMap.put(employee.getEmployeeNumber(), employee);
            if ((i.get() % 1000) == 0) {
                System.out.println("Loaded " + i.get());
            }
            return true;
        });
    }

    @GetMapping("/rest/employee/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable("id") String id) {
        Employee employee = employeeMap.get(id);
        return ResponseEntity.ofNullable(employee);
    }
}
