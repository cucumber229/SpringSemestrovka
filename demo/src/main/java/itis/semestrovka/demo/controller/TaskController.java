// src/main/java/itis/semestrovka/demo/controller/TaskController.java
package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.model.entity.Task;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.model.entity.Comment;
import itis.semestrovka.demo.service.ProjectService;
import itis.semestrovka.demo.service.TaskService;
import itis.semestrovka.demo.service.UserService;
import itis.semestrovka.demo.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Controller
@RequestMapping("/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService    taskService;
    private final ProjectService projectService;
    private final UserService    userService;   // ← добавили
    private final CommentService commentService;

    /* ----------  ФОРМА СОЗДАНИЯ  ---------- */
    @GetMapping("/new")
    public String newForm(@PathVariable Long projectId,
                          Model model,
                          @AuthenticationPrincipal User currentUser) {

        Project project = projectService.findById(projectId);
        List<User> candidates = (project.getTeam() != null)
                ? project.getTeam().getMembers()
                : List.of(project.getOwner());

        model.addAttribute("task", new Task());
        model.addAttribute("projectId", projectId);
        model.addAttribute("candidates", candidates);
        return "task/form";
    }

    /* ----------  СОХРАНЕНИЕ (create / update)  ---------- */
    @PostMapping                                    // *** единственный POST-метод ***
    public String save(@PathVariable Long projectId,
                       @Valid @ModelAttribute("task") Task task,
                       BindingResult br,
                       @RequestParam(value = "assigneeId", required = false) Long assigneeId,
                       @RequestParam(value = "participantIds", required = false) List<Long> participantIds,
                       Model model,
                       @AuthenticationPrincipal User currentUser) {

        // Валидация ------------------------------------------------------
        if (br.hasErrors()) {
            Project project = projectService.findById(projectId);
            List<User> candidates = (project.getTeam() != null)
                    ? project.getTeam().getMembers()
                    : List.of(project.getOwner());

            // восстановить выбранных участников при ошибке валидации
            Set<User> selected = new HashSet<>();
            if (participantIds != null) {
                for (Long uid : participantIds) {
                    selected.add(userService.findById(uid));
                }
            }
            task.setParticipants(selected);

            model.addAttribute("projectId", projectId);
            model.addAttribute("candidates", candidates);
            return "task/form";
        }

        // Привязка к проекту и исполнителю ------------------------------
        Project project = projectService.findById(projectId);
        task.setProject(project);

        if (assigneeId != null && assigneeId > 0) {
            task.setAssignedUser(userService.findById(assigneeId));
        } else {
            task.setAssignedUser(null);
        }

        // Назначение участников задачи (M2M) только из членов команды
        Set<User> participants = new HashSet<>();
        if (participantIds != null) {
            Set<Long> allowed = new HashSet<>();
            if (project.getTeam() != null) {
                for (User u : project.getTeam().getMembers()) {
                    allowed.add(u.getId());
                }
            } else {
                allowed.add(project.getOwner().getId());
            }
            for (Long uid : participantIds) {
                if (allowed.contains(uid)) {
                    participants.add(userService.findById(uid));
                }
            }
        }
        task.setParticipants(participants);

        taskService.save(task);
        return "redirect:/projects/" + projectId + "/view";
    }

    /* ----------  ФОРМА РЕДАКТИРОВАНИЯ  ---------- */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long projectId,
                           @PathVariable Long id,
                           Model model) {

        Project project = projectService.findById(projectId);
        List<User> candidates = (project.getTeam() != null)
                ? project.getTeam().getMembers()
                : List.of(project.getOwner());

        model.addAttribute("task", taskService.findById(id));
        model.addAttribute("projectId", projectId);
        model.addAttribute("candidates", candidates);
        return "task/form";
    }

    /* ----------  УДАЛЕНИЕ  ---------- */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long projectId,
                         @PathVariable Long id) {
        taskService.deleteById(id);
        return "redirect:/projects/" + projectId + "/view";
    }
    /* ----------  ПРОСМОТР ОДНОЙ ЗАДАЧИ  ---------- */
    @GetMapping("/{id}")
    public String view(@PathVariable Long projectId,
                       @PathVariable Long id,
                       Model model,
                       @AuthenticationPrincipal User currentUser) {

        Task    task    = taskService.findById(id);
        Project project = projectService.findById(projectId);
        List<Comment> comments = commentService.findAllByTaskId(id);

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        boolean isAssigned = (task.getAssignedUser() != null && task.getAssignedUser().getId().equals(currentUser.getId()))
                || task.getParticipants().contains(currentUser);
        boolean isTeamMember = project.getTeam() != null && project.getTeam().getMembers().contains(currentUser);

        boolean allowed = isOwner || isAdmin || (isTeamMember && isAssigned);

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute("task",    task);
        model.addAttribute("project", project);          // ← добавили
        model.addAttribute("comments", comments);
        model.addAttribute("projectId", projectId);
        model.addAttribute("title", "Задача «" + task.getTitle() + "»");
        return "task/details";
    }

    /* ----------  ДОБАВЛЕНИЕ КОММЕНТАРИЯ  ---------- */
    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long projectId,
                             @PathVariable Long id,
                             @AuthenticationPrincipal User currentUser,
                             @RequestParam("content") String content) {
        Task task = taskService.findById(id);
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(currentUser);
        comment.setContent(content);
        commentService.save(comment);
        return "redirect:/projects/" + projectId + "/tasks/" + id;
    }

}
