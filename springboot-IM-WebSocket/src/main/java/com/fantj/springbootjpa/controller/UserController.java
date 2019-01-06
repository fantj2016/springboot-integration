package com.fantj.springbootjpa.controller;

import com.fantj.springbootjpa.pojo.User;
import com.fantj.springbootjpa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @GetMapping("/{id}")
    public User findById(@PathVariable Integer id){
        return userService.queryById(id);
    }

    @PostMapping("/insert")
    public void addUser(User user){
        userService.addUser(user);
    }


    @PostMapping("/update")
    public User updateUser(User user){
         return userService.updateUser(user);
    }


    @GetMapping("/del/{id}")
    public void delUser(@PathVariable Integer id){
        userService.delUser(id);
    }

    @GetMapping("/list")
    public List<User> findAll(){
        return userService.findAll();
    }
}
