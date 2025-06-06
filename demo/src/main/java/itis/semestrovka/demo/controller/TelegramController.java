package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.service.UserService;
import itis.semestrovka.demo.service.telegram.TelegramService;
import itis.semestrovka.demo.service.oauth.GoogleOAuthService;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/telegram")
public class TelegramController {

    private final UserService userService;
    private final TelegramService telegramService;
    private final GoogleOAuthService googleOAuthService;

    public TelegramController(UserService userService, TelegramService telegramService, GoogleOAuthService googleOAuthService) {
        this.userService = userService;
        this.telegramService = telegramService;
        this.googleOAuthService = googleOAuthService;

    }

    @GetMapping("/register")
    public String register(@RequestParam String chatId,
                           @AuthenticationPrincipal User user,
                           HttpSession session) {
        userService.updateTelegramChatId(user, chatId);

        String username = (String) session.getAttribute("pendingUsername");
        String password = (String) session.getAttribute("pendingPassword");
        if (username != null && password != null) {
            String msg = "Ваш логин: " + username + "\nПароль: " + password;
            telegramService.sendMessage(chatId, msg);
            session.removeAttribute("pendingUsername");
            session.removeAttribute("pendingPassword");
        }

        return "ok";
    }

    /**
     * Endpoint used by the Telegram bot when the user opens the link with a
     * token. The bot passes the token and chat id so that the service can send
     * the credentials to this chat.
     */
    @GetMapping("/complete")
    public String complete(@RequestParam String token, @RequestParam String chatId) {
        googleOAuthService.completeTelegramRegistration(token, chatId);
        return "ok";
    }
}
