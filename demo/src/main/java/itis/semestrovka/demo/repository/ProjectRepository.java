package itis.semestrovka.demo.repository;
import itis.semestrovka.demo.model.entity.Project;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByOwnerId(Long ownerId, Sort sort);
    List<Project> findAllByTeamId(Long teamId, Sort sort);
    List<Project> findAllByTeamIdOrOwnerIdAndTeamIsNull(Long teamId, Long ownerId, Sort sort);
}
