package itis.semestrovka.demo.service.impl;

import itis.semestrovka.demo.model.entity.UserProfile;
import itis.semestrovka.demo.repository.UserProfileRepository;
import itis.semestrovka.demo.repository.UserRepository;
import itis.semestrovka.demo.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository repo;
    private final UserRepository userRepo;

    @Override
    public UserProfile findByUserId(Long userId) {
        return repo.findByUser_Id(userId).orElseGet(() -> {
            UserProfile p = new UserProfile();
            p.setUser(userRepo.findById(userId).orElseThrow());
            return repo.save(p);
        });
    }

    @Override
    public UserProfile save(UserProfile profile) {
        return repo.save(profile);
    }
}
