package com.project.myhome.service;

import com.project.myhome.model.Role;
import com.project.myhome.model.User;
import com.project.myhome.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User save(User user){
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        Role role = new Role();
        role.setId(1);
        user.setEnabled(true);
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    public boolean checkUserName(String username){
        return userRepository.existsByUsername(username);
    }
}
