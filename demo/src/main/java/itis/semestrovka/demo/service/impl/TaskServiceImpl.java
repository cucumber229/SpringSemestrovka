// src/main/java/itis/semestrovka/demo/service/impl/TaskServiceImpl.java
package itis.semestrovka.demo.service.impl;

import itis.semestrovka.demo.model.dto.TaskDto;
import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.model.entity.Task;
import itis.semestrovka.demo.repository.TaskRepository;
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

    /* ===== REST / AJAX (заглушки) ===== */

    @Override
    public Task create(Project project, TaskDto dto) {
        throw new UnsupportedOperationException("REST-метод пока не используется");
    }

    @Override
    public void delete(Long projectId, Long taskId) {
        throw new UnsupportedOperationException("REST-метод пока не используется");
    }
}
