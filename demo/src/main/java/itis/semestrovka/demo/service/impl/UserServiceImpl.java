// src/main/java/itis/semestrovka/demo/service/impl/UserServiceImpl.java
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

/**
 * Реализация {@link UserService}.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository   users;
    private final PasswordEncoder  encoder;

    /* ---------- регистрация ---------- */
    @Override
    public User register(RegistrationForm form) {
        users.findByUsername(form.getUsername())
                .ifPresent(u -> { throw new IllegalArgumentException("Пользователь уже существует"); });

        User u = new User();
        u.setUsername(form.getUsername());
        u.setPassword(encoder.encode(form.getPassword()));
        u.setRole(Role.ROLE_USER);
        return users.save(u);
    }

    /* ---------- поиск по id ---------- */
    @Override
    public User findById(Long id) {
        return users.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Пользователь с id=" + id + " не найден"));
    }

    /* ---------- для Spring-Security ---------- */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return users.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Пользователь " + username + " не найден"));
    }
}
