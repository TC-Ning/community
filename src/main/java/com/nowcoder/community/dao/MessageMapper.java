package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    List<Message> selectConversationList(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    int selectConversationCount(int userId);

    List<Message> selectMessageList(@Param("conversationId") String conversationId, @Param("offset") int offset, @Param("limit") int limit);

    int selectMessageCount(String conversation);

    int selectUnreadMessageCount(@Param("userId") int userId, @Param("conversationId") String conversationId);

    int insertMessage(Message message);

    int updateStatus(@Param("ids") List<Integer> ids, @Param("status") int status);

}
