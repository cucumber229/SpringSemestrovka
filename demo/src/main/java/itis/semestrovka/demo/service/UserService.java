// src/main/java/itis/semestrovka/demo/service/UserService.java
package itis.semestrovka.demo.service;

import itis.semestrovka.demo.model.dto.RegistrationForm;
import itis.semestrovka.demo.model.entity.User;

public interface UserService {
    User register(RegistrationForm form);
}
