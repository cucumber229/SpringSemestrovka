package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.entity.Role;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("title", "Регистрация");
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") User user,
            BindingResult br,
            Model model
    ) {
        // 1) если есть ошибки валидации — сразу возвращаем форму
        if (br.hasErrors()) {
            model.addAttribute("title", "Регистрация");
            return "auth/register";
        }

        // 2) проверяем заранее, есть ли уже такой username
        if (userRepo.existsByUsername(user.getUsername())) {
            br.rejectValue("username", "duplicate", "Пользователь с таким именем уже существует");
            model.addAttribute("title", "Регистрация");
            return "auth/register";
        }

        // 3) кодируем пароль, ставим роль и сохраняем
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);
        userRepo.save(user);

        // 4) редирект на логин с флагом registered=true
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String loginForm(
            Model model,
            @RequestParam(required = false) String registered
    ) {
        model.addAttribute("title", "Вход");
        model.addAttribute("registered", registered != null);
        return "auth/login";
    }
}
