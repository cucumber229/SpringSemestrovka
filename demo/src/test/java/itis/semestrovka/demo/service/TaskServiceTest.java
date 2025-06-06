package itis.semestrovka.demo.service;

import itis.semestrovka.demo.model.dto.TaskDto;
import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.model.entity.Task;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.repository.TaskRepository;
import itis.semestrovka.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @MockBean
    private TaskRepository taskRepository;

    @MockBean
    private UserRepository userRepository;

    @Test
    void createSetsAssignedUserWhenFound() {
        Project project = new Project();
        project.setId(1L);

        TaskDto dto = new TaskDto();
        dto.setTitle("Test");
        dto.setAssignedUsername("john");

        User user = new User();
        user.setId(5L);
        user.setUsername("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });

        Task result = taskService.create(project, dto);

        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();
        assertThat(saved.getProject()).isEqualTo(project);
        assertThat(saved.getAssignedUser()).isEqualTo(user);
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void deleteDelegatesToRepository() {
        taskService.delete(1L, 2L);
        verify(taskRepository).deleteByIdAndProjectId(2L, 1L);
    }
}
