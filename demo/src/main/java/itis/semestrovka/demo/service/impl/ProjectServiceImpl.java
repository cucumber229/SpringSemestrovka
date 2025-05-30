// src/main/java/itis/semestrovka/demo/service/impl/ProjectServiceImpl.java
package itis.semestrovka.demo.service.impl;

import itis.semestrovka.demo.model.entity.Project;
import itis.semestrovka.demo.repository.ProjectRepository;
import itis.semestrovka.demo.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository repo;
    public ProjectServiceImpl(ProjectRepository repo) { this.repo = repo; }

    @Override
    public List<Project> findAllByOwnerId(Long ownerId) {
        return repo.findAllByOwnerId(ownerId);
    }

    @Override
    public Project findById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    @Override
    public Project save(Project project) {
        return repo.save(project);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
