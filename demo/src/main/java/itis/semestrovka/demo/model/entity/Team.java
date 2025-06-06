package itis.semestrovka.demo.model.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Entity
@Table(name = "teams")
@Getter @Setter @NoArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<User> members = new HashSet<>();
    private String name;
    private String description;
    private LocalDateTime createdAt = LocalDateTime.now();
    public List<User> getMembers() {
        return List.copyOf(members);   
    }
}
