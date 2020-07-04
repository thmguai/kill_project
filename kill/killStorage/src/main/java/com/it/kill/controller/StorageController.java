package com.it.kill.controller;


import com.it.kill.service.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StorageController {

    @Autowired
    private IStorageService iStorageService;

    @RequestMapping("/insertStorage/{sku_id}/{in_quanty}/{out_quanty}")
    public Map<String,Object> insertStorage(@PathVariable("sku_id") String sku_id,
                                            @PathVariable("in_quanty") double in_quanty,
                                            @PathVariable("out_quanty") double out_quanty){
        return iStorageService.insertStorage(sku_id,in_quanty,out_quanty);
    }

}
