package com.common.filter;

import com.common.annotation.mapper.JsonMapper;
import com.common.config.Constants;
import com.common.realm.StatelessToken;
import com.common.utils.Encodes;
import com.fasterxml.jackson.databind.JavaType;
import com.server.Entity.RequestBody;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import sun.misc.BASE64Decoder;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * <p>User: sky
 * <p>Date: 14-2-26
 * <p>Version: 1.0
 */
public class StatelessAuthcFilter extends FormAuthenticationFilter /*AccessControlFilter*/ {

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) /*throws Exception */{

        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        //1、客户端生成的消息摘要
        //String clientDigest = request.getParameter(Constants.PARAM_PASSWORD);
        //2、客户端传入的用户身份
        //String username = request.getParameter(Constants.PARAM_USERNAME);
        String requestBody=request.getParameter("requestBody");
        //System.out.println("------------------------------------------------"+((HttpServletRequest)request).getRequestURI());
        //System.out.println("------------------------------------------------"+request.getContentType());
        //System.out.println("------------------------------------------------"+request.getParameterMap());
        //System.out.println("------------------------------------------------"+request.getCharacterEncoding());
        //System.out.println("------------------------------------------------"+request.getInputStream());
        BASE64Decoder decoder = new BASE64Decoder();
        requestBody= new String(decoder.decodeBuffer(requestBody), "UTF-8");
        //System.out.println("------------------------------------------------"+requestBody);
        //System.out.println("(((((("+requestBody);
       /* JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, double[].class);
        exclude = JsonMapper.getInstance().fromJson(condition.getC8(), javaType);*/
        RequestBody ppp=(RequestBody)JsonMapper.fromJsonString(requestBody,RequestBody.class);
        //System.out.println("----------filter"+requestBody);
        //System.out.println("----------filter"+ppp.getUsername());
        //System.out.println("----------filter"+ppp.getPassword());
        //System.out.println("----------filter"+ppp.getParams());
        //System.out.println("----------filter"+ppp);
        //String clientDigest = (String)((ArrayList)ppp.get(Constants.PARAM_PASSWORD)).get(0);
        //String clientDigest = ppp.getPassword();略去密码验证
        String clientDigest = ppp.getPassword();
        //System.out.println("ccccc"+clientDigest);
        //String username = (String)((ArrayList)ppp.get(Constants.PARAM_USERNAME)).get(0);
        String username =ppp.getUsername();
        //3、客户端请求的参数列表
        Map<String, String[]> params = new HashMap<String, String[]>(request.getParameterMap());
        params.remove(Constants.PARAM_PASSWORD);

        //System.out.println("----------filter"+request.getParameterMap());

        //4、生成无状态Token
        StatelessToken token = new StatelessToken(username, params, clientDigest);

        try {
            //5、委托给Realm进行登录
            getSubject(request, response).login(token);
        } catch (Exception e) {
            e.printStackTrace();
            onLoginFail(response); //6、登录失败
            return false;
        }
        return true;
    }
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject,
                                     ServletRequest request, ServletResponse response) throws Exception {
        System.out.println("登录成功");
        return false;
    }

    //登录失败时默认返回401状态码
    private void onLoginFail(ServletResponse response) throws IOException {
        System.out.println("登录失败");
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.getWriter().write("login error");
    }
}
