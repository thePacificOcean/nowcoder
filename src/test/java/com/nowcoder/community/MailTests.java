package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

/**
 * @Author: xhy
 * @description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail(){
        mailClient.sendMail("2539140904@qq.com","测试","欢迎使用 Spring Boot");
    }

    //发送动态网页
    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "sunday");  //将变量内容填充到模板中

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("x2539140904@qq.com","HTML",content);
    }

}
