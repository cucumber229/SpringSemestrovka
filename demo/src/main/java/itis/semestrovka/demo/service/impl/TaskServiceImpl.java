// src/main/java/itis/semestrovka/demo/service/impl/TaskServiceImpl.java
package itis.semestrovka.demo.service.impl;

import itis.semestrovka.demo.model.dto.TaskDto;
import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.model.entity.Task;
import itis.semestrovka.demo.mapper.TaskConverter;
import itis.semestrovka.demo.repository.TaskRepository;
import itis.semestrovka.demo.repository.UserRepository;
import itis.semestrovka.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repo;
    private final UserRepository userRepo;

    /* ===== MVC ===== */

    @Override
    public Task save(Task task) {
        return repo.save(task);
    }

    @Override
    public Task findById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    public List<Task> findAllByProject(Long projectId) {
        return repo.findAllByProjectId(projectId);
    }

    @Override
    public List<Task> findAll() {
        return repo.findAll();
    }

    @Override
    public List<Task> findAllByUser(Long userId) {
        return repo.findAllByParticipants_Id(userId);
    }

    /* ===== REST / AJAX ===== */

    @Override
    public Task create(Project project, TaskDto dto) {
        // Преобразуем DTO в сущность с помощью конвертера
        Task task = TaskConverter.toEntity(dto);
        task.setProject(project);

        // Назначение пользователя осуществляется через репозиторий
        if (dto.getAssignedUsername() != null) {
            userRepo.findByUsername(dto.getAssignedUsername())
                    .ifPresent(task::setAssignedUser);
        }

        return repo.save(task);
    }

    @Override
    public void delete(Long projectId, Long taskId) {
        repo.deleteByIdAndProjectId(taskId, projectId);
    }
}
