package itis.semestrovka.demo.service.impl;

import itis.semestrovka.demo.service.sms.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class SmsServiceImpl implements SmsService {

    @Value("${sms.api-url}")
    private String apiUrl;

    @Value("${sms.api-id}")
    private String apiId;

    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public void sendSms(String phone, String message) {
        try {
            String url = apiUrl + "?api_id=" + encode(apiId)
                    + "&to=" + encode(phone)
                    + "&msg=" + encode(message)
                    + "&json=1";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send SMS", e);
        }
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
