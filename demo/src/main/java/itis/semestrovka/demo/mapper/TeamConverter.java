package itis.semestrovka.demo.mapper;

import itis.semestrovka.demo.model.dto.TeamDto;
import itis.semestrovka.demo.model.entity.Team;

/**
 * Converter for {@link Team} and {@link TeamDto}.
 */
public class TeamConverter {

    /** Convert Team entity to DTO. */
    public static TeamDto toDto(Team team) {
        if (team == null) return null;
        TeamDto dto = new TeamDto();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setDescription(team.getDescription());
        return dto;
    }

    /** Convert DTO to Team entity. */
    public static Team toEntity(TeamDto dto) {
        if (dto == null) return null;
        Team team = new Team();
        team.setId(dto.getId());
        team.setName(dto.getName());
        team.setDescription(dto.getDescription());
        return team;
    }
}
