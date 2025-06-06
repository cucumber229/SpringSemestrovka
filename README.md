# TeamManager Demo Application

This project is a simple Spring Boot app used for managing teams and projects. It requires PostgreSQL and Java 21.

## Running locally

1. Create a PostgreSQL database called `teammanagerdb` and update credentials in `application.yaml` if necessary.
2. Ensure the following environment variables are set before starting the application:
   - `GOOGLE_CLIENT_ID` – Client ID from your Google Cloud OAuth consent screen.
   - `GOOGLE_CLIENT_SECRET` – Client secret for the same OAuth client.
   - `GOOGLE_REDIRECT_URI` (optional) – Defaults to `http://localhost:8080/oauth2/callback/google` and must match the allowed redirect URI in Google settings.
   - `SLACK_WEBHOOK_URL` (optional) – Incoming webhook for Slack notifications.


3. Build and run using Maven:

```bash
cd demo
./mvnw spring-boot:run
```

After launch, open `http://localhost:8080/login` and use the "Войти через Google" button to authenticate via Google.
After Google authentication a new account will be created if it does not exist. The generated username and password will be automatically inserted into the login form so you can sign in immediately.



## DTO conversion

Separate converter classes are used to transform entities into DTOs and back. For example, `TaskConverter` converts between `Task` and `TaskDto` objects:

```java
Task task = TaskConverter.toEntity(dto);
TaskDto dto = TaskConverter.toDto(task);
```

`TaskConverter` is invoked from `TaskServiceImpl#create` when a new task is created through the REST API.

`TeamConverter` is used in `TeamRestController` to expose REST endpoints that work with `TeamDto` objects.

## REST API and Swagger

All REST endpoints are grouped under the `/api/**` path. The API documentation
is generated using **springdoc-openapi** and is available at
`http://localhost:8080/swagger-ui.html` once the application is running.

Example HTTP request files for `TeamRestController` and `TaskRestController` can
be found in the `demo/http` directory. They can be executed from IntelliJ IDEA
or imported into tools like Postman.
