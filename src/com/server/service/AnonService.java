package com.server.service;

import com.common.config.Global;
import com.common.mapper.JsonMapper;
import com.common.sys.entity.User;
import com.common.utils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.Entity.Condition;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.mapper.*;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.*;

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
    @Autowired
    BbglMapper bbglMapper;
    @Autowired
    BczbMapper bczbMapper;

    private static String upgradeDec = "upgrade";
    private static String databbDec = "databb";

    //private static String upgradeFilePath="C:\\\\upload\\\\softwarefiles\\\\";
    public ResponseBody testConnect(RequestBody rq, Map params, String id) {
        ResponseBody r = new ResponseBody(params, "1", "连接成功", id, rq.getTaskid());
        return r;
    }

    public ResponseBody isRegist(RequestBody rq, Map params, String id) {
        ResponseBody r = new ResponseBody(params, "0", "该机器未曾注册！", id, rq.getTaskid());
        try {
            //HashMap m=anonMapper.selectRegistInf(params);
            HashMap m = jqsqMapper.isRegist(params);
            if (m != null) {
                r.setIssuccess("1");
                r.setMessage("该机器已被注册过！目前处于" + ("1".equals((String) m.get("STAUTS")) ? "停用状态" : "启用状态"));
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
        } catch (Exception e) {
            e.printStackTrace();
            r.setIssuccess("0");
            r.setMessage("注册验证失败！");
        }
        return r;
    }

    public ResponseBody regist(RequestBody rq, Map params, String id) {
        ResponseBody r = new ResponseBody(params, "1", "注册申请提交成功！", id, rq.getTaskid());
        try {
            //HashMap m=anonMapper.selectRegistInf(params);
            jqsqMapper.regist(params);
            HashMap m = jqsqMapper.isRegist(params);
            if (m != null) {
                r.setMessage("该机器以前注册过！目前处于" + ("1".equals((String) m.get("STAUTS")) ? "停用状态" : "启用状态"));
                r.setDatas(JsonMapper.toJsonString(m));
            }
        } catch (Exception e) {
            e.printStackTrace();
            r.setIssuccess("0");
            r.setMessage("注册申请提交失败！");
        }
        return r;
    }

    public ResponseBody registUser(RequestBody rq, Map params, String id) {
        ResponseBody r = new ResponseBody(params, "1", "注册申请提交成功！", id, rq.getTaskid());
        try {
            params.put("LOGINNAME", params.get("LOGINNAME"));
            HashMap m = jqsqMapper.isRegistUser(params);
            if (m != null) {
                r.setIssuccess("2");
                r.setMessage("当前登录名已经被占用！请更换登录名！");
                r.setDatas(JsonMapper.toJsonString(m));
            } else jqsqMapper.registUser(params);
        } catch (Exception e) {
            e.printStackTrace();
            r.setIssuccess("0");
            r.setMessage("注册申请提交失败！");
        }
        return r;
    }

    public ResponseBody getDeptList(RequestBody rq, Map params, String id) {
        ResponseBody r = new ResponseBody(params, "1", "获取单位列表成功！", id, rq.getTaskid());

        List<HashMap> hm = userMapper.getDeptList(params);
        r.setDatas(JsonMapper.toJsonString(hm));
        return r;
    }

    public ResponseBody getXtSjInfo(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取系统更新信息成功", id, rq.getTaskid());
        try {
            try {
                List<HashMap> infos = bbglMapper.getXtSjInfo(params);
                //System.out.println(params);
                for (HashMap info : infos) {
                    String path = Global.getUserfilesBaseDir()
                            + info.get("SUBMITTER") + "/" + info.get("ID") + "/";
                    String doc = Global.SOFTWAREFILES;
                    List<String> ul = new ArrayList<String>();
                    traverseFolder2(ul, path, doc);
                    info.put("filelist", ul);
                }
                String zbstr = com.common.annotation.mapper.JsonMapper.getInstance().toJson(infos);
                rp.setDatas(zbstr);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取系统更新信息成功失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取系统更新信息成功失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getDatabbList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据升级包列表", id, rq.getTaskid());
        try {
            /*File file = new File(Global.getUserfilesBaseDir() + Global.SOFTWAREFILES_BASE_URL+databbDec);
            List<String> ul=new ArrayList<String>();
            traverseFolder2(ul,Global.getUserfilesBaseDir() + Global.SOFTWAREFILES_BASE_URL+databbDec,databbDec);
            rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(ul));*/
            User user = UserUtils.getUser();
            //System.out.println("--------"+user);
            params.put("OILID", user.getOilId());
            params.put("DEPTS", AppUtils.getParentDept(user.getDeptId()));
            try {
                //syncMapper.updsycnt(null);
                //String sycnTime=syncMapper.getCurrentTime();
                List<HashMap> bbls = syncMapper.getDataBb(params);
                HashMap datas = new HashMap();
                datas.put("BBXX", bbls);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据升级包列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据升级包列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody login(RequestBody rq, Map params, String id) {
        ResponseBody r = new ResponseBody(params, "1", "登录成功", id, rq.getTaskid());
        try {
            HashMap info = userMapper.validRegst(rq.getCpu());
            System.out.println(rq.getCpu());
            if (userMapper.validRegst(rq.getCpu()) == null) {
                r.setIssuccess("03");
                r.setMessage("登录机器未注册！");
                return r;
            } else {
                if ("1".equals(info.get("STATUS"))) {
                    r.setIssuccess("031");
                    r.setMessage("登录机器被停用！");
                    return r;
                }
                if ("2".equals(info.get("STATUS"))) {
                    r.setIssuccess("032");
                    r.setMessage("登录机器被注销！");
                    return r;
                }
            }
            HashMap m = userMapper.getUserOne(rq.getUsername());
            if (m == null) {
                r.setIssuccess("01");
                r.setMessage("该用户不存在！");
                return r;
            } else {
                if (StringUtils.isNotBlank(rq.getPassword())) {
                    System.out.println("11   (" + (String) m.get("PWD"));
                    System.out.println(rq.getPassword());
                    if (rq.getPassword().equals((String) m.get("PWD"))) {
                        r.setIssuccess("1");
                        r.setMessage("登录成功！");
                        r.setDatas(JsonMapper.toJsonString(m));
                    } else {
                        r.setIssuccess("02");
                        r.setMessage("密码不正确！");
                    }
                } else {
                    r.setIssuccess("02");
                    r.setMessage("密码不正确！");
                }
                return r;
            }
        } catch (Exception e) {
            e.printStackTrace();
            r.setIssuccess("04");
            r.setMessage("登录失败，请联系管理员！");
        }
        return r;
    }

    public ResponseBody authgetuser(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取用户列表成功", id, rq.getTaskid());
        try {

            List<HashMap> ul = userMapper.getUserList(params);
            rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(ul));
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取用户列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody AuthRealm(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取用户成功", id, rq.getTaskid());
        try {
            //System.out.println();
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            //System.out.println("------"+condition);
            HashMap m = userMapper.getUserOne(condition.getLoginName());
            rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(m));
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取用户失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //region  getMenuList
    /*public ResponseBody getMenuList(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取数据列表成功！",id,rq.getTaskid());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            //Condition condition=objectMapper.readValue(rq.getParams(), Condition.class);
            try{
                Map mmmm=frameMapper.getPermission("menuList");
                Blob bbbb=(Blob)mmmm.get("VALUE");
                InputStream inStream = bbbb.getBinaryStream();
*//*                byte[] data = new byte[1024];
                inStream.read(data);
                inStream.close();*//*
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
    }*/
    //endregion
    public ResponseBody getUpgradeList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取升级包列表", id, rq.getTaskid());
        try {
            File file = new File(Global.getUserfilesBaseDir() + Global.SOFTWAREFILES_BASE_URL + upgradeDec);
            List<String> ul = new ArrayList<String>();
            traverseFolder2(ul, Global.getUserfilesBaseDir() + Global.SOFTWAREFILES_BASE_URL + upgradeDec, upgradeDec);
            rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(ul));
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取升级包列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody modifyPsw(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "修改密码成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            userMapper.modifyPsw(condition);
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("修改密码失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody modifyUser(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "修改成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            userMapper.modifyUser(condition);
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("修改失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getServiceIp(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        rp.setIp(Global.getConfig("server.ip.port"));
        return rp;
    }

    public ResponseBody bczbCk(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        String root="54bc0d83-300d-479c-87c2-c8ef998e681e";
        LinkedHashMap<String,LinkedHashMap> treeM=new LinkedHashMap<>();
        List<LinkedHashMap> treeMm=bczbMapper.getBczbTreeById(root);
        for(LinkedHashMap t:treeMm){
            treeM.put((String)t.get("ID"),t);
        }
        System.out.println("00 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        List<LinkedHashMap> test1=bczbMapper.getOtherTest();
        System.out.println("01 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        List<LinkedHashMap> test2=bczbMapper.getOtherTest2();
        System.out.println("11 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        List<LinkedHashMap> propertyL=bczbMapper.getBczbProperty(root);
        System.out.println("22 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));

        Map<String,Map> propertyM=new HashMap<String,Map>();
        for(LinkedHashMap m:propertyL){
            Map<String,Object> p=propertyM.get((String)m.get("OID"));
            if(p==null){
                p=new LinkedHashMap<String,Object>();
                propertyM.put((String)m.get("OID"),p);
            }
            p.put((String)m.get("OKEY"),(String)m.get("OVALUE")==null?"":(String)m.get("OVALUE"));
        }
        System.out.println("33 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));

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
        System.out.println("44 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));

        Map<String,Object> docStr=getDoc(treeM,tree,root,propertyM);
        System.out.println("55 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));

        Document doc = null;
        try {
            //System.out.println(docStr);
            doc = XmlUtils.Map2Xml(docStr);
            System.out.println("66 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));

            //System.out.println(XmlUtils.FormatXml(doc));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }

    public Map<String,Object> getDoc(HashMap<String,LinkedHashMap> objdic,Map<String,List> tree,String root,Map<String,Map> propertyM){
        Map<String,Object> doc =new LinkedHashMap<String,Object>();
        Map<String,Object> obj=(Map<String,Object>)propertyM.get(root);
        if(obj==null) obj=new LinkedHashMap<String,Object>();
        doc.put((String)((LinkedHashMap)objdic.get(root)).get("OTYPE"),obj);
        List childreno=tree.get(root);
        if(childreno==null) return doc;
        for(Object c : childreno){
            if(obj.get((String)((Map)c).get("OTYPE"))==null)
                obj.put((String)((Map)c).get("OTYPE"),getDoc(objdic,tree,(String)((Map)c).get("ID"),propertyM).get((String)((Map)c).get("OTYPE")));
            else{
                if(obj.get((String)((Map)c).get("OTYPE")) instanceof List){
                    List<Object> ccc=(List)obj.get((String)((Map)c).get("OTYPE"));
                    ccc.add(getDoc(objdic,tree,(String)((Map)c).get("ID"),propertyM).get((String)((Map)c).get("OTYPE")));
                }else{
                    List<Object> ccc=new ArrayList<Object>();
                    ccc.add(obj.get((String)((Map)c).get("OTYPE")));
                    obj.put((String)((Map)c).get("OTYPE"),ccc);
                    ccc.add(getDoc(objdic,tree,(String)((Map)c).get("ID"),propertyM).get((String)((Map)c).get("OTYPE")));
                }
            }
        }
        return doc;
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
            String sql = this.readMap2Sql(map, "-1");
            HashMap h = new HashMap();
            h.put("sql", sql);
            bczbMapper.mergeProject(h);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return rp;
    }

    public String readMap2Sql(Map<String, Object> map, String pid) {
        StringBuffer sql = new StringBuffer("");
        for (String key : map.keySet()) {
            //Map<String, Object> node = (Map<String, Object>) map.get(key);
            Object node = map.get(key);
            if ("dxgc".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ID");
                String cpuid = (String) ((Map<String, Object>) node).get("@CPUID");
                String userid = (String) ((Map<String, Object>) node).get("@USERID");
                String deptid=(String)((Map<String, Object>) node).get("@DEPTID");
                String pkey = id + cpuid + userid;
                sql.append(" v_deptid:="+deptid+";");
                sql.append(" delete from APP_OPROPERTY where oid in (select id from APP_OBJECT_BCZBTREE connect by prior id=pid start with PKEY='" + pkey + "');");
                sql.append(" delete from APP_OBJECT_BCZBTREE where id in (select id from APP_OBJECT_BCZBTREE connect by prior id=pid start with PKEY='" + pkey + "');");
                //String uuid = UUID.randomUUID().toString();
                String uuid = IdGen.uuid();
                sql.append("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,PKEY,LB,DEPTID) VALUES ('" + uuid + "','" + pid + "','" + key + "','" + pkey + "','1','"+deptid+"');");
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String)
                        sql.append(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + okey.substring(1) + "','" + property.toString() + "',v_deptid);");
                    else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql(child1, uuid));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql(child1, uuid));
                        }
                    }
                }
            } else {
                String uuid = IdGen.uuid();
                if (node instanceof String) {
                    sql.append(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + key.substring(1) + "','" + node.toString() + "',v_deptid);");
                }
                else {
                    sql.append("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,LB,DEPTID) VALUES ('" + uuid + "','" + pid + "','" + key + "','1',v_deptid);");
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String)
                            sql.append(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + okey.substring(1) + "','" + property.toString() + "',v_deptid);");
                        else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            sql.append(readMap2Sql(child1, uuid));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                sql.append(readMap2Sql(child1, uuid));
                            }
                        }
                    }
                }
            }
        }
        return sql.toString();
    }

    public void traverseFolder2(List<String> l, String path, String doc) {
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
                        traverseFolder2(l, file2.getAbsolutePath(), doc);
                    } else {
                        System.out.println("文件:" + file2.getPath());
                        l.add(file2.getPath().substring(file2.getPath().
                                indexOf(Global.USERFILES) + Global.USERFILES.length()));
                        //l.add(file2.getPath());
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
    }
}
