package com.it.kill.dao.impl;

import com.it.kill.dao.IOrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Map;

@Repository
public class OrderDaoImpl implements IOrderDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean insertOrder(Map<String, Object> orderInfo) {

        //写入主表
        String sql = "insert into tb_order (order_id, total_fee, actual_fee, post_fee, payment_type, user_id, status, create_time) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?)";
        boolean result = jdbcTemplate.update(sql, orderInfo.get("order_id"), orderInfo.get("total_fee"), orderInfo.get("actual_fee"),
                orderInfo.get("post_fee"), orderInfo.get("payment_type"), orderInfo.get("user_id"), orderInfo.get("status"),
                orderInfo.get("create_time"))==1;

        //写入明细表
        if (result){
            sql = "INSERT INTO tb_order_detail (order_id, sku_id, num, title, own_spec, price, image, create_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            result = jdbcTemplate.update(sql, orderInfo.get("order_id"), orderInfo.get("sku_id"), orderInfo.get("num"),
                    orderInfo.get("title"), orderInfo.get("own_spec"), orderInfo.get("price"), orderInfo.get("image"), orderInfo.get("create_time"))==1;
        }

        return result;
    }

    public ArrayList<Map<String, Object>> getOrder(String order_id){
        String sql = "select d.sku_id, m.order_id, d.price " +
                "from tb_order m inner join tb_order_detail d on m.order_id = d.order_id " +
                "where m.order_id = ?";
        return (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql, order_id);
    }

    public boolean updateOrderStatus(String order_id){
        String sql = "update tb_order set status = 2 where order_id = ?";
        return jdbcTemplate.update(sql, order_id)==1;
    }
}
