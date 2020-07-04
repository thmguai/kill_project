package com.it.kill.controller;


import com.alibaba.fastjson.JSON;
import com.it.kill.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private IUserService iUserService;

    @RequestMapping("/login/{username}/{password}")
    public Map<String,Object> login(@PathVariable("username") String username,
                                    @PathVariable("password") String password, HttpServletRequest request){
        Map<String, Object> userMap = new HashMap<String, Object>();
        userMap = iUserService.getUser(username, password);
        if (!(Boolean) userMap.get("result")){
            //没查到，插入一个用户
            userMap = iUserService.insertUser(username, password, password);
            if (!(Boolean) userMap.get("result")){
                return userMap;
            }
        }
        HttpSession session = request.getSession();
        String user = JSON.toJSONString(userMap);
        session.setAttribute("user",user);
        return userMap;
    }
}
