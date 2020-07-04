package com.it.kill.service.impl;

import com.it.kill.dao.IUserDao;
import com.it.kill.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private IUserDao iUserDao;

    public Map<String, Object> getUser(String username, String password) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        if (username == null || username.equals("")){
            resultMap.put("result",false);
            resultMap.put("msg","用户名不能为空");
            return resultMap;
        }
        ArrayList<Map<String, Object>> list = iUserDao.getUser(username, password);
        if (list == null || list.size() == 0){
            resultMap.put("result",false);
            resultMap.put("msg","没找到会员消息");
            return resultMap;
        }
        resultMap = list.get(0);
        resultMap.put("result",true);
        resultMap.put("msg","");
        return resultMap;
    }

    public Map<String, Object> insertUser(String username, String password, String phone) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (username == null || username.equals("")){
            resultMap.put("result",false);
            resultMap.put("msg","用户名不能为空");
            return resultMap;
        }
        int user_id = iUserDao.insertUser(username, password, phone);
        if (user_id <= 0){
            resultMap.put("result",false);
            resultMap.put("msg","数据库没有执行成功");
            return resultMap;
        }
        resultMap.put("user_id",user_id);
        resultMap.put("username",username);
        resultMap.put("password",password);
        resultMap.put("phone",phone);
        resultMap.put("result",true);
        resultMap.put("msg","");
        return resultMap;
    }
}
