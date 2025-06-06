package itis.semestrovka.demo.service.telegram;

/**
 * Simple service for sending notifications via the Telegram Bot API.
 */
public interface TelegramService {
    void sendMessage(String chatId, String text);

}
