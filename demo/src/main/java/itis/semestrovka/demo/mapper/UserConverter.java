package itis.semestrovka.demo.mapper;

import itis.semestrovka.demo.model.dto.UserDto;
import itis.semestrovka.demo.model.entity.User;

/**
 * Converter for {@link User} and {@link UserDto}.
 */
public class UserConverter {

    /** Convert User entity to DTO. */
    public static UserDto toDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        if (user.getTeam() != null) {
            dto.setTeamId(user.getTeam().getId());
        }
        return dto;
    }

    /** Convert DTO to User entity. */
    public static User toEntity(UserDto dto) {
        if (dto == null) return null;
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        return user;
    }
}
