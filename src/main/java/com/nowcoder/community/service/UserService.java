package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        User user = getCache(id);
        if(user == null) {
            user = initCache(id);
        }
        return user;
    }

    public User findUserByName(String username) {
        return userMapper.selectByUsername(username);
    }

    public LoginTicket findLoginTicket(String ticket) {
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
//        return loginTicketMapper.selectByTicket(ticket);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //????????????
        if(user == null) {
            throw new IllegalArgumentException("?????????????????????");
        }
        if(StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "?????????????????????");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "?????????????????????");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "?????????????????????");
            return map;
        }
        if(user.getUsername().length() > 50) {
            map.put("usernameMsg", "??????????????????");
        }
        //????????????
        User u = userMapper.selectByUsername(user.getUsername());
        if(u != null) {
            map.put("usernameMsg", "?????????????????????");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null) {
            map.put("emailMsg", "????????????????????????");
            return map;
        }

        //????????????
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //????????????
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("mail/activation", context);
        mailClient.sendMail(user.getEmail(), "????????????", content);

        return map;
    }

    public int activation(int userId, String activatioinCode) {
        User user = userMapper.selectById(userId);
        if(user == null) {
            throw new IllegalArgumentException("??????????????????");
        }
        if(user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(activatioinCode)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * ??????
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        //????????????
        if(StringUtils.isBlank(username)) {
            map.put("usernameMsg", "?????????????????????");
            return map;
        }
        if(StringUtils.isBlank(password)) {
            map.put("passwordMsg", "?????????????????????");
            return map;
        }
        //????????????
        User user = userMapper.selectByUsername(username);
        if(user == null) {
            map.put("usernameMsg", "?????????????????????");
            return map;
        }
        //????????????
        if(user.getStatus() == 0) {
            map.put("usernameMsg", "?????????????????????");
            return map;
        }
        //????????????
        password = CommunityUtil.md5(password + user.getSalt());
        if(!password.equals(user.getPassword())) {
            map.put("passwordMsg", "??????????????????");
            return map;
        }
        //??????????????????
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        // ?????????????????????Redis
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * ????????????
     * @param ticket
     */
    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    /**
     * ????????????
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    /**
     * ????????????
     * @param userId
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();
        if(StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "???????????????????????????");
            return map;
        }
        if(StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "????????????????????????");
            return map;
        }
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "???????????????????????????");
            return map;
        }
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);
        clearCache(userId);
        return map;
    }

    public String sendForgetCode(String email) {
        Context context = new Context();
        context.setVariable("email", email);
        String code = CommunityUtil.generateUUID().substring(0, 4);
        context.setVariable("verifyCode", code);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "????????????", content);
        return code;
    }

    public Map<String, Object> resetPassword(String email, String password) {
        Map<String, Object> map = new HashMap<>();
        if(StringUtils.isBlank(email)) {
            map.put("emailMsg", "?????????????????????");
            return map;
        }
        if(StringUtils.isBlank(password)) {
            map.put("passwordMsg", "?????????????????????");
            return map;
        }
        User user = userMapper.selectByEmail(email);
        if(user == null) {
            map.put("emailMsg", "????????????????????????");
            return map;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);
        clearCache(user.getId());
        return map;
    }

    // 1.????????????????????????
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return  (User) redisTemplate.opsForValue().get(userKey);
    }

    // 2.??????????????????????????????????????????????????????
    private User initCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        User user = userMapper.selectById(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.???????????????????????????
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
}
