package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/telegram")
public class TelegramController {

    private final UserService userService;

    public TelegramController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String register(@RequestParam String chatId,
                           @AuthenticationPrincipal User user) {
        userService.updateTelegramChatId(user, chatId);
        return "ok";
    }
}
