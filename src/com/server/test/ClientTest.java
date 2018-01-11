package com.server.test;

import com.common.annotation.mapper.JsonMapper;
import com.common.config.Constants;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * <p>User: sky
 * <p>Date: 14-2-26
 * <p>Version: 1.0
 */
public class ClientTest {
   // private static Server server;
    private RestTemplate restTemplate = new RestTemplate();


    @BeforeClass
    public static void beforeClass() throws Exception {
        /*//创建一个server
        server = new Server(8080);
        WebAppContext context = new WebAppContext();
        String webapp = "shiro-example-chapter20/src/main/webapp";
        context.setDescriptor(webapp + "/WEB-INF/web.xml");  //指定web.xml配置文件
        context.setResourceBase(webapp);  //指定webapp目录
        context.setContextPath("/");
        context.setParentLoaderPriority(true);

        server.setHandler(context);
        server.start();*/
    }

    @Test
    public void testServiceHelloSuccess() {
        String username = "admin";
        String param11 = "参数11";
        String param12 = "参数12";
        String param2 = "参数2";
        String key = "admin";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add(Constants.PARAM_USERNAME, username);
        params.add("param1", param11);
        params.add("param1", param12);
        params.add("param2", param2);

        try {
            //params.add(Constants.PARAM_PASSWORD, URLEncoder.encode( new String(Des.encrypt(key.getBytes("UTF-8"),"111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"),"UTF-8")));
            params.add(Constants.PARAM_PASSWORD, "111");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(key);
        //System.out.println(params.get(Constants.PARAM_PASSWORD));
        MultiValueMap<String, String> m=new LinkedMultiValueMap<String, String>();
        m.add("requestBody", JsonMapper.toJsonString(params));
        String url = UriComponentsBuilder
                .fromHttpUrl("http://localhost:8080/zjzx/a/hello")
                .queryParams(m).build().toUriString();
        System.out.println("请求地址："+"http://localhost:8080/zjzx/a/hello");
        System.out.println("请求参数："+m);
        ResponseEntity responseEntity = restTemplate.getForEntity(url, String.class,JsonMapper.toJsonString(params));
        //Assert.assertEquals("hello" + param11 + param12 + param2, responseEntity.getBody());
        System.out.println("响应信息："+responseEntity.getBody());
    }

    @Test
    public void testServiceHelloFail() {
        /*String username = "admin";
        String param11 = "param11";
        String param12 = "param12";
        String param2 = "param2";
        String key = "dadadswdewq2ewdwqdwadsadasd";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add(Constants.PARAM_USERNAME, username);
        params.add("param1", param11);
        params.add("param1", param12);
        params.add("param2", param2);
        params.add(Constants.PARAM_DIGEST, HmacSHA256Utils.digest(key, params));
        params.set("param2", param2 + "1");

        String url = UriComponentsBuilder
                .fromHttpUrl("http://localhost:8080/hello")
                .queryParams(params).build().toUriString();

        try {
            ResponseEntity responseEntity = restTemplate.getForEntity(url, String.class);
        } catch (HttpClientErrorException e) {
            Assert.assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
            Assert.assertEquals("login error", e.getResponseBodyAsString());
        }*/
    }

    @AfterClass
    public static void afterClass() throws Exception {
        //server.stop(); //当测试结束时停止服务器
    }
}
