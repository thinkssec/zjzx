package com.common.interceptor;

import com.server.Entity.ResponseBody;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/10/18.
 */
@Aspect
@Component
public class ResponseAop {
    @Pointcut("@annotation(com.common.annotation.CompResponseBody)")
    public void compResponseBody() {
    }
    @Around("compResponseBody()")
    public Object aroundExec(ProceedingJoinPoint pjp) throws Throwable {
        ResponseBody o = null;
        try{
            o = (ResponseBody)pjp.proceed();
            if(o==null) o=new ResponseBody();
            o.setIssuccess("1");
            o.setMessage("处理成功！");
        }catch(Exception ex){
            ex.printStackTrace();
            o=new ResponseBody();
            o.setIssuccess("0");
            o.setMessage("处理失败！");
        }
        return o;
    }
}
