// src/main/java/itis/semestrovka/demo/model/dto/RegistrationForm.java
package itis.semestrovka.demo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationForm {
    @NotBlank @Size(min=4, max=20)
    private String username;
    @NotBlank @Size(min=6)
    private String password;
}
