package itis.semestrovka.demo.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Название проекта — обязательно
    @Column(nullable = false)
    @NotBlank
    private String name;

    // Описание — опционально
    private String description;

    // Дата создания (инициируется автоматически при создании объекта)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Новое поле: приоритет проекта (1 – Низкий, 2 – Средний, 3 – Высокий) */
    @Column(name = "priority", nullable = false)
    @NotNull
    private Integer priority = 1;

    /**
     * Связь «многие-к-одному» к Team.
     * Для личных проектов `team` может быть null, поэтому nullable = true.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    /**
     * Связь «многие-к-одному» к владельцу (owner) — обязательно.
     * Здесь nullable = false, так как у любого проекта должен быть владелец.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
