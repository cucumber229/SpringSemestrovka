package itis.semestrovka.demo.mapper;

import itis.semestrovka.demo.model.dto.UserDto;
import itis.semestrovka.demo.model.entity.Role;
import itis.semestrovka.demo.model.entity.Team;
import itis.semestrovka.demo.model.entity.User;

public class UserConverter {
    public static UserDto toDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        if (user.getRole() != null) {
            dto.setRole(user.getRole().name());
        }
        if (user.getTeam() != null) {
            dto.setTeamId(user.getTeam().getId());
        }
        return dto;
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) return null;
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        if (dto.getRole() != null) {
            user.setRole(Role.valueOf(dto.getRole()));
        }
        if (dto.getTeamId() != null) {
            Team team = new Team();
            team.setId(dto.getTeamId());
            user.setTeam(team);
        }
        return user;
    }
}
