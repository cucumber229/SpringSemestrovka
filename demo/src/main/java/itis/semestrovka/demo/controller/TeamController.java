// src/main/java/itis/semestrovka/demo/controller/TeamController.java
package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.entity.Team;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    //
    // 1) ПУБЛИЧНЫЕ (не-админские) ЭКШЕНЫ — доступны всем аутентифицированным пользователям
    //

    /** 1.1. Список всех команд (главная страница раздела «Команды») */
    @GetMapping
    public String list(@RequestParam(value = "search", required = false) String search,
                       Model model) {
        List<Team> teams = (search != null && !search.isBlank())
                ? teamService.findByPartialName(search)
                : teamService.findAllTeams();
        model.addAttribute("teams", teams);
        model.addAttribute("search", search);
        model.addAttribute("title", "Список команд");
        return "team/list";
    }

    /** 1.2. Просмотр деталей конкретной команды (включая её участников) */
    @GetMapping("/{id}/view")
    public String view(@PathVariable Long id, Model model) {
        Team team = teamService.findById(id);
        model.addAttribute("team", team);
        model.addAttribute("title", "Просмотр команды");
        return "team/view";
    }


    //
    // 2) АДМИНСКИЕ ЭКШЕНЫ — защищены @PreAuthorize("hasRole('ROLE_ADMIN')")
    //

    /** 2.1. Страница администрирования (альтернативный список команд для админа) */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public String adminList(@RequestParam(value = "search", required = false) String search,
                            Model model) {
        List<Team> teams = (search != null && !search.isBlank())
                ? teamService.findByPartialName(search)
                : teamService.findAllTeams();
        model.addAttribute("teams", teams);
        model.addAttribute("search", search);
        model.addAttribute("title", "Управление командами");
        return "team/admin_list";
    }

    /** 2.2. Форма создания новой команды */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("team", new Team());
        model.addAttribute("title", "Новая команда");
        return "team/form";
    }

    /** 2.3. Сохранение новой или отредактированной команды */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("team") Team formTeam,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            model.addAttribute("title",
                    (formTeam.getId() == null) ? "Новая команда" : "Редактирование команды");
            return "team/form";
        }

        Team team;
        if (formTeam.getId() != null) {
            team = teamService.findById(formTeam.getId());
            team.setName(formTeam.getName());
            team.setDescription(formTeam.getDescription());
        } else {
            team = formTeam;
        }

        teamService.create(team);
        return "redirect:/teams/admin";
    }

    /** 2.4. Форма редактирования существующей команды */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Team team = teamService.findById(id);
        model.addAttribute("team", team);
        model.addAttribute("title", "Редактировать команду");
        return "team/form";
    }

    /** 2.5. Удалить команду */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        teamService.deleteById(id);
        return "redirect:/teams/admin";
    }

    /** 2.6. Форма добавления пользователя в команду */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{teamId}/add-user")
    public String addUserForm(@PathVariable Long teamId, Model model) {
        Team team = teamService.findById(teamId);
        List<User> allUsers = teamService.findAllUsers();
        model.addAttribute("team", team);
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("title", "Добавить пользователя в команду");
        return "team/add_user";
    }

    /** 2.7. Обработка формы «добавить пользователя в команду» */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{teamId}/add-user")
    public String addUserToTeam(
            @PathVariable Long teamId,
            @RequestParam("userId") Long userId
    ) {
        teamService.addUserToTeam(teamId, userId);
        return "redirect:/teams/" + teamId + "/view";
    }

    /** 2.8. Удаление пользователя из команды */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{teamId}/remove-user")
    public String removeUserFromTeam(
            @PathVariable Long teamId,
            @RequestParam("userId") Long userId
    ) {
        teamService.removeUserFromTeam(teamId, userId);
        return "redirect:/teams/" + teamId + "/view";
    }
}
