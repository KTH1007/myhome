package com.project.myhome.service;

import com.project.myhome.model.Role;
import com.project.myhome.model.User;
import com.project.myhome.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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

    public List<User> findByUsernameQuery(String username) {
        return userRepository.findByUsernameQuery(username);
    }

    public List<User> findByUsernameNativeQuery(String username) {
        return userRepository.findByUsernameNativeQuery(username);
    }

    public User findByUsername(String username){
        return userRepository.findByUsername(username);
    }

}
