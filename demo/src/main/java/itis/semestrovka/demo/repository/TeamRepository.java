package itis.semestrovka.demo.repository;
import itis.semestrovka.demo.model.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.members WHERE t.id = :id")
    Optional<Team> findByIdWithMembers(@Param("id") Long id);
    @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY t.createdAt DESC")
    java.util.List<Team> findByPartialName(@Param("name") String name);
}
