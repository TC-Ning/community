package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class MessageMapperTest {

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void selectConversationListTest() {
        List<Message> conversationList = messageMapper.selectConversationList(111, 0, 5);
        for (Message message : conversationList) {
            System.out.println(message);
        }
    }

    @Test
    public void selectConversationCountTest() {
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);
    }

    @Test
    public void selectMessageListTest() {
        List<Message> messageList = messageMapper.selectMessageList("111_113", 0, 5);
        for (Message message : messageList) {
            System.out.println(message);
        }
    }

    @Test
    public void selectMessageCountTest() {
        System.out.println(messageMapper.selectMessageCount("111_113"));
    }

    @Test
    public void selectUnreadMessageCountTest() {
        int count = messageMapper.selectUnreadMessageCount(111, null);
        System.out.println(count);
        count = messageMapper.selectUnreadMessageCount(111, "111_113");
        System.out.println(count);
    }

    @Test
    public void insertMessageTest() {

    }

    @Test
    public void updateStatusTest() {
        messageMapper.updateStatus(Arrays.asList(1, 2, 3), 1);
    }
}
