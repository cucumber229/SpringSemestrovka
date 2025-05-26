package itis.semestrovka.demo.repository;

import itis.semestrovka.demo.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}