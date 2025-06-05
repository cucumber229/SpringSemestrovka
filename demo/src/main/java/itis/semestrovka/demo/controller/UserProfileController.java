package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.model.entity.UserProfile;
import itis.semestrovka.demo.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    @GetMapping("/profile")
    public String view(@AuthenticationPrincipal User currentUser, Model model) {
        UserProfile profile = profileService.findByUserId(currentUser.getId());
        model.addAttribute("profile", profile);
        model.addAttribute("title", "Профиль");
        return "profile/view";
    }

    @GetMapping("/users/{userId}/profile")
    public String viewByUserId(@PathVariable Long userId, Model model) {
        UserProfile profile = profileService.findByUserId(userId);
        model.addAttribute("profile", profile);
        model.addAttribute("title", "Профиль пользователя");
        return "profile/view";
    }

    @GetMapping("/profile/edit")
    public String editForm(@AuthenticationPrincipal User currentUser, Model model) {
        UserProfile profile = profileService.findByUserId(currentUser.getId());
        model.addAttribute("profile", profile);
        model.addAttribute("title", "Редактировать профиль");
        return "profile/form";
    }

    @PostMapping("/profile")
    public String save(@AuthenticationPrincipal User currentUser, UserProfile profile) {
        profile.setUser(currentUser);
        profileService.save(profile);
        return "redirect:/profile";
    }
}
