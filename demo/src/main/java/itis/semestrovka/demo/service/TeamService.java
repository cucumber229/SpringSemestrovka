// src/main/java/.../service/TeamService.java
package itis.semestrovka.demo.service;

import itis.semestrovka.demo.model.entity.Team;
import itis.semestrovka.demo.model.entity.User;

import java.util.List;

public interface TeamService {

    /* уже существовавшие */
    List<Team> findAllTeams();
    Team findById(Long id);
    Team create(Team t);
    void deleteById(Long id);

    /**
     * Поиск команд по подстроке в названии.
     */
    java.util.List<Team> findByPartialName(String name);

    /* ---- ДОБАВЛЯЕМ ---- */
    List<User> findAllUsers();

    void addUserToTeam(Long teamId, Long userId);

    void removeUserFromTeam(Long teamId, Long userId);
}
