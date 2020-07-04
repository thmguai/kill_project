package com.it.kill.queue;


import com.it.kill.service.IStorageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StorageQueue {

    @Autowired
    private IStorageService iStorageService;

    @RabbitListener(queues = "storage_queue")
    public void insertStorage(String msg){
        System.out.println("storage_queue接受消息：" + msg);
        Map<String,Object> resultMap = new HashMap<String, Object>();
        resultMap = iStorageService.insertStorage(msg, 0, 1);
        if (!(Boolean)resultMap.get("result")){
            System.out.println("storage_queue处理消息失败：" + resultMap.get("msg").toString());
        }else {
            System.out.println("storage_queue处理消息成功");
        }
    }
}
