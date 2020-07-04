package com.it.kill.dao.impl;

import com.it.kill.dao.IStorageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class StorageDaoImpl implements IStorageDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> insertStorage(String sku_id, double in_quanty, double out_quanty) {
        String sql = "SELECT id FROM tb_stock_storage WHERE sku_id = ?";
        ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql,sku_id);
        int new_id = 0;
        Map<String,Object> resultMap = new HashMap<String, Object>();
        boolean result = false;
        double thisQuanty = in_quanty - out_quanty;
        if (list != null && list.size() > 0){
            new_id = Integer.parseInt(list.get(0).get("id").toString());
        }else {
            //如果没有，写入主表，并且获取id
            KeyHolder keyHolder = new GeneratedKeyHolder();
            sql = "insert into tb_stock_storage(warehouse_id, sku_id, quanty) values (1," + sku_id + "," + thisQuanty + ")";
            final String finalSql = sql;
            result = jdbcTemplate.update(new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement statement = connection.prepareStatement(finalSql, Statement.RETURN_GENERATED_KEYS);
                    return statement;
                }
            },keyHolder) == 1;
            if (!result){
                resultMap.put("result",false);
                resultMap.put("msg","写入库存主表失败");
                return resultMap;
            }
            new_id = keyHolder.getKey().intValue();
        }
        //写入历史表
        sql = "insert into tb_stock_storage_history(stock_storage_id, in_quanty, out_quanty) values (?,?,?)";
        result = jdbcTemplate.update(sql,new_id,in_quanty,out_quanty) == 1;
        if (!result){
            resultMap.put("result",false);
            resultMap.put("msg","写入库存历史表失败");
            return resultMap;
        }
        //更新主表
        if (list != null && list.size() > 0){
            sql = "update tb_stock_storage set quanty = quanty + " + thisQuanty + "where id = " + new_id;
            result = jdbcTemplate.update(sql) == 1;
            if (!result){
                resultMap.put("result",false);
                resultMap.put("msg","更新库存主表失败");
                return resultMap;
            }
        }
        resultMap.put("result",true);
        resultMap.put("msg","写入库存成功");
        return resultMap;
    }
}
