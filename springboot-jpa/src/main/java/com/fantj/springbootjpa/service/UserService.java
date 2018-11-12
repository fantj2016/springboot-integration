package com.fantj.springbootjpa.service;

import com.fantj.springbootjpa.pojo.User;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService {
    User queryById(Integer id);

    void addUser(User user);

    User updateUser(User user);

    void delUser(Integer userId);

    List<User> findAll();
}
