package com.project.myhome.controller;

import com.project.myhome.model.User;
import com.project.myhome.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(){
        return "account/login";
    }

    @GetMapping("/register")
    public String register(){
        return "account/register";
    }


    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password1, @RequestParam String password2, Model model){
        if (userService.checkUserName(username)) {
            model.addAttribute("usernameError", "이미 사용 중인 아이디입니다.");
            return "account/register";
        }
        if(!password1.equals(password2)){
            model.addAttribute("passwordError", "비밀번호가 일치하지 않습니다.");
            return "account/register";
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password1);
        userService.save(user);
        return "redirect:/";
    }

}
