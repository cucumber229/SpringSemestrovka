// src/main/java/itis/semestrovka/demo/repository/TeamRepository.java
package itis.semestrovka.demo.repository;

import itis.semestrovka.demo.model.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    // можно при необходимости добавить свои методы @Query/Criteria
}
