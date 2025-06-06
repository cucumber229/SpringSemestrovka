package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.dto.ChangePasswordForm;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/change-password")
@RequiredArgsConstructor
public class ChangePasswordController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String form(Model model) {
        model.addAttribute("title", "Сменить пароль");
        model.addAttribute("form", new ChangePasswordForm());
        return "auth/change_password";
    }

    @PostMapping
    public String change(@Valid @ModelAttribute("form") ChangePasswordForm form,
                         BindingResult br,
                         @AuthenticationPrincipal User currentUser,
                         Model model) {
        model.addAttribute("title", "Сменить пароль");
        if (!passwordEncoder.matches(form.getCurrentPassword(), currentUser.getPassword())) {
            br.rejectValue("currentPassword", "invalid", "Неверный текущий пароль");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            br.rejectValue("confirmPassword", "mismatch", "Пароли не совпадают");
        }
        if (br.hasErrors()) {
            return "auth/change_password";
        }
        userService.updatePassword(currentUser, form.getNewPassword());
        return "redirect:/profile";
    }
}
