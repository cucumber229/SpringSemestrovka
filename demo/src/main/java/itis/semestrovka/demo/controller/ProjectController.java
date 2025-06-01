package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // 1) Список только своих проектов
    @GetMapping
    public String list(Model model,
                       @AuthenticationPrincipal User currentUser) {
        Long teamId = currentUser.getTeam().getId();
        // загружаем все проекты, у которых project.team.id == teamId
        List<Project> projects = projectService.findAllByTeamId(teamId);
        model.addAttribute("projects",
                projects);
        model.addAttribute("title", "Мои проекты");
        return "project/list";
    }

    // 2) Форма создания
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("project", new Project());
        model.addAttribute("title", "Новый проект");
        return "project/form";
    }

    // 3) Сохранение нового или отредактированного проекта
    @PostMapping
    public String save(@Valid @ModelAttribute("project") Project project,
                       BindingResult br,
                       @AuthenticationPrincipal User currentUser,
                       Model model) {
        if (br.hasErrors()) {
            model.addAttribute("title",
                    project.getId() == null ? "Новый проект" : "Редактирование проекта");
            return "project/form";
        }
        // Подставляем владельца
        project.setOwner(currentUser);
        // Подставляем команду пользователя
        project.setTeam(currentUser.getTeam());
        projectService.save(project);
        return "redirect:/projects";
    }

    // 4) Форма редактирования
    @GetMapping("/{id}/edit")
    @PreAuthorize("@projectService.findById(#id).owner.username == authentication.name or hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        Project p = projectService.findById(id);
        model.addAttribute("project", p);
        model.addAttribute("title", "Редактирование проекта");
        return "project/form";
    }

    // 5) Удаление
    @PostMapping("/{id}/delete")
    @PreAuthorize("@projectService.findById(#id).owner.username == authentication.name or hasRole('ADMIN')")
    public String delete(@PathVariable Long id) {
        projectService.deleteById(id);
        return "redirect:/projects";
    }
}
