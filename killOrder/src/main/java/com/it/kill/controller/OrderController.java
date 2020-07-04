package com.it.kill.controller;

import com.alibaba.fastjson.JSONObject;
import com.it.kill.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OrderController {

    @Autowired
    private IOrderService iOrderService;


    @RequestMapping("/createOrder/{sku_id}")
    public Map<String,Object> createOrder(@PathVariable("sku_id") String sku_id, HttpServletRequest request){
        Map<String, Object> resultMap = new HashMap<String, Object>();
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");
        if (user == null || user.equals("")){
            resultMap.put("result",false);
            resultMap.put("msg","您未登入");
            return resultMap;
        }
        Map<String,Object> userMap = JSONObject.parseObject(user, Map.class);
        String user_id = userMap.get("user_id").toString();
        //String user_id = "1";
        resultMap = iOrderService.createOrder(sku_id, user_id);
        if (!(Boolean) resultMap.get("result")){
            resultMap.put("msg","订单创建失败");
            return resultMap;
        }
        resultMap.put("result",true);
        resultMap.put("msg","");
        return resultMap;
    }

    @RequestMapping(value = "/getOrder/{order_id}")
    public Map<String, Object> getOrder(@PathVariable("order_id") String order_id){
        return iOrderService.getOrder(order_id);
    }

    @RequestMapping(value = "/payOrder/{order_id}/{sku_id}")
    public Map<String, Object> payOrder(@PathVariable("order_id") String order_id, @PathVariable("sku_id") String sku_id){
        //正常情况下在这里会调用支付接口，我们这里模拟支付已经返回正常数据
        boolean isPay = true;
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (!isPay){
            resultMap.put("result", false);
            resultMap.put("msg", "支付接口调用失败！");
            return resultMap;
        }

        return iOrderService.payOrder(order_id, sku_id);
    }
}
