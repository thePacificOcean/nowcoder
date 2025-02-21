package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @Author: xhy
 * @description:
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService  discussPostService;

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;

    @RequestMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user=hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJsonString(403,"你还没有登录");
        }
        DiscussPost discussPost=new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        return CommunityUtil.getJsonString(0,"发布成功");
    }

    @RequestMapping(path = "/detail/{discussPostId}",method= RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){

        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        User user=userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        //查评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(post.getCommentCount());

        //帖子的评论：称为--评论
        //评论的评论：称为--回复
        //评论列表
        List<Comment> comments=commentService.findCommentsByEntity(
                ENTITY_TYPE_POST,post.getId(),page.getOffset(),page.getLimit());
        List<Map<String,Object>> commentVoList=new ArrayList<>();
        if(comments!=null){
            for(Comment c:comments){
                //评论Vo ：Vo的意思是viewObject的意思 视图对象
                Map<String,Object>commentVo=new HashMap<>();
                //放评论
                commentVo.put("comment",c);
                //放作者
                commentVo.put("user",userService.findUserById(c.getUserId()));

                //回复列表
                List<Comment> replys=commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT,c.getId(),0, Integer.MAX_VALUE);
                List<Map<String,Object>> replyVoList=new ArrayList<>();
                if(replys!=null){
                    for(Comment r:replys){
                        Map<String,Object> replyVo=new HashMap<>();
                        replyVo.put("reply",r);
                        replyVo.put("user",userService.findUserById(r.getUserId()));

                        User target= r.getTargetId()==0 ? null :userService.findUserById(r.getUserId());
                        replyVo.put("target",target);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);
                //一条评论的回复数量
                int replyCount=commentService.findCountsByEntity(ENTITY_TYPE_COMMENT,c.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);

        return "/site/discuss-detail";
    }
}
