package com.common.interceptor;

import com.common.annotation.RequestPermission;
import com.common.annotation.SqlServerDao;
import com.common.sys.entity.User;
import com.common.utils.UserUtils;
import com.server.Entity.ResponseBody;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/10/18.
 */
@Aspect
@Component
public class RequestPermissionAop {
    @Pointcut("@annotation(com.common.annotation.RequestPermission)")
    public void compResponseBody() {
    }
    @Around("compResponseBody()")
    public Object aroundExec(ProceedingJoinPoint pjp) throws Throwable {
        ResponseBody o = null;
        try{
            Method method = null;
                try {
                    Signature signature = pjp.getSignature();
                    MethodSignature msig = null;
                    if (!(signature instanceof MethodSignature)) {
                        throw new IllegalArgumentException("该注解只能用于方法");
                    }
                    msig = (MethodSignature) signature;
                    method = pjp.getTarget().getClass().getMethod(msig.getName(), msig.getParameterTypes());
                } catch (NoSuchMethodException e) {

                } catch (SecurityException e) {
                }
            Annotation a=method.getAnnotation(RequestPermission.class);
            RequestPermission b=(RequestPermission)a;
            //System.out.println("------------------------dasdfas =="+b.value()[0]+ "  "+b.logical().toString());
            User user= UserUtils.getUser();
            HashMap qx=user.getPermissionList();
            boolean m=false;
            if(b.logical().toString().equals("AND")){
                m=true;
                for(String i:b.value()){
                    if(qx.get(i)==null) {
                        m=false;
                        break;
                    }
                }
            }
            else{
                m=false;
                for(String i:b.value()){
                    if(qx.get(i)!=null) {
                        m=true;
                        break;
                    }
                }
            }
            //System.out.println(m);
            if(m) o=(ResponseBody)pjp.proceed();
            else {
                o=new ResponseBody();
                o.setIssuccess("0");
                o.setMessage("权限不足");
            }
        }catch(Exception ex){
            ex.printStackTrace();
            o=new ResponseBody();
            o.setIssuccess("0");
            o.setMessage("出错了");
        }
        return o;
    }
}
