package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.service.UserService;
import itis.semestrovka.demo.service.telegram.TelegramService;
import itis.semestrovka.demo.service.oauth.GoogleOAuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
                           @AuthenticationPrincipal User user) {
        userService.updateTelegramChatId(user, chatId);
        return "ok";
    }

    /**
     * Endpoint used by the Telegram bot to finish Google registration.
     * It expects a token generated during OAuth login, the phone number
     * entered by the user in Telegram and the chat id to reply to.
     */
    @GetMapping("/complete")
    public String complete(@RequestParam String token,
                           @RequestParam String phone,
                           @RequestParam String chatId) {
        googleOAuthService.completePhoneRegistration(token, phone, chatId);
        return "ok";
    }
}
