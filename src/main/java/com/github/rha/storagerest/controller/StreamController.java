package com.github.rha.storagerest.controller;

import com.azure.core.annotation.QueryParam;
import com.azure.storage.blob.BlobContainerClient;
import com.github.rha.storagerest.service.EmployeeService;
import com.github.rha.storagerest.service.FieldFilterService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
public class StreamController {
    @Value("${allemployees.file.name}")
    private String allEmployeesFileName;

    private static final Logger log = LoggerFactory.getLogger(StreamController.class);

    @Autowired
    private FieldFilterService cbacLogic;

    @Autowired
    private BlobContainerClient blobContainerClient;

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/rest/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/rest/all")
    public void allRecords(HttpServletResponse response, @QueryParam("clientId") String clientId) throws Exception {
        try (var out = response.getOutputStream()) {
            employeeService.forEachLine(line -> {
                try {
                    String newJsonWithCbac = cbacLogic.apply(clientId, line);
                    out.write(newJsonWithCbac.getBytes(StandardCharsets.UTF_8));
                    out.write('\n');
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return true;
            });
            out.flush();
        } // try
    }
}
