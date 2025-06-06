package itis.semestrovka.demo.controller.oauth;

import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.service.UserService;
import itis.semestrovka.demo.service.sms.SmsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/oauth2/phone")
public class PhoneController {

    private final UserService userService;
    private final SmsService smsService;

    public PhoneController(UserService userService, SmsService smsService) {
        this.userService = userService;
        this.smsService = smsService;
    }

    @GetMapping
    public String phoneForm(Model model) {
        model.addAttribute("title", "Введите телефон");
        return "oauth/phone";
    }

    @PostMapping
    public String submitPhone(@RequestParam String phone,
                              HttpSession session,
                              @AuthenticationPrincipal User user) {
        userService.updatePhone(user, phone);
        String password = (String) session.getAttribute("oauth_password");
        session.removeAttribute("oauth_password");
        if (password != null) {
            String msg = "Ваш логин: " + user.getUsername() + "\nПароль: " + password;
            smsService.sendSms(phone, msg);
        }
        return "redirect:/projects";
    }
}
