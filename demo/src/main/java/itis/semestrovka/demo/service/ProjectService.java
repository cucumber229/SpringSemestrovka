package itis.semestrovka.demo.service;

import itis.semestrovka.demo.model.entity.Project;
import java.util.List;

public interface ProjectService {
    List<Project> findAll();
    Project findById(Long id);
    Project save(Project project);
    void deleteById(Long id);
}