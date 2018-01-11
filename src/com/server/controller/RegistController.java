package com.server.controller;

import com.common.annotation.Queuen;
import com.common.annotation.mapper.JsonMapper;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.mapper.SysControlMapper;
import com.server.service.RegisterService;
import com.server.service.SysService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * <p>User: sky
 * <p>Date: 14-2-26
 * <p>Version: 1.0
 */
@RestController
@RequestMapping(value = "${anon}")
public class RegistController {
    @Autowired
    RegisterService registerService;
    public static Logger log = Logger.getLogger(RegistController.class);
    /*
    接受参数
     */
    @Queuen
    @RequestMapping("regist")
    public ResponseBody regist(String requestBody,String id){

        ResponseBody p=new ResponseBody();
        try{

        }catch(Exception e){
            e.printStackTrace();
        }
        return p;
    }

}
