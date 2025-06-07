package itis.semestrovka.demo.service;
import itis.semestrovka.demo.model.entity.Team;
import itis.semestrovka.demo.model.entity.User;
import java.util.List;
public interface TeamService {
    List<Team> findAllTeams();
    Team findById(Long id);
    Team create(Team t);
    void deleteById(Long id);
    java.util.List<Team> findByPartialName(String name);
    List<User> findAllUsers();
    List<User> findTeamMembers(Long teamId);
    void addUserToTeam(Long teamId, Long userId);
    void removeUserFromTeam(Long teamId, Long userId);
}
