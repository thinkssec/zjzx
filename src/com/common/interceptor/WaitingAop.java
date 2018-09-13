package com.common.interceptor;

import com.common.annotation.mapper.JsonMapper;
import com.common.config.DataSourceContextHolder;
import com.common.utils.Encodes;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.mapper.SysControlMapper;
import com.server.service.DataSourceType;
import com.server.service.SysService;
import org.apache.commons.lang.StringEscapeUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.resource.EncodedResource;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Administrator on 2017/10/11.
 */
@Aspect
@Component
public class WaitingAop {
    private ArrayList dl = new ArrayList();
    private Boolean pd = false;
    private Boolean nm = false;
    @Autowired
    SysControlMapper sysControlMapper;
    @Autowired
    SysService sysService;

    @Pointcut("@annotation(com.common.annotation.Queuen)")
    public void queuen() {
    }

    @Pointcut("@annotation(com.common.annotation.QueuenT)")
    public void queuent() {
    }

    @Pointcut("@annotation(com.common.annotation.QueuenF)")
    public void queuenf() {
    }


    /*@Around("queuen()")
    public Object aroundExec(ProceedingJoinPoint pjp) throws Throwable {
        Object o = null;
        o = pjp.proceed();
        return o;
    }*/
    @Around("queuen()")
    public Object aroundExec(ProceedingJoinPoint pjp) throws Throwable {
        //System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        Object o = null;
        Object[] d = pjp.getArgs();
        Object[] e = null;
        String id = "";
        boolean f = false;
        RequestBody r = null;
        String filePath = "";
        //synchronized (pd) {
        dl.add("请求时间：" + new Date());
        id = UUID.randomUUID().toString();
        if (d.length == 0) return o;
        else {
            //urldecode spring 会自动处理，不用人为urldecode
            BASE64Decoder decoder = new BASE64Decoder();
            //System.out.println(d[0]);
            //System.out.println(Encodes.urlDecode("%7b%22call%22%3a%22getXtSjInfo%22%2c%22params%22%3a%22%7b%5c%22SJC%5c%22%3a%5c%2220170101%c2%a021%3a00%3a00%5c%22%7d%22%7d"));
            //d[0]= Encodes.urlDecode((String)d[0]);
            //d[0]=StringEscapeUtils.unescapeHtml((String)d[0]);
            d[0] = new String(decoder.decodeBuffer((String) d[0]), "UTF-8");
            d[0] = StringEscapeUtils.unescapeHtml((String) d[0]);
            //System.out.println(d[0]);
            String args = (String) d[0];
            if (d.length == 3) e = new Object[3];
            else e = new Object[2];
            e[0] = d[0];
            e[1] = id;
            if (d.length == 3) e[2] = d[2];
            System.out.println("WaitingAop--请求参数：" + args);
            r = (RequestBody) JsonMapper.fromJsonString(args, RequestBody.class);
            if (r == null) {
                ResponseBody oo = new ResponseBody();
                oo.setIssuccess("0");
                oo.setMessage("请求字符串存在格式错误！");
                String t = JsonMapper.toJsonString(oo);
                return Encodes.encodeToBase64((String) t);
            }
            r.setId(id);
        }
        synchronized (nm) {
            if ("f".equals(r.getType())) {
            } else {
                DataSourceContextHolder.setDbType(DataSourceType.Datasource1);
                try {
                    sysControlMapper.insertRequest(r);
                } catch (Exception eeeee) {
                    eeeee.printStackTrace();
                }
            }
        }
        //sysControlMapper.insertRequest(r);
        if ("f".equals(r.getType())) {
            //System.out.println("ffffffffffffffffffff");
            try {
                if (e != null && (e.length == 2 || e.length == 3)) {
                    synchronized (pd) {
                        DataSourceContextHolder.setDbType(DataSourceType.Datasource1);
                        sysControlMapper.insertRequest(r);
                        filePath = (String) pjp.proceed(e);
                        r.setAttach(filePath);
                        closeRequest2(r, id);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            synchronized (nm) {
                closeRequest2(r, id);
            }
        }
        //}
        //System.out.println("******************106"+e.length);
        synchronized (dl) {
            closeRequest3(r, id);
            if ("f".equals(r.getType())) {
                Map<String, String> params = (Map<String, String>) JsonMapper.fromJsonString(r.getParams(), Map.class);
                String call = r.getCall();
                Method m = SysService.class.getMethod(call, new Class[]{RequestBody.class, Map.class, String.class});
                o = m.invoke(sysService, r, params, id);
                if (o instanceof ResponseBody) {
                    String oo = "";
                    oo = JsonMapper.toJsonString(o);
                    o = oo;
                }
                //System.out.println("------114"+o);
                closeHandMsg(r, id);
            } else {
                //System.out.println("******************"+e.length);
                try {
                    if (e != null && (e.length == 2 || e.length == 3)) {
                        o = pjp.proceed(e);
                        //System.out.println("------120"+o);
                        closeHandMsg(r, id);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            //pd = true;
            pd = false;
            dl.remove(0);
        }
        if (o instanceof String)
            o = Encodes.encodeToBase64((String) o);
        return o;
    }

    @Around("queuent()")
    public Object aroundExect(ProceedingJoinPoint pjp) throws Throwable {
        Object o = null;
        Object[] d = pjp.getArgs();
        Object[] e = null;
        String id = "";
        boolean f = false;
        RequestBody r = null;
        String filePath = "";
        //synchronized (pd) {
        dl.add("请求时间：" + new Date());
        id = UUID.randomUUID().toString();
        if (d.length == 0) return o;
        else {
            //urldecode spring 会自动处理，不用人为urldecode
            BASE64Decoder decoder = new BASE64Decoder();
            //System.out.println(d[0]);
            //System.out.println(Encodes.urlDecode("%7b%22call%22%3a%22getXtSjInfo%22%2c%22params%22%3a%22%7b%5c%22SJC%5c%22%3a%5c%2220170101%c2%a021%3a00%3a00%5c%22%7d%22%7d"));
            //d[0]= Encodes.urlDecode((String)d[0]);
            //d[0]=StringEscapeUtils.unescapeHtml((String)d[0]);
            d[0] = new String(decoder.decodeBuffer((String) d[0]), "UTF-8");
            d[0] = StringEscapeUtils.unescapeHtml((String) d[0]);
            String args = (String) d[0];
            if (d.length == 3) e = new Object[3];
            else e = new Object[2];
            e[0] = d[0];
            e[1] = id;
            if (d.length == 3) e[2] = d[2];
            r = (RequestBody) JsonMapper.fromJsonString(args, RequestBody.class);
            if (r == null) {
                ResponseBody oo = new ResponseBody();
                oo.setIssuccess("0");
                oo.setMessage("请求字符串存在格式错误！");
                String t = JsonMapper.toJsonString(oo);
                return Encodes.encodeToBase64((String) t);
            }
            r.setId(id);
        }
        synchronized (nm) {
            if ("f".equals(r.getType())) {

            } else {
                DataSourceContextHolder.setDbType(DataSourceType.Datasource1);
                //System.out.println("(((((((((((((((((82");
                try {
                    sysControlMapper.insertRequest(r);
                } catch (Exception eeeee) {
                    eeeee.printStackTrace();
                }
            }
        }
        //sysControlMapper.insertRequest(r);
        if ("f".equals(r.getType())) {
            try {
                if (e != null && (e.length == 2 || e.length == 3)) {
                    synchronized (pd) {
                        DataSourceContextHolder.setDbType(DataSourceType.Datasource1);
                        sysControlMapper.insertRequest(r);
                        filePath = (String) pjp.proceed(e);
                        r.setAttach(filePath);
                        closeRequest2(r, id);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            synchronized (nm) {
                closeRequest2(r, id);
            }
        }
        //}
        //System.out.println("******************106"+e.length);
        synchronized (dl) {
            closeRequest3(r, id);
            if ("f".equals(r.getType())) {
                Map<String, String> params = (Map<String, String>) JsonMapper.fromJsonString(r.getParams(), Map.class);
                String call = r.getCall();
                Method m = SysService.class.getMethod(call, new Class[]{RequestBody.class, Map.class, String.class});
                o = m.invoke(sysService, r, params, id);
                if (o instanceof ResponseBody) {
                    String oo = "";
                    oo = JsonMapper.toJsonString(o);
                    o = oo;
                }
                //System.out.println("------114"+o);
                closeHandMsg(r, id);
            } else {
                //System.out.println("******************"+e.length);
                try {
                    if (e != null && (e.length == 2 || e.length == 3)) {
                        o = pjp.proceed(e);
                        //System.out.println("------120"+o);
                        closeHandMsg(r, id);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            //pd = true;
            pd = false;
            dl.remove(0);
        }
        if (o instanceof String)
            o = Encodes.encodeToBase64((String) o);
        HttpServletResponse resp = ((ServletWebRequest) RequestContextHolder.getRequestAttributes()).getResponse();
        resp.setContentType("text/html;charset=utf-8");
        resp.setHeader("pragma", "no-cache");
        resp.setHeader("cache-control", "no-cache");
        try {
            resp.getWriter().write(o.toString());
        } catch (IOException eeee) {
            eeee.printStackTrace();
        }
        return o;
    }

    public void closeHandMsg(RequestBody r, String id) {
        r.setId(id);
        DataSourceContextHolder.setDbType(DataSourceType.Datasource1);
        try {
            sysControlMapper.updateHdlMsg(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Around("queuenf()")
    public Object aroundExecf(ProceedingJoinPoint pjp) throws Throwable {
        //System.out.println("48：" + new Date());
        Object o = null;
        Object[] d = pjp.getArgs();
        Object[] e = null;
        String id = "";
        RequestBody r = null;
        synchronized (pd) {
            dl.add("请求时间：" + new Date());
            id = UUID.randomUUID().toString();
            if (d.length == 0) return o;
            else {
                BASE64Decoder decoder = new BASE64Decoder();
                d[0] = new String(decoder.decodeBuffer((String) d[0]), "UTF-8");
                String args = (String) d[0];
                if (d.length == 3) e = new Object[3];
                else e = new Object[2];
                e[0] = d[0];
                e[1] = id;
                if (d.length == 3) e[2] = d[2];
                r = (RequestBody) JsonMapper.fromJsonString(args, RequestBody.class);
                r.setId(id);
            }
            if (pd) {
                System.out.println("当前队列位置：" + dl.size());
            }
            DataSourceContextHolder.setDbType(DataSourceType.Datasource1);
            sysControlMapper.insertRequest(r);
            //System.out.println("74：" + new Date());
        }
        synchronized (dl) {
            //DataSourceContextHolder. setDbType(DataSourceType. Datasource1);
            //sysControlMapper.insertRequest(r);
            pd = true;
            //System.out.println("开始运行：" + new Date());
            try {
                /*Object[] d=pjp.getArgs();
                for(int i=0;i<d.length;i++){
                    String args=(String)d[i];
                    RequestBody r = (RequestBody) JsonMapper.fromJsonString(args, RequestBody.class);
                    sysControlMapper.insertRequest(r);
                }*/
                if (e != null && (e.length == 2 || e.length == 3)) {
                    o = pjp.proceed(e);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            pd = false;
            dl.remove(0);
            //System.out.println("运行完毕:" + new Date());
        }
        if (o instanceof String)
            o = Encodes.encodeToBase64((String) o);
        return o;
    }

    public void closeRequest(RequestBody r, String id) {
        DataSourceContextHolder.setDbType(DataSourceType.Datasource1);
        r.setId(id);
        try {
            sysControlMapper.updateRequest(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeRequest2(RequestBody r, String id) {
        DataSourceContextHolder.setDbType(DataSourceType.Datasource1);
        r.setId(id);
        try {
            sysControlMapper.updateRequest2(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeRequest3(RequestBody r, String id) {
        DataSourceContextHolder.setDbType(DataSourceType.Datasource1);
        r.setId(id);
        try {
            //r.setParams(r.getParams().getBytes());
            sysControlMapper.updateRequest3(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
