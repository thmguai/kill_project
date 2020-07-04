package com.it.kill.dao;

import java.util.Map;

public interface IStorageDao {

    public Map<String,Object> insertStorage(String sku_id, double in_quanty, double out_quanty);
}
