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

    /**
     * 1) Список проектов:
     *    – если у пользователя есть команда — показать все проекты его команды,
     *    – иначе — только проекты, которыми он владеет.
     */
    @GetMapping
    public String list(Model model,
                       @AuthenticationPrincipal User currentUser) {

        boolean hasTeam = (currentUser.getTeam() != null);
        model.addAttribute("hasTeam", hasTeam);

        List<Project> projects;
        if (!hasTeam) {
            // Личные проекты (у пользователя нет команды):
            // — показываем только проекты, где он — владелец
            projects = projectService.findAllByOwnerId(currentUser.getId());
            model.addAttribute("title", "Мои проекты");
        } else {
            // У пользователя есть команда — показываем все проекты его команды
            Long teamId = currentUser.getTeam().getId();
            projects = projectService.findAllByTeamId(teamId);
            model.addAttribute("title", "Проекты команды «" + currentUser.getTeam().getName() + "»");
        }
        model.addAttribute("projects", projects);
        return "project/list";
    }

    /**
     * 2) Форма создания нового проекта.
     *    Передаём флаг hasTeam, чтобы в шаблоне можно было отобразить выбор «Личный»/«Командный» проект.
     */
    @GetMapping("/new")
    public String createForm(Model model,
                             @AuthenticationPrincipal User currentUser) {
        model.addAttribute("project", new Project());
        model.addAttribute("hasTeam", currentUser.getTeam() != null);
        model.addAttribute("title", "Новый проект");
        return "project/form";
    }

    /**
     * 3) Обработка сохранения (нового или редактируемого проекта).
     *    В запросе обязательно приходит параметр "projectType" (значения: "PRIVATE" или "TEAM"),
     *    который отвечает за то, где хранить проект (связь с командой или нет).
     */
    @PostMapping
    public String save(
            @Valid @ModelAttribute("project") Project project,
            BindingResult br,
            @RequestParam("projectType") String projectType,
            @AuthenticationPrincipal User currentUser,
            Model model
    ) {
        // 3.1) Если есть ошибки валидации (например, пустое название) — возвращаем форму
        if (br.hasErrors()) {
            model.addAttribute("hasTeam", currentUser.getTeam() != null);
            model.addAttribute("title",
                    project.getId() == null ? "Новый проект" : "Редактирование проекта");
            return "project/form";
        }

        // 3.2) Устанавливаем владельца (owner = текущий пользователь)
        project.setOwner(currentUser);

        // 3.3) В зависимости от projectType («TEAM» или «PRIVATE»)
        if ("TEAM".equals(projectType) && currentUser.getTeam() != null) {
            project.setTeam(currentUser.getTeam());
        } else {
            project.setTeam(null);
        }

        // 3.4) Сохраняем проект
        projectService.save(project);
        return "redirect:/projects";
    }

    /**
     * 4) Форма редактирования проекта:
     *    доступно владельцу проекта или ADMIN.
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize(
            "@projectService.findById(#id).owner.username == authentication.name " +
                    "or hasRole('ROLE_ADMIN')"
    )
    public String editForm(@PathVariable Long id,
                           Model model,
                           @AuthenticationPrincipal User currentUser) {
        Project p = projectService.findById(id);
        model.addAttribute("project", p);
        model.addAttribute("hasTeam", currentUser.getTeam() != null);
        model.addAttribute("title", "Редактирование проекта");
        return "project/form";
    }

    /**
     * 5) Удаление проекта:
     *    доступно владельцу проекта или ADMIN.
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize(
            "@projectService.findById(#id).owner.username == authentication.name " +
                    "or hasRole('ROLE_ADMIN')"
    )
    public String delete(@PathVariable Long id) {
        projectService.deleteById(id);
        return "redirect:/projects";
    }

    /**
     * 6) Страница просмотра деталей проекта.
     */
    @GetMapping("/{id}/view")
    @PreAuthorize(
            "@rojectService.findById(#id).owner.username == authentication.name " +
                    "or ( @projectService.findById(#id).team != null && " +
                    "     @projectService.findById(#id).team.id == principal.team.id ) " +
                    "or hasRole('ROLE_ADMIN')"
    )
    public String view(@PathVariable Long id,
                       Model model) {

        Project project = projectService.findById(id);
        model.addAttribute("project", project);
        model.addAttribute("title", "Детали проекта «" + project.getName() + "»");
        return "project/details";
    }
}
