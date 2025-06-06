package itis.semestrovka.demo.model.dto;

import itis.semestrovka.demo.model.entity.Task;
import lombok.Data;

@Data
public class TaskDto {
    private Long   id;
    private String title;
    private String status;
    private String assignedUsername;

    public static TaskDto from(Task t){
        TaskDto d = new TaskDto();
        d.id   = t.getId();
        d.title = t.getTitle();
        d.status = t.getStatus();
        d.assignedUsername =
                t.getAssignedUser() != null ? t.getAssignedUser().getUsername() : null;
        return d;
    }
}
