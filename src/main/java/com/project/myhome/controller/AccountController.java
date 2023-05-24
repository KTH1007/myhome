package com.project.myhome.controller;

import com.project.myhome.model.User;
import com.project.myhome.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/account")
public class AccountController {
    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

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
        if(username.length() < 6){
            model.addAttribute("usernameError", "id는 6~20자리 이상이어야 됩니다.");
            return "account/register";
        }
        if(password1.length() < 8){
            model.addAttribute("passwordError", "비밀번호는 8자리 이상이어야 됩니다.");
            return "account/register";
        }
        String pattern = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*+=]).+$";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(password1);
        if(!matcher.matches()){
            model.addAttribute("passwordError", "비밀번호는 영어, 숫자, 특수문자(!, @, #, $, %, ^, &, *, +,=)가 포함되어야 합니다.");
            return "account/register";
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password1);
        userService.save(user);
        return "redirect:/";
    }

}
