package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.dto.RegistrationForm;
import itis.semestrovka.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        model.addAttribute("title", "Регистрация");
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("form") RegistrationForm form,
            BindingResult br,
            Model model
    ) {
        model.addAttribute("title", "Регистрация");
        // 1) валидация DTO
        if (br.hasErrors()) {
            return "auth/register";
        }
        try {
            // 2) делегируем регистрацию в сервис
            userService.register(form);
        } catch (IllegalArgumentException ex) {
            // 3) ловим дубликат и возвращаем форму
            br.rejectValue("username", "duplicate", ex.getMessage());
            return "auth/register";
        }
        // 4) успех — редиректим на логин с флагом
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String loginForm(
            Model model,
            @RequestParam(required = false) String registered,
            @RequestParam(required = false) String error,
            HttpSession session
    ) {
        model.addAttribute("title", "Вход");
        model.addAttribute("registered", registered != null);
        model.addAttribute("loginError", error != null);
        model.addAttribute("prefillUsername", session.getAttribute("pendingUsername"));
        model.addAttribute("prefillPassword", session.getAttribute("pendingPassword"));
        session.removeAttribute("pendingUsername");
        session.removeAttribute("pendingPassword");
        return "auth/login";
    }
}
