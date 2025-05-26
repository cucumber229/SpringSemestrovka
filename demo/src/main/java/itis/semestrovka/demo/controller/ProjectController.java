// src/main/java/itis/semestrovka/demo/controller/ProjectController.java
package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("projects", projectService.findAll());
        return "project/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("project", new Project());
        return "project/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute Project project, BindingResult br) {
        if (br.hasErrors()) {
            return "project/form";
        }
        projectService.save(project);
        return "redirect:/projects";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id);
        model.addAttribute("project", project);
        return "project/form";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        projectService.deleteById(id);
        return "redirect:/projects";
    }
}