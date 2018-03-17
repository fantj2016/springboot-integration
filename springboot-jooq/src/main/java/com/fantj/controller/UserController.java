package com.fantj.controller;

import com.fantj.pojos.User;
import com.fantj.service.UserService;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Fant.J.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.GET,value = "/delete/{id}")
    public void delete(@PathVariable("id")int id){
        userService.delete(id);
    }

    @RequestMapping(method = RequestMethod.POST,value = "/insert")
    public void insert(User user){
        userService.insert(user);
    }
    @RequestMapping(method = RequestMethod.POST,value = "/update/{id}")
    public void update(@RequestParam User user){
        userService.update(user);
    }

    @RequestMapping(method = RequestMethod.GET,value = "/{id}/select")
    public User select(@PathVariable("id")int id){
        return userService.selectById(id);
    }

    @RequestMapping(method = RequestMethod.GET,value = "/selectAll/{pageNum}/{pageSize}")
    public List<User> selectAll(@PathVariable("pageNum") int pageNum, @PathVariable("pageSize") int pageSize){
        Iterator<User> userIterator = userService.selectAll(pageNum, pageSize);
        List<User> list = new ArrayList<>();
        while(userIterator.hasNext()){
            list.add(userIterator.next());
        }
        return list;
    }

}
