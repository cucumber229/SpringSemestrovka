package itis.semestrovka.demo.service;

import itis.semestrovka.demo.model.entity.Project;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ProjectService {
    List<Project> findAllByOwnerId(Long ownerId, Sort sort);
    List<Project> findAllByTeamId(Long teamId, Sort sort);
    List<Project> findTeamAndPersonalProjects(Long teamId, Long ownerId, Sort sort);

    Project findById(Long id);
    Project save(Project project);
    void deleteById(Long id);

}
