package com.meng.tools.oauth;

import com.meng.tools.Repos.UserRepos;
import com.meng.tools.entity.Myuser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @description:
 * @author: xiapq
 * @date: 2019-07-13 15:54
 */
public class MyUserDetail implements UserDetailsService {


    @Autowired
    private UserRepos userRepos;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Myuser userByName = userRepos.findUserByName(username);
        if (userByName==null){
            throw  new UsernameNotFoundException("Myuser: "+username +"not be found");
        }
        User user=new User(userByName.getName(),userByName.getPwd().toString(),true,true,true,true,null);
        return user;
    }
}
