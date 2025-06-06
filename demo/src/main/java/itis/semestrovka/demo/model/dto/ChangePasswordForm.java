package itis.semestrovka.demo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordForm {
    @NotBlank(message = "Введите текущий пароль")
    private String currentPassword;

    @NotBlank(message = "Введите новый пароль")
    @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
    private String newPassword;

    @NotBlank(message = "Повторите новый пароль")

    private String confirmPassword;
}
