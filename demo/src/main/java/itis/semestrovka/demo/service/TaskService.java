package itis.semestrovka.demo.service;
import itis.semestrovka.demo.model.dto.TaskDto;
import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.model.entity.Task;
import java.util.List;
public interface TaskService {
    Task save(Task task);                
    void deleteById(Long id);            
    Task findById(Long id);
    List<Task> findAllByProject(Long projectId);
    List<Task> findAll();
    List<Task> findAllByUser(Long userId);
    Task create(Project project, TaskDto dto);     
    void delete(Long projectId, Long taskId);      
}
