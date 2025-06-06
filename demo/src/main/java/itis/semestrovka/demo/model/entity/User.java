package itis.semestrovka.demo.model.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(unique = true, nullable = false)
    private String phone;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean passwordSet = false;
    @Column(nullable = false)
    private boolean enabled = true;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
    @ManyToMany(mappedBy = "participants")
    private Set<Task> tasks = new HashSet<>();
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile profile;
    @Override public Collection<Role> getAuthorities() { return Collections.singleton(role); }
    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return enabled; }
}
