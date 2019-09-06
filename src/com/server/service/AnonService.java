package com.server.service;

import com.common.config.Global;
import com.common.mapper.JsonMapper;
import com.common.sys.entity.Office;
import com.common.sys.entity.User;
import com.common.utils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.Entity.Condition;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.mapper.*;

import org.apache.tools.ant.Project;    
import org.apache.tools.ant.taskdefs.Expand;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.util.zip.ZipOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.PostConstruct;

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
    @Autowired
    EmailMapper emailMapper;
    String bczbXfPath = Global.YSBCZBXF_BASE_URL;
    String yhbczbPath = Global.YHBCZBXF_BASE_URL;
    String dwbczbPath = Global.DWBCZBXF_BASE_URL;
    String dwbcsbzcPath = Global.DWBCSBZC_BASE_URL;
    String bbh_last="@_@";
    @Autowired
    SysService sysService;
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
            System.out.println("-------------------"+rq.getCpu());
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
                    //System.out.println("11   (" + (String) m.get("PWD"));
                    //System.out.println(rq.getPassword());
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
        System.out.println("++++++++++++"+rq.getTaskid());
        System.out.println("++++++++++++"+Global.getConfig("server.ip.port"));
        rp.setIp(Global.getConfig("server.ip.port"));
        return rp;
    }
    public ResponseBody bczbRk(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        String textFromFile = "";
        User user = UserUtils.getUser();
        try {
            textFromFile = FileUtils.readFileToString(new File("c:/55555555.xml"), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> map = null;
        HashMap h=new HashMap();
        String sql="";
        try {
            map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
            //String sql = AppUtils.readMap2Sql22(map, "-1","","1045","askdf;allasdj;kf","12313121312","");
            //System.out.println(sql);
            /*HashMap h = new HashMap();
            h.put("sql", sql);*/
            System.out.println("------入库开始时间"+DateUtils.getDateTime());
            //bczbMapper.mergeProject(h);
            sql=AppUtils.readMap222Sql2(map, "-1", "", "1093", (String) rq.getCpu(), user.getId(), "");
            map.put("sql",sql);
            bczbMapper.mergeProject(h);
            System.out.println("------入库结束时间"+DateUtils.getDateTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody bczbRk2(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        String textFromFile = "";
        User user = UserUtils.getUser();
        try {
            textFromFile = FileUtils.readFileToString(new File("c:/55555555.xml"), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> map = null;
        try {
            map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
            //String sql = AppUtils.readMap2Sql22(map, "-1","","1045","askdf;allasdj;kf","12313121312","");
            //System.out.println(sql);
            /*HashMap h = new HashMap();
            h.put("sql", sql);*/
            System.out.println("------入库开始时间"+DateUtils.getDateTime());
            //bczbMapper.mergeProject(h);
            HashMap<String,List> sqlHt=AppUtils.readMap2Sql2(map, "-1", "", "1093", (String) rq.getCpu(), user.getId(), "");
            List aList=sqlHt.get("a");
            List bList=sqlHt.get("b");
            HashMap h = new HashMap();
            if(aList!=null){
                String sql=" null;";
                for(int i=0;i<aList.size();i++){
                    sql+=aList.get(i);
                }
                h.put("sql", sql);
                bczbMapper.mergeProject(h);
            }
            if(bList!=null){
                String sql=" null;";
                for(int i=0;i<bList.size();i++){
                    sql+=bList.get(i);
                    if(i%3000==0){
                        h.put("sql", sql);
                        bczbMapper.mergeProject(h);
                        sql=" null;";
                    }
                }
                h.put("sql", sql);
                bczbMapper.mergeProject(h);
            }
            System.out.println("------入库结束时间"+DateUtils.getDateTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody bczbRk22(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        String textFromFile = "";
        User user = UserUtils.getUser();
        try {
            textFromFile = FileUtils.readFileToString(new File("c:/55555555.xml"), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> map = null;
        try {
            map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
            System.out.println("------入库开始时间"+DateUtils.getDateTime());
            HashMap<String,List> sqlHt=AppUtils.readMap2222Sql2(map, "-1", "", "1093", (String) rq.getCpu(), user.getId(), "");
            List aList=sqlHt.get("a");
            List bList1=sqlHt.get("b1");
            List bList2=sqlHt.get("b2");
            HashMap h = new HashMap();
            if(aList!=null){
                String sql=" null;";
                for(int i=0;i<aList.size();i++){
                    sql+=aList.get(i);
                }
                h.put("sql", sql);
                bczbMapper.mergeProject(h);
            }
            HashMap m=new HashMap();
            if(bList1!=null){
                m.put("list",bList1);
                bczbMapper.insertBczb1(m);
            }
            if(bList2!=null){
                m.put("list",bList2);
                bczbMapper.insertBczb2(m);
            }
            System.out.println("------入库结束时间"+DateUtils.getDateTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody bczbRk3(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        String textFromFile = "";
        User user = UserUtils.getUser();
        try {
            textFromFile = FileUtils.readFileToString(new File("c:/444.xml"), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> map = null;
        try {
            map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
            String dw = "1093";
            if (StringUtils.isNotBlank(user.getDeptCode())) dw = user.getDeptCode().substring(0, 4);
            //String sql = AppUtils.readMap2Sql2(map, "-1", "", dw, (String) rq.getCpu(), user.getId(), "");
            System.out.println("------解析开始时间"+DateUtils.getDateTime());
            HashMap<String,List> sqlHt=AppUtils.readMap2Sql22(map, "-1", "", dw, (String) rq.getCpu(), user.getId(), "");
            System.out.println("------解析完成时间"+DateUtils.getDateTime());
            System.out.println("------入库开始时间"+DateUtils.getDateTime());
            List aList=sqlHt.get("a");
            List bList=sqlHt.get("b");
            HashMap h = new HashMap();
            if(aList!=null){
                String sql=" null;";
                for(int i=0;i<aList.size();i++){
                    sql+=aList.get(i);
                }
                h.put("sql", sql);
                bczbMapper.mergeProject(h);
            }
            if(bList!=null){
                String sql=" null;";
                for(int i=0;i<bList.size();i++){
                    sql+=bList.get(i);
                    if(i%400==0){
                        final String ttt=sql;
                        /*Thread t = new Thread(new Runnable(){
                            public void run(){

                            }});
                        t.start();*/
                        HashMap wowo=new HashMap();
                        wowo.put("sql", ttt);
                        bczbMapper.mergeProject(wowo);
                        sql=" null;";
                    }
                }
                h.put("sql", sql);
                bczbMapper.mergeProject(h);
            }
            System.out.println("------入库结束时间"+DateUtils.getDateTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody bczbCk2(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        String root="f73f6d30a37341be88c22beeb1219d2e";
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
    public ResponseBody bczbCk3(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        String root="cbfeb5a54e0d4ec69b55640c29b9a3cd";
        LinkedHashMap<String,LinkedHashMap> treeM=new LinkedHashMap<>();
        List<LinkedHashMap> treeMm=bczbMapper.getBczbTreeById(root);
        for(LinkedHashMap t:treeMm){
            treeM.put((String)t.get("ID"),t);
        }

        List<HashMap> propertyL=bczbMapper.getDic();
        Map<String,List<String>> propertyM=new HashMap<String,List<String>>();
        for(HashMap p:propertyL){
            List<String> m=propertyM.get(p.get("OTYPE")+"");
            if(m==null) {
                m=new ArrayList();
                propertyM.put(p.get("OTYPE")+"",m);
            }
            m.add(p.get("OKEY")+"");
        }
        Map<String,Map> propertys=new HashMap<String,Map>();
        for(LinkedHashMap m:treeMm){
            List<String> pl=propertyM.get(m.get("OTYPE"));
            Map mm=propertys.get(m.get("ID"));
            if(mm==null) {
                mm=new LinkedHashMap();
                propertys.put(m.get("ID")+"",mm);
            }
            if(pl!=null)
                for(String mmm:pl){
                    mm.put("@"+mmm,m.get("P_"+mmm.toUpperCase())==null?"":m.get("P_"+mmm.toUpperCase()));
                }
            else{
                System.out.println(m.get("OTYPE"));
            }
            //mm.put("#text",m.get("P_TEXT")==null?"":m.get("P_TEXT"));

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
        Map<String,Object> docStr=AppUtils.readSql22Map(treeM,tree,root,propertys);
        //System.out.println("55 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Document doc = null;
        try {
            //System.out.println(docStr);
            doc = XmlUtils.Map2Xml(docStr);
            rp.setMessage(docStr.toString());
            //System.out.println("66 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
            System.out.println(XmlUtils.FormatXml(doc));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody gxList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取单位补充指标更新列表成功", id, rq.getTaskid());
        try {
            String code = frameMapper.getCodeByCpu(rq.getCpu());
            //String rootMl = bczbMapper.getBczbRootByCode(code);
            String oil=frameMapper.getOilByCpu(rq.getCpu());
            Map pp = new HashMap();
            pp.put("CODE", code);
            {
                try {
                    HashMap<String, Office> lsHt = frameMapper.bczbMlLists(pp);
                    pp.put("OILIDS",oil);
                    List<HashMap> zbdic = frameMapper.getMlZbAList(pp);
                    LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                    HashMap<String, List<Office>> children = new HashMap();
                    Map<String, List<Map>> zbchildren = new HashMap();
                    String rootId = "";
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        if("9".equals(lsHt.get(key).getType())) continue;
                        zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                    for (HashMap z : zbdic) {
                        String mlid = (String) z.get("ZHZBID");
                        String ppp=lsHt.get(mlid).getParentId();
                        List c = zbchildren.get(ppp);
                        c.add(z);
                    }
                    Map<String, Object> docStr = AppUtils.getMlMap(lsHt, children, rootId, zbchildren);
                    String xml = "";
                    Document doc = XmlUtils.Map2Xml(docStr);
                    String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbczbPath + "/" + rq.getCpu();

                    xml = XmlUtils.FormatXml(doc);
                    FileUtils.writeToFile(saveDirectoryPath, xml, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    rp.setIssuccess("0");
                    rp.setMessage("操作失败！" + e.getMessage());
                }
            }
            List<HashMap> ml = frameMapper.getMlZbAList(pp);
            try {
                Map rrr = new HashMap();
                rrr.put("ML", rq.getCpu());
                rrr.put("BCZB", ml);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(rrr));
            } catch (Exception e) {
                rp.setIssuccess("0");
                rp.setMessage("获取单位补充指标失败");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody gxSbzcList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取单位补充设备主材更新列表成功", id, rq.getTaskid());
        try {
            String code = frameMapper.getCodeByCpu(rq.getCpu());
            //String rootMl = bczbMapper.getBczbRootByCode(code);
            String oil=frameMapper.getOilByCpu(rq.getCpu());
            Map pp = new HashMap();
            pp.put("CODE", code);
            {
                try {
                    HashMap<String, Office> lsHt = frameMapper.bcsbzcAMlLists(pp);
                    pp.put("OILIDS",oil);
                    List<HashMap> zbdic = frameMapper.getMlSbzcAList(pp);
                    LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                    HashMap<String, List<Office>> children = new HashMap();
                    Map<String, List<Map>> zbchildren = new HashMap();
                    String rootId = "";
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        if("10".equals(lsHt.get(key).getType())) continue;
                        zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                    for (HashMap z : zbdic) {
                        String mlid = (String) z.get("ZHID");
                        String ppp=lsHt.get(mlid).getParentId();
                        List c = zbchildren.get(ppp);
                        c.add(z);
                    }
                    Map<String, Object> docStr = AppUtils.getSbzcMlMap(lsHt, children, rootId, zbchildren);
                    String xml = "";
                    Document doc = XmlUtils.Map2Xml(docStr);
                    String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcsbzcPath + "/" + rq.getCpu();
                    xml = XmlUtils.FormatXml(doc);
                    FileUtils.writeToFile(saveDirectoryPath, xml, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    rp.setIssuccess("0");
                    rp.setMessage("操作失败！" + e.getMessage());
                }
            }
            List<HashMap> ml = frameMapper.getMlSbzcAList(pp);
            try {
                Map rrr = new HashMap();
                rrr.put("ML", rq.getCpu());
                rrr.put("BCSBZC", ml);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(rrr));
            } catch (Exception e) {
                rp.setIssuccess("0");
                rp.setMessage("获取单位补充设备主材失败");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody getSbzcBbList(RequestBody rq, Map params, String id){
        ResponseBody rp = new ResponseBody(params, "1", "获取单位补充设备主材更新列表成功", id, rq.getTaskid());
        String code = frameMapper.getCodeByCpu(rq.getCpu());
        //String rootMl = bczbMapper.getBczbRootByCode(code);
        String oil=frameMapper.getOilByCpu(rq.getCpu());

        params.put("DEPTID",code);
        try {
            try {
                List<HashMap> infos = bbglMapper.getSbzcBbList(params);
                //System.out.println(params);
                Map<String,Object> bbM=new HashMap();
                Map wwwww=new HashMap();
                List lll=new ArrayList();
                wwwww.put("BBXX",lll);
                bbM.put("ArrayOfBBXX",wwwww);
                for (HashMap info : infos) {
                    Map bbb=new HashMap();
                    bbb.put("XH",info.get("BBH")==null?"":(BigDecimal)info.get("BBH")+"");
                    bbb.put("BBMC",info.get("MC")==null?"":(String)info.get("MC"));
                    bbb.put("SYSJ",info.get("SYSJ")==null?"":(String)info.get("SYSJ"));
                    bbb.put("WJH",info.get("WJH")==null?"":(String)info.get("WJH"));
                    bbb.put("SM",info.get("BZ")==null?"":(String)info.get("BZ"));
                    bbb.put("WJ",rq.getCpu()+bbh_last+(BigDecimal)info.get("BBH"));
                    lll.add(bbb);
                    String bbh=(BigDecimal)info.get("BBH")+"";
                    List<String> ul = new ArrayList<String>();
                    ul.add(rq.getCpu()+bbh_last+bbh);
                    info.put("filelist", ul);
                    {
                        try {
                            Map pp=new HashMap();
                            pp.put("CODE", code);
                            pp.put("BBH",info.get("BBH"));
                            HashMap<String, Office> lsHt = frameMapper.bcsbzcAMlLists(pp);
                            pp.put("OILIDS",oil);
                            //System.out.println("--------0"+pp);
                            List<HashMap> zbdic = frameMapper.getMlSbzcAList2(pp);
                            LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                            HashMap<String, List<Office>> children = new HashMap();
                            Map<String, List<Map>> zbchildren = new HashMap();
                            String rootId = "";
                            for (String key : lsHt.keySet()) {
                                if (lsHt.get(key).getCode().length() == 8) rootId = key;
                                List c = children.get(lsHt.get(key).getParentId());
                                if("10".equals(lsHt.get(key).getType())) continue;
                                zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                                if (c == null) {
                                    c = new ArrayList<Office>();
                                    children.put((String) lsHt.get(key).getParentId(), c);
                                }
                                c.add(lsHt.get(key));
                            }
                            for (HashMap z : zbdic) {
                                String mlid = (String) z.get("ZHID");
                                String ppp=lsHt.get(mlid).getParentId();
                                List c = zbchildren.get(ppp);
                                c.add(z);
                            }
                            //System.out.println("-----------1"+zbdic);
                            //System.out.println("-----------2"+zbchildren);
                            Map<String, Object> docStr = AppUtils.getSbzcMlMap(lsHt, children, rootId, zbchildren);
                            String xml = "";
                            Document doc = XmlUtils.Map2Xml(docStr);
                            String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcsbzcPath + "/" + rq.getCpu()+bbh_last+bbh;
                            xml = XmlUtils.FormatXml(doc);
                            FileUtils.writeToFile(saveDirectoryPath, xml, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                            rp.setIssuccess("0");
                            rp.setMessage("操作失败！" + e.getMessage());
                        }
                    }
                }
                Document wowo=XmlUtils.Map2Xml(bbM);
                String zbstr = com.common.annotation.mapper.JsonMapper.getInstance().toJson(infos);
                rp.setDatas(zbstr);
                rp.setMessage(XmlUtils.FormatXml(wowo));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取补充设备主材更新信息失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取补充设备主材更新信息失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody milGet1Attach(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取邮件附件信息成功！", id, rq.getTaskid());
        /*try {
            HashMap el = emailMapper.getMail1Attach(params);
            String apath=(String)el.get("PATH");
            String lb=(String)params.get("LB");
            String fullPath="";
            if("2".equals(lb)||"9".equals(lb)){
                fullPath=Global.YSBCZBXF_BASE_URL+ apath;
            }else if("3".equals(lb)||"8".equals(lb)){
                fullPath=Global.YHBCZBXF_BASE_URL+ apath;
            }else {
                fullPath=Global.getUserfilesBaseDir() + apath;
            }
            File file = new File(Global.getUserfilesBaseDir() + apath);
            String fileStr = FileCopyUtils.copyToString(new FileReader(file));
            rp.setDatas(Encodes.encode2Base64(fileStr));
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("获取邮件附件信息失败!");
        }*/
        return rp;
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
                        //System.out.println("文件:" + file2.getPath());
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

    public ResponseBody readbczbMapSql(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        String textFromFile = "";
        User user = UserUtils.getUser();
        try {
            textFromFile = FileUtils.readFileToString(new File("E:/111.xml"), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> map = null;
        try {
            map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
            //String sql = AppUtils.readMap2Sql22(map, "-1","","1045","askdf;allasdj;kf","12313121312","");
            //System.out.println(sql);
            /*HashMap h = new HashMap();
            h.put("sql", sql);*/
            System.out.println("------入库开始时间"+DateUtils.getDateTime());
            //bczbMapper.mergeProject(h);
            HashMap<String,List> sqlHt=AppUtils.readMap2Sql2(map, "-1", "", "1093", (String) rq.getCpu(), user.getId(), "");
            List aList=sqlHt.get("a");
            List bList=sqlHt.get("b");
            HashMap h = new HashMap();
            if(aList!=null){
                String sql=" null;";
                for(int i=0;i<aList.size();i++){
                    sql+=aList.get(i);
                }
                h.put("sql", sql);
                bczbMapper.mergeProject(h);
            }
            if(bList!=null){
                String sql=" null;";
                for(int i=0;i<bList.size();i++){
                    sql+=bList.get(i);
                    if(i%3000==0){
                        h.put("sql", sql);
                        bczbMapper.mergeProject(h);
                        sql=" null;";
                    }
                }
                h.put("sql", sql);
                bczbMapper.mergeProject(h);
            }
            System.out.println("------入库结束时间"+DateUtils.getDateTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }

   //补全基础指标库字段 
    public void bqzd(Map<String, Object> map){
    	try{
    		//查询获取xml文件中所有属性，并去重
    		List<String> keylist = digui(map);
    		//格式化
    		List<String> newkeylist = new ArrayList<String>();
    		for(int i = 0 ;i<keylist.size();i++){
        		if(keylist.get(i).indexOf("@")!=-1){
        			newkeylist.add(keylist.get(i).replace("@", ""));
        		}else{
        			newkeylist.add(keylist.get(i));
        		}
        	}
    		HashMap h = new HashMap();
    		for(int n = 0; n <newkeylist.size(); n++){
    			String checksql = "where table_name=upper('app_zbxx') and (column_name=upper('"+newkeylist.get(n)+"')or column_name=upper('P_"+newkeylist.get(n)+"'))";
    			String insertsql = "alter table app_zbxx add p_"+newkeylist.get(n)+" varchar2(500)";
    			h.put("checksql", checksql);
    			h.put("insertsql", insertsql);
    			bczbMapper.bqzd(h);
    		}
    	}catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    List<String> keylist = new ArrayList<String>();
    public List digui(Map<String, Object> map){
    	for (String key : map.keySet()){
    		if(!keylist.contains(key)&&!"#text".equals(key)){
        		keylist.add(key);
    		}
			 Object node = map.get(key);
/*			 if("GeneralInformation".equals(key) && node instanceof Map){*/
				 for (String okey : ((Map<String, Object>) node).keySet()){
					 Object property = ((Map<String, Object>) node).get(okey);
					 if (property instanceof String){
						 if(!keylist.contains(okey)&&!"#text".equals(okey)){
				        		keylist.add(okey);	
				    		}
					 }else if(property instanceof Map){
						 digui((Map<String, Object>) map.get(okey));
					 }else if (property instanceof List){
						 for (int i = 0; i < ((List) property).size(); i++){
							 Map<String, Object> child = new HashMap<String, Object>();
	                            child.put(okey, ((List) property).get(i));
	                            digui(child);
						 }
						 
					 }
				 } 
		 }
    	return  keylist;
    }
  //补充指标统计采集
    public ResponseBody bczbtjCj(RequestBody rq, Map params, String id) {
        //System.out.println("----------------------------------------------------");
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        try {
            User user = UserUtils.getUser();
            String textFromFile = "";
            BASE64Decoder decoder = new BASE64Decoder();
            try {
            	textFromFile = FileUtils.readFileToString(new File("E:/20190424092250228.aup"), "UTF-8");
                //System.out.println(textFromFile);
                if (StringUtils.isBlank(textFromFile)) return rp;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Object> map = null;
            Map<String, Object> judgeMap = null;
            try {
            	judgeMap = XmlUtils.Xml2MapWithAttr(textFromFile, true);
            	try{
            		bqzd(judgeMap);
            	}catch (Exception e) {
                    e.printStackTrace();
                }
                map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
                String dw = "1093";
                if (StringUtils.isNotBlank(user.getDeptCode())) 
                	dw = user.getDeptCode().substring(0, 4);
                //String sql = AppUtils.readMap2Sql2(map, "-1", "", dw, (String) rq.getCpu(), user.getId(), "");
                HashMap<String, List> sqlHt = AppUtils.insertBczbTjSql(map, "-1", "", dw, (String) rq.getCpu(), user.getId(), "");
                List aList = sqlHt.get("a");
                List bList = sqlHt.get("b");
                HashMap h = new HashMap();
                if (aList != null) {
                    String sql = " null;";
                    for (int i = 0; i < aList.size(); i++) {
                        sql += aList.get(i);
                    }
                    h.put("sql", sql);
                    bczbMapper.mergeProject(h); 
                }
                if (bList != null) {
                    String sql = " null;";
                    for (int i = 0; i < bList.size(); i++) {
                        sql += bList.get(i);
                        if (i % 400 == 0) {
                            h.put("sql", sql);
                            bczbMapper.mergeProject(h);
                            sql = " null;";
                        }
                    }
                    h.put("sql", sql);
                    bczbMapper.mergeProject(h);
                }
            } catch (Exception e) {
                rp.setIssuccess("0");
                rp.setMessage("获取失败");
                e.printStackTrace();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return rp;
    } 
    
    
    
    
  

    //补充指标采集
    public ResponseBody bczbCjp(RequestBody rq, Map params, String id) {
        //System.out.println("----------------------------------------------------");
    	long timestart = (long) System.currentTimeMillis();
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        try {
            User user = UserUtils.getUser();
            String textFromFile = "";
            try {
            	textFromFile = FileUtils.readFileToString(new File("E:/zip/a0069a6f38d6434389b7e18c7f7f5e.zip"), "UTF-8");
                if (StringUtils.isBlank(textFromFile)) return rp;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Object> map = null;
            try {
                map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
                String dw = "1093";
                if (StringUtils.isNotBlank(user.getDeptCode())) 
                	dw = user.getDeptCode().substring(0, 4);
                //String sql = AppUtils.readMap2Sql2(map, "-1", "", dw, (String) rq.getCpu(), user.getId(), "");
                HashMap<String, List> sqlHt = AppUtils.readMap2Sql22(map, "-1", "", dw, (String) rq.getCpu(), user.getId(), "");
                List aList = sqlHt.get("a");
                List bList = sqlHt.get("b");
                HashMap h = new HashMap();
                if (aList != null) {
                    String sql = " null;";
                    for (int i = 0; i < aList.size(); i++) {
                        sql += aList.get(i);
                    }
                    h.put("sql", sql);
                    long alisttime = (long) System.currentTimeMillis();
                    bczbMapper.mergeProject(h);
                }
               if (bList != null) {
                    String sql = " null;";
                    for (int i = 0; i < bList.size(); i++) {
                        sql += bList.get(i);
                        System.out.println(i);
                        if (i % 400 == 0) {
                            h.put("sql", sql);
                            //bczbMapper.mergeProject(h);
                            sql = " null;";
                        }
                    }
                    h.put("sql", sql);
                    //bczbMapper.mergeProject(h);
                    long timeout = (long) System.currentTimeMillis();
                    System.out.println("组建前5000入库sql耗时："+(timeout-timestart)/1000+"秒");
                }
                /*if (bList != null) {
                	//bczbMapper.changshi(bList);
                	StringBuilder sql = new StringBuilder();
                    sql.append(" null; ");
                    for (int i = 0; i < bList.size(); i++) {
                    	String nnn =(String) bList.get(i);
                    	sql.append(nnn);
                        System.out.println(i);
                        if (i % 400 == 0) {
                        	//sql.append(" SELECT 1 FROM DUAL;");
                            h.put("sql", sql);
                            //System.out.println(sql);
                           // bczbMapper.mergeProject(h);
                            sql.delete(0, sql.length());
                            sql.append(" null; ");
                        }
                   }
                    h.put("sql", sql);
                   // bczbMapper.mergeProject(h);
                    long timeout = (long) System.currentTimeMillis();
                    System.out.println("优化后解析加组建sql耗时："+(timeout-timestart)/1000+"秒");
                }*/
            } catch (Exception e) {
                rp.setIssuccess("0");
                rp.setMessage("获取失败");
                e.printStackTrace();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return rp;
    }
  //解压缩字符串
    public  String releaseCompression(String compressStr) throws IOException{
    	BASE64Decoder decoder = new BASE64Decoder();
    	byte[] b = decoder.decodeBuffer(compressStr);
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	ByteArrayInputStream in = new ByteArrayInputStream(b);
        GZIPInputStream gunzip = new GZIPInputStream(in);
    	byte[] buffer = new byte[256];
    	int n;
    	while ((n = gunzip.read(buffer)) >= 0) {
    	    out.write(buffer, 0, n);
    	}
    	  // toString()使用平台默认编码，也可以显式的指定如toString("GBK")
        return  out.toString("UTF-8");
    }
    
    //字符串压缩
    public  String compress(String uncompressStr) throws IOException {
    	if(uncompressStr==null||uncompressStr.length()<=0){
    		return uncompressStr;
    	}
    	try{
    		BASE64Encoder ecoder = new BASE64Encoder();
    		ByteArrayOutputStream out = new ByteArrayOutputStream();
    	    GZIPOutputStream gzip = new GZIPOutputStream(out);
    	    gzip.write(uncompressStr.getBytes("UTF-8"));
    	    gzip.close();
    	return ecoder.encode(out.toByteArray());
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    	    
    	
    }
    
    public Document getMapFromDb3(String root) {
        LinkedHashMap<String, LinkedHashMap> treeM = new LinkedHashMap<>();
        List<LinkedHashMap> treeMm = bczbMapper.getYhBczbTreeById(root);
        for (LinkedHashMap t : treeMm) {
            treeM.put((String) t.get("ID"), t);
        }
        List<HashMap> propertyL = bczbMapper.getDic();
        Map<String, List<String>> propertyM = new HashMap<String, List<String>>();
        for (HashMap p : propertyL) {
            List<String> m = propertyM.get(p.get("OTYPE") + "");
            if (m == null) {
                m = new ArrayList();
                propertyM.put(p.get("OTYPE") + "", m);
            }
            m.add(p.get("OKEY") + "");
        }
        Map<String, Map> propertys = new HashMap<String, Map>();
        for (LinkedHashMap m : treeMm) {
            List<String> pl = propertyM.get(m.get("OTYPE"));
            Map mm = propertys.get(m.get("ID"));
            if (mm == null) {
                mm = new LinkedHashMap();
                propertys.put(m.get("ID") + "", mm);
            }
            if (pl != null)
                for (String mmm : pl) {
                    mm.put("@" + mmm, m.get("P_" + mmm.toUpperCase()) == null ? "" : m.get("P_" + mmm.toUpperCase()));
                }
            else {
                System.out.println(m.get("OTYPE"));
            }
            //mm.put("#text",m.get("P_TEXT")==null?"":m.get("P_TEXT"));

        }
        //System.out.println("33 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String, List> tree = new LinkedHashMap<String, List>();
        for (String key : treeM.keySet()) {
            LinkedHashMap m = treeM.get(key);
            List children = tree.get(m.get("PID"));
            if (children == null) {
                children = new ArrayList();
                tree.put((String) m.get("PID"), children);
            }
            children.add(treeM.get(key));
        }
        Map<String, Object> docStr = AppUtils.readSql22Map(treeM, tree, root, propertys);
        Document doc = null;
        String xmlstr = "";
       try{
            doc = XmlUtils.Map2Xml(docStr);
            xmlstr = XmlUtils.FormatXml(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }
    
  //用户补充指标入库
    public ResponseBody yhbczbRkTest(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        User user = UserUtils.getUser();
        int c = frameMapper.isAdmin(user.getId());
        System.out.println("管理员提交-------------"+user.getId());
        if (c > 0) {
            String textFromFile = "";
            BASE64Decoder decoder = new BASE64Decoder();
            try {
            	String textFromFile1 = new String(decoder.decodeBuffer((String) params.get("BCZBDATA")), "UTF-8");
                textFromFile = releaseCompression(textFromFile1);
                if (StringUtils.isBlank(textFromFile)) return rp;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Object> map = null;
            try {
                String dw = "1093";
                if (StringUtils.isNotBlank(user.getDeptCode())) dw = user.getDeptCode().substring(0, 4);
                map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
                String sql = AppUtils.readMap2Sql4(map, "-1", "", dw, (String) rq.getCpu(), user.getId(), "");
                HashMap h = new HashMap();

                h.put("sql", sql);

                bczbMapper.mergeProject4(h);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                String textFromFile = "";
                BASE64Decoder decoder = new BASE64Decoder();
                try {
                	textFromFile = FileUtils.readFileToString(new File("E:/c88663f6a9fc4c0493b31196a55650.dwbc"), "UTF-8");
                    //System.out.println(textFromFile);
                    if (StringUtils.isBlank(textFromFile)) return rp;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Map<String, Object> map = null;
                List<HashMap> adm = new ArrayList<HashMap>();
                try {
                    String dw = "1093";//默认临时组
                    if (StringUtils.isNotBlank(user.getDeptCode())) dw = user.getDeptCode().substring(0, 4);
                    adm = frameMapper.getAdminByCode(dw);
                    if (adm.size() == 0) {
                        rp.setIssuccess("0");
                        rp.setMessage("获取单位管理员列表失败！");
                        return rp;
                    }
                    map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
                    String sql = AppUtils.readMap2Sql3(map, "-1", "", dw, (String) rq.getCpu(), user.getId(), "");
                    HashMap h = new HashMap();
                    h.put("sql", sql);
                    bczbMapper.mergeProject3(h);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (map.get("GeneralInformation") == null) return rp;
                String[] bczbids = ((String) ((Map) map.get("GeneralInformation")).get("@BCZBIDS")).split(",");
                Document doc = DocumentHelper.createDocument();
                Element ls = DocumentHelper.createElement("bcList");
                ls.addAttribute("OilCode", (String) ((Map) map.get("GeneralInformation")).get("@OilCode"));
                //ls.addAttribute("OilCode", "8100");
                //ls.addAttribute("BBH", "3");
                doc.add(ls);
                String sss = "'" + UUID.randomUUID().toString() + "'";
                for (String dd : bczbids) {
                    sss += ",'" + dd + "'";

                }
                sss = "(" + sss + ")";
                List<HashMap> llll = bczbMapper.getZbByServerId(sss);
                for (HashMap dd : llll) {
                    Document n = getMapFromDb3((String) dd.get("ID"));
                    ls.add(n.getRootElement());
                }
                //bczbXfPath
                String ii = UUID.randomUUID().toString();
                String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + yhbczbPath + "/" + ii;
                String xml = "";
                try {
                    xml = XmlUtils.FormatXml(doc);
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FileUtils.writeToFile(saveDirectoryPath, xml, false);
                for (HashMap tm : adm) {
                    Map mail = new HashMap();
                    mail.put("FJRID", user.getId());
                    mail.put("FJRYHM", user.getLoginName());
                    mail.put("FJRXM", user.getUsername());
                    //mail.put("SJRID", "2BD695B466C2435BA3F6B6258AFBA49E");
                    mail.put("SJRID", tm.get("USERID"));
                    mail.put("ZT", "用户提交补充指标");
                    mail.put("LB", "3");
                    mail.put("PATH", "/" + ii);
                    mail.put("ID", UUID.randomUUID().toString());
                    emailMapper.sendMail(mail);
                }

            } catch (Exception e) {
                rp.setIssuccess("0");
                rp.setMessage("操作失败！");
                e.printStackTrace();
            }
        }
        return rp;
    } 
    
    
    
}

