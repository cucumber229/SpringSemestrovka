package itis.semestrovka.demo.service;

public interface SlackService {
    void sendTaskCreatedNotification(String projectName, String taskTitle, String authorUsername);
}
