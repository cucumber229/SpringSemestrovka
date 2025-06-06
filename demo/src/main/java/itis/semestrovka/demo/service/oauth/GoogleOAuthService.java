package itis.semestrovka.demo.service.oauth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import itis.semestrovka.demo.model.entity.Role;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@Service
@Transactional
public class GoogleOAuthService {
    @Value("${google.client-id}")
    private String clientId;
    @Value("${google.client-secret}")
    private String clientSecret;
    @Value("${google.redirect-uri}")
    private String redirectUri;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper mapper = new ObjectMapper();
    public GoogleOAuthService(UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public String buildAuthorizationUrl(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("oauth2_state", state);
        String base = "https://accounts.google.com/o/oauth2/v2/auth";
        String url = base
                + "?client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=" + encode("openid email profile")
                + "&state=" + encode(state);
        return url;
    }
    public OAuthResult processCallback(String code, String state, HttpSession session)
            throws IOException, InterruptedException {
        String savedState = (String) session.getAttribute("oauth2_state");
        session.removeAttribute("oauth2_state");
        if (savedState == null || !savedState.equals(state)) {
            throw new IllegalStateException("Invalid state");
        }
        String accessToken = fetchAccessToken(code);
        GoogleUserInfo info = fetchUserInfo(accessToken);
        Optional<User> existing = userRepository.findByEmail(info.email());
        if (existing.isPresent()) {
            return new OAuthResult(existing.get());
        }
        RegisteredUser created = registerUser(info, session);
        return new OAuthResult(created.user());
    }
    private String fetchAccessToken(String code) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(ofFormData(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "code", code,
                        "redirect_uri", redirectUri,
                        "grant_type", "authorization_code"
                )))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Failed to obtain token, status=" + response.statusCode());
        }
        JsonNode node = mapper.readTree(response.body());
        return node.get("access_token").asText();
    }
    private GoogleUserInfo fetchUserInfo(String accessToken) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Failed to obtain user info, status=" + response.statusCode());
        }
        JsonNode node = mapper.readTree(response.body());
        return new GoogleUserInfo(
                node.get("id").asText(),
                node.get("email").asText(),
                node.has("name") ? node.get("name").asText() : node.get("email").asText()
        );
    }
    private RegisteredUser registerUser(GoogleUserInfo info, HttpSession session) {
        String baseUsername = info.name().replaceAll("\\s+", "").toLowerCase();
        if (baseUsername.isEmpty()) {
            baseUsername = info.email().split("@")[0];
        }
        String username = baseUsername;
        int i = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + i++;
        }
        User u = new User();
        u.setUsername(username);
        u.setEmail(info.email());
        u.setPhone("google-" + info.id());
        String rawPassword = UUID.randomUUID().toString();
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setPasswordSet(false);
        u.setRole(Role.ROLE_USER);
        u = userRepository.save(u);
        session.setAttribute("pendingUsername", username);
        session.setAttribute("pendingPassword", rawPassword);
        return new RegisteredUser(u, rawPassword);
    }
    public record OAuthResult(User user) {}
    private record RegisteredUser(User user, String rawPassword) {}
    private record GoogleUserInfo(String id, String email, String name) {}
    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
    private static HttpRequest.BodyPublisher ofFormData(Map<String, String> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(encode(entry.getKey()))
                    .append("=")
                    .append(encode(entry.getValue()));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}
