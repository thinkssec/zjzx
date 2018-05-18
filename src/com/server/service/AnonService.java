package com.server.service;

import com.common.config.Global;
import com.common.mapper.JsonMapper;
import com.common.utils.AppUtils;
import com.common.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.Entity.Condition;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.mapper.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/7.
 */
@Service
@Component
public class AnonService {
    @Autowired
    AnonMapper anonMapper;
    @Autowired
    SyncMapper syncMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    JqsqMapper jqsqMapper;
    @Autowired
    FrameMapper frameMapper;
    private static String upgradeDec="upgrade";
    private static String databbDec="databb";
    private static String upgradeFilePath="C:\\\\upload\\\\softwarefiles\\\\";
    public ResponseBody testConnect(RequestBody rq, Map params, String id){
        ResponseBody r=new ResponseBody(params,"1","连接成功",id,rq.getTaskid());
        return r;
    }

    public  ResponseBody isRegist(RequestBody rq, Map params, String id){
        ResponseBody r=new ResponseBody(params,"0","该机器未曾注册！",id,rq.getTaskid());
        try {
            //HashMap m=anonMapper.selectRegistInf(params);
            HashMap m=jqsqMapper.isRegist(params);
            if(m!=null) {
                r.setIssuccess("1");
                r.setMessage("该机器已被注册过！目前处于"+("1".equals((String)m.get("STAUTS"))?"停用状态":"启用状态"));
                r.setDatas(JsonMapper.toJsonString(m));
                return r;
            }/*else{
                m=jqsqMapper.isRegist2(params);
                if(m!=null){
                    r.setIssuccess("0");
                    r.setMessage("该机器已被停用或者注销！");
                    r.setDatas(JsonMapper.toJsonString(m));
                    return r;
                }
            }*/
        }catch(Exception e){
            e.printStackTrace();
            r.setIssuccess("0");
            r.setMessage("注册验证失败！");
        }
        return r;
    }

    public ResponseBody regist(RequestBody rq, Map params, String id){
        ResponseBody r=new ResponseBody(params,"1","注册申请提交成功！",id,rq.getTaskid());
        try {
            //HashMap m=anonMapper.selectRegistInf(params);
            jqsqMapper.regist(params);
            HashMap m=jqsqMapper.isRegist(params);
            if(m!=null){
                r.setMessage("该机器以前注册过！目前处于"+("1".equals((String)m.get("STAUTS"))?"停用状态":"启用状态"));
                r.setDatas(JsonMapper.toJsonString(m));
            }
        }catch(Exception e){
            e.printStackTrace();
            r.setIssuccess("0");
            r.setMessage("注册申请提交失败！");
        }
        return r;
    }

    public ResponseBody getDeptList(RequestBody rq, Map params, String id){
        ResponseBody r=new ResponseBody(params,"1","获取单位列表成功！",id,rq.getTaskid());

        List<HashMap> hm=userMapper.getDeptList(params);
        r.setDatas(JsonMapper.toJsonString(hm));
        return r;
    }

    public ResponseBody login(RequestBody rq, Map params, String id){
        ResponseBody r=new ResponseBody(params,"1","登录成功",id,rq.getTaskid());
        try {
            HashMap m=userMapper.getUserOne(rq.getUsername());
            if(m==null) {
                r.setIssuccess("01");
                r.setMessage("该用户不存在！");
                return r;
            }else{
                if(StringUtils.isNotBlank(rq.getPassword())){
                    if(rq.getPassword().equals((String)m.get("MM"))){
                        r.setIssuccess("1");
                        r.setMessage("登录成功！");
                        r.setDatas(JsonMapper.toJsonString(m));
                    }else{
                        r.setIssuccess("02");
                        r.setMessage("密码不正确！");
                    }
                }else{
                    r.setIssuccess("02");
                    r.setMessage("密码不正确！");
                }
                if(userMapper.validRegstUser(rq.getUsername())==null){
                    r.setIssuccess("03");
                    r.setMessage("登录机器未注册！");
                }
                return r;
            }
        }catch(Exception e){
            e.printStackTrace();
            r.setIssuccess("03");
            r.setMessage("登录失败，请联系管理员！");
        }
        return r;
    }



    public ResponseBody authgetuser(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取用户列表成功",id,rq.getTaskid());
        try{

            List<HashMap> ul=userMapper.getUserList(params);
            rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(ul));
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("获取用户列表失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody AuthRealm(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取用户成功",id,rq.getTaskid());
        try{
            //System.out.println();
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            //System.out.println("------"+condition);
            HashMap m=userMapper.getUserOne(condition.getLoginName());
            rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(m));
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("获取用户失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody getMenuList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            //Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                Map mmmm=frameMapper.getPermission("menuList");
                Blob bbbb=(Blob)mmmm.get("VALUE");
                InputStream inStream = bbbb.getBinaryStream();
/*                byte[] data = new byte[1024];
                inStream.read(data);
                inStream.close();*/
                //String jdata= new String(new String(data));;
                String jdata = IOUtils.toString(inStream);
                inStream.close();
                //System.out.println(jdata);
                rp.setDatas(jdata);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody getUpgradeList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取升级包列表",id,rq.getTaskid());
        try{
            File file = new File(Global.getUserfilesBaseDir() + Global.SOFTWAREFILES_BASE_URL+upgradeDec);
            List<String> ul=new ArrayList<String>();
            traverseFolder2(ul,Global.getUserfilesBaseDir() + Global.SOFTWAREFILES_BASE_URL+upgradeDec,upgradeDec);
            rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(ul));
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("获取升级包列表失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody modifyPsw(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","修改密码成功",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            userMapper.modifyPsw(condition);
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("修改密码失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody modifyUser(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","修改成功",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            userMapper.modifyUser(condition);
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("修改失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public void traverseFolder2(List<String> l,String path,String doc) {

        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
                //System.out.println("文件夹是空的!");
                return;
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        //System.out.println("文件夹:" + file2.getAbsolutePath());
                        //l.add(file2.getAbsolutePath());
                        traverseFolder2(l,file2.getAbsolutePath(),doc);
                    } else {
                        System.out.println("文件:" + file2.getAbsolutePath());
                        l.add(file2.getPath().replaceAll(upgradeFilePath+doc,""));
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
    }
}
