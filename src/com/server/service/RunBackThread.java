package com.server.service;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by Administrator on 2017/10/19.
 */
public class RunBackThread implements ServletContextListener {
    private RunBackG thread;
    public RunBackThread() {

    }

    public void  contextInitialized(ServletContextEvent arg0) {
        if (thread == null) {
            thread = new RunBackG();
            thread.run();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
