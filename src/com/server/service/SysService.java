package com.server.service;

import com.common.annotation.CompResponseBody;
import com.common.annotation.SqliteDao;
import com.common.annotation.mapper.JsonMapper;
import com.common.config.DataSourceContextHolder;
import com.common.sys.entity.User;
import com.common.utils.AppUtils;
import com.common.utils.UserUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.mapper.*;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Administrator on 2017/10/18.
 */
@Service
@Component
public class SysService{
    @Autowired
    UserMapper userMapper;
    @Autowired
    EmailMapper emailMapper;
    @Autowired
    SyncService syncService;
    @Autowired
    SysControlMapper2 sysControlMapper2;
    @Autowired
    SyncMapper syncMapper;
    @Autowired
    PanService panService;
    //同步用户信息
    public ResponseBody authupduser(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","用户信息更新成功",id,rq.getTaskid());

        try{
            JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
            List<HashMap> h = JsonMapper.getInstance().fromJson(rq.getParams(), javaType);
            HashMap p=new HashMap();
            p.put("list",h);
            userMapper.updUserList(p);
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("用户更新失败");
        }
        return rp;
    }
    public ResponseBody authdeluser(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","用户信息删除成功",id,rq.getTaskid());
        try{
            JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
            List<HashMap> h = JsonMapper.getInstance().fromJson(rq.getParams(), javaType);
            HashMap p=new HashMap();
            p.put("list",h);
            userMapper.updUserList(p);
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("用户删除失败");
        }

        return rp;
    }

    //发送文件

    public ResponseBody milSend(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","邮件发送成功",id,rq.getTaskid());
        try{
            doSend(params);
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("邮件发送失败");
        }
        return rp;
    }
    public ResponseBody milGetMlist(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取邮件列表成功",id,rq.getTaskid());
        params.put("USERNAME",rq.getUsername());
        try{
            List<HashMap> el=emailMapper.getMailList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("获取邮件列表失败");
        }
        return rp;
    }

    public ResponseBody milGetDetail(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取邮件内容成功！",id,rq.getTaskid());
        params.put("USERNAME",rq.getUsername());
        try{
            List<HashMap> el=emailMapper.getMailList(params);
            if(el.size()>0)
                rp.setDatas(JsonMapper.toJsonString(el.get(0)));
            else
                rp.setDatas(JsonMapper.toJsonString(null));
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("获取邮件内容失败!");
        }
        return rp;
    }
    public ResponseBody milGetAttach(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取邮件附件信息成功！",id,rq.getTaskid());
        try{
            List<HashMap> el=emailMapper.getMailAList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("获取邮件附件信息失败!");
        }
        return rp;
    }

    @Transactional
    public void doSend(Map params){
        emailMapper.sendMail(params);
        emailMapper.sendAttach(params);
    }

