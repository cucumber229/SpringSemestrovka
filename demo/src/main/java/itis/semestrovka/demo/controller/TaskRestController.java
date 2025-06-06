package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.dto.TaskDto;
import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.model.entity.Task;
import itis.semestrovka.demo.service.ProjectService;
import itis.semestrovka.demo.service.TaskService;
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

    private final TaskService     taskService;
    private final ProjectService  projectService;

    /* ----------  CREATE  ---------- */
    @PostMapping                                        // POST /api/projects/3/tasks
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

        if (!allowed)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        Task task = taskService.create(project, dto);
        return TaskDto.from(task);                    // → JSON
    }

    /* ----------  DELETE  ---------- */
    @DeleteMapping("/{taskId}")                       // DELETE /api/projects/3/tasks/7
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

        if (!allowed)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        taskService.delete(projectId, taskId);
    }
}
