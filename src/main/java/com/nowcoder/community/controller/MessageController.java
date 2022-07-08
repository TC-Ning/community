package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.jws.WebParam;
import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

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
        int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", unreadNoticeCount);

        return "site/letter";
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
            List<Integer> ids = getUnreadLetterIds(messageList);
            if(!ids.isEmpty()) {
                messageService.readMessages(ids);
            }
        }
        return "site/letter-detail";
    }

    private List<Integer> getUnreadLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        for(Message message : letterList) {
            if(message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0) {
                ids.add(message.getId());
            }
        }
        return ids;
    }

    @PostMapping("/letter/send")
    @ResponseBody
    public String sendMessage(String toName, String content) {
        User target = userService.findUserByName(toName);
        if(target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }
        if(StringUtils.isBlank(content)) {
            return CommunityUtil.getJSONString(1, "内容不能为空！");
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

    @GetMapping("/notice/list")
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> noticeVo = handleNotice(message, user);
        model.addAttribute("commentNotice", noticeVo);

        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        noticeVo = handleNotice(message, user);
        model.addAttribute("likeNotice", noticeVo);

        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        noticeVo = handleNotice(message, user);
        model.addAttribute("followNotice", noticeVo);

        int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", unreadNoticeCount);
        int unreadMessageCount = messageService.findUnreadMessageCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", unreadMessageCount);

        return "site/notice";
    }

    private Map<String, Object> handleNotice(Message message, User user) {
        Map<String, Object> noticeVo = new HashMap<>();
        noticeVo.put("message", message);
        if(message != null) {
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            noticeVo.put("user", userService.findUserById((Integer) data.get("userId")));
            noticeVo.put("entityType", data.get("entityType"));
            noticeVo.put("entityId", data.get("entityId"));
            noticeVo.put("postId", data.get("postId"));
            int noticeCount = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            noticeVo.put("count", noticeCount);
            int unread = messageService.findUnreadNoticeCount(user.getId(), TOPIC_COMMENT);
            noticeVo.put("unread", unread);
        }
        return noticeVo;
    }

    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(Model model, Page page, @PathVariable("topic") String topic) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));
        List<Message> notices = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if(notices != null) {
            for(Message notice : notices) {
                Map<String, Object> map = new HashMap<>();
                map.put("notice", notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);
        List<Integer> ids = getUnreadLetterIds(notices);
        if(!ids.isEmpty()) {
            messageService.readMessages(ids);
        }
        return "site/notice-detail";
    }
}
