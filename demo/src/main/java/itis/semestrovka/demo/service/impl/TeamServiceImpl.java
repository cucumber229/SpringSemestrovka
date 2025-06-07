package itis.semestrovka.demo.service.impl;
import itis.semestrovka.demo.model.entity.Team;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.repository.TeamRepository;
import itis.semestrovka.demo.repository.UserRepository;
import itis.semestrovka.demo.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@Transactional
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepo;
    private final UserRepository userRepo;
    @Override public List<Team> findAllTeams()   { return teamRepo.findAll(); }
    @Override
    public Team findById(Long id) {
        return teamRepo.findByIdWithMembers(id).orElseThrow();
    }
    @Override public Team create(Team t)         { return teamRepo.save(t); }
    @Override public void deleteById(Long id)    { teamRepo.deleteById(id); }
    @Override
    public java.util.List<Team> findByPartialName(String name) {
        return teamRepo.findByPartialName(name);
    }
    @Override
    public List<User> findAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public List<User> findTeamMembers(Long teamId) {
        return userRepo.findAllByTeamId(teamId);
    }
    @Override
    public void addUserToTeam(Long teamId, Long userId) {
        Team team = teamRepo.findById(teamId).orElseThrow();
        User user = userRepo.findById(userId).orElseThrow();
        user.setTeam(team);
        userRepo.save(user);
    }
    @Override
    public void removeUserFromTeam(Long teamId, Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        Team userTeam = user.getTeam();
        if (userTeam == null || !userTeam.getId().equals(teamId)) {
            throw new IllegalStateException("Пользователь не состоит в этой команде");
        }
        user.setTeam(null);
        userRepo.save(user);
    }
}
