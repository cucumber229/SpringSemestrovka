package itis.semestrovka.demo.service;
import itis.semestrovka.demo.model.dto.RegistrationForm;
import itis.semestrovka.demo.model.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;
public interface UserService extends UserDetailsService {
    User register(RegistrationForm form);
    User findById(Long id);
    void updatePhone(User user, String phone);
    void updatePassword(User user, String password);
}
