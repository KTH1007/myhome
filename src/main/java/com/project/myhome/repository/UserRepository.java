package com.project.myhome.repository;

import com.project.myhome.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long>{
    @EntityGraph(attributePaths = {"boards"})
    List<User> findAll();
    User findByUsername(String username);
}
