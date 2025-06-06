package itis.semestrovka.demo.service.impl;
import itis.semestrovka.demo.model.dto.RegistrationForm;
import itis.semestrovka.demo.model.entity.Role;
import itis.semestrovka.demo.model.entity.User;
import itis.semestrovka.demo.repository.UserRepository;
import itis.semestrovka.demo.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository   users;
    private final PasswordEncoder  encoder;
    @Override
    public User register(RegistrationForm form) {
        users.findByUsername(form.getUsername())
                .ifPresent(u -> { throw new IllegalArgumentException("Пользователь уже существует"); });
        users.findByEmail(form.getEmail())
                .ifPresent(u -> { throw new IllegalArgumentException("Email уже используется"); });
        users.findByPhone(form.getPhone())
                .ifPresent(u -> { throw new IllegalArgumentException("Телефон уже используется"); });
        User u = new User();
        u.setUsername(form.getUsername());
        u.setPassword(encoder.encode(form.getPassword()));
        u.setPasswordSet(true);
        u.setEmail(form.getEmail());
        u.setPhone(form.getPhone());
        u.setRole(Role.ROLE_USER);
        return users.save(u);
    }
    @Override
    public User findById(Long id) {
        return users.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Пользователь с id=" + id + " не найден"));
    }
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        return users.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Пользователь " + username + " не найден"));
    }
    @Override
    public void updatePhone(User user, String phone) {
        users.findByPhone(phone).ifPresent(u -> {
            if (!u.getId().equals(user.getId())) {
                throw new IllegalArgumentException("Телефон уже используется");
            }
        });
        user.setPhone(phone);
        users.save(user);
    }
    @Override
    public void updatePassword(User user, String password) {
        user.setPassword(encoder.encode(password));
        user.setPasswordSet(true);
        users.save(user);
    }
}
