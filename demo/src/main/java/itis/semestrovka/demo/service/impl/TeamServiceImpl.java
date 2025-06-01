package itis.semestrovka.demo.service.impl;

import itis.semestrovka.demo.model.entity.Team;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.repository.TeamRepository;
import itis.semestrovka.demo.repository.UserRepository;
import itis.semestrovka.demo.service.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepo;
    private final UserRepository userRepo;

    public TeamServiceImpl(TeamRepository teamRepo, UserRepository userRepo) {
        this.teamRepo = teamRepo;
        this.userRepo = userRepo;
    }

    @Override
    public List<Team> findAllTeams() {
        return teamRepo.findAll();
    }

    @Override
    public Team findById(Long id) {
        return teamRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Команда с id=" + id + " не найдена"));
    }

    @Override
    public Team create(Team team) {
        return teamRepo.save(team);
    }

    @Override
    public Team update(Team team) {
        // Предполагаем, что team.id уже установлен и существует в БД
        return teamRepo.save(team);
    }

    @Override
    public void deleteById(Long id) {
        teamRepo.deleteById(id);
    }

    @Override
    public void addUserToTeam(Long teamId, Long userId) {
        Team team = teamRepo.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Команда с id=" + teamId + " не найдена"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с id=" + userId + " не найден"));

        // Добавляем пользователя в команду
        team.getMembers().add(user);
        // Устанавливаем у пользователя ссылку на эту команду
        user.setTeam(team);

        // Сохраняем изменения (хозяин связи – User, но сохраняем и Team для единообразия)
        userRepo.save(user);
        teamRepo.save(team);
    }

    @Override
    public void removeUserFromTeam(Long teamId, Long userId) {
        Team team = teamRepo.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Команда с id=" + teamId + " не найдена"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с id=" + userId + " не найден"));

        // Убираем пользователя из списка членов
        team.getMembers().remove(user);
        // Убираем ссылку у пользователя
        user.setTeam(null);

        userRepo.save(user);
        teamRepo.save(team);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepo.findAll();
    }
}
