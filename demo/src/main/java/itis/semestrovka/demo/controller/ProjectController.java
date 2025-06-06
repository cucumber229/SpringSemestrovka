// src/main/java/itis/semestrovka/demo/controller/ProjectController.java
package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.model.entity.Task;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.model.entity.Role;
import itis.semestrovka.demo.service.ProjectService;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
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
    @Value("${telegram.bot-link:https://t.me/your_bot}")
    private String botLink;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 1) Список проектов. GET-параметры:
     *    - type (возможные значения: all / team / personal; default = "all")
     *    - sortField (имя поля, по которому сортируем; default = "name")
     *    - sortDir   (asc или desc; default = "asc")
     *
     *    Если у пользователя нет команды, принудительно type = "personal"
     */
    @GetMapping
    public String list(
            @RequestParam(value = "type", required = false, defaultValue = "all") String type,
            @RequestParam(value = "sortField", required = false, defaultValue = "name") String sortField,
            @RequestParam(value = "sortDir", required = false, defaultValue = "asc") String sortDir,
            @RequestParam(value = "telegramPrompt", required = false) String telegramPrompt,
            Model model,
            @AuthenticationPrincipal User currentUser
    ) {
        boolean hasTeam = (currentUser.getTeam() != null);
        // Если у пользователя нет команды, показываем только личные проекты
        if (!hasTeam) {
            type = "personal";
        }

        // Сортировка: если field = "type", мы будем сортировать принудительно по приоритету team IS NULL / not null,
        // иначе по любому существующему поле Project.
        // Для простоты здесь поддерживаем только: name, owner.username, priority, и «type» (type=team/personal).
        Sort sort;
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        if ("type".equals(sortField)) {
            // «type» сортируем так: сначала все «Командные» (team != null), затем «Личные» (team is null)
            // Чтобы завести такую логику через Sort, добавим virtual property "team.id" (некоторые СУБД разрешают)
            // либо прямо сортируем по условию: (p.team is null) ASC — тогда командные (не null) будут выше.
            // В JPA/Spring Data «team.id» null => приходит первым, поэтому если direction=ASC, «Личные» будут наверху.
            // Чтоб поменять местами, просто инвертируем direction:
            if (direction == Sort.Direction.ASC) {
                // ASC по team.id (пустые → NULL → «Личные» наверху, а хотелось бы «Командные»)
                sort = Sort.by(Sort.Order.asc("team.id")).and(Sort.by(direction, "name"));
            } else {
                // DESC по team.id (не NULL → сразу «Командные» идут первыми)
                sort = Sort.by(Sort.Order.desc("team.id")).and(Sort.by(direction, "name"));
            }
        } else {
            // Остальные поля: «name», «priority» или «owner.username»
            // Для сортировки по владельцу: укажем «owner.username»
            sort = Sort.by(direction, sortField);
        }

        List<Project> projects;
        String title;

        if (!hasTeam) {
            // Личные проекты
            projects = projectService.findAllByOwnerId(currentUser.getId(), sort);
            title = "Мои личные проекты";
        } else {
            Long teamId  = currentUser.getTeam().getId();
            Long ownerId = currentUser.getId();

            switch (type) {
                case "team":
                    projects = projectService.findAllByTeamId(teamId, sort);
                    title = "Только командные проекты «" + currentUser.getTeam().getName() + "»";
                    break;
                case "personal":
                    // Только личные (team IS NULL)
                    projects = projectService.findAllByOwnerId(currentUser.getId(), sort)
                            .stream()
                            .filter(p -> p.getTeam() == null)
                            .toList();
                    title = "Только мои личные проекты";
                    break;
                case "all":
                default:
                    projects = projectService.findTeamAndPersonalProjects(teamId, ownerId, sort);
                    title = "Все проекты (команда + личные)";
                    break;
            }
        }

        model.addAttribute("projects", projects);
        model.addAttribute("hasTeam", hasTeam);
        model.addAttribute("type", type);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("title", title);

        // Для удобства переключения направления сортировки (при клике на заголовок)
        String reverseDir = sortDir.equalsIgnoreCase("asc") ? "desc" : "asc";
        model.addAttribute("reverseDir", reverseDir);
        model.addAttribute("showTelegramPrompt", telegramPrompt != null);
        model.addAttribute("botLink", botLink);

        return "project/list";
    }

    /**
     * 2) Форма создания проекта.
     */
    @GetMapping("/new")
    public String createForm(Model model, @AuthenticationPrincipal User currentUser) {
        model.addAttribute("project", new Project());
        model.addAttribute("hasTeam", currentUser.getTeam() != null);
        model.addAttribute("title", "Новый проект");
        return "project/form";
    }

    /**
     * 3) Сохранение (нового или редактируемого) проекта.
     */
    @PostMapping
    public String save(
            @Valid @ModelAttribute("project") Project project,
            BindingResult br,
            @RequestParam("projectType") String projectType,
            @AuthenticationPrincipal User currentUser,
            Model model
    ) {
        if (br.hasErrors()) {
            model.addAttribute("hasTeam", currentUser.getTeam() != null);
            model.addAttribute("title",
                    project.getId() == null ? "Новый проект" : "Редактирование проекта");
            return "project/form";
        }

        // Назначаем владельца
        project.setOwner(currentUser);

        // В зависимости от projectType: TEAM или PRIVATE
        if ("TEAM".equals(projectType) && currentUser.getTeam() != null) {
            project.setTeam(currentUser.getTeam());
        } else {
            project.setTeam(null);
        }

        projectService.save(project);
        return "redirect:/projects";
    }

    /**
     * 4) Форма редактирования.
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize(
            "@projectService.findById(#id).owner.username == authentication.name " +
                    "or hasRole('ROLE_ADMIN')"
    )
    public String editForm(@PathVariable Long id, Model model, @AuthenticationPrincipal User currentUser) {
        Project p = projectService.findById(id);
        model.addAttribute("project", p);
        model.addAttribute("hasTeam", currentUser.getTeam() != null);
        model.addAttribute("title", "Редактировать проект");
        return "project/form";
    }

    /**
     * 5) Удаление проекта.
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
     * 6) Просмотреть детали проекта.
     */
    @GetMapping("/{id}/view")
    @PreAuthorize(
            "@projectService.findById(#id).owner.username == authentication.name " +
                    "or ( @projectService.findById(#id).team != null && " +
                    "     @projectService.findById(#id).team.id == authentication.principal.team.id ) " +
                    "or hasRole('ROLE_ADMIN')"
    )
    public String view(@PathVariable Long id,
                       Model model,
                       @AuthenticationPrincipal User currentUser) {
        Project project = projectService.findById(id);

        List<Task> tasks;
        boolean isOwner = currentUser.getId().equals(project.getOwner().getId());
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        if (isOwner || isAdmin) {
            tasks = project.getTasks();
        } else {
            tasks = project.getTasks().stream()
                    .filter(t ->
                            (t.getAssignedUser() != null && t.getAssignedUser().getId().equals(currentUser.getId())) ||
                            t.getParticipants().contains(currentUser)
                    )
                    .toList();
        }

        model.addAttribute("project", project);
        model.addAttribute("tasks", tasks);
        model.addAttribute("title", "Детали проекта «" + project.getName() + "»");
        return "project/details";
    }
}
