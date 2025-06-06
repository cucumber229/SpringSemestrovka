package itis.semestrovka.demo.model.dto;

import lombok.Data;

/**
 * Data transfer object for {@link itis.semestrovka.demo.model.entity.User}.
 */
@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String role;
    private Long teamId;
}
