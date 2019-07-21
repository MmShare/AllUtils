package com.meng.tools.controller;

import com.meng.tools.Repos.UserRepos;
import com.meng.tools.entity.Myuser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @description:
 * @author: xiapq
 * @date: 2019-06-15 16:22
 */

@Controller
@RequestMapping(value = "/test")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepos userRepos;

    @RequestMapping(value = "/get")
    @ResponseBody
    public List<Myuser> get() {
        logger.info("get information");
        return userRepos.findAll();
    }
}
