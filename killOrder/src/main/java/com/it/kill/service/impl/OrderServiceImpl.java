package com.it.kill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.it.kill.dao.IOrderDao;
import com.it.kill.service.IOrderService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private IOrderDao iOrderDao;

    public Map<String, Object> createOrder(String sku_id, String user_id) {
        Map<String,Object> resultMap = new HashMap<String, Object>();
        // 1 判断参数
        if (sku_id == null || sku_id.equals("")){
            resultMap.put("result",false);
            resultMap.put("msg","前端参数输入有误");
            return resultMap;
        }
        //2 取redis政策
        String order_id = String.valueOf(System.currentTimeMillis());
        String policy = redisTemplate.opsForValue().get("LIMIT_POLICY" + sku_id);
        if (!(policy == null) && !policy.equals("")){
            Map<String,Object> policyInfo = JSONObject.parseObject(policy,Map.class);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = restTemplate.getForObject("http://kill-time-server/getTime", String.class);
            try {
                Date now_time = format.parse(now);
                Date begin_time = format.parse(policyInfo.get("begin_time").toString());
                Date end_time = format.parse(policyInfo.get("end_time").toString());
                if (begin_time.getTime()<=now_time.getTime()&&now_time.getTime()<=end_time.getTime()){
                    long limitQuanty = Long.parseLong(policyInfo.get("quanty").toString());
                    if (redisTemplate.opsForValue().increment("SKU_QUANTY_" + sku_id,1) <= limitQuanty){
                        //写入队列
                        String sku = redisTemplate.opsForValue().get("SKU_" + sku_id);
                        Map<String,Object> skuMap = JSONObject.parseObject(sku, Map.class);

                        Map<String, Object> orderInfo = new HashMap<String, Object>();
                        orderInfo.put("order_id", order_id);
                        orderInfo.put("total_fee", skuMap.get("price"));
                        orderInfo.put("actual_fee", policyInfo.get("price"));
                        orderInfo.put("post_fee", 0);
                        orderInfo.put("payment_type", 0);
                        orderInfo.put("user_id", user_id);
                        orderInfo.put("status", 1);
                        orderInfo.put("create_time", now);
                        orderInfo.put("sku_id", skuMap.get("sku_id"));
                        orderInfo.put("num", 1);
                        orderInfo.put("title", skuMap.get("title"));
                        orderInfo.put("own_spec", skuMap.get("own_spec"));
                        orderInfo.put("price", policyInfo.get("price"));
                        orderInfo.put("image", skuMap.get("images"));
                        try {
                            String order = JSON.toJSONString(orderInfo);
                            amqpTemplate.convertAndSend("order_queue",order);
                            redisTemplate.opsForValue().set("ORDER_" + order_id, order);
                        }catch (Exception e){
                            resultMap.put("result",false);
                            resultMap.put("msg","队列写入失败");
                            return resultMap;
                        }
                    }else {
                        resultMap.put("result",false);
                        resultMap.put("msg","被踢回");
                        return resultMap;
                    }
                }else {
                    resultMap.put("result",false);
                    resultMap.put("msg","政策不存在或政策已过期");
                    return resultMap;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else {
            resultMap.put("result",false);
            resultMap.put("msg","政策不存在或政策已过期");
            return resultMap;
        }
        resultMap.put("order_id",order_id);
        resultMap.put("result",true);
        resultMap.put("msg","");
        return resultMap;
    }

    public Map<String, Object> insertOrder(Map<String, Object> orderInfo) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (orderInfo==null||orderInfo.isEmpty()){
            map.put("result", false);
            map.put("msg", "传入参数有误！");
            return map;
        }

        boolean result = iOrderDao.insertOrder(orderInfo);

        if (!result){
            map.put("result", false);
            map.put("msg", "订单写入失败！");
            return map;
        }

        map.put("result", true);
        map.put("msg", "");
        return map;
    }

    public Map<String, Object> getOrder(String order_id) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (order_id==null||order_id.equals("")){
            resultMap.put("result", false);
            resultMap.put("msg", "参数传入有误！");
            return resultMap;
        }
        ArrayList<Map<String, Object>> list = iOrderDao.getOrder(order_id);
        resultMap.put("order", list);
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;
    }

    public Map<String, Object> payOrder(String order_id, String sku_id){
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (order_id==null||order_id.equals("")){
            resultMap.put("result", false);
            resultMap.put("msg", "订单有误！");
            return resultMap;
        }

        boolean result = iOrderDao.updateOrderStatus(order_id);

        if (!result){
            resultMap.put("result", false);
            resultMap.put("msg", "订单状态更新失败！");
            return resultMap;
        }

        amqpTemplate.convertAndSend("storage_queue", sku_id);

        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;
    }
}
