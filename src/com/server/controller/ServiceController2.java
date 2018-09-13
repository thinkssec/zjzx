package com.server.controller;

import com.common.annotation.Queuen;
import com.common.annotation.QueuenT;
import com.common.annotation.mapper.JsonMapper;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.service.FileService;
import com.server.service.SysService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>User: sky
 * <p>Date: 14-2-26
 * <p>Version: 1.0
 */
@Controller
@RequestMapping(value = "/t")
public class ServiceController2 {
    @Autowired
    SysService sysService;
    public static Logger log = Logger.getLogger(ServiceController2.class);
    @Autowired
    FileService fileService;
    /*@Autowired
    CacheManage cacheManage;*/
    /*
    接受参数
     */


    @QueuenT
    @RequestMapping("requestf")
    public String requestf(String requestBody, String id, @RequestParam("file") CommonsMultipartFile file) {
        System.out.println("requestf==requestBody="+requestBody);
        ResponseBody res = new ResponseBody();
        RequestBody r = (RequestBody) JsonMapper.fromJsonString(requestBody, RequestBody.class);
        Map<String, String> params = null;
        try {
            params = (Map<String, String>) JsonMapper.fromJsonString(r.getParams(), Map.class);
        }catch(Exception e){
            e.printStackTrace();
        }
        if(params==null)params=new HashMap<String,String>();
        String filepath=fileService.upLoad(r,params, id, file);

        return filepath;
    }


}
