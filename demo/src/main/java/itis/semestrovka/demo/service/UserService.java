// src/main/java/itis/semestrovka/demo/service/UserService.java
package itis.semestrovka.demo.service;

import itis.semestrovka.demo.model.dto.RegistrationForm;
import itis.semestrovka.demo.model.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Сервис работы с пользователями.
 * Наследуется от {@link UserDetailsService}, чтобы Spring-Security
 * мог использовать его для загрузки пользователя по username.
 */
public interface UserService extends UserDetailsService {

    /** Регистрация нового пользователя */
    User register(RegistrationForm form);

    /** Найти пользователя по id (или бросить исключение) */
    User findById(Long id);

    /** Обновить номер телефона пользователя */
    void updatePhone(User user, String phone);
}
