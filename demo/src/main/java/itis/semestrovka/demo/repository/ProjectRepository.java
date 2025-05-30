// src/main/java/itis/semestrovka/demo/repository/ProjectRepository.java
package itis.semestrovka.demo.repository;

import itis.semestrovka.demo.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project,Long> {
    List<Project> findAllByOwnerId(Long ownerId);
}
