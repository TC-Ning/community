package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    UserMapper userMapper;

    @Test
    public void selectByIdTest() {
        User user = userMapper.selectById(101);
        System.out.println(user);
    }

    @Test
    public void selectByUsernameTest() {
        User user = userMapper.selectByUsername("liubei");
        System.out.println(user);
    }

    @Test
    public void selectByEmailTest() {
        User user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void insertUserTest() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void updateStatusTest() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);
    }

    @Test
    public void updateHeaderTest() {
        int rows = userMapper.updateHeader(150, "http://images.nowcoder.com/head/102.png");
        System.out.println(rows);
    }

    @Test
    public void updatePasswordTest() {
        int rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }
}
