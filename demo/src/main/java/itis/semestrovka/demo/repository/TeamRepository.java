// src/main/java/itis/semestrovka/demo/repository/TeamRepository.java
package itis.semestrovka.demo.repository;

import itis.semestrovka.demo.model.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * Загрузить команду вместе с участниками одним запросом.
     * Пример JPQL-запроса, который демонстрирует использование
     * собственных запросов помимо методов Spring Data.
     */
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.members WHERE t.id = :id")
    Optional<Team> findByIdWithMembers(@Param("id") Long id);
}
