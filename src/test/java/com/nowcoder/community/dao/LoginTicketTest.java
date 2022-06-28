package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
public class LoginTicketTest {

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void insertLoginTicketTest() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 10 * 60 * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void updateStatusTest() {
        loginTicketMapper.updateStatus("abc", 1);
    }

    @Test
    public void selectByTicketTest() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

}
