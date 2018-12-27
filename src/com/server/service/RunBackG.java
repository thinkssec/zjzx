package com.server.service;

import com.common.utils.PropertiesUtil;
import com.common.utils.SpringContextHolder;
import com.common.utils.SqlSessionUtil;
import com.server.mapper.SysControlMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.core.NestedIOException;

import java.io.FileNotFoundException;

/**
 * Created by Administrator on 2017/10/19.
 */
public class RunBackG implements java.lang.Runnable {

    public static Logger log = Logger.getLogger(Runnable.class);
    public static SysControlMapper sysControlMapper;
    private Long beforeTime = 0L; // 上一次刷新时间
    private static boolean refresh = false; // 是否执行刷新
    private static int delaySeconds = 10;// 延迟刷新秒数
    private static int sleepSeconds = 1;// 休眠时间

    private static boolean enabled = false;

    static {
        delaySeconds = PropertiesUtil.getInt("delaySeconds");
        sleepSeconds = PropertiesUtil.getInt("sleepSeconds");
        enabled = "true".equals(PropertiesUtil.getString("enabled"));

        delaySeconds = delaySeconds == 0 ? 50 : delaySeconds;
        sleepSeconds = sleepSeconds == 0 ? 1 : sleepSeconds;
        sysControlMapper= SpringContextHolder.getApplicationContext().getBean(SysControlMapper.class);
        log.debug("[delaySeconds] " + delaySeconds);
        log.debug("[sleepSeconds] " + sleepSeconds);
    }

    public static boolean isRefresh() {
        return refresh;
    }


    @Override
    public void run() {
        beforeTime = System.currentTimeMillis();
        if (enabled) {
            start(this);
        }
    }

    public void start(final RunBackG runnable) {
        new Thread(new java.lang.Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delaySeconds * 1000);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                refresh = true;
                //System.out.println("=========runback=========");
                while (true) {
                    try {
                        runnable.refresh(beforeTime);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    try {
                        Thread.sleep(sleepSeconds * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 执行刷新
     *
     * @param beforeTime 上次刷新时间
     * @throws NestedIOException
     * @throws FileNotFoundException
     */
    public void refresh(Long beforeTime) throws Exception {
        // 本次刷新时间
        Long refrehTime = System.currentTimeMillis();
        try {
            sysControlMapper.deleteRequest(null);
        }catch(Exception e){

        }finally{
        }

        this.beforeTime = refrehTime;
    }


}