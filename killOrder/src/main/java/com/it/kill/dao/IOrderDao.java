package com.it.kill.dao;

import java.util.ArrayList;
import java.util.Map;

public interface IOrderDao {
    public boolean insertOrder(Map<String, Object> orderInfo);
    public ArrayList<Map<String, Object>> getOrder(String order_id);
    public boolean updateOrderStatus(String order_id);
}
