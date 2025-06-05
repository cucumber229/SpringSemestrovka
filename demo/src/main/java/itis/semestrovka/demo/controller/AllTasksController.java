package itis.semestrovka.demo.controller;

import itis.semestrovka.demo.model.entity.Task;
import itis.semestrovka.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import itis.semestrovka.demo.model.entity.User;
import java.util.List;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class AllTasksController {
    private final TaskService taskService;

    @GetMapping
    public String list(Model model, @AuthenticationPrincipal User user) {
        List<Task> tasks = taskService.findAll();
        model.addAttribute("tasks", tasks);
        model.addAttribute("title", "Все задачи");
        return "task/all";
    }
}
