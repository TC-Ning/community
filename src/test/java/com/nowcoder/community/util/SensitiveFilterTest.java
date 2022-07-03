package com.nowcoder.community.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SensitiveFilterTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void filterTest() {
        System.out.println(sensitiveFilter.filter("坚决不能✧吸✧毒和嫖娼"));
    }

}
