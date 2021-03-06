package com.server.controller;

import com.common.annotation.Queuen;
import com.common.annotation.mapper.JsonMapper;
import com.common.sys.entity.User;
import com.common.utils.CacheManage;
import com.common.utils.Encodes;
import com.common.utils.UserUtils;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.mapper.SysControlMapper;
import com.server.service.FileService;
import com.server.service.SysService;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import sun.misc.BASE64Decoder;

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
@RestController
@RequestMapping(value = "${adminPath}")
public class ServiceController {
    @Autowired
    SysService sysService;
    @Autowired
    SysControlMapper sysControlMapper;
    public static Logger log = Logger.getLogger(ServiceController.class);
    @Autowired
    FileService fileService;

    /*@Autowired
    CacheManage cacheManage;*/
    /*
    接受参数
     */
    @RequestMapping("request")
    public String request(String requestBody, String id) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ResponseBody res = new ResponseBody();
        //System.out.println("requestBody "+requestBody);
        RequestBody r = (RequestBody) JsonMapper.fromJsonString(requestBody, RequestBody.class);
        Map<String, String> params = null;
        try {
            params = (Map<String, String>) JsonMapper.fromJsonString(r.getParams(), Map.class);
        } catch (Exception e) {
            params = new HashMap<String, String>();
            //e.printStackTrace();
        }
        String call = r.getCall();
        //System.out.println("1111111111"+cacheManage.get("testCache")+"1111111111");
        //User user= UserUtils.getUser();
        //user.setId("你好！@#￥%……&*（）");
        //cacheManage.set("testCache",user);
        //System.out.println("2222222222"+(User)cacheManage.get("testCache")+"2222222222");
        /*HashMap qx=user.getPermissionList();
        if(qx!=null){
            if(qx.get(call)==null){
                res.setIssuccess("0");
                res.setMessage("权限不足，请联系管理员！");
                return JsonMapper.toJsonString(res);
            }
        }else{
            res.setIssuccess("0");
            res.setMessage("权限不足，请联系管理员！");
            return JsonMapper.toJsonString(res);
        }*/
        Method m = SysService.class.getMethod(call, new Class[]{RequestBody.class, Map.class, String.class});
        res = (ResponseBody) m.invoke(sysService, r, params, id);
        return JsonMapper.toJsonString(res);
    }

    @Queuen
    @RequestMapping("requestf")
    public String requestf(String requestBody, String id, @RequestParam("file") CommonsMultipartFile file) {
        //System.out.println("requestf==requestBody="+requestBody);
        ResponseBody res = new ResponseBody();
        RequestBody r = (RequestBody) JsonMapper.fromJsonString(requestBody, RequestBody.class);
        Map<String, String> params = null;
        try {
            params = (Map<String, String>) JsonMapper.fromJsonString(r.getParams(), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (params == null) params = new HashMap<String, String>();
        String filepath = fileService.upLoad(r, params, id, file);
        return filepath;
    }

    @Queuen
    @RequestMapping("requestb")
    public String requestb(String requestBody, String id) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ResponseBody res = new ResponseBody();
        //System.out.println("requestBody=============================================="+requestBody);
        RequestBody r = (RequestBody) JsonMapper.fromJsonString(requestBody, RequestBody.class);
        Map<String, String> params = null;
        try {
            params = (Map<String, String>) JsonMapper.fromJsonString(r.getParams(), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (params == null) params = new HashMap<String, String>();
        String call = r.getCall();
        Method m = SysService.class.getMethod(call, new Class[]{RequestBody.class, Map.class, String.class});
        res = (ResponseBody) m.invoke(sysService, r, params, id);
        return JsonMapper.toJsonString(res);
    }

    @RequestMapping("requestc")
    public String requestc(String requestBody, String id) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ResponseBody res = new ResponseBody();
        //System.out.println("requestBody=============================================="+requestBody);
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            requestBody = new String(decoder.decodeBuffer((String) requestBody), "UTF-8");
            requestBody = StringEscapeUtils.unescapeHtml((String) requestBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
        RequestBody r = (RequestBody) JsonMapper.fromJsonString(requestBody, RequestBody.class);
        sysControlMapper.insertRequest(r);
        try {
            Map<String, String> params = null;
            try {
                params = (Map<String, String>) JsonMapper.fromJsonString(r.getParams(), Map.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (params == null) params = new HashMap<String, String>();
            String call = r.getCall();
            Method m = SysService.class.getMethod(call, new Class[]{RequestBody.class, Map.class, String.class});
            res = (ResponseBody) m.invoke(sysService, r, params, id);
        }catch(Exception e){
            e.printStackTrace();
        }
        String rtn="";
        try{
            rtn = JsonMapper.toJsonString(res);
            rtn = Encodes.encodeToBase64((String) rtn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sysControlMapper.updateRequest2(r);
        return rtn;
    }
// region
    /*public String hello1(String requestBody) {
        //System.out.println("接受的参数："+requestBody);
        //System.out.println("lock");
        Boolean pd = false;
        List dl = new ArrayList();
        synchronized (pd) {
            dl.add("请求时间：" + new Date() + requestBody);
            if (pd) {
                System.out.println("当前队列位置：" + dl.size());
            }
        }
        synchronized (dl) {
            pd = true;
            System.out.println("开始运行：" + new Date());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pd = false;
            dl.remove(0);
            System.out.println("运行完毕:" + new Date());
        }
        HashMap m = new HashMap();
        m.put("message", "验证成功");
        m.put("username", "admin");
        HashMap r = new HashMap();
        r.put("reponseBody", m);
        return JsonMapper.toJsonString(r);
    }*/
    // endregion
}
