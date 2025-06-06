// src/main/java/itis/semestrovka/demo/model/dto/RegistrationForm.java
package itis.semestrovka.demo.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationForm {
    @NotBlank(message = "Имя обязательно")
    @Size(min = 4, max = 20, message = "Имя должно быть от 4 до 20 символов")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
    private String password;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный формат email")
    private String email;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "(8|\\+7)\\d{10}", message = "Телефон должен быть в формате +7XXXXXXXXXX или 8XXXXXXXXXX")
    private String phone;

}
