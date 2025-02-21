package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @Author: xhy
 * @description:持有用户信息，用于代替session对象
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users=new ThreadLocal<User>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    //从线程池中清理当前对象
    public void clear(){
        users.remove();
    }
}
