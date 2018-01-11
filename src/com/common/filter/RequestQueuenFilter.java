package com.common.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2017/10/11.
 */
public class RequestQueuenFilter implements Filter {
    private FilterConfig config;
    private ArrayList dl=new ArrayList();
    private Boolean pd=false;
    public void destroy() {
        config = null;
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        /*// TODO Auto-generated method stub
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        long before = System.currentTimeMillis();
        System.out.println("开始过滤。。。");
        System.out.println("拦截到用户IP地址："+ httpRequest.getRemoteAddr());
        chain.doFilter(request, response);//过滤器不进行消息处理，消息处理继续传递到servlet中进行响应
        long after = System.currentTimeMillis();
        System.out.println("过滤结束！过滤所花时间为："+ (after-before) );
        System.out.println("请求被定为到：" + httpRequest.getRequestURL());*/
//		if("James".equals(request.getAttribute("username") ) && "admin".equals(request.getAttribute("userpassword")))
//		{
//			RequestDispatcher dispatcher = request.getRequestDispatcher("/success.jsp");
//			dispatcher.forward(request, response);
//		}
//		else
//		{
//			RequestDispatcher dispatcher = request.getRequestDispatcher("/error.jsp");
//			dispatcher.forward(request, response);
//		}

        synchronized (pd){
            String requestBody=request.getParameter("requestBody");
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


    }

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
    }
}
