package itis.semestrovka.demo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordForm {
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
    private String password;
}
