package org.example.Tasks2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.HttpBinResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Task4 {
    private String urlToOpen = "https://httpbin.org/headers";

    public Task4(){
    }

    /**
     * Метод, что отправляет запрос на указанный URL,
     * а после возвращающий заголовки запроса
     */
    public void UrlHeadersTask4(){
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlToOpen))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            HttpBinResponse parsed = mapper.readValue(response.body(), HttpBinResponse.class);

            String result = String.join(", ", parsed.getHeaders().values());
            System.out.println("Заголовки запроса: " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
