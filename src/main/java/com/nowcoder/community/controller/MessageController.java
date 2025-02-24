package com.nowcoder.community.controller;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @Author: xhy
 * @description:
 */
@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hosrHolder;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    @RequestMapping(path="letter/list",method= RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user=hostHolder.getUser();

        //设置分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        //会话列表
        List<Message> conversationList=messageService.findConversations(user.getId(),page.getOffset(),page.getLimit());
        List<Map<String,Object>> conversations=new ArrayList<>();
        if(conversationList!=null){
            for(Message message:conversationList){
                Map<String,Object> map=new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));

                //得到对话对方的userId
                int targetId=user.getId()==message.getFromId()? message.getToId(): message.getFromId();
                map.put("target",userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        //用户未读信息数量
        int letterUnreadCount=messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        return "/site/letter";
    }

    @RequestMapping(path="/letter/detail/{conversationId}",method=RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Page page,Model model){
        //设置分页信息
        page.setPath("letter/detail/"+conversationId);
        page.setLimit(5);
        page.setRows(messageService.findLetterCount(conversationId));

        //私信列表
        List<Message> letterList=messageService.findLetters(conversationId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> letters=new ArrayList<>();
        if(letterList!=null){
            for(Message message:letterList){
                Map<String,Object> map=new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letterList);
        //判断和谁对话
        User targetUser=getTargetUser(conversationId);
        model.addAttribute("targetUser",targetUser);

        //设置已读
        List<Integer> ids=getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "site/letter-detail";
    }
    private User getTargetUser(String conversationId){
        User user=hostHolder.getUser();
        String[] ids=conversationId.split("_");
        int id0=Integer.parseInt(ids[0]);
        int id1=Integer.parseInt(ids[1]);

        if(user.getId()==id0){
            return userService.findUserById(id1);
        }else{
            return userService.findUserById(id0);
        }
    }
    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids=new ArrayList<>();
        for(Message message:letterList){
            if(hostHolder.getUser().getId()==message.getToId()&&message.getStatus()==0){
                ids.add(message.getId());
            }
        }
        return ids;
    }

    @RequestMapping(path="/letter/send",method=RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){
        User targetUser = userService.findUserByName(toName);
        if(targetUser==null){
            return CommunityUtil.getJsonString(1,"目标用户不存在");
        }
        Message message=new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(targetUser.getId());
        String conversationId=targetUser.getId()<hostHolder.getUser().getId() ?
                targetUser.getId()+"_"+hostHolder.getUser().getId() :
                hostHolder.getUser().getId()+"_"+targetUser.getId();
        message.setConversationId(conversationId);
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);
        messageService.addMessage(message);

        return CommunityUtil.getJsonString(0);
    }


}
