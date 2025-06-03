// src/main/java/itis/semestrovka/demo/repository/ProjectRepository.java
package itis.semestrovka.demo.repository;

import itis.semestrovka.demo.model.entity.Project;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // 1) Найти все личные проекты (owner = :ownerId) с сортировкой
    List<Project> findAllByOwnerId(Long ownerId, Sort sort);

    // 2) Найти все проекты команды (team_id = :teamId) с сортировкой
    List<Project> findAllByTeamId(Long teamId, Sort sort);

    // 3) Найти все проекты: либо командные (team_id = :teamId), либо личные (team IS NULL и owner = :ownerId), с сортировкой
    List<Project> findAllByTeamIdOrOwnerIdAndTeamIsNull(Long teamId, Long ownerId, Sort sort);
}
