package com.server.service;

import com.common.annotation.RequestPermission;
import com.common.annotation.mapper.JsonMapper;
import com.common.config.Global;
import com.common.realm.StatelessRealm;
import com.common.sys.entity.Office;
import com.common.sys.entity.Role;
import com.common.sys.entity.User;
import com.common.utils.*;
import com.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.server.Entity.Condition;
import com.server.Entity.Menu;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.mapper.*;
import com.sun.jna.Library;
import com.sun.jna.Native;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
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
    @Autowired
    JqsqMapper jqsqMapper;
    @Autowired
    BbglMapper bbglMapper;
    @Autowired
    PermisionMapper permisionMapper;
    @Autowired
    FrameMapper frameMapper;
    @Autowired
    BczbMapper bczbMapper;
    String bczbXfPath=Global.YSBCZBXF_BASE_URL;
    String bczbSjPath="/bczbSj";
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
    public ResponseBody modifyUser(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","修改成功",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            condition.setC21(rq.getAttach());
            userMapper.modifyUser(condition);
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("修改失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    //发送文件
    public ResponseBody milSend(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","邮件发送成功",id,rq.getTaskid());
        try{
            params.put("PATH",rq.getAttach());
            emailMapper.sendMail(params);
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("邮件发送失败");
        }
        return rp;
    }
    public ResponseBody milReply(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","邮件发送成功",id,rq.getTaskid());
        try{
            params.put("PATH",rq.getAttach());
            emailMapper.replyMail(params);
        }catch(Exception e){
            e.printStackTrace();
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
    public ResponseBody milDelete(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","邮件删除成功！",id,rq.getTaskid());
        try{
            emailMapper.delete(params);
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("邮件删除失败!");
        }
        return rp;
    }
    public ResponseBody milRead(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","邮件已读标记成功！",id,rq.getTaskid());
        try{
            emailMapper.readed(params);
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("邮件已读标记失败");
        }
        return rp;
    }
    public ResponseBody panMCreate(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            //UserUtils.getPrincipal2();
            params.put("USERNAME",rq.getUsername());
            frameMapper.panMCreate(params);
            HashMap u=userMapper.getUserOne(rq.getUsername());
            params.put("USERID",u.get("USERID"));
            List<HashMap> el=frameMapper.getPanMList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }
    public ResponseBody panMUpdate(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            frameMapper.panMUpdate(params);
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }
    public ResponseBody panMDel(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            frameMapper.panMDelete(params);
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }
    public ResponseBody panMList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取列表成功",id,rq.getTaskid());
        HashMap u=userMapper.getUserOne(rq.getUsername());
        params.put("USERID",u.get("USERID"));
        try{
            List<HashMap> el=frameMapper.getPanMList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("获取列表失败");
        }
        return rp;
    }
    public ResponseBody panMUpload(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            params.put("PATH",rq.getAttach());
            frameMapper.panMUpload(params);
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }
    public ResponseBody panMXmList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取列表成功",id,rq.getTaskid());
        try{
            HashMap u=userMapper.getUserOne(rq.getUsername());
            params.put("USERID",u.get("USERID"));
            List<HashMap> el=frameMapper.getPanMXmList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("获取列表失败");
        }
        return rp;
    }
    public ResponseBody panMXmDel(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            frameMapper.panMXmDelete(params);
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }
    public ResponseBody panDHasEditP(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            //UserUtils.getPrincipal2();
            User u=UserUtils.getUser();
            if(!u.hasPermission("dw:dept:edit")){
                rp.setIssuccess("0");
                rp.setMessage("权限不足");
            }
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }
    public ResponseBody panDCreate(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            //UserUtils.getPrincipal2();
            User u=UserUtils.getUser();
            frameMapper.panDCreate(params);
            params.put("RT",u.getTopDeptid());
            List<HashMap> el=frameMapper.getPanDList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }
    public ResponseBody panDUpdate(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            frameMapper.panDCreate(params);
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }
    public ResponseBody panDDel(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            frameMapper.panDDelete(params);
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }
    public ResponseBody panDList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取列表成功",id,rq.getTaskid());
        try{
            User u=UserUtils.getUser();
            params.put("RT",u.getTopDeptid());
            List<HashMap> el=frameMapper.getPanDList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("获取列表失败");
        }
        return rp;
    }
    public ResponseBody panDUpload(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            params.put("PATH",rq.getAttach());
            frameMapper.panDUpload(params);
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }
    public ResponseBody panDXmList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取列表成功",id,rq.getTaskid());
        try{
            User u=UserUtils.getUser();
            params.put("DEPTID",u.getDeptId());
            params.put("RT",u.getTopDeptid());
            List<HashMap> el=frameMapper.getPanDXmList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("获取列表失败");
        }
        return rp;
    }
    public ResponseBody panDXmDel(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            frameMapper.panMXmDelete(params);
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
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
    //过时
    public ResponseBody sycndata(RequestBody rq, Map params, String id){
        return syncService.sycndata(rq,params,id);
    }
    public ResponseBody pancreateDic(RequestBody rq, Map params, String id){
        return panService.createDirectory(rq,params,id);
    }
    public ResponseBody pancreateFile(RequestBody rq, Map params, String id){
        return panService.createFile(rq,params,id);
    }
    public ResponseBody getJqsq(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> bbls=jqsqMapper.getJqsq(condition);
                condition.setStart(null);
                HashMap datas=new HashMap();
                datas.put("rows",bbls);
                datas.put("total",jqsqMapper.getJqsq(condition).size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
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

    public ResponseBody getRysq(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> bbls=jqsqMapper.getRysq(condition);
                condition.setStart(null);
                HashMap datas=new HashMap();
                datas.put("rows",bbls);
                datas.put("total",jqsqMapper.getRysq(condition).size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
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
    public ResponseBody jqsqwpzList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> bbls=jqsqMapper.getJqsqwpz(condition);
                condition.setStart(null);
                HashMap datas=new HashMap();
                datas.put("rows",bbls);
                datas.put("total",jqsqMapper.getJqsqwpz(condition).size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
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
    public ResponseBody rysqwpzList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> bbls=jqsqMapper.getRysqwpz(condition);
                condition.setStart(null);
                HashMap datas=new HashMap();
                datas.put("rows",bbls);
                datas.put("total",jqsqMapper.getRysqwpz(condition).size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
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
    public ResponseBody jqsqpzList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            //System.out.println(rq.getParams());
            try{
                List<HashMap> bbls=jqsqMapper.getJqsqpz(condition);
                condition.setStart(null);
                HashMap datas=new HashMap();
                datas.put("rows",bbls);
                datas.put("total",jqsqMapper.getJqsqpz(condition).size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
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
    public ResponseBody jqsqty(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                jqsqMapper.saveJqsqty(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody rysqty(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                jqsqMapper.saveRysqty(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody savejqsqpz(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                //String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                String updL = condition.getC6();
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                jqsqMapper.saveJqsqpz(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody jqsqbty(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                jqsqMapper.saveJqsqbty(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody rysqbty(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                jqsqMapper.saveRysqbty(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody jqsqdel(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                jqsqMapper.saveJqsqdel(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody rysqdel(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                jqsqMapper.saveRysqdel(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody getDw(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> lsHt = jqsqMapper.getDw(condition);
                String jdata= JsonMapper.getInstance().toJson(lsHt);
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
    public ResponseBody getDw2(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> lsHt = jqsqMapper.getDw2(condition);
                String jdata= JsonMapper.getInstance().toJson(lsHt);
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
    public ResponseBody getDw3(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                System.out.println("-------------"+condition.getDwdm());
                List<HashMap> lsHt = jqsqMapper.getDw3(condition);
                String jdata= JsonMapper.getInstance().toJson(lsHt);
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
    public ResponseBody getOfficeById(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            try{
                Office lsHt = frameMapper.getOfficeById(office);
                List<Menu> ls=frameMapper.getOfficeMenuById(office);
                lsHt.setMenuList(ls);
                String jdata= JsonMapper.getInstance().toJson(lsHt);
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
    public ResponseBody getMypanById(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            try{
                Office lsHt = frameMapper.getMypanById(office);
                String jdata= JsonMapper.getInstance().toJson(lsHt);
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
    public ResponseBody getYq(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> lsHt = jqsqMapper.getYq(condition);
                String jdata= JsonMapper.getInstance().toJson(lsHt);
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
    public ResponseBody deptLists(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> lsHt = permisionMapper.getDeptList(condition);
                HashMap r=new HashMap();
                condition.setStart(null);
                r.put("total",permisionMapper.getDeptList(condition).size());
                r.put("rows",lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
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
    //获得用户组
    public ResponseBody officeLists(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            try{
                List<Office> lsHt = frameMapper.getOfficeList(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
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
    //获得角色
    public ResponseBody officeRLists(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            try{
                List<Office> lsHt = frameMapper.getOfficeRList(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
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
    //获得单位组列表
@RequestPermission(value={"dw:dept:edit"})
    public ResponseBody officeDLists(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            try{
                //System.out.println("dwdm------------------------"+office.getDwdm());
                List<Office> lsHt = frameMapper.getOfficeDList(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
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
    //获得单位目录
    public ResponseBody officeMLists(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            try{
                List<Office> lsHt = frameMapper.getOfficeMList(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
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
    //获得个人目录
    public ResponseBody officeMyLists(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            office.setId(UserUtils.getUser().getId());
            try{
                List<Office> lsHt = frameMapper.getOfficeMyList(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
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
    //个人空间项目管理
    public ResponseBody xmglLists(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            office.setId(UserUtils.getUser().getId());
            try{
                List<Office> lsHt = frameMapper.xmglLists(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
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
    //单位空间项目管理
    public ResponseBody xmglListsD(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            User user=UserUtils.getUser();
            ObjectMapper objectMapper = new ObjectMapper();
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            office.setId(user.getTopDeptid());
            office.setDwdm(user.getDeptId());
            try{
                List<Office> lsHt = frameMapper.xmglListsD(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
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
    //获取单位补充指标目录
    public ResponseBody bczbMlLists(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa=new HashMap();
            pa.put("CODE", user.getDeptCode().substring(0,4));
            try{
                HashMap<String,Office> lsHt = frameMapper.bczbMlLists(pa);
                HashMap<String, List<Office>> children = new HashMap();
                String rootId="";
                System.out.println("========"+lsHt);
                for (String key : lsHt.keySet()) {
                    if(lsHt.get(key).getCode().length()==8) rootId=key;
                    List c = children.get(lsHt.get(key).getParentId());
                    if (c == null) {
                        c = new ArrayList<Office>();
                        children.put((String) lsHt.get(key).getParentId(), c);
                    }
                    c.add(lsHt.get(key));
                }
                if(StringUtils.isBlank(rootId)){

                    frameMapper.insertBczbRoot(user.getDeptCode(),user.getDeptId());
                    lsHt = frameMapper.bczbMlLists(pa);
                    children = new HashMap();
                    for (String key : lsHt.keySet()) {
                        if(lsHt.get(key).getCode().length()==8) rootId=key;
                        List c = children.get(lsHt.get(key).getParentId());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                }
                System.out.println("-------------"+children);
                HashMap m=toTree(lsHt, children, rootId);
                List mm=new ArrayList();
                mm.add(m);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(mm));
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
    public HashMap toTree(HashMap<String, Office> dic, HashMap<String, List<Office>> children, String rootId) {
        HashMap h = new HashMap();
        Office m = dic.get(rootId);
        h.put("id", m.getId());
        h.put("code", m.getCode());
        h.put("text", m.getName());
        List c = null;
        c=new ArrayList();
        h.put("children", c);
        List<Office> cd = children.get(rootId);
        if (cd != null) {
            for (Office i : cd) {
                c.add(toTree(dic, children, (String)i.getId()));
            }
        } else {
        }

        return h;
    }
    //获取能够添加下级目录的目录
    public ResponseBody officeMLists2(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            try{
                List<Office> lsHt = frameMapper.getOfficeMList2(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
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
    public ResponseBody officeMmyLists2(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            try{
                List<Office> lsHt = frameMapper.getOfficeMmyList2(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
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
    public ResponseBody userLists(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> lsHt = permisionMapper.getUserList(condition);
                HashMap r=new HashMap();
                condition.setStart(null);
                r.put("total",permisionMapper.getUserList(condition).size());
                r.put("rows",lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
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
    //获取单位管理员的用户列表
    public ResponseBody userListsD(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                System.out.println(condition.getUserid()+"----------dwdm---"+condition.getDwdm());
                List<HashMap> lsHt = permisionMapper.getUserListD(condition);
                HashMap r=new HashMap();
                condition.setStart(null);
                r.put("total",permisionMapper.getUserListD(condition).size());
                r.put("rows",lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
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
    public ResponseBody userLists2(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> lsHt = permisionMapper.getUserList2(condition);
                HashMap r=new HashMap();
                condition.setStart(null);
                System.out.println("=================="+condition.getId());
                r.put("total",permisionMapper.getUserList2(condition).size());
                r.put("rows",lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
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
    public ResponseBody saveDept(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                //String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                String updL = condition.getC6();
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                permisionMapper.saveDeptList(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody deleteDept(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                permisionMapper.deteleDept(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody saveUserList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                //String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                String updL = condition.getC6();
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                System.out.println("updL-------------"+updL);
                HashMap m=new HashMap();
                m.put("list",h);
                permisionMapper.saveUserList(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody saveUserListD(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                //String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                String updL = condition.getC6();
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                System.out.println("updL-------------"+updL);
                HashMap m=new HashMap();
                m.put("list",h);
                permisionMapper.saveUserListD(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody deleteUserList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                permisionMapper.delUserlist(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody qyUserList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                permisionMapper.qyUserlist(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody tyUserList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                permisionMapper.tyUserlist(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody assignUser(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                m.put("id",condition.getId());
                permisionMapper.assignUser(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody assignUserGly(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                m.put("id",condition.getId());
                permisionMapper.assignUserGly(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody jqsqtingy(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                jqsqMapper.saveJqsqtingy(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody jqsqzhux(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                jqsqMapper.saveJqsqzhux(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody jqsqqiy(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                jqsqMapper.saveJqsqqiy(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody xtbbList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> lsHt = bbglMapper.getBbxtlist(condition);
                HashMap r=new HashMap();
                condition.setStart(null);
                r.put("total",bbglMapper.getBbxtlist(condition).size());
                r.put("rows",lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
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
    public ResponseBody bbxtsave(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                m.put("submitter",UserUtils.getUser().getId());
                bbglMapper.saveBbxt(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody bbxtxf(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                bbglMapper.xfBbxt(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody bbsjxf(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                bbglMapper.xfBbsj(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody xtbbdel(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                bbglMapper.delBbxt(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody sjbbdel(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                bbglMapper.delBbsj(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody sjbbList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> lsHt = bbglMapper.getBbsjlist(condition);
                HashMap r=new HashMap();
                condition.setStart(null);
                r.put("total",bbglMapper.getBbsjlist(condition).size());
                r.put("rows",lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
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
    public ResponseBody bbsjsave(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m=new HashMap();
                m.put("list",h);
                bbglMapper.saveBbsj(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody getSjtype(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> zblist=bbglMapper.getSjtypeList(null);
                String zbstr=JsonMapper.getInstance().toJson(zblist);
                rp.setDatas(zbstr);
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
    public ResponseBody getXtSjInfo(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取系统更新信息成功",id,rq.getTaskid());
        try{
            try{
                List<HashMap> infos=bbglMapper.getXtSjInfo(params);
                for(HashMap info :infos){
                    String path= Global.getUserfilesBaseDir()
                            + info.get("SUBMITTER") +"/"+info.get("ID")+ "/";
                    String doc=Global.SOFTWAREFILES;
                    List<String> ul=new ArrayList<String>();
                    traverseFolder2(ul,path,doc);
                    info.put("filelist",ul);
                }
                String zbstr=JsonMapper.getInstance().toJson(infos);
                rp.setDatas(zbstr);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取系统更新信息成功失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("获取系统更新信息成功失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody getMenuList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            try{
                //System.out.println("username   1111===="+UserUtils.getUser().getLoginName());
                List<Menu> mmmm=null;
                ObjectMapper objectMapper = new ObjectMapper();
                Menu menu=new Menu();
                try {
                    menu = objectMapper.readValue(rq.getParams(), Menu.class);
                }catch(Exception e){
                    //e.printStackTrace();

                }
                /*if("test".equals(UserUtils.getUser().getLoginName()))
                      mmmm=frameMapper.findAllList(new Menu());
                else{
                    Menu m = new Menu();
                    m.setUserId(UserUtils.getUser().getId());
                    mmmm = frameMapper.findByUserId(m);
                }*/
                mmmm=frameMapper.findAllList(menu);
                String jdata = JsonMapper.getInstance().toJson(mmmm);
                //System.out.println("==========="+mmmm.size());
                //System.out.println("==========="+jdata);
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
    public ResponseBody getMenuById(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                Menu mmmm=frameMapper.findMenuById(condition.getId());
                String jdata = JsonMapper.getInstance().toJson(mmmm);
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
    public ResponseBody deleteMenu(RequestBody rq, Map params, String id){
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Menu menu=objectMapper.readValue(rq.getParams(), Menu.class);
            //System.out.println("--------------------------menu="+menu);
            try{
                frameMapper.deleteMenu(menu);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody updateMenuSort(RequestBody rq, Map params, String id){
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, Menu.class);
            List<Menu> h = JsonMapper.getInstance().fromJson(rq.getParams(), javaType);
            //System.out.println("--------------------------menu="+menu);
            HashMap hhh=new HashMap();
            try{
                hhh.put("list",h);
                frameMapper.updateMenuSort(hhh);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody saveMenu(RequestBody rq, Map params, String id){
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Menu menu=objectMapper.readValue(rq.getParams(), Menu.class);
            //System.out.println("--------------------------menu="+menu);
            try{
                Menu pm=frameMapper.findMenuById(menu.getParentId());
                System.out.println("------"+menu.getParentId());
                System.out.println("------"+pm);
                menu.setParent(pm);
                // 获取修改前的parentIds，用于更新子节点的parentIds
                String oldParentIds = menu.getParentIds();
                // 设置新的父节点串
                menu.setParentIds(menu.getParent().getParentIds()+menu.getParent().getId()+",");
                // 保存或更新实体
                if (StringUtils.isBlank(menu.getId())){
                    menu.preInsert();
                    frameMapper.insertMenu(menu);
                }else{
                    menu.preUpdate();
                    frameMapper.updateMenu(menu);
                }
                // 更新子节点 parentIds
                Menu m = new Menu();
                m.setParentIds("%,"+menu.getId()+",%");
                List<Menu> list = frameMapper.findByParentIdsLike(m);
                for (Menu e : list){
                    e.setParentIds(e.getParentIds().replace(oldParentIds, menu.getParentIds()));
                    frameMapper.updateParentIds(e);
                }
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("保存失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody findAllRole(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            try{
                List<Role> mmmm=null;
                mmmm=frameMapper.findAllRole(new Role());
                String jdata = JsonMapper.getInstance().toJson(mmmm);
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
    public ResponseBody getRoleById(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                //System.out.println("condition.getId()  "+condition.getId());
                Role mmmm=frameMapper.findRoleById(condition.getId());
                //System.out.println("mmmm  "+mmmm);
                String jdata = JsonMapper.getInstance().toJson(mmmm);
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
    public ResponseBody getRoleByName(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                Role mmmm=frameMapper.getRoleByName(condition.getId());
                String jdata = JsonMapper.getInstance().toJson(mmmm);
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
    public ResponseBody saveRole(RequestBody rq, Map params, String id){
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap m=objectMapper.readValue(rq.getParams(), HashMap.class);
            String rstr=(String)m.get("role");
            String mstr=(String)m.get("menuList");
            Role role=objectMapper.readValue(rstr, Role.class);

            JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, Menu.class);
            List<Menu> menuList = JsonMapper.getInstance().fromJson(mstr, javaType);
            role.setMenuList(menuList);
            //System.out.println("--------------------------menu="+menu);
            try{
                if (StringUtils.isBlank(role.getId())){
                    role.preInsert();
                    frameMapper.insertRole(role);
                }else{
                    role.preUpdate();
                    frameMapper.updateRole(role);
                }
                // 更新子节点 parentIds
                frameMapper.deleteRoleMenu(role);
                if (role.getMenuList().size() > 0){
                    frameMapper.insertRoleMenu(role);
                }
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("保存失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody saveOfficeRs(RequestBody rq, Map params, String id){
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap m=objectMapper.readValue(rq.getParams(), HashMap.class);
            String rstr=(String)m.get("office");
            String mstr=(String)m.get("menuList");
            Office role=objectMapper.readValue(rstr, Office.class);

            JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, Menu.class);
            List<Menu> menuList = JsonMapper.getInstance().fromJson(mstr, javaType);
            role.setMenuList(menuList);
            //System.out.println("--------------------------menu="+menu);
            try{
                frameMapper.deleteOfficeMenu(role);
                if (role.getMenuList().size() > 0){
                    frameMapper.insertOfficeMenu(role);
                }
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("保存失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody saveOffice(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            frameMapper.saveOffice(office);
            //System.out.println("--------------------------menu="+menu);
            try{

            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("保存失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody saveOfficer(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            //System.out.println("(((((((((((((((((((((((((((((("+rq.getParams());
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            frameMapper.saveOfficer(office);
            //System.out.println("--------------------------menu="+menu);
            try{

            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("保存失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    //保存目录
    public ResponseBody saveOfficem(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            //System.out.println("(((((((((((((((((((((((((((((("+rq.getParams());
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            frameMapper.saveOfficem(office);
            //System.out.println("--------------------------menu="+menu);
            try{

            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("保存失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody saveMypan(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            //System.out.println("(((((((((((((((((((((((((((((("+rq.getParams());
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            frameMapper.saveMypan(office);
            //System.out.println("--------------------------menu="+menu);
            try{

            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("保存失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody deleteRole(RequestBody rq, Map params, String id){
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Role role=objectMapper.readValue(rq.getParams(), Role.class);
            //System.out.println("--------------------------menu="+menu);
            try{
                frameMapper.deleteRole(role);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody findUserByRoleId(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            User user=objectMapper.readValue(rq.getParams(), User.class);
            try{
                List<User> mmmm=null;
                mmmm=frameMapper.findUserByRoleId(user);
                String jdata = JsonMapper.getInstance().toJson(mmmm);
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
    public ResponseBody findUserByOfficeId(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office office=objectMapper.readValue(rq.getParams(), Office.class);
            try{
                List<User> mmmm=null;
                mmmm=frameMapper.findUserByOfficeId(office);
                String jdata = JsonMapper.getInstance().toJson(mmmm);
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
    public ResponseBody getAllOffice(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            try{
                List<Office> mmmm=null;
                mmmm=frameMapper.getAllOffice();
                String jdata = JsonMapper.getInstance().toJson(mmmm);
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
    public ResponseBody deleteOffice(RequestBody rq, Map params, String id){
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office role=objectMapper.readValue(rq.getParams(), Office.class);
            //System.out.println("--------------------------menu="+menu);
            try{
                frameMapper.deleteOffice(role);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody deleteMypan(RequestBody rq, Map params, String id){
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office role=objectMapper.readValue(rq.getParams(), Office.class);
            //System.out.println("--------------------------menu="+menu);
            try{
                frameMapper.deleteMypan(role);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody deleteXm(RequestBody rq, Map params, String id){
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Office role=objectMapper.readValue(rq.getParams(), Office.class);
            //System.out.println("--------------------------menu="+menu);
            try{
                frameMapper.deleteXm(role);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody outrole(RequestBody rq, Map params, String id){
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            User user=objectMapper.readValue(rq.getParams(), User.class);
            System.out.println("--------------------------userid="+user.getId()+" role="+user.getRole().getId());
            try{
                frameMapper.outrole(user);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody outroleGly(RequestBody rq, Map params, String id){
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            User user=objectMapper.readValue(rq.getParams(), User.class);
            //System.out.println("--------------------------userid="+user.getId()+" role="+user.getRole().getId());
            try{
                frameMapper.outroleGly(user);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //补充指标
    public ResponseBody getBczbList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取原始补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa=new HashMap();
            pa.put("DEPTID", user.getDeptCode().substring(0,4));
            System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getYsBczb(pa);
                //转换后的指标Map
                HashMap<String,HashMap> objects=new HashMap();
                HashMap<String,String> plist=new HashMap();
                for(HashMap m:bbls){
                    plist.put((String)m.get("PID"),"project");
                    HashMap zb=(HashMap)objects.get((String)m.get("OID"));
                    if(zb==null){
                        zb=new HashMap();
                        objects.put((String)m.get("OID"),zb);
                    }
                    zb.put((String)m.get("OKEY"),(String)m.get("OVALUE"));
                    zb.put("PID",(String)m.get("PID"));
                    zb.put("OID",(String)m.get("OID"));
                    zb.put("RKSJ",(String)m.get("RKSJ"));
                }
                //获取project 范围
                String scope="''";
                for(String k:plist.keySet()){
                    scope+=",'"+k+"'";
                }
                List<HashMap> lsP=bczbMapper.getProjectByScope(scope);

                //转换后的project
                HashMap<String,HashMap> projects=new HashMap();
                for(HashMap m:lsP){
                    HashMap pro=(HashMap)projects.get((String)m.get("ID"));
                    if(pro==null){
                        pro=new HashMap();
                        projects.put((String)m.get("ID"),pro);
                    }
                    pro.put((String)m.get("OKEY"),(String)m.get("OVALUE"));
                    pro.put("PRO_ID",(String)m.get("ID"));
                    pro.put("PRO_DXGC",(String)m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for(String k :objects.keySet()){
                    HashMap o=objects.get(k);
                    o.putAll(projects.get(o.get("PID")));
                }
                List<HashMap> mdList=new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String,String> orderby=new LinkedHashMap<>();
                if(StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                }else{
                    orderby.put("RKSJ","desc");
                }
                AppUtils.sort(mdList,orderby);
                HashMap datas = new HashMap();
                int pageNo = Integer.parseInt(condition.getStart());   //从1开始
                int pageSize = Integer.parseInt(condition.getLimit());
                int fromIndex = pageSize * (pageNo - 1);
                int toIndex = pageSize * pageNo;
                if (toIndex > mdList.size()) {
                    toIndex = mdList.size();
                }
                if (fromIndex > toIndex) {
                    fromIndex = toIndex;
                }
                datas.put("rows",mdList.subList(fromIndex, toIndex));
                datas.put("total",mdList.size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取原始补充指标失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取原始补充指标失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody getBczbByFzList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取原始补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);

            Map pa=new HashMap();
            pa.put("c1", condition.getC1());
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getBczbByFz(pa);
                //转换后的指标Map
                HashMap<String,HashMap> objects=new HashMap();
                HashMap<String,String> plist=new HashMap();
                for(HashMap m:bbls){
                    plist.put((String)m.get("PID"),"project");
                    HashMap zb=(HashMap)objects.get((String)m.get("OID"));
                    if(zb==null){
                        zb=new HashMap();
                        objects.put((String)m.get("OID"),zb);
                    }
                    zb.put((String)m.get("OKEY"),(String)m.get("OVALUE"));
                    zb.put("PID",(String)m.get("PID"));
                    zb.put("OID",(String)m.get("OID"));
                    zb.put("RKSJ",(String)m.get("RKSJ"));
                }
                //获取project 范围
                String scope="''";
                for(String k:plist.keySet()){
                    scope+=",'"+k+"'";
                }
                List<HashMap> lsP=bczbMapper.getProjectByScope(scope);

                //转换后的project
                HashMap<String,HashMap> projects=new HashMap();
                for(HashMap m:lsP){
                    HashMap pro=(HashMap)projects.get((String)m.get("ID"));
                    if(pro==null){
                        pro=new HashMap();
                        projects.put((String)m.get("ID"),pro);
                    }
                    pro.put((String)m.get("OKEY"),(String)m.get("OVALUE"));
                    pro.put("PRO_ID",(String)m.get("ID"));
                    pro.put("PRO_DXGC",(String)m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for(String k :objects.keySet()){
                    HashMap o=objects.get(k);
                    o.putAll(projects.get(o.get("PID")));
                }
                List<HashMap> mdList=new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String,String> orderby=new LinkedHashMap<>();
                if(StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                }else{
                    orderby.put("RKSJ","desc");
                }
                AppUtils.sort(mdList,orderby);
                HashMap datas = new HashMap();
                int pageNo = Integer.parseInt(condition.getStart());   //从1开始
                int pageSize = Integer.parseInt(condition.getLimit());
                int fromIndex = pageSize * (pageNo - 1);
                int toIndex = pageSize * pageNo;
                if (toIndex > mdList.size()) {
                    toIndex = mdList.size();
                }
                if (fromIndex > toIndex) {
                    fromIndex = toIndex;
                }
                datas.put("rows",mdList.subList(fromIndex, toIndex));
                datas.put("total",mdList.size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取原始补充指标失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取原始补充指标失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody fzxf(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            try{
                List<HashMap> lszb=bczbMapper.getZbByFz(condition);
                String xml="";
                Document doc = DocumentHelper.createDocument();
                Element ls = DocumentHelper.createElement("bcList");
                doc.add(ls);
                HashMap<String,String> projectHt=new HashMap<String,String>();
                //System.out.println("lszb=="+lszb);
                for(HashMap m:lszb){
                    String root=(String)m.get("INFO");
                    if(StringUtils.isEmpty(root)) continue;
                    if(projectHt.get(root)==null)
                        projectHt.put(root,"''");
                    projectHt.put(root,projectHt.get(root)+",'"+m.get("BCZBID")+"'");
                }
                //System.out.println("projectHt=="+projectHt);
                for(String k :projectHt.keySet()){
                    try{
                        Document n=getMapFromDb2(k,projectHt.get(k));
                        ls.add(n.getRootElement());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                //bczbXfPath
                String saveDirectoryPath = Global.getConfig("upLoadPath") +"/"+ bczbXfPath+"/"+condition.getC1();
                xml=XmlUtils.FormatXml(doc);
                FileUtils.writeToFile(saveDirectoryPath,xml,false);
                bczbMapper.updateFzPath(condition.getC1(),saveDirectoryPath);
                Map mail=new HashMap();
                mail.put("FJRID",user.getId());
                mail.put("FJRYHM",user.getLoginName());
                mail.put("FJRXM",user.getUsername());
                mail.put("SJRID",condition.getC2());
                mail.put("ZT",condition.getC3());
                mail.put("LB","2");
                mail.put("PATH","/"+condition.getC1());
                emailMapper.sendMail(mail);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody fzxf2(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","操作成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                List<HashMap> lszb=bczbMapper.getZbByFz(condition);
                String xml="";
                Document doc = DocumentHelper.createDocument();
                Element ls = DocumentHelper.createElement("bcList");
                doc.add(ls);
                for(HashMap m:lszb){
                    String root=(String)m.get("BCZBID");
                    //xml+=getMapFromDb(root);
                    Document n=getMapFromDb(root);
                    /*if(doc==null) doc=n;
                    else doc.add(n.getRootElement());*/
                    ls.add(n.getRootElement());
                }
                //bczbXfPath
                String saveDirectoryPath = Global.getConfig("upLoadPath") +"/"+ bczbXfPath+"/"+condition.getC1();
                xml=XmlUtils.FormatXml(doc);
                FileUtils.writeToFile(saveDirectoryPath,xml,false);
                bczbMapper.updateFzPath(condition.getC1(),saveDirectoryPath);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public Document getMapFromDb(String root){
        LinkedHashMap<String,LinkedHashMap> treeM=new LinkedHashMap<>();
        List<LinkedHashMap> treeMm=bczbMapper.getBczbTreeById(root);
        for(LinkedHashMap t:treeMm){
            treeM.put((String)t.get("ID"),t);
        }
        //System.out.println("11 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        List<LinkedHashMap> propertyL=bczbMapper.getBczbProperty(root);
        //System.out.println("22 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String,Map> propertyM=new HashMap<String,Map>();
        for(LinkedHashMap m:propertyL){
            Map<String,Object> p=propertyM.get((String)m.get("OID"));
            if(p==null){
                p=new LinkedHashMap<String,Object>();
                propertyM.put((String)m.get("OID"),p);
            }
            p.put((String)m.get("OKEY"),(String)m.get("OVALUE")==null?"":(String)m.get("OVALUE"));
        }
        //System.out.println("33 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String,List> tree=new LinkedHashMap<String,List>();
        for (String key : treeM.keySet()) {
            LinkedHashMap m=treeM.get(key);
            List children=tree.get(m.get("PID"));
            if(children==null){
                children=new ArrayList();
                tree.put((String)m.get("PID"),children);
            }
            children.add(treeM.get(key));
        }
        //System.out.println("44 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String,Object> docStr=AppUtils.readSql2Map(treeM,tree,root,propertyM);
        Document doc = null;
        String xmlstr="";
        try {
            doc = XmlUtils.Map2Xml(docStr);
            xmlstr = XmlUtils.FormatXml(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }
    public Document getMapFromDb2(String root,String scope){
        LinkedHashMap<String,LinkedHashMap> treeM=new LinkedHashMap<>();
        List<LinkedHashMap> treeMm=bczbMapper.getBczbTreeById2(root,scope);
        for(LinkedHashMap t:treeMm){
            treeM.put((String)t.get("ID"),t);
        }
        //System.out.println("11 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        List<LinkedHashMap> propertyL=bczbMapper.getBczbProperty2(root,scope);
        //System.out.println("22 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String,Map> propertyM=new HashMap<String,Map>();
        for(LinkedHashMap m:propertyL){
            Map<String,Object> p=propertyM.get((String)m.get("OID"));
            if(p==null){
                p=new LinkedHashMap<String,Object>();
                propertyM.put((String)m.get("OID"),p);
            }
            p.put((String)m.get("OKEY"),(String)m.get("OVALUE")==null?"":(String)m.get("OVALUE"));
        }
        //System.out.println("33 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String,List> tree=new LinkedHashMap<String,List>();
        for (String key : treeM.keySet()) {
            LinkedHashMap m=treeM.get(key);
            List children=tree.get(m.get("PID"));
            if(children==null){
                children=new ArrayList();
                tree.put((String)m.get("PID"),children);
            }
            children.add(treeM.get(key));
        }
        //System.out.println("44 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String,Object> docStr=AppUtils.readSql2Map(treeM,tree,root,propertyM);
        Document doc = null;
        String xmlstr="";
        try {
            doc = XmlUtils.Map2Xml(docStr);
            xmlstr = XmlUtils.FormatXml(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }
    public ResponseBody bczbCk(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        String root="8b7f5824833143a18045b9e620521eb2";
        LinkedHashMap<String,LinkedHashMap> treeM=new LinkedHashMap<>();
        List<LinkedHashMap> treeMm=bczbMapper.getBczbTreeById(root);
        for(LinkedHashMap t:treeMm){
            treeM.put((String)t.get("ID"),t);
        }
        //System.out.println("11 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        List<LinkedHashMap> propertyL=bczbMapper.getBczbProperty(root);
        //System.out.println("22 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String,Map> propertyM=new HashMap<String,Map>();
        for(LinkedHashMap m:propertyL){
            Map<String,Object> p=propertyM.get((String)m.get("OID"));
            if(p==null){
                p=new LinkedHashMap<String,Object>();
                propertyM.put((String)m.get("OID"),p);
            }
            p.put((String)m.get("OKEY"),(String)m.get("OVALUE")==null?"":(String)m.get("OVALUE"));
        }
        //System.out.println("33 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String,List> tree=new LinkedHashMap<String,List>();
        for (String key : treeM.keySet()) {
            LinkedHashMap m=treeM.get(key);
            List children=tree.get(m.get("PID"));
            if(children==null){
                children=new ArrayList();
                tree.put((String)m.get("PID"),children);
            }
            children.add(treeM.get(key));
        }
        //System.out.println("44 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String,Object> docStr=AppUtils.readSql2Map(treeM,tree,root,propertyM);
        //System.out.println("55 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Document doc = null;
        try {
            //System.out.println(docStr);
            doc = XmlUtils.Map2Xml(docStr);
            //System.out.println("66 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
            //System.out.println(XmlUtils.FormatXml(doc));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody bczbRk(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        String textFromFile = "";
        try {
            textFromFile = FileUtils.readFileToString(new File("c:/bczb.xml"), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> map = null;
        try {
            map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
            String sql = AppUtils.readMap2Sql(map, "-1");
            HashMap h = new HashMap();
            h.put("sql", sql);
            bczbMapper.mergeProject(h);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody bczbRk2(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        String textFromFile = "";
        try {
            textFromFile = FileUtils.readFileToString(new File("c:/bczb.xml"), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> map = null;
        try {
            map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
            String sql = AppUtils.readMap2Sql2(map, "-1","","");
            HashMap h = new HashMap();
            h.put("sql", sql);
            bczbMapper.mergeProject(h);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody getZhzbList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取指标组合列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            //System.out.println(rq.getParams());
            User user = UserUtils.getUser();
            condition.setDwdm(user.getDeptCode().substring(0,4));
            try{
                List<HashMap> lsHt = bczbMapper.getZhzbList(condition);
                HashMap r=new HashMap();
                condition.setStart(null);
                r.put("total",bczbMapper.getZhzbList(condition).size());
                r.put("rows",lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取指标组合失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("获取指标组合失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody getJsrList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取指标组合列表",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            condition.setDwdm(user.getDeptCode().substring(0,4));
            try{
                List<HashMap> lsHt = bczbMapper.getJsrList(condition);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取指标组合失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("获取指标组合失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody zhsave(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                User user = UserUtils.getUser();
                condition.setUserid(user.getId());
                condition.setDwdm(user.getDeptCode().substring(0,4));
                bczbMapper.saveBczbZh(condition);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody addZb2Fz(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC1());
                String c2=condition.getC2();
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                Map m=new HashMap();
                m.put("list",h);
                m.put("fzid",c2);
                //System.out.println("6666666666666666"+m);
                bczbMapper.addZb2Fz(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody delZb2Fz(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","保存成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                String updL = StringEscapeUtils.unescapeHtml(condition.getC1());
                String c2=condition.getC2();
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                Map m=new HashMap();
                m.put("list",h);
                m.put("fzid",c2);
                bczbMapper.delZb2Fz(m);
            }catch (Exception e){
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！"+e.getMessage());
            }
        }catch(Exception e){
            rp.setIssuccess("0");
            rp.setMessage("操作失败！"+e.getMessage());
            e.printStackTrace();
        }
        return rp;
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
                        System.out.println("文件:" + file2.getPath());
                        l.add(file2.getPath().substring(file2.getPath().
                                indexOf(Global.USERFILES)+Global.USERFILES.length()));
                        //l.add(file2.getPath());
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
    }
    //region
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
    //endregion
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