    public ResponseBody authgetuser(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取用户列表成功",id,rq.getTaskid());
        try{

            List<HashMap> ul=userMapper.getUserList(params);
            rp.setDatas(JsonMapper.toJsonString(ul));
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("获取用户列表失败！");
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody sycndata(RequestBody rq, Map params, String id){
        return syncService.sycndata(rq,params,id);
    }

    public ResponseBody pancreateDic(RequestBody rq, Map params, String id){
        return panService.createDirectory(rq,params,id);
    }

    public ResponseBody pancreateFile(RequestBody rq, Map params, String id){
        return panService.createFile(rq,params,id);
    }
    public interface CLibrary extends Library {

        //String filePath = CLibrary.class.getResource("").getPath().replaceFirst("/","").replaceAll("%20"," ")+"dll/x86/SQLite.Interop";
        //CLibrary sdtapi = (CLibrary) Native.loadLibrary(filePath, CLibrary.class);
        CLibrary sdtapi = (CLibrary) Native.loadLibrary("dll/System.Data.SQLite", CLibrary.class);
        //动态链接库中的方法
        int InitComm(int port);
        //动态链接库中的方法
        void fn_StartBegin();
    }
    public ResponseBody testNormal(RequestBody rq, Map params, String id){
        ResponseBody r = new ResponseBody();


        return r;
    }
    public static void main(String[] args) {
        try {
            //System.out.println(System.getProperty("java.library.path"));
            //调用方法
            CLibrary.sdtapi.fn_StartBegin();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Transactional
    public ResponseBody getDatabbList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据升级包列表",id,rq.getTaskid());
        try{
            /*File file = new File(Global.getUserfilesBaseDir() + Global.SOFTWAREFILES_BASE_URL+databbDec);
            List<String> ul=new ArrayList<String>();
            traverseFolder2(ul,Global.getUserfilesBaseDir() + Global.SOFTWAREFILES_BASE_URL+databbDec,databbDec);
            rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(ul));*/
            User user=UserUtils.getUser();
            params.put("DEPTS", AppUtils.getParentDept((String)params.get("DEPTID")));
            try{
                //syncMapper.updsycnt(null);
                //String sycnTime=syncMapper.getCurrentTime();
                List<HashMap> bbls=syncMapper.getDataBb(params);

                HashMap datas=new HashMap();
                datas.put("BBXX",bbls);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));

            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据升级包列表失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("获取数据升级包列表失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    /*@CompResponseBody
    //@Transactional
    public ResponseBody testNormal(RequestBody rq, Map params, String id){
        ResponseBody r = new ResponseBody();
        DataSourceContextHolder. setDbType(DataSourceType. Datasource3);
        try{
            sysControlMapper2.attachDb();
        }catch(Exception e){
            //e.printStackTrace();
        }
        sysControlMapper2.insertTesst();
        List<HashMap> ls=sysControlMapper2.selectFromTest();
        r.setDatas(JsonMapper.toJsonString(ls));

        return r;
    }*/
// region
    /*public ResponseBody upLoad(Map params,String id, CommonsMultipartFile file) {
        //清除上次上传进度信息
        ResponseBody r = new ResponseBody();
        String curProjectPath = "E:\\gitlocal\\zjzx\\web";
        String saveDirectoryPath = curProjectPath + "/" + uploadFolderName;
        File saveDirectory = new File(saveDirectoryPath);
        System.out.println("开始接受 "+new Date());
        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            String fileExtension = FilenameUtils.getExtension(fileName);

            try {
                file.transferTo(new File(saveDirectory, fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("接受完毕  "+new Date());
        return r;
    }*/

    /*
    获取请求
     */
    /*public ResponseBody getRequestList(Map params, String id) {
        ResponseBody r = new ResponseBody();
        List<HashMap> ls = sysControlMapper.getRequestList();
        r.setIssuccess("1");
        r.setDatas(JsonMapper.toJsonString(ls));
        try {
            Thread.sleep(1123);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        r.setMessage("接收成功！");
        return r;
    }

    @CompResponseBody
    @Transactional
    public ResponseBody updateTest(Map params, String id) {
        closeRequest(id);
        //sysControlMapper.insertRequest(d);
        return null;
    }

    @CompResponseBody
    //@Transactional
    public ResponseBody testNormal(Map params, String id){
        ResponseBody r = new ResponseBody();
        DataSourceContextHolder. setDbType(DataSourceType. Datasource3);
        try{
            sysControlMapper2.attachDb();
        }catch(Exception e){
            //e.printStackTrace();
        }
        sysControlMapper2.insertTesst();
        List<HashMap> ls=sysControlMapper2.selectFromTest();
        r.setDatas(JsonMapper.toJsonString(ls));
        return r;
    }



    public void closeRequest(String id){
        DataSourceContextHolder. setDbType(DataSourceType. Datasource1);
        RequestBody d = new RequestBody();
        d.setId(id);
        try{

            sysControlMapper.updateRequest(d);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void doBuz(Map params){
        DataSourceContextHolder. setDbType(DataSourceType. Datasource1);
        //System.out.println("--------------------------------------"+params);
        if("ALL".equals((String)params.get("scope"))){
            List<HashMap> ls = sysControlMapper.getRequestList();
            for(HashMap m :ls){
                try {
                    doCall(m);
                    //Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else{
            String id=(String)params.get("ID");
            try {
                doCall(sysControlMapper.getRequestOne(id));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    @Transactional
    public void doCall(HashMap p) throws Exception {
        if(p==null) return;
        String call=(String)p.get("CALL");
        HashMap map=new HashMap();
        map.put("id",(String)p.get("ID"));
        Method m = null;
        m =AppService.class.getMethod(call, new Class[]{Map.class});
        m.invoke(appService, map);
        sysControlMapper.doBuzByin(map);
    }

    */
// endregion
}
