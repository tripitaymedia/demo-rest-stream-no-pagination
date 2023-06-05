package com.github.rha.storagerest;

import com.azure.core.annotation.QueryParam;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.github.rha.storagerest.model.Address;
import com.github.rha.storagerest.model.Employee;
import com.github.rha.storagerest.model.WorkLocation;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@RestController
public class EmployeeController {
    @Value("${allemployees.file.name}")
    private String allEmployeesFileName;

    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private FieldFilterService cbacLogic;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BlobContainerClient blobContainerClient;

    @GetMapping("/rest/load")
    public ResponseEntity<String> allRecords() throws IOException {
        var start = Instant.now();
        BlobClient blobClient = blobContainerClient.getBlobClient(allEmployeesFileName);
        var is = new PipedInputStream();
        var out = new PipedOutputStream(is);
        var t = new Thread(() -> {
            try (out) {
                for (int i = 1; i <= 300_000; i++) {
                //for (int i = 0; i < 100; i++) {
                    if ((i % 1000) == 0) {
                        log.info("records: {}", i);
                    }
                    Employee model = generateEntityModel(i);
                    String line = null;
                    try {
                        line = objectMapper.writeValueAsString(model);
                    } catch (JsonProcessingException e) {
                        log.error("error serializing json", e);
                    }
                    out.write(line.strip().getBytes(StandardCharsets.UTF_8));
                    out.write('\n');
                } // for

                out.flush();
            } catch (IOException e) {
                log.error("error", e);
                throw new RuntimeException(e);
            }
        }); // thread
        t.setDaemon(true);
        t.start();

        blobClient.upload(is, true);
        var end = Instant.now();
        Duration duration = Duration.between(start, end);

        return ResponseEntity.ok().body("""
                {"status": "ok", "duration": """ + duration.getSeconds() + """
                }""");
    }


    @GetMapping("/rest/all")
    public void allRecords(HttpServletResponse response, @QueryParam("clientId") String clientId) throws Exception {
        try (
            var out = response.getOutputStream();
        ) {
            //
            BlobClient blobClient = blobContainerClient.getBlobClient(allEmployeesFileName);
            try (
                BlobInputStream is = blobClient.openInputStream();
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)
            ) {
                for (String jsonLine = reader.readLine(); jsonLine != null; jsonLine = reader.readLine()) {
                    String newJsonWithCbac = cbacLogic.apply(clientId, jsonLine);
                    newJsonWithCbac = newJsonWithCbac.strip();
                    out.write(newJsonWithCbac.getBytes(StandardCharsets.UTF_8));
                    out.write('\n');
                }
            } // try
            out.flush();
        } // try
    }


    private Faker faker = new Faker();

    private Employee generateEntityModel(int i) {
        Employee model = new Employee();
        model.setEmployeeNumber( String.format("%06d", i) );
        model.setFirstName(faker.name().firstName());
        model.setLastName(faker.name().lastName());
        model.setWorkLocation(generateWorkLocation(i));
        model.setEmail(faker.internet().emailAddress());
        model.setHomeAddress(new Address(
                faker.address().streetAddress(),
                faker.address().cityName(),
                faker.address().state(),
                faker.address().zipCode(),
                faker.address().countryCode()
        ));
        model.setWorkAddress(new Address(
                faker.address().streetAddress(),
                faker.address().cityName(),
                faker.address().state(),
                faker.address().zipCode(),
                faker.address().countryCode()
        ));

        model.setPhone(faker.phoneNumber().cellPhone());
        model.setWorkPhone(faker.phoneNumber().phoneNumber());
        model.setSalary(choose(salaryList));
        model.setSsn(faker.number().digits(9));

        return model;
    }
    private static final String[] salaryList = new String[]{
            "30.000", "33.000", "35.000", "37.000", "40.000", "43.000", "45.000",
            "47.000", "50.000", "53.000", "55.000", "57.000", "60.000", "65.000",
            "70.000", "75.000", "80.000", "85.000", "90.000", "95.000", "100.000", "110.000", "120.000"
    };

    WorkLocation generateWorkLocation(int en) {
        var store =    (en / 25) + 1;
        var district = (en / (25 * 2)) + 1;
        var area =     (en / (25 * 2 * 2)) + 1;
        var region =   (en / (25 * 2 * 2 * 2)) + 1;
        var zone =     (en / (25 * 2 * 2 * 2 * 2)) + 1;
        return new WorkLocation(
                zone,
                region,
                area,
                district,
                store
        );
    }


    private Random random = new Random();
    public <T> T choose(T[] arr) {
        return arr[random.nextInt(arr.length)];
    }

}
