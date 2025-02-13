package com.example.music.user;

import com.example.music.free.FreeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username) != null;
    }

    public void createSiteUser(SiteUser siteUser) {
        if(siteUser.getRole() == null) {
            siteUser.setRole(Role.USER);
        }
        userRepository.save(siteUser);
    }

    public SiteUser validateUser(String username, String password) {
        SiteUser user = userRepository.findByUsername(username);
        if(user != null && user.getPassword().equals(password)) {
            return user;
        }else {
            return null;
        }
    }
}
