package itis.semestrovka.demo.controller;
import itis.semestrovka.demo.service.TeamService;
import itis.semestrovka.demo.model.dto.TeamDto;
import itis.semestrovka.demo.model.dto.UserDto;
import itis.semestrovka.demo.mapper.TeamConverter;
import itis.semestrovka.demo.mapper.UserConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "X-CSRF-TOKEN")
@Tag(name = "Teams", description = "Operations with teams")
public class TeamRestController {
    private final TeamService teamService;
    @GetMapping
    @Operation(summary = "Get all teams")
    public java.util.List<TeamDto> getAll() {
        return teamService.findAllTeams().stream()
                .map(TeamConverter::toDto)
                .toList();
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create team")
    public TeamDto create(@RequestBody TeamDto dto) {
        return TeamConverter.toDto(teamService.create(TeamConverter.toEntity(dto)));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get team by id")
    public TeamDto getById(@PathVariable Long id) {
        return TeamConverter.toDto(teamService.findById(id));
    }

    @GetMapping("/{teamId}/users")
    @Operation(summary = "Get team members")
    public java.util.List<UserDto> getTeamMembers(@PathVariable Long teamId) {
        return teamService.findTeamMembers(teamId).stream()
                .map(UserConverter::toDto)
                .toList();
    }
    @DeleteMapping("/{teamId}/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Remove user from team")
    public void removeUser(
            @PathVariable Long teamId,
            @PathVariable Long userId
    ) {
        teamService.removeUserFromTeam(teamId, userId);
    }
}
