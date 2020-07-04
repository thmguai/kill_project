package com.it.kill.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.it.kill.dao.IStockDao;
import com.it.kill.service.IStockService;
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
import java.util.concurrent.TimeUnit;

@Service
public class StockServiceImpl implements IStockService{



    @Autowired
    private IStockDao iStockDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    public Map<String, Object> getStockList() {

        Map<String,Object> resultMap = new HashMap<String, Object>();
        ArrayList<Map<String, Object>> list = iStockDao.getStockList();
        if (list == null || list.size() == 0){
            resultMap.put("result",false);
            resultMap.put("msg","您没有取出商品信息");
            return resultMap;
        }
        //从redis取政策
        resultMap = getLimitPolicy(list);
        resultMap.put("sku_list",list);
        return resultMap;

    }

    public Map<String, Object> getStock(String sku_id) {
        Map<String,Object> resultMap = new HashMap<String, Object>();
        if (sku_id == null || sku_id.equals("")){
            resultMap.put("result",false);
            resultMap.put("msg","您传入的参数有误");
            return resultMap;
        }
        ArrayList<Map<String, Object>> list = iStockDao.getStock(sku_id);
        if (list == null || list.size() == 0){
            resultMap.put("result",false);
            resultMap.put("msg","您没有取出商品信息");
            return resultMap;
        }
        //从redis取政策
        resultMap = getLimitPolicy(list);
        resultMap.put("sku",list);
        return resultMap;

    }

    public Map<String, Object> insertLimitPolicy(Map<String, Object> policyInfo) {
        Map<String,Object> resultMap = new HashMap<String, Object>();
        if (policyInfo == null || policyInfo.isEmpty()){
            resultMap.put("result",false);
            resultMap.put("msg","您传入的参数有误");
            return resultMap;
        }
        boolean result = iStockDao.insertLimitPolicy(policyInfo);
        if(!result){
            resultMap.put("result",false);
            resultMap.put("msg","数据库写入政策失败");
            return resultMap;
        }
        long diff = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = restTemplate.getForObject("http://kill-time-server/getTime",String.class);
        try {
            Date end_time = format.parse(policyInfo.get("end_time").toString());
            Date now_time = format.parse(now);
            diff = end_time.getTime() - now_time.getTime();
            if (diff <= 0){
                resultMap.put("result",false);
                resultMap.put("msg","结束时间不能小于当前时间");
                return resultMap;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String policy = JSON.toJSONString(policyInfo);
        //政策存入redis
        redisTemplate.opsForValue().set("LIMIT_POLICY" + policyInfo.get("sku_id").toString(),policy,diff,TimeUnit.SECONDS);
        //商品存入redis
        ArrayList<Map<String, Object>> list = iStockDao.getStock(policyInfo.get("sku_id").toString());
        String sku = JSON.toJSONString(list.get(0));
        redisTemplate.opsForValue().set("SKU_" + policyInfo.get("sku_id").toString(),sku,diff,TimeUnit.SECONDS);
        resultMap.put("result",true);
        resultMap.put("msg","政策写入完毕");
        return resultMap;

    }
    private Map<String,Object> getLimitPolicy(ArrayList<Map<String,Object>> list){
        Map<String,Object> resultMap = new HashMap<String, Object>();
        for (Map<String, Object> skuMap : list) {
            String policy = redisTemplate.opsForValue().get("LIMIT_POLICY" + skuMap.get("sku_id").toString());
            if (policy != null && !policy.equals("")){
                Map<String,Object> policyInfo = JSONObject.parseObject(policy, Map.class);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String now = restTemplate.getForObject("http://kill-time-server/getTime", String.class);

                try {
                    Date begin_time = format.parse(policyInfo.get("begin_time").toString());
                    Date end_time = format.parse(policyInfo.get("end_time").toString());
                    Date now_time = format.parse(now);
                    if (begin_time.getTime() <= now_time.getTime() && now_time.getTime() <= end_time.getTime()){
                        skuMap.put("limitPrice",policyInfo.get("price"));
                        skuMap.put("limitQuanty",policyInfo.get("quanty"));
                        skuMap.put("limitBeginTime",policyInfo.get("begin_time"));
                        skuMap.put("limitEndTime",policyInfo.get("end_time"));
                        skuMap.put("nowTime",now);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }
        resultMap.put("result",true);
        resultMap.put("msg","");
        return resultMap;
    }
}
