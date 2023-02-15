package com.project.myhome.repository;

import com.project.myhome.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{
    User findByUsername(String username);
}
