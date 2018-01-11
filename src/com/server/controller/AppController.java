package com.server.controller;

import com.common.annotation.Queuen;
import com.common.annotation.mapper.JsonMapper;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.service.SysService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * <p>User: sky
 * <p>Date: 14-2-26
 * <p>Version: 1.0
 */
@RestController
public class AppController {
    //private static ArrayList dl=new ArrayList();
    //private static Boolean pd=false;
    @Autowired
    SysService sysService;
    public static Logger log = Logger.getLogger(AppController.class);

    /*
    接受参数
     */
    @RequestMapping("${adminPath}/doBuz")
    public String request(String requestBody, String id) {
        ResponseBody res = new ResponseBody();
        try {
            RequestBody r = (RequestBody) JsonMapper.fromJsonString(requestBody, RequestBody.class);
            Map<String, String> params = (Map<String, String>) JsonMapper.fromJsonString(r.getParams(), Map.class);
            String call = r.getCall();
            Method m = SysService.class.getMethod(call, new Class[]{Map.class});
            res = (ResponseBody) m.invoke(sysService, params);

        } catch (Exception e) {
            e.printStackTrace();
            //log.error(e.getMessage());
            /*res.setIsSuccess("0");
            res.setMassage("接收失败！");*/
            return JsonMapper.toJsonString(res);
        }
        //res.setIsSuccess("1");
        //res.setMassage("接收成功！");
        return JsonMapper.toJsonString(res);
    }
    /*public String hello1(String requestBody) {
        //System.out.println("接受的参数："+requestBody);
        //System.out.println("lock");
        synchronized (pd){
            dl.add("请求时间："+  new Date()+requestBody);
            if(pd){
                System.out.println("当前队列位置："+dl.size());
            }
        }
        synchronized (dl){
            pd=true;
            System.out.println("开始运行："+new Date());
            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            pd=false;
            dl.remove(0);
            System.out.println("运行完毕:"+new Date());
        }
        HashMap m=new HashMap();
        m.put("message","验证成功");
        m.put("username","admin");
        HashMap r=new HashMap();
        r.put("reponseBody", m);
        return JsonMapper.toJsonString(r);
    }*/
}
