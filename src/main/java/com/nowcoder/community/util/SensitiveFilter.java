package com.nowcoder.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xhy
 * @description:
 */
@Component
public class SensitiveFilter {
    private static final Logger logger= LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT="***";

    //根节点
    private TireNode rootNode=new TireNode();

    @PostConstruct
    public void init(){
        try(
                InputStream is=this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader=new BufferedReader(new InputStreamReader(is))
        ){
            String keyword;
            while((keyword=reader.readLine())!=null){
                //添加到前缀树
                this.addKeyword(keyword);
            }
        }catch(IOException e){
            logger.error("加载敏感词文件失败："+e.getMessage());
        }
    }

    //将一个敏感词添加到前缀树
    private void addKeyword(String keyword){
        TireNode tempNode=rootNode;
        for(int i=0;i<keyword.length();i++){
            char c=keyword.charAt(i);
            TireNode subNode=tempNode.getSubNode(c);
            if(subNode==null){
                subNode=new TireNode();
                tempNode.addSubNode(c,subNode);
            }
            
            tempNode=subNode;
            if(i==keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }

    }

    /**
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }
        //指针1
        TireNode tempNode=rootNode;
        //指针2
        int begin=0;
        //指针3
        int position=0;
        //结果
        StringBuilder sb=new StringBuilder();
        while(position<text.length()){
            char c=text.charAt(position);

            //跳过特殊符号
            if(isSymbol(c)){
                if(tempNode==rootNode){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            //检查下级节点
            tempNode=tempNode.getSubNode(c);
            if(tempNode==null){
                sb.append(text.charAt(begin));
                position=++begin;
                tempNode=rootNode;
            }else if(tempNode.isKeywordEnd()){
                sb.append(REPLACEMENT);
                begin=++position;
                tempNode=rootNode;
            }else{
                position++;
            }

        }
        sb.append(text.substring(begin));

        return sb.toString();
    }
    //判断是否是符号
    private boolean isSymbol(Character c){
        //0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c<0x2E80 || c>0x9FFF);
    }
    //前缀树
    private class TireNode{
        //关键词结束表示
        private boolean isKeywordEnd = false;

        //子节点（key是下级字符，value是下级节点）
        private Map<Character,TireNode> subNodes=new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character c,TireNode node){
            subNodes.put(c,node);
        }
        public TireNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
