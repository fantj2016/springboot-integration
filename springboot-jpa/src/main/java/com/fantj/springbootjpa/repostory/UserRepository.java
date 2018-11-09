package com.fantj.springbootjpa.repostory;

import com.fantj.springbootjpa.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Integer>{

    User findUserByUserName(String userName);

    User findUserById(Integer id);

    void deleteById(Integer id);
}
