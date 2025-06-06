package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.service.UserService;
import itis.semestrovka.demo.service.telegram.TelegramService;
import jakarta.servlet.http.HttpSession;
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

    public TelegramController(UserService userService, TelegramService telegramService) {
        this.userService = userService;
        this.telegramService = telegramService;
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
}
