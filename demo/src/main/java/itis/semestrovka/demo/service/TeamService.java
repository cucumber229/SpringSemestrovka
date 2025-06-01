// src/main/java/itis/semestrovka/demo/service/TeamService.java
package itis.semestrovka.demo.service;

import itis.semestrovka.demo.model.entity.Team;
import itis.semestrovka.demo.model.entity.User;

import java.util.List;

public interface TeamService {
    List<Team> findAllTeams();
    Team findById(Long id);
    Team create(Team team);
    Team update(Team team);
    void deleteById(Long id);

    /**
     * Добавляет пользователя user в команду team.
     * @throws IllegalArgumentException, если команда или пользователь не найдены.
     */
    void addUserToTeam(Long teamId, Long userId);

    /**
     * Убирает пользователя user из команды team.
     * @throws IllegalArgumentException, если команда или пользователь не найдены.
     */
    void removeUserFromTeam(Long teamId, Long userId);

    List<User> findAllUsers();
}
