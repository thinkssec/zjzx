package com.common.interceptor;

import com.common.annotation.mapper.JsonMapper;
import com.common.config.DataSourceContextHolder;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.mapper.SysControlMapper;
import com.server.service.DataSourceType;
import com.server.service.SysService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

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
        Object o = null;
        Object[] d = pjp.getArgs();
        Object[] e = null;
        String id = "";
        boolean f = false;
        RequestBody r = null;
        String filePath="";
        //synchronized (pd) {
        dl.add("请求时间：" + new Date());
        id = UUID.randomUUID().toString();
        if (d.length == 0) return o;
        else {
            String args = (String) d[0];
            if (d.length == 3) e = new Object[3];
            else e = new Object[2];
            e[0] = d[0];
            e[1] = id;
            if (d.length == 3) e[2] = d[2];
            r = (RequestBody) JsonMapper.fromJsonString(args, RequestBody.class);
            r.setId(id);
        }
        synchronized (nm) {
            if ("f".equals(r.getType())) {

            } else {
                DataSourceContextHolder.setDbType(DataSourceType.Datasource1);
                sysControlMapper.insertRequest(r);
            }
        }
        //sysControlMapper.insertRequest(r);
        if ("f".equals(r.getType())) {
            try {
                if (e != null && (e.length == 2 || e.length == 3)) {
                    synchronized (pd) {
                        DataSourceContextHolder.setDbType(DataSourceType.Datasource1);
                        sysControlMapper.insertRequest(r);
                        filePath=(String)pjp.proceed(e);
                        r.setAttach(filePath);
                        closeRequest2(r, id);
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } else {
            synchronized (nm) {
                closeRequest2(r, id);
            }
        }
        //}

        synchronized (dl) {
            closeRequest3(r, id);
            if ("f".equals(r.getType())) {
                Map<String, String> params = (Map<String, String>) JsonMapper.fromJsonString(r.getParams(), Map.class);
                String call = r.getCall();
                Method m = SysService.class.getMethod(call, new Class[]{RequestBody.class,Map.class, String.class});
                o = m.invoke(sysService, r,params, id);
                //System.out.println("------114"+o);
                closeHandMsg(r, id);
            } else {
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
            pd = true;
            pd = false;
            dl.remove(0);
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
            sysControlMapper.updateRequest3(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
