package itis.semestrovka.demo.mapper;

import itis.semestrovka.demo.model.dto.TaskDto;
import itis.semestrovka.demo.model.entity.Task;
import itis.semestrovka.demo.model.entity.User;

/**
 * Converter for {@link Task} and {@link TaskDto}.
 * <p>
 * Example usage:
 * {@code Task task = TaskConverter.toEntity(dto);}
 * {@code TaskDto dto = TaskConverter.toDto(task);}
 */
public class TaskConverter {

    /** Convert Task entity to DTO. */
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

    /** Convert DTO to Task entity (without setting project). */
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
