package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.dto.TaskDto;
import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.model.entity.Task;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.mapper.TaskConverter;
import itis.semestrovka.demo.service.ProjectService;
import itis.semestrovka.demo.service.TaskService;
import itis.semestrovka.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/tasks")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "X-CSRF-TOKEN")
public class TaskRestController {

    private final TaskService    taskService;
    private final ProjectService projectService;
    private final UserService    userService;

    /* ----------  CREATE ЗАДАЧИ  ---------- */
    // POST /api/projects/{projectId}/tasks
    @PostMapping
    public TaskDto create(@PathVariable Long projectId,
                          @RequestBody @Valid TaskDto dto,
                          Principal principal) {

        Project project = projectService.findById(projectId);

        // доступ только владельцу проекта или ADMIN
        boolean allowed =
                principal.getName().equals(project.getOwner().getUsername()) ||
                        SecurityContextHolder.getContext().getAuthentication()
                                .getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Task task = taskService.create(project, dto);
        return TaskConverter.toDto(task);
    }

    /* ----------  DELETE ЗАДАЧИ  ---------- */
    // DELETE /api/projects/{projectId}/tasks/{taskId}
    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long projectId,
                       @PathVariable Long taskId,
                       Principal principal) {

        Project project = projectService.findById(projectId);

        boolean allowed =
                principal.getName().equals(project.getOwner().getUsername()) ||
                        SecurityContextHolder.getContext().getAuthentication()
                                .getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        taskService.delete(projectId, taskId);
    }

    /* ----------  ADD PARTICIPANT (добавить участника)  ---------- */
    // POST /api/projects/{projectId}/tasks/{taskId}/participants/{userId}
    @PostMapping("/{taskId}/participants/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addParticipant(@PathVariable Long projectId,
                               @PathVariable Long taskId,
                               @PathVariable Long userId,
                               Principal principal) {

        Project project = projectService.findById(projectId);
        Task task = taskService.findById(taskId);

        // Только владелец проекта или ADMIN могут добавлять участника
        boolean allowed =
                principal.getName().equals(project.getOwner().getUsername()) ||
                        SecurityContextHolder.getContext().getAuthentication()
                                .getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Найдём пользователя по userId
        User user = userService.findById(userId);

        // Проверим, что пользователь входит в команду проекта (если команда существует)
        if (project.getTeam() != null) {
            boolean isInTeam = project.getTeam().getMembers().stream()
                    .anyMatch(u -> u.getId().equals(userId));
            if (!isInTeam) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Пользователь не входит в команду проекта");
            }
        } else {
            // Если у проекта нет команды, единственный допустимый участник — это владелец
            if (!project.getOwner().getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "У проекта нет команды — можно добавить только владельца");
            }
        }

        // Добавим в список участников, если ещё не добавлен
        if (task.getParticipants().stream().noneMatch(u -> u.getId().equals(userId))) {
            task.getParticipants().add(user);
            taskService.save(task);
        }
        // Если уже был участником, просто возвращаем 204 без ошибок
    }

    /* ----------  REMOVE PARTICIPANT (удалить участника)  ---------- */
    // DELETE /api/projects/{projectId}/tasks/{taskId}/participants/{userId}
    @DeleteMapping("/{taskId}/participants/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeParticipant(@PathVariable Long projectId,
                                  @PathVariable Long taskId,
                                  @PathVariable Long userId,
                                  Principal principal) {

        Project project = projectService.findById(projectId);
        Task task = taskService.findById(taskId);

        // Только владелец проекта или ADMIN могут удалять участника
        boolean allowed =
                principal.getName().equals(project.getOwner().getUsername()) ||
                        SecurityContextHolder.getContext().getAuthentication()
                                .getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Если у задачи нет такого участника, просто ничего не делаем
        boolean wasParticipant = task.getParticipants().removeIf(u -> u.getId().equals(userId));
        if (wasParticipant) {
            taskService.save(task);
        }
        // Если участник отсутствовал, всё равно возвращаем 204
    }
}
