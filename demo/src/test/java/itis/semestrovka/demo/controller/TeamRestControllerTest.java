package itis.semestrovka.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import itis.semestrovka.demo.model.dto.TeamDto;
import itis.semestrovka.demo.model.entity.Team;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.service.TeamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamRestController.class)
class TeamRestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TeamService teamService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllReturnsTeamList() throws Exception {
        Team team = new Team();
        team.setId(1L);
        team.setName("Alpha");
        when(teamService.findAllTeams()).thenReturn(List.of(team));

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReturnsCreatedTeam() throws Exception {
        TeamDto dto = new TeamDto();
        dto.setName("Alpha");
        Team saved = new Team();
        saved.setId(2L);
        when(teamService.create(any(Team.class))).thenReturn(saved);

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByIdReturnsTeam() throws Exception {
        Team team = new Team();
        team.setId(5L);
        when(teamService.findById(5L)).thenReturn(team);

        mockMvc.perform(get("/api/teams/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeUserCallsService() throws Exception {
        mockMvc.perform(delete("/api/teams/1/users/7"))
                .andExpect(status().isNoContent());

        verify(teamService).removeUserFromTeam(1L, 7L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMembersReturnsUserList() throws Exception {
        User u = new User();
        u.setId(3L);
        u.setUsername("user");
        when(teamService.findTeamMembers(1L)).thenReturn(List.of(u));

        mockMvc.perform(get("/api/teams/1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }
}
