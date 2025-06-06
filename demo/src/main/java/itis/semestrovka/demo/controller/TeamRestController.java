package itis.semestrovka.demo.controller;
import itis.semestrovka.demo.service.TeamService;
import itis.semestrovka.demo.model.dto.TeamDto;
import itis.semestrovka.demo.mapper.TeamConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
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
    @Operation(summary = "Create team")
    public TeamDto create(@RequestBody TeamDto dto) {
        return TeamConverter.toDto(teamService.create(TeamConverter.toEntity(dto)));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get team by id")
    public TeamDto getById(@PathVariable Long id) {
        return TeamConverter.toDto(teamService.findById(id));
    }
    @DeleteMapping("/{teamId}/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove user from team")
    public void removeUser(
            @PathVariable Long teamId,
            @PathVariable Long userId
    ) {
        teamService.removeUserFromTeam(teamId, userId);
    }
}
