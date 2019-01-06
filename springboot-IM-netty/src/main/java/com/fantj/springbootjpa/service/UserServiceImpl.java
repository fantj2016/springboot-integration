package com.fantj.springbootjpa.service;

import com.fantj.springbootjpa.pojo.User;
import com.fantj.springbootjpa.repostory.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;
    /**
     * 根据id查询user信息
     */
    @Override
    public User queryById(Integer id) {
//        Example example = new Example();
        return userRepository.findUserById(id);
    }

    @Override
    public void addUser(User user) {
        userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void delUser(Integer userId) {
         userRepository.deleteById(userId);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }


}
