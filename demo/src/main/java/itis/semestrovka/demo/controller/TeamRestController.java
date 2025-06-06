package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.service.TeamService;
import itis.semestrovka.demo.model.dto.TeamDto;
import itis.semestrovka.demo.mapper.TeamConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "X-CSRF-TOKEN")
public class TeamRestController {

    private final TeamService teamService;

    /** Получить список всех команд. */
    @GetMapping
    public java.util.List<TeamDto> getAll() {
        return teamService.findAllTeams().stream()
                .map(TeamConverter::toDto)
                .toList();
    }

    /** Создать новую команду. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamDto create(@RequestBody TeamDto dto) {
        return TeamConverter.toDto(teamService.create(TeamConverter.toEntity(dto)));
    }

    /** Получить конкретную команду по id. */
    @GetMapping("/{id}")
    public TeamDto getById(@PathVariable Long id) {
        return TeamConverter.toDto(teamService.findById(id));
    }

    /** Удаление пользователя из команды через AJAX */
    @DeleteMapping("/{teamId}/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(
            @PathVariable Long teamId,
            @PathVariable Long userId
    ) {
        teamService.removeUserFromTeam(teamId, userId);
    }
}
