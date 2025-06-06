package itis.semestrovka.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import itis.semestrovka.demo.model.dto.TaskDto;
import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.model.entity.Task;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.service.ProjectService;
import itis.semestrovka.demo.service.TaskService;
import itis.semestrovka.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskRestController.class)
class TaskRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "owner")
    void createReturnsCreatedTask() throws Exception {
        Project project = new Project();
        User owner = new User();
        owner.setUsername("owner");
        project.setOwner(owner);
        when(projectService.findById(1L)).thenReturn(project);

        TaskDto dto = new TaskDto();
        dto.setTitle("New Task");
        Task saved = new Task();
        saved.setId(5L);
        when(taskService.create(any(Project.class), any(TaskDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(taskService).create(eq(project), any(TaskDto.class));
    }

    @Test
    @WithMockUser(username = "owner")
    void deleteCallsServiceAndReturnsNoContent() throws Exception {
        Project project = new Project();
        User owner = new User();
        owner.setUsername("owner");
        project.setOwner(owner);
        when(projectService.findById(1L)).thenReturn(project);

        mockMvc.perform(delete("/api/projects/1/tasks/2"))
                .andExpect(status().isNoContent());

        verify(taskService).delete(1L, 2L);
    }
}
