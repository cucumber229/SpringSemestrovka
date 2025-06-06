package itis.semestrovka.demo.service.telegram;

/**
 * Simple service for sending notifications via the Telegram Bot API.
 */
public interface TelegramService {
    void sendMessage(String chatId, String text);

    /**
     * Send a message identified by phone number. Default implementation simply
     * treats the phone as a chat id, allowing custom implementations to
     * override this behaviour if needed.
     */
    default void sendMessageToPhone(String phone, String text) {
        sendMessage(phone, text);
    }
}
