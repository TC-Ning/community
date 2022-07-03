package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "**";

    private TrieNode root = new TrieNode();

    @PostConstruct
    public void init() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    private void addKeyword(String keyword) {
        TrieNode temp = root;
        for (int i = 0; i < keyword.length(); i++) {
            char ch = keyword.charAt(i);
            TrieNode subNode = temp.getSubNode(ch);
            if (subNode == null) {
                subNode = new TrieNode();
                temp.addSubNode(ch, subNode);
            }
            temp = subNode;
        }
        temp.setEnd(true);
    }

    public String filter(String text) {
        TrieNode temp;
        StringBuilder sb = new StringBuilder();
        int i = 0, j = 0;
        while(i < text.length()) {
            temp = root;
            for(j = i; j < text.length(); j++) {
                char ch = text.charAt(j);
                if(isSymbol(ch)) {
                    if(j == i) {
                        sb.append(ch);
                        i++;
                    }
                    continue;
                }
                TrieNode subNode = temp.getSubNode(ch);
                if(subNode != null) {
                    if(subNode.isEnd()) {
                        sb.append(REPLACEMENT);
                        i = j + 1;
                        break;
                    }
                    temp = subNode;
                } else {
                    sb.append(text.charAt(i));
                    i++;
                    break;
                }
            }
        }
        return sb.toString();
    }

    private boolean isSymbol(Character c) {
        return (c < 0x2E80 || c > 0x9FFF) && !CharUtils.isAsciiAlphanumeric(c);
    }

    private class TrieNode {

        private Map<Character, TrieNode> subNodes = new HashMap<>();

        private boolean isEnd;

        public boolean isEnd() {
            return isEnd;
        }

        public void setEnd(boolean end) {
            isEnd = end;
        }

        // 添加子结点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子结点
        public TrieNode getSubNode(Character character) {
            return subNodes.get(character);
        }
    }

}
