package itis.semestrovka.demo.service.impl;

import itis.semestrovka.demo.service.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${email.api-url}")
    private String apiUrl;

    @Value("${email.api-key}")
    private String apiKey;

    @Value("${email.from}")
    private String from;

    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            StringBuilder data = new StringBuilder();
            data.append("api_key=").append(encode(apiKey))
                    .append("&email=").append(encode(to))
                    .append("&sender_email=").append(encode(from))
                    .append("&sender_name=").append(encode(from))
                    .append("&subject=").append(encode(subject))
                    .append("&body=").append(encode(body));

            String url = apiUrl + "?format=json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
