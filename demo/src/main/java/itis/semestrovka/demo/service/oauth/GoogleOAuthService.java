package itis.semestrovka.demo.service.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import itis.semestrovka.demo.model.entity.Role;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.repository.UserRepository;
import itis.semestrovka.demo.service.telegram.TelegramService;
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
    private final TelegramService telegramService;
    private final ObjectMapper mapper = new ObjectMapper();

    // Хранилище токена → (userId, username, rawPassword)
    private final Map<String, PendingData> pending = new ConcurrentHashMap<>();

    public GoogleOAuthService(UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              TelegramService telegramService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.telegramService = telegramService;
    }

    /**
     * Составляем URL для авторизации через Google и сохраняем state в сессии.
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
     * Обрабатываем коллбэк от Google: получаем токен, инфо о пользователе, сохраняем/обновляем запись в базе.
     * Если пользователь новый, создаём его, генерируем временный пароль и сохраняем его в сессии/Map для передачи в Telegram.
     */
    public OAuthResult processCallback(String code, String state, HttpSession session)
            throws IOException, InterruptedException {

        String savedState = (String) session.getAttribute("oauth2_state");
        session.removeAttribute("oauth2_state");
        if (savedState == null || !savedState.equals(state)) {
            throw new IllegalStateException("Invalid state");
        }

        // Запрашиваем access_token
        String accessToken = fetchAccessToken(code);

        // Получаем информацию о пользователе
        GoogleUserInfo info = fetchUserInfo(accessToken);

        // Проверяем, существует ли пользователь с таким email
        Optional<User> existing = userRepository.findByEmail(info.email());
        if (existing.isPresent()) {
            // Уже зарегистрирован → возвращаем существующего без токена
            return new OAuthResult(existing.get(), null);
        }

        // Новый пользователь → создаём запись, запоминаем его логин/пароль
        RegisteredUser created = registerUser(info, session);
        // Генерируем токен для последующей передачи в Telegram
        String tokenToBot = storePending(created.user().getId(),
                created.user().getUsername(),
                created.rawPassword());

        return new OAuthResult(created.user(), tokenToBot);
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

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

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

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

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
     * Создаём нового пользователя:
     * - формируем уникальный username
     * - генерируем rawPassword (случайный UUID), шифруем в базу
     * - сохраняем в БД
     * - кладём username+rawPassword в сессию для последующей передачи в Telegram
     */
    private RegisteredUser registerUser(GoogleUserInfo info, HttpSession session) {
        // Генерируем базовый username из имени или из email
        String baseUsername = info.name().replaceAll("\\s+", "").toLowerCase();
        if (baseUsername.isEmpty()) {
            baseUsername = info.email().split("@")[0];
        }
        String username = baseUsername;
        int i = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + i++;
        }

        // Создаём сущность User и сохраняем
        User u = new User();
        u.setUsername(username);
        u.setEmail(info.email());
        u.setPhone("google-" + info.id()); // временное поле “phone”
        String rawPassword = UUID.randomUUID().toString();
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(Role.ROLE_USER);
        u = userRepository.save(u);

        // Запомним в сессии, чтобы позднее отослать Telegram
        session.setAttribute("pendingUsername", username);
        session.setAttribute("pendingPassword", rawPassword);

        return new RegisteredUser(u, rawPassword);
    }

    /**
     * Метод вызывает из TelegramController, когда бот передаёт token, phone и chatId:
     * - находит PendingData по токену → соответствующие username/rawPassword
     * - сохраняет в базе chatId и настоящий номер телефона
     * - отправляет пользователю в Telegram сообщение с логином/паролем
     */
    public void completePhoneRegistration(String token, String phone, String chatId) {
        PendingData data = pending.remove(token);
        if (data == null) {
            throw new IllegalArgumentException("Invalid token");
        }

        User user = userRepository.findById(data.userId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        user.setPhone(phone);
        user.setTelegramChatId(chatId);
        userRepository.save(user);

        String msg = "Ваш логин: " + data.username() + "\nПароль: " + data.rawPassword();
        // Обращаемся к действующему chatId Telegram
        telegramService.sendMessage(chatId, msg);
    }

    /**
     * Сохраняем в мапе pendingData → (userId, username, rawPassword),
     * возвращаем сгенерированный токен.
     */
    private String storePending(long userId, String username, String rawPassword) {
        String token = UUID.randomUUID().toString();
        pending.put(token, new PendingData(userId, username, rawPassword));
        return token;
    }

    /** Простые структуры-обёртки */
    private record PendingData(long userId, String username, String rawPassword) {}
    public record OAuthResult(User user, String token) {}
    private record RegisteredUser(User user, String rawPassword) {}
    private record GoogleUserInfo(String id, String email, String name) {}

    /** Вспомогательный URL-энкодер */
    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** Вспомогательный билд POST-формы */
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
