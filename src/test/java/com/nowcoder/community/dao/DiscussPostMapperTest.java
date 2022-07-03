package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
public class DiscussPostMapperTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void selectDiscussPostsTest() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10);
//        for(DiscussPost discussPost : list) {
//            System.out.println(discussPost);
//        }
        list = discussPostMapper.selectDiscussPosts(149, 0 ,10);
        for(DiscussPost discussPost : list) {
            System.out.println(discussPost);
        }
    }

    @Test
    public void selectDiscussPostRowsTest() {
        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
        rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void insertDiscussPostTest() {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(150);
        discussPost.setTitle("SpringBoot");
        discussPost.setContent("111");
        discussPost.setType(0);
        discussPost.setStatus(1);
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(0);
        discussPost.setScore(0);
        discussPostMapper.insertDiscussPost(discussPost);
    }
}
