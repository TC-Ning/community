package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @GetMapping("/letter/list")
    @LoginRequired
    public String getConversationPage(Model model, Page page) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        List<Message> conversationList = messageService.findConversationList(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversationVoList = new ArrayList<>();
        for(Message message : conversationList) {
            Map<String, Object> conversationVo = new HashMap<>();
            conversationVo.put("conversation", message);
            conversationVo.put("letterCount", messageService.findMessageCount(message.getConversationId()));
            conversationVo.put("unreadCount", messageService.findUnreadMessageCount(user.getId(), message.getConversationId()));
            if(user.getId() == message.getFromId()) {
                conversationVo.put("target", userService.findUserById(message.getToId()));
            } else {
                conversationVo.put("target", userService.findUserById(message.getFromId()));
            }
            conversationVoList.add(conversationVo);
        }
        model.addAttribute("conversations", conversationVoList);
        int unreadLetterCount = messageService.findUnreadMessageCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", unreadLetterCount);
        return "/site/letter";
    }

    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(Model model, Page page, @PathVariable("conversationId") String conversationId) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findMessageCount(conversationId));
        List<Message> messageList = messageService.findMessageList(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letterVoList = new ArrayList<>();
        for(Message message : messageList) {
            Map<String, Object> map = new HashMap<>();
            map.put("letter", message);
            User fromUser = userService.findUserById(message.getFromId());
            map.put("fromUser", fromUser);
            if(user.getId() == fromUser.getId()) {
                map.put("right", true);
            } else {
                map.put("right", false);
            }
            letterVoList.add(map);
        }
        model.addAttribute("letters", letterVoList);
        model.addAttribute("target", null);
        if(!messageList.isEmpty()) {
            int targetId = user.getId() == messageList.get(0).getFromId() ? messageList.get(0).getToId() : messageList.get(0).getFromId();
            model.addAttribute("target", userService.findUserById(targetId));
            List<Integer> ids = new ArrayList<>();
            for(Message message : messageList) {
                if(message.getToId() == user.getId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
            if(!ids.isEmpty()) {
                messageService.readMessages(ids);
            }
        }
        return "/site/letter-detail";
    }

    @PostMapping("/letter/send")
    @ResponseBody
    public String sendMessage(String toName, String content) {
        User target = userService.findUserByName(toName);
        if(target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        message.setContent(content);
        message.setCreateTime(new Date());
        if(message.getFromId() > message.getToId()) {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        } else {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }
}
