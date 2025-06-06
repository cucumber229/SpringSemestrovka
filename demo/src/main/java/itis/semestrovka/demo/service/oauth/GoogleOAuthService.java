// src/main/java/itis/semestrovka/demo/service/oauth/GoogleOAuthService.java

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

    /**
     * Формирует URL для перенаправления на Google OAuth, сохраняет state в сессии.
     */
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

    /**
     * Обрабатывает callback от Google:
     * 1) Валидирует state из сессии.
     * 2) Меняет code → access_token.
     * 3) Запрашивает userinfo у Google.
     * 4) Если пользователь с таким email уже есть в базе — возвращает его (без токена).
     * 5) Иначе создаёт нового пользователя, генерирует rawPassword, сохраняет его в БД,
     *    запоминает rawPassword в сессии и выдаёт токен, который пригодится Telegram-боту.
     */
    public OAuthResult processCallback(String code, String state, HttpSession session)
            throws IOException, InterruptedException {

        // 1) Проверка state
        String savedState = (String) session.getAttribute("oauth2_state");
        session.removeAttribute("oauth2_state");
        if (savedState == null || !savedState.equals(state)) {
            throw new IllegalStateException("Invalid state");
        }

        // 2) Получаем access_token
        String accessToken = fetchAccessToken(code);

        // 3) Запрашиваем у Google информацию о пользователе
        GoogleUserInfo info = fetchUserInfo(accessToken);

        // 4) Ищем существующего пользователя по email
        Optional<User> existing = userRepository.findByEmail(info.email());
        if (existing.isPresent()) {
            return new OAuthResult(existing.get());
        }

        RegisteredUser created = registerUser(info, session);
        return new OAuthResult(created.user());
    }

    /**
     * Запрос к token-endpoint Google: code → access_token
     */
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

    /**
     * Запрос к userinfo-endpoint Google: access_token → (id, email, name)
     */
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

    /**
     * Создаёт новую запись User в базе:
     *  - Формирует уникальный username
     *  - Генерирует случайный rawPassword (UUID)
     *  - Шифрует пароль и сохраняет поля email, phone, role
     *  - Сохраняет user в репозитории
     *  - Запоминает username/rawPassword в сессии на случай дальнейшей передачи в Telegram
     */
    private RegisteredUser registerUser(GoogleUserInfo info, HttpSession session) {
        // 1) Уникальный username
        String baseUsername = info.name().replaceAll("\\s+", "").toLowerCase();
        if (baseUsername.isEmpty()) {
            baseUsername = info.email().split("@")[0];
        }
        String username = baseUsername;
        int i = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + i++;
        }

        // 2) Заполняем User-сущность
        User u = new User();
        u.setUsername(username);
        u.setEmail(info.email());
        // «Служебное» поле phone, чтобы не оставалось пустым
        u.setPhone("google-" + info.id());
        // Генерируем случайный пароль
        String rawPassword = UUID.randomUUID().toString();
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setPasswordSet(false);
        u.setRole(Role.ROLE_USER);

        // 3) Сохраняем в базу
        u = userRepository.save(u);

        // 4) Сохраняем в HTTP-сессии для дальнейшего использования
        session.setAttribute("pendingUsername", username);
        session.setAttribute("pendingPassword", rawPassword);

        return new RegisteredUser(u, rawPassword);
    }

    public record OAuthResult(User user) {}

    private record RegisteredUser(User user, String rawPassword) {}

    private record GoogleUserInfo(String id, String email, String name) {}

    // === Вспомогательные методы ===

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
