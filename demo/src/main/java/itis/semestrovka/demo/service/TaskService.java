// src/main/java/itis/semestrovka/demo/service/TaskService.java
package itis.semestrovka.demo.service;

import itis.semestrovka.demo.model.dto.TaskDto;
import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.model.entity.Task;

import java.util.List;

public interface TaskService {

    /*=== MVC-формы ===*/
    Task save(Task task);                // create / update из обычной формы
    void deleteById(Long id);            // удалить без проверки projectId
    Task findById(Long id);
    List<Task> findAllByProject(Long projectId);

    /*=== REST / AJAX ===*/
    Task create(Project project, TaskDto dto);     // POST JSON → Task
    void delete(Long projectId, Long taskId);      // DELETE в рамках проекта
}
