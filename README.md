# TeamManager Demo Application

This project is a simple Spring Boot app used for managing teams and projects. It requires PostgreSQL and Java 21.

## Running locally

1. Create a PostgreSQL database called `teammanagerdb` and update credentials in `application.yaml` if necessary.
2. Ensure the following environment variables are set before starting the application:
   - `GOOGLE_CLIENT_ID` – Client ID from your Google Cloud OAuth consent screen.
   - `GOOGLE_CLIENT_SECRET` – Client secret for the same OAuth client.
   - `GOOGLE_REDIRECT_URI` (optional) – Defaults to `http://localhost:8080/oauth2/callback/google` and must match the allowed redirect URI in Google settings.
   - `TELEGRAM_BOT_TOKEN` – Bot token obtained from @BotFather.
3. Build and run using Maven:

```bash
cd demo
./mvnw spring-boot:run
```

After launch, open `http://localhost:8080/login` and use the "Войти через Google" button to authenticate via Google.

To link your Telegram account, send `/start` to your bot and make it call
`/telegram/register?chatId=<yourChatId>` while you are logged in.

