package itis.semestrovka.demo.model.dto;

import lombok.Data;

/**
 * Data transfer object for {@link itis.semestrovka.demo.model.entity.Team}.
 */
@Data
public class TeamDto {
    private Long id;
    private String name;
    private String description;
}
