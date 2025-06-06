package itis.semestrovka.demo.service.impl;

import itis.semestrovka.demo.service.SlackService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SlackServiceImpl implements SlackService {

    private static final Logger log = LoggerFactory.getLogger(SlackServiceImpl.class);

    private final RestTemplate restTemplate;

    @Value("${slack.webhook-url}")
    private String webhookUrl;

    @Override
    public void sendTaskCreatedNotification(String projectName, String taskTitle, String authorUsername) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Slack webhook URL is not configured");
            return;
        }
        String text = String.format("Новая задача \"%s\" в проекте \"%s\" (автор: %s)",
                taskTitle, projectName, authorUsername);
        Map<String, String> payload = Map.of("content", text);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);
        try {
            restTemplate.postForEntity(webhookUrl, entity, String.class);
        } catch (Exception e) {
            log.error("Failed to send Slack notification", e);
        }
    }
}
