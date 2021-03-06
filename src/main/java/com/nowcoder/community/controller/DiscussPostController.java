package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @PostMapping("/add")
    @ResponseBody
    @LoginRequired
    public String addDiscussPost(String title, String content) {
        if(StringUtils.isBlank(title)) {
            return CommunityUtil.getJSONString(1, "标题不能为空！");
        }
        User user = hostHolder.getUser();
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.insertDiscussPost(post);

        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    @GetMapping("/detail/{discussPostId}")
    @LoginRequired
    public String getDetailPage(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 帖子作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeCount", likeCount);
        // 点赞状态
        int likeStatus = likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeStatus", likeStatus);

        page.setLimit(5);
        page.setRows(commentService.findCommentCount(ENTITY_TYPE_POST, discussPostId));
        page.setPath("/discuss/detail/" + discussPostId);

        // 评论：给帖子的评论
        // 回复：给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, discussPostId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        for(Comment comment : commentList) {
            Map<String, Object> commentVo = new HashMap<>();
            commentVo.put("comment", comment);
            commentVo.put("user", userService.findUserById(comment.getUserId()));
            likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
            commentVo.put("likeCount", likeCount);
            likeStatus = likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
            commentVo.put("likeStatus", likeStatus);

            // 当前评论的回复列表
            List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
            List<Map<String, Object>> replyVoList = new ArrayList<>();
            for(Comment reply : replyList) {
                Map<String, Object> replyVo = new HashMap<>();
                replyVo.put("reply", reply);
                replyVo.put("user", userService.findUserById(reply.getUserId()));
                User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                replyVo.put("target", target);
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                replyVo.put("likeCount", likeCount);
                likeStatus = likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                replyVo.put("likeStatus", likeStatus);

                replyVoList.add(replyVo);
            }
            commentVo.put("replys", replyVoList);
            int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
            commentVo.put("replyCount", replyCount);
            commentVoList.add(commentVo);
        }

        model.addAttribute("comments", commentVoList);
        return "site/discuss-detail";
    }



}
