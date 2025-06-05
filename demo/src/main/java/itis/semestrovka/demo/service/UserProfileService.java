package itis.semestrovka.demo.service;

import itis.semestrovka.demo.model.entity.UserProfile;

public interface UserProfileService {
    UserProfile findByUserId(Long userId);
    UserProfile save(UserProfile profile);
}
