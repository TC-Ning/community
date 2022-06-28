package com.nowcoder.community.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailClientTest {

    @Autowired
    private MailClient mailClient;

    @Test
    public void sendMailTest() {
        mailClient.sendMail("1251694728@qq.com", "test", "Hello mail!");
    }

}
