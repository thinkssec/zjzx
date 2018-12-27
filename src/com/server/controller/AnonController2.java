package com.server.controller;

import com.common.annotation.Queuen;
import com.common.annotation.mapper.JsonMapper;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.service.AnonService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * <p>User: sky
 * <p>Date: 14-2-26
 * <p>Version: 1.0
 */
@RestController
@RequestMapping(value = "/cn")
public class AnonController2 {
    @Autowired
    AnonService anonService;
    public static Logger log = Logger.getLogger(AnonController2.class);
    @Queuen
    @RequestMapping("request")
    public String request(String requestBody, String id) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ResponseBody res = new ResponseBody();
        //System.out.println("requestBody="+requestBody);
        //System.out.println("requestBody=============================================="+requestBody);
        RequestBody r = (RequestBody) JsonMapper.fromJsonString(requestBody, RequestBody.class);
        Map<String, String> params = (Map<String, String>) JsonMapper.fromJsonString(r.getParams(), Map.class);
        String call = r.getCall();
        Method m = AnonService.class.getMethod(call, new Class[]{RequestBody.class,Map.class, String.class});
        res = (ResponseBody) m.invoke(anonService, r,params, id);
        return JsonMapper.toJsonString(res);
    }

}
