package itis.semestrovka.demo.mapper;
import itis.semestrovka.demo.model.dto.TaskDto;
import itis.semestrovka.demo.model.entity.Task;
import itis.semestrovka.demo.model.entity.User;
public class TaskConverter {
    public static TaskDto toDto(Task task) {
        if (task == null) return null;
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setStatus(task.getStatus());
        if (task.getAssignedUser() != null) {
            dto.setAssignedUsername(task.getAssignedUser().getUsername());
        }
        return dto;
    }
    public static Task toEntity(TaskDto dto) {
        if (dto == null) return null;
        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setStatus(dto.getStatus());
        if (dto.getAssignedUsername() != null) {
            User user = new User();
            user.setUsername(dto.getAssignedUsername());
            task.setAssignedUser(user);
        }
        return task;
    }
}
