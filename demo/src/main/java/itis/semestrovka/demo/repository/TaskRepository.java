// src/main/java/itis/semestrovka/demo/repository/TaskRepository.java
package itis.semestrovka.demo.repository;

import itis.semestrovka.demo.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByProjectId(Long projectId);

    // для безопасного удаления через REST
    void deleteByIdAndProjectId(Long id, Long projectId);
}
