package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.dto.PasswordForm;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/set-password")
@RequiredArgsConstructor
public class PasswordController {

    private final UserService userService;

    @GetMapping
    public String form(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("title", "Установить пароль");
        boolean alreadySet = !currentUser.getPhone().startsWith("google-") || currentUser.isPasswordSet();
        model.addAttribute("alreadySet", alreadySet);
        if (alreadySet) {
            return "auth/set_password";
        }
        model.addAttribute("form", new PasswordForm());
        return "auth/set_password";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("form") PasswordForm form,
                       BindingResult br,
                       @AuthenticationPrincipal User currentUser,
                       Model model) {
        model.addAttribute("title", "Установить пароль");
        boolean alreadySet = !currentUser.getPhone().startsWith("google-") || currentUser.isPasswordSet();
        model.addAttribute("alreadySet", alreadySet);
        if (alreadySet) {
            return "auth/set_password";
        }
        if (br.hasErrors()) {
            return "auth/set_password";
        }
        userService.updatePassword(currentUser, form.getPassword());
        return "redirect:/projects";
    }
}
