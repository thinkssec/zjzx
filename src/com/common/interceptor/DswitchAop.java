package com.common.interceptor;

import com.common.annotation.SqlServerDao;
import com.common.annotation.SqliteDao;
import com.common.config.DataSourceContextHolder;
import com.server.service.DataSourceType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/10/26.
 */
@Aspect
@Component
public class DswitchAop {

    @Pointcut("@annotation(com.common.annotation.SqliteDao)")
    public void switc(){
        //System.out.println("--------------");
    }

    @Around("switc()")
    public Object aroundExec(ProceedingJoinPoint pjp) throws Throwable {
        //System.out.println("------------------------------------------");
        Class c=pjp.getTarget().getClass();
        Annotation a=c.getAnnotation(SqlServerDao.class);
        Annotation b=c.getAnnotation(SqliteDao.class);

        if (a!=null) {
            DataSourceContextHolder. setDbType(DataSourceType. Datasource1);
        }
        if(b!=null){
            DataSourceContextHolder. setDbType(DataSourceType. Datasource3);
        }
        return pjp.proceed();
    }

}
