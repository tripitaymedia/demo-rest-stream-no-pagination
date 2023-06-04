package com.github.rha.storagerest;

import com.github.rha.storagerest.model.WorkLocation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

class RestEndpointTest {

    private EmployeeController restEndpoint = new EmployeeController();

    @Test
    public void genZrad() {
        var given = 1;
        var expected = new WorkLocation(1, 1, 1, 1, 1);
        var zrad = restEndpoint.generateWorkLocation(given);
        Assertions.assertThat(zrad).isEqualTo(expected);

    }

    @Test
    public void genZradGiven26() {
        var given = 26;
        var expected = new WorkLocation(1, 1, 1, 1, 2);
        var zrad = restEndpoint.generateWorkLocation(given);
        Assertions.assertThat(zrad).isEqualTo(expected);
    }

    @Test
    public void genZradGiven51() {
        var given = 51;
        var expected = new WorkLocation(1, 1, 1, 2, 3);
        var zrad = restEndpoint.generateWorkLocation(given);
        Assertions.assertThat(zrad).isEqualTo(expected);
    }

    @Test
    public void download() throws IOException {
        var start = Instant.now();
        httpGetInputStream("http://localhost:8080/rest/all", inputStream -> {
                //
                try (
                        InputStreamReader isr = new InputStreamReader(inputStream);
                        BufferedReader reader = new BufferedReader(isr)
                ) {
                    int i = 0;
                    for (String jsonLine = reader.readLine(); jsonLine != null; jsonLine = reader.readLine()) {
                        i++;
                        if ((i % 1000) == 0) {
                            System.out.println(i);
                        }
                    }
                } // try
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
        });
        System.out.println("###########################################");
        System.out.println("Duration: " + Duration.between(start, Instant.now()).getSeconds());
        System.out.println("###########################################");
    }
    public void httpGetInputStream(String url, Consumer<InputStream> streamConsumer) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        final ResponseExtractor responseExtractor =
                (ClientHttpResponse clientHttpResponse) -> {
                    streamConsumer.accept(clientHttpResponse.getBody());
                    return null;
                };

        restTemplate.execute(url, HttpMethod.GET, null, responseExtractor);
    }
}