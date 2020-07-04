package com.it.kill.queue;


import com.alibaba.fastjson.JSONObject;
import com.it.kill.service.IOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderQueue {

    @Autowired
    private IOrderService iOrderService;

    @RabbitListener(queues = "order_queue")
    public void insertOrder(String msg){
        //1、接收消息并输出
        System.out.println("order_queue接收消息："+msg);

        //2、调用一个写入订单方法
        Map<String, Object> orderInfo = JSONObject.parseObject(msg, Map.class);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap = iOrderService.insertOrder(orderInfo);

        //3、如果没写成功输出错误消息
        if (!(Boolean) resultMap.get("result")){
            System.out.println("order_queue处理消息失败：");
        }

        //4、成功输出消息
        System.out.println("order_queue处理消息成功！");
    }
}
