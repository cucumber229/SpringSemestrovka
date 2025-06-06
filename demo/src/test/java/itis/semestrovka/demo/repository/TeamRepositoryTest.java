package itis.semestrovka.demo.repository;

import itis.semestrovka.demo.model.entity.Role;
import itis.semestrovka.demo.model.entity.Team;
import itis.semestrovka.demo.model.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TeamRepositoryTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("findByIdWithMembers should return team with users")
    void findByIdWithMembers() {
        Team team = new Team();
        team.setName("Alpha");

        User user = new User();
        user.setUsername("u1");
        user.setEmail("u1@mail.ru");
        user.setPhone("123");
        user.setPassword("pass");
        user.setRole(Role.ROLE_USER);
        user.setTeam(team);
        team.getMembers().add(user);

        em.persist(team);
        em.flush();

        Team found = teamRepository.findByIdWithMembers(team.getId()).orElseThrow();
        assertThat(found.getMembers()).hasSize(1);
        assertThat(found.getMembers().get(0).getUsername()).isEqualTo("u1");
    }

    @Test
    @DisplayName("findByPartialName should search and sort")
    void findByPartialName() {
        Team older = new Team();
        older.setName("My Team");
        older.setCreatedAt(LocalDateTime.now().minusHours(2));

        Team newer = new Team();
        newer.setName("Team Rocket");
        newer.setCreatedAt(LocalDateTime.now());

        em.persist(older);
        em.persist(newer);
        em.flush();

        List<Team> result = teamRepository.findByPartialName("team");
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Team Rocket");
        assertThat(result.get(1).getName()).isEqualTo("My Team");
    }
}
