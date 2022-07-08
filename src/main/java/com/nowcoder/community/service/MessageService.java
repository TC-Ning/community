package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Comparator;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversationList(int userId, int offset, int limit) {
        return messageMapper.selectConversationList(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findMessageList(String conversationId, int offset, int limit) {
        List<Message> messageList = messageMapper.selectMessageList(conversationId, offset, limit);
        messageList.sort(new Comparator<Message> () {
            @Override
            public int compare(Message m1, Message m2) {
                return m1.getId() - m2.getId();
            }
        });
        return messageList;
    }

    public int findMessageCount(String conversation) {
        return messageMapper.selectMessageCount(conversation);
    }

    public int findUnreadMessageCount(int userId, String conversationId) {
        return messageMapper.selectUnreadMessageCount(userId, conversationId);
    }

    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int readMessages(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }

    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    public int findUnreadNoticeCount(int userId, String topic) {
        return messageMapper.selectUnreadNoticeCount(userId, topic);
    }

    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }
}
