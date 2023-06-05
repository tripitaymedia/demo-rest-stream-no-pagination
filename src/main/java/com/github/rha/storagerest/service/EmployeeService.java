package com.github.rha.storagerest.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rha.storagerest.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Function;

@Service
public class EmployeeService {

    @Value("${allemployees.file.name}")
    private String allEmployeesFileName;

    @Autowired
    private BlobContainerClient blobContainerClient;

    @Autowired
    ObjectMapper objectMapper;

    public void forEachObject(Function<Employee, Boolean> employeeConsumer) throws Exception {
        forEachLine(line -> {
            try {
                var employee = objectMapper.readValue(line, Employee.class);
                return employeeConsumer.apply(employee);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void forEachLine(Function<String, Boolean> lineConsumer) throws Exception {
        //
        BlobClient blobClient = blobContainerClient.getBlobClient(allEmployeesFileName);
        try (
                BlobInputStream is = blobClient.openInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr)
        ) {
            for (String jsonLine = reader.readLine(); jsonLine != null; jsonLine = reader.readLine()) {
                boolean continueExecution = lineConsumer.apply(jsonLine.strip());
                if (!continueExecution) {
                    is.close();
                    break;
                }
            }
        } // try
    }
}
