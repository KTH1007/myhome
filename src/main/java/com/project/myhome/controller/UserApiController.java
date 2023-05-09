package com.project.myhome.controller;

import com.project.myhome.mapper.UserMapper;
import com.project.myhome.model.Board;
import com.project.myhome.model.User;
import com.project.myhome.repository.UserRepository;
import com.project.myhome.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
class UserApiController {

    private final UserRepository userRepositoryrepository;

    private final UserService userService;

    private final UserMapper userMapper;

    public UserApiController(UserRepository userRepositoryrepository, UserService userService, UserMapper userMapper) {
        this.userRepositoryrepository = userRepositoryrepository;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    List<User> all(@RequestParam(required = false) String method, @RequestParam(required = false) String text) {
        List<User> users = null;
        if("query".equals(method)){
            users = userService.findByUsernameQuery(text);
        }
        else if("nativeQuery".equals(method)){
            users = userService.findByUsernameNativeQuery(text);
        }
        else if("mybatis".equals(method)){
            users = userMapper.getUsers(text);
        }
        else users = userRepositoryrepository.findAll();
        return users;
    }

    @PostMapping("/users")
    User newUser(@RequestBody User newUser) {
        return userRepositoryrepository.save(newUser);
    }


    @GetMapping("/users/{id}")
    User one(@PathVariable Long id) {

        return userRepositoryrepository.findById(id).orElse(null);
    }

    @PutMapping("/users/{id}")
    User replaceUser(@RequestBody User newUser, @PathVariable Long id) {

        return userRepositoryrepository.findById(id)
                .map(user -> {
//                    user.setTitle(newUser.getTitle());
//                    user.setContent(newUser.getContent());
//                    user.setBoards(newUser.getBoards());
                    user.getBoards().clear();
                    user.getBoards().addAll(newUser.getBoards());
                    for(Board board : user.getBoards()){
                        board.setUser(user);
                    }
                    return userRepositoryrepository.save(user);
                })
                .orElseGet(() -> {
                    newUser.setId(id);
                    return userRepositoryrepository.save(newUser);
                });
    }

    @DeleteMapping("/users/{id}")
    void deleteUser(@PathVariable Long id) {
        userRepositoryrepository.deleteById(id);
    }
}
