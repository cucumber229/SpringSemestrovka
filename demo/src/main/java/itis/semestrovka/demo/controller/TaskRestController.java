package itis.semestrovka.demo.controller;
import itis.semestrovka.demo.model.dto.TaskDto;
import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.model.entity.Task;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.mapper.TaskConverter;
import itis.semestrovka.demo.service.ProjectService;
import itis.semestrovka.demo.service.TaskService;
import itis.semestrovka.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tasks", description = "Operations with project tasks")
public class TaskRestController {
    private final TaskService    taskService;
    private final ProjectService projectService;
    private final UserService    userService;
    @PostMapping
    @Operation(summary = "Create task")
    public TaskDto create(@PathVariable Long projectId,
                          @RequestBody @Valid TaskDto dto,
                          Principal principal) {
        Project project = projectService.findById(projectId);
        checkOwnerOrAdmin(principal.getName(), project.getOwner().getUsername());
        Task task = taskService.create(project, dto);
        return TaskConverter.toDto(task);
    }
    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete task")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long projectId,
                       @PathVariable Long taskId,
                       Principal principal) {
        Project project = projectService.findById(projectId);
        checkOwnerOrAdmin(principal.getName(), project.getOwner().getUsername());
        taskService.delete(projectId, taskId);
    }
    @PostMapping("/{taskId}/participants/{userId}")
    @Operation(summary = "Add participant to task")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addParticipant(@PathVariable Long projectId,
                               @PathVariable Long taskId,
                               @PathVariable Long userId,
                               Principal principal) {
        Project project = projectService.findById(projectId);
        Task task = taskService.findById(taskId);
        checkOwnerOrAdmin(principal.getName(), project.getOwner().getUsername());
        User user = userService.findById(userId);
        if (project.getTeam() != null) {
            boolean isInTeam = project.getTeam().getMembers().stream()
                    .anyMatch(u -> u.getId().equals(userId));
            if (!isInTeam) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Пользователь не входит в команду проекта");
            }
        } else if (!project.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "У проекта нет команды — можно добавить только владельца");
        }
        if (task.getParticipants().stream().noneMatch(u -> u.getId().equals(userId))) {
            task.getParticipants().add(user);
            taskService.save(task);
        }
    }
    @DeleteMapping("/{taskId}/participants/{userId}")
    @Operation(summary = "Remove participant from task")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeParticipant(@PathVariable Long projectId,
                                  @PathVariable Long taskId,
                                  @PathVariable Long userId,
                                  Principal principal) {
        Project project = projectService.findById(projectId);
        Task task = taskService.findById(taskId);
        checkOwnerOrAdmin(principal.getName(), project.getOwner().getUsername());
        boolean wasParticipant = task.getParticipants().removeIf(u -> u.getId().equals(userId));
        if (wasParticipant) {
            taskService.save(task);
        }
    }
    private void checkOwnerOrAdmin(String username, String ownerUsername) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!username.equals(ownerUsername) && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
