package itis.semestrovka.demo.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter @Setter @NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ======= Основные поля ======= */

    @NotBlank
    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull
    @Column(nullable = false)
    private Integer priority = 1;          // 1 – низкий, 2 – средний, 3 – высокий

    /* ======= Связи ======= */

    // (nullable=true) → личный или командный
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "team_id")
    private Team team;

    // владелец всегда обязателен
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // список задач проекта  <——  ЭТО НОВОЕ
    @OneToMany(mappedBy = "project",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Task> tasks = new ArrayList<>();
}
