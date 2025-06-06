
package itis.semestrovka.demo.service.impl;

import itis.semestrovka.demo.service.telegram.TelegramService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class TelegramServiceImpl implements TelegramService {

    @Value("${telegram.bot-token}")
    private String botToken;

    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public void sendMessage(String chatId, String text) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            String body = "chat_id=" + encode(chatId) + "&text=" + encode(text);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send Telegram message", e);
        }
    }

    @Override
    public void sendMessageToPhone(String phone, String text) {
        sendMessage(phone, text);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}