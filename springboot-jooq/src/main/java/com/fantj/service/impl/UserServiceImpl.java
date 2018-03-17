package com.fantj.service.impl;

import com.fantj.daos.UserDao;
import com.fantj.service.UserService;
import com.generator.tables.User;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Iterator;


/**
 * Created by Fant.J.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    DSLContext dsl;
/*
    @Autowired
    private UserDao userDao;*/

    com.generator.tables.User u =  User.USER_.as("u");
    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(int id) {
        dsl.delete(u).where(u.ID.eq(id));
    }

    /**
     * 增加
     *
     * @param user
     */
    @Override
    public void insert(com.fantj.pojos.User user) {
        dsl.insertInto(u).
                columns(u.ADDRESS,u.BIRTHDAY,u.SEX,u.USERNAME).
                values(user.getAddress(),user.getBirthday(),user.getSex(),user.getUsername())
                .execute();
    }

    /**
     * 更新
     *
     * @param user
     */
    @Override
    public int update(com.fantj.pojos.User user) {
        dsl.update(u).set((Record) user);
        return 0;
    }

    /**
     * 查询单个
     *
     * @param id
     */
    @Override
    public com.fantj.pojos.User selectById(int id) {
        Result result =  dsl.select(u.ADDRESS,u.BIRTHDAY,u.ID,u.SEX,u.USERNAME)
                .from(u)
                .where(u.ID.eq(id)).fetch();
        System.out.println(result.get(0).toString());
        String className = result.get(0).getClass().getName();
        System.out.println(className);
        com.fantj.pojos.User user = new com.fantj.pojos.User();
        return null;
        /*com.fantj.pojos.User user1 = userDao.findById(id);
        return user1;*/
    }

    /**
     * 查询全部列表
     *  @param pageNum
     * @param pageSize
     */
    @Override
    public Iterator<com.fantj.pojos.User> selectAll(int pageNum, int pageSize) {
        Result result = dsl.select().from(u).fetch();

        return result.iterator();
    }
}
