package itis.semestrovka.demo.controller.oauth;

import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.service.oauth.GoogleOAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/oauth2")
public class GoogleOAuthController {

    private final GoogleOAuthService googleOAuthService;

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
                           HttpSession session) throws Exception {
        GoogleOAuthService.OAuthResult result = googleOAuthService.processCallback(code, state, session);

        session.setAttribute("pendingUsername", result.user().getUsername());
        if (session.getAttribute("pendingPassword") != null) {
            session.removeAttribute("pendingPassword");
        }

        UsernamePasswordAuthenticationToken auth =
                UsernamePasswordAuthenticationToken.authenticated(
                        result.user(), result.user().getPassword(), result.user().getAuthorities());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);


        return "redirect:/projects";

    }
}
