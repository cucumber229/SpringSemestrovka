package itis.semestrovka.demo.controller.oauth;

import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.service.oauth.GoogleOAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
@RequestMapping("/oauth2")
public class GoogleOAuthController {

    private final GoogleOAuthService googleOAuthService;
    @Value("${telegram.bot-link:https://t.me/your_bot}")
    private String botLink;

    public GoogleOAuthController(GoogleOAuthService googleOAuthService) {
        this.googleOAuthService = googleOAuthService;
    }

    @GetMapping("/authorize/google")
    public String authorize(HttpSession session) {
        String url = googleOAuthService.buildAuthorizationUrl(session);
        return "redirect:" + url;
    }

    @GetMapping("/callback/google")
    public String callback(@RequestParam String code,
                           @RequestParam String state,
                           HttpSession session,
                           Model model) throws Exception {
        User user = googleOAuthService.processCallback(code, state, session);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        if (user.getPhone() != null && user.getPhone().startsWith("google-")) {
            return "auth/phone";
        }
        if (user.getTelegramChatId() == null) {
            model.addAttribute("botLink", botLink);
            return "telegram/redirect";
        }
        return "redirect:/projects";
    }

    @PostMapping("/phone")
    public String submitPhone(@RequestParam String phone,
                              @AuthenticationPrincipal User user,
                              HttpSession session) {
        googleOAuthService.updatePhoneAndNotify(user, phone, session);
        return "redirect:/projects";
    }
}
