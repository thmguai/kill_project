package com.it.kill.dao;

import java.util.ArrayList;
import java.util.Map;

public interface IStockDao {
    public ArrayList<Map<String,Object>> getStockList();
    public ArrayList<Map<String,Object>> getStock(String sku_id);
    public boolean insertLimitPolicy(Map<String,Object> policyInfo);
}
