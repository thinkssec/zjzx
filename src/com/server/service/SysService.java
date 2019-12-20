package com.server.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.common.annotation.RequestPermission;
import com.common.annotation.mapper.JsonMapper;
import com.common.config.Global;
import com.common.sys.entity.Office;
import com.common.sys.entity.Role;
import com.common.sys.entity.User;
import com.common.utils.AppUtils;
import com.common.utils.FileUtils;
import com.common.utils.StringUtils;
import com.common.utils.UserUtils;
import com.common.utils.XmlUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.server.Entity.Condition;
import com.server.Entity.Menu;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.mapper.BbglMapper;
import com.server.mapper.BczbMapper;
import com.server.mapper.EmailMapper;
import com.server.mapper.FrameMapper;
import com.server.mapper.JqsqMapper;
import com.server.mapper.PermisionMapper;
import com.server.mapper.SyncMapper;
import com.server.mapper.SysControlMapper2;
import com.server.mapper.UserMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Created by Administrator on 2017/10/18.
 */
@Service
@Component
public class SysService {
    protected Logger logger = LoggerFactory.getLogger(getClass());
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
    @Autowired
    FileService fileService;
    String bczbXfPath = Global.YSBCZBXF_BASE_URL;
    String yhbczbPath = Global.YHBCZBXF_BASE_URL;
    String dwbczbPath = Global.DWBCZBXF_BASE_URL;
    String dwbcelfPath = Global.DWBCELFXF_BASE_URL;
    String dwbcgcfPath = Global.DWBCGCFXF_BASE_URL;
    String dwbcsbzcPath = Global.DWBCSBZC_BASE_URL;

    //同步用户信息
    public ResponseBody authupduser(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "用户信息更新成功", id, rq.getTaskid());

        try {
            JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
            List<HashMap> h = JsonMapper.getInstance().fromJson(rq.getParams(), javaType);
            HashMap p = new HashMap();
            p.put("list", h);
            userMapper.updUserList(p);
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("用户更新失败");
        }
        return rp;
    }

    public ResponseBody authdeluser(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "用户信息删除成功", id, rq.getTaskid());
        try {
            JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
            List<HashMap> h = JsonMapper.getInstance().fromJson(rq.getParams(), javaType);
            HashMap p = new HashMap();
            p.put("list", h);
            userMapper.updUserList(p);
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("用户删除失败");
        }

        return rp;
    }

    public ResponseBody modifyUser(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "修改成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            condition.setC21(rq.getAttach());
            userMapper.modifyUser(condition);
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("修改失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //发送文件
    public ResponseBody milSend(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "邮件发送成功", id, rq.getTaskid());
        try {
            //params.put("PATH", rq.getAttach());
            String yjid = UUID.randomUUID().toString();
            params.put("ID", yjid);
            emailMapper.sendMail(params);
            Map m = new HashMap();
            m.put("ID", yjid);
            rp.setDatas(JsonMapper.toJsonString(m));
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("邮件发送失败");
        }
        return rp;
    }
    public ResponseBody milSendAttach(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "邮件发送成功", id, rq.getTaskid());
        try {
            String mid = (String) params.get("ID");
            String files = (String) params.get("FILES");
            JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, String.class);
            List<HashMap> h = JsonMapper.getInstance().fromJson(files, javaType);
            for (HashMap f : h) {
                byte[] b = ((String) f.get("BYTE")).getBytes();
                String n = (String) f.get("NAME");
                String filepath = fileService.upLoadAttatch(n, b);
                Map m = new HashMap();
                m.put("id", UUID.randomUUID().toString());
                m.put("eid", mid);
                m.put("path", filepath);
                emailMapper.sendAttach(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("邮件发送失败");
        }
        return rp;
    }

    public ResponseBody milGetAttach(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取邮件附件信息成功！", id, rq.getTaskid());
        try {
            List<HashMap> all = emailMapper.getMail1Attach(params);
            List<Map> ls = new ArrayList();
            for (HashMap el : all) {
                String apath = (String) el.get("PATH");
                String lb = (String) params.get("LB");
                String fullPath = "";
                if ("2".equals(lb) || "9".equals(lb)) {
                    fullPath = Global.getConfig("upLoadPath") + Global.YSBCZBXF_BASE_URL + apath;
                } else if ("3".equals(lb) || "8".equals(lb)) {
                    fullPath = Global.getConfig("upLoadPath") + Global.YHBCZBXF_BASE_URL + apath;
                } else {
                    fullPath = Global.getUserfilesBaseDir() + apath;
                }
                try {
                    /*File file = new File(fullPath);
                    String fileStr = FileCopyUtils.copyToString(new FileReader(file));*/
                    String fileStr =new String (FileUtils.getBytes(fullPath),"UTF-8");
                    fileStr = compress(fileStr);
                    Map mmmm=new HashMap();
                    mmmm.put("YJID",(String) params.get("ID"));
                    mmmm.put("FJMC",apath.replaceAll("/",""));
                    mmmm.put("FJL",fileStr);
                    ls.add(mmmm);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            rp.setDatas(JsonMapper.toJsonString(ls));
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("获取邮件附件信息失败!");
        }
        return rp;
    }

    public ResponseBody milReply(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "邮件发送成功", id, rq.getTaskid());
        try {
            params.put("PATH", rq.getAttach());
            emailMapper.replyMail(params);
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("邮件发送失败");
        }
        return rp;
    }

    public ResponseBody milGetMlist(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取邮件列表成功", id, rq.getTaskid());
        User u = UserUtils.getUser();
        params.put("USERID", u.getId());
        try {
            List<String> el = emailMapper.getMailList2(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取邮件列表失败");
        }
        return rp;
    }

    public ResponseBody milGetUlist(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取收件箱成功", id, rq.getTaskid());
        params.put("USERNAME", rq.getUsername());
        try {
            List<HashMap> el = emailMapper.getMailUList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取收件箱失败");
        }
        return rp;
    }

    public ResponseBody milGetDetail(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取邮件内容成功！", id, rq.getTaskid());
        User user = UserUtils.getUser();
        params.put("USERID", user.getId());
        try {
            List<HashMap> el = emailMapper.getMailList(params);
            if (el.size() > 0)
                rp.setDatas(JsonMapper.toJsonString(el.get(0)));
            else
                rp.setDatas(JsonMapper.toJsonString(null));
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取邮件内容失败!");
        }
        return rp;
    }

    public ResponseBody milGetAttachList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取邮件附件信息成功！", id, rq.getTaskid());
        try {
            List<HashMap> el = emailMapper.getMailAList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取邮件附件信息失败!");
        }
        return rp;
    }

    public ResponseBody milDelete(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "邮件删除成功！", id, rq.getTaskid());
        try {
            emailMapper.delete(params);
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("邮件删除失败!");
        }
        return rp;
    }

    public ResponseBody milRead(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "邮件已读标记成功！", id, rq.getTaskid());
        try {
            emailMapper.readed(params);
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("邮件已读标记失败");
        }
        return rp;
    }

    public ResponseBody panMCreate(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            //UserUtils.getPrincipal2();
            params.put("USERNAME", rq.getUsername());
            frameMapper.panMCreate(params);
            HashMap u = userMapper.getUserOne(rq.getUsername());
            params.put("USERID", u.get("USERID"));
            List<HashMap> el = frameMapper.getPanMList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }

    public ResponseBody panMUpdate(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            frameMapper.panMUpdate(params);
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }

    public ResponseBody panMDel(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            frameMapper.panMDelete(params);
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }

    public ResponseBody panMList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取列表成功", id, rq.getTaskid());
        HashMap u = userMapper.getUserOne(rq.getUsername());
        params.put("USERID", u.get("USERID"));
        try {
            List<HashMap> el = frameMapper.getPanMList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("获取列表失败");
        }
        return rp;
    }

    public ResponseBody panMUpload(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            params.put("PATH", rq.getAttach());
            frameMapper.panMUpload(params);
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }

    public ResponseBody panMXmList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取列表成功", id, rq.getTaskid());
        try {
            HashMap u = userMapper.getUserOne(rq.getUsername());
            params.put("USERID", u.get("USERID"));
            List<HashMap> el = frameMapper.getPanMXmList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("获取列表失败");
        }
        return rp;
    }

    public ResponseBody panMXmDel(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            frameMapper.panMXmDelete(params);
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }

    public ResponseBody panDHasEditP(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            //UserUtils.getPrincipal2();
            User u = UserUtils.getUser();
            if (!u.hasPermission("dw:dept:edit")) {
                rp.setIssuccess("0");
                rp.setMessage("权限不足");
            }
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }

    public ResponseBody panDCreate(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            //UserUtils.getPrincipal2();
            User u = UserUtils.getUser();
            frameMapper.panDCreate(params);
            params.put("RT", u.getTopDeptid());
            List<HashMap> el = frameMapper.getPanDList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }

    public ResponseBody panDUpdate(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            frameMapper.panDCreate(params);
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }

    public ResponseBody panDDel(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            frameMapper.panDDelete(params);
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }

    public ResponseBody panDList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取列表成功", id, rq.getTaskid());
        try {
            User u = UserUtils.getUser();
            params.put("RT", u.getTopDeptid());
            List<HashMap> el = frameMapper.getPanDList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("获取列表失败");
        }
        return rp;
    }

    public ResponseBody panDUpload(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            params.put("PATH", rq.getAttach());
            frameMapper.panDUpload(params);
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }

    public ResponseBody panDXmList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取列表成功", id, rq.getTaskid());
        try {
            User u = UserUtils.getUser();
            params.put("DEPTID", u.getDeptId());
            params.put("RT", u.getTopDeptid());
            List<HashMap> el = frameMapper.getPanDXmList(params);
            rp.setDatas(JsonMapper.toJsonString(el));
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("获取列表失败");
        }
        return rp;
    }

    public ResponseBody panDXmDel(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            frameMapper.panMXmDelete(params);
        } catch (Exception e) {
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("操作失败");
        }
        return rp;
    }

    public ResponseBody authgetuser(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取用户列表成功", id, rq.getTaskid());
        try {
            List<HashMap> ul = userMapper.getUserList(params);
            rp.setDatas(JsonMapper.toJsonString(ul));
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取用户列表失败！");
            e.printStackTrace();
        }
        return rp;
    }

    //过时
    public ResponseBody sycndata(RequestBody rq, Map params, String id) {
        return syncService.sycndata(rq, params, id);
    }

    public ResponseBody pancreateDic(RequestBody rq, Map params, String id) {
        return panService.createDirectory(rq, params, id);
    }

    public ResponseBody pancreateFile(RequestBody rq, Map params, String id) {
        return panService.createFile(rq, params, id);
    }

    public ResponseBody getJqsq(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> bbls = jqsqMapper.getJqsq(condition);
                condition.setStart(null);
                HashMap datas = new HashMap();
                datas.put("rows", bbls);
                datas.put("total", jqsqMapper.getJqsq(condition).size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getRysq(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> bbls = jqsqMapper.getRysq(condition);
                condition.setStart(null);
                HashMap datas = new HashMap();
                datas.put("rows", bbls);
                datas.put("total", jqsqMapper.getRysq(condition).size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody jqsqwpzList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> bbls = jqsqMapper.getJqsqwpz(condition);
                condition.setStart(null);
                HashMap datas = new HashMap();
                datas.put("rows", bbls);
                datas.put("total", jqsqMapper.getJqsqwpz(condition).size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody rysqwpzList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> bbls = jqsqMapper.getRysqwpz(condition);
                condition.setStart(null);
                HashMap datas = new HashMap();
                datas.put("rows", bbls);
                datas.put("total", jqsqMapper.getRysqwpz(condition).size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody jqdeptlist(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            //System.out.println(rq.getParams());
            try {
                List<HashMap> bbls = jqsqMapper.getJqdept(condition);
                condition.setStart(null);
                HashMap datas = new HashMap();
                datas.put("rows", bbls);
                datas.put("total", jqsqMapper.getJqdept(condition).size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody ourjqList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            //System.out.println(rq.getParams());
            try {
                List<HashMap> bbls = jqsqMapper.getOurJq(condition);
                condition.setStart(null);
                HashMap datas = new HashMap();
                datas.put("rows", bbls);
                datas.put("total", jqsqMapper.getOurJq(condition).size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody jqsqpzList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            //System.out.println(rq.getParams());
            try {
                List<HashMap> bbls = jqsqMapper.getJqsqpz(condition);
                condition.setStart(null);
                HashMap datas = new HashMap();
                datas.put("rows", bbls);
                datas.put("total", jqsqMapper.getJqsqpz(condition).size());
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(datas));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody jqsqty(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                for(int i = 0; i < h.size(); i++){
                    }
                jqsqMapper.saveJqsqty(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody rysqty(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                jqsqMapper.saveRysqty(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody savejqsqpz(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                //String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                String updL = condition.getC6();
                String HPDWMC = condition.getC1();
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                if(h.size()==0){
                	HashMap map = new HashMap();
                	if(condition.getC4().equals("del")){
                		map.put("HPDWMC", null);
                        map.put("HPDW", null);
                        map.put("HPYQSJ", null);
                        map.put("HPYQSJMC", null);
                	}if(condition.getC4().equals("save")){
                		map.put("HPDWMC", HPDWMC);
                        map.put("HPDW", condition.getC2());
                        map.put("HPYQSJ", "8200,8300,8400,8100,8500,8600,8900,9000,9100");
                        map.put("HPYQSJMC", "中原,河南,江汉,胜利,江苏,西北,华北,西南,长输");
                	}
                	map.put("CPUID", condition.getC3());
                    h.add(map);
                }
                HashMap m = new HashMap();
                m.put("list", h);
                jqsqMapper.saveJqsqpz(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody jqsqbty(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                jqsqMapper.saveJqsqbty(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody rysqbty(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                jqsqMapper.saveRysqbty(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody jqsqdel(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                jqsqMapper.saveJqsqdel(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody rysqdel(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                jqsqMapper.saveRysqdel(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getDw(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> lsHt = jqsqMapper.getDw(condition);
                String jdata = JsonMapper.getInstance().toJson(lsHt);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getDw2(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> lsHt = jqsqMapper.getDw2(condition);
                String jdata = JsonMapper.getInstance().toJson(lsHt);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getDw3(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                //System.out.println("-------------" + condition.getDwdm());
                List<HashMap> lsHt = jqsqMapper.getDw3(condition);
                String jdata = JsonMapper.getInstance().toJson(lsHt);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getDw4(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                //System.out.println("-------------" + condition.getDwdm());
                List<HashMap> lsHt = jqsqMapper.getDw4(condition);
                String jdata = JsonMapper.getInstance().toJson(lsHt);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getOfficeById(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            try {
                Office lsHt = frameMapper.getOfficeById(office);
                List<Menu> ls = frameMapper.getOfficeMenuById(office);
                lsHt.setMenuList(ls);
                String jdata = JsonMapper.getInstance().toJson(lsHt);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getMypanById(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            try {
                Office lsHt = frameMapper.getMypanById(office);
                String jdata = JsonMapper.getInstance().toJson(lsHt);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getYq(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> lsHt = jqsqMapper.getYq(condition);
                String jdata = JsonMapper.getInstance().toJson(lsHt);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody getbb(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> lsHt = jqsqMapper.getbb(condition);
                String jdata = JsonMapper.getInstance().toJson(lsHt);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody deptLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> lsHt = permisionMapper.getDeptList(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", permisionMapper.getDeptList(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //获得用户组
    public ResponseBody officeLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            try {
                List<Office> lsHt = frameMapper.getOfficeList(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //获得角色
    public ResponseBody officeRLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            try {
                List<Office> lsHt = frameMapper.getOfficeRList(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //获得单位组列表
    @RequestPermission(value = {"dw:dept:edit"})
    public ResponseBody officeDLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            try {
                //System.out.println("dwdm------------------------"+office.getDwdm());
                List<Office> lsHt = frameMapper.getOfficeDList(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //获得单位目录
    public ResponseBody officeMLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            try {
                List<Office> lsHt = frameMapper.getOfficeMList(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //获得个人目录
    public ResponseBody officeMyLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            office.setId(UserUtils.getUser().getId());
            try {
                List<Office> lsHt = frameMapper.getOfficeMyList(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //个人空间项目管理
    public ResponseBody xmglLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            office.setId(UserUtils.getUser().getId());
            try {
                List<Office> lsHt = frameMapper.xmglLists(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //协同项目管理
    public ResponseBody xtxmglLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            office.setId(UserUtils.getUser().getId());
            try {
                List<Office> lsHt = frameMapper.xtxmglLists(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //单位空间项目管理
    public ResponseBody xmglListsD(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            User user = UserUtils.getUser();
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            office.setId(user.getTopDeptid());
            office.setDwdm(user.getDeptId());
            try {
                List<Office> lsHt = frameMapper.xmglListsD(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //获取单位补充二类费目录
    public ResponseBody bcelfMlLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("CODE", user.getDeptCode().substring(0, 4));

            try {
                HashMap<String, Office> lsHt = frameMapper.bcelfMlLists(pa);
                HashMap<String, List<Office>> children = new HashMap();
                List<HashMap> bczbList = bczbMapper.getDwElfByDw(pa);
                for (HashMap hh : bczbList) {
                    String mt = (String) hh.get("ZHZBID");
                    Office ppp = lsHt.get(mt);
                    ppp.setAnyone1((String) hh.get("TITLE"));
                }
                String rootId = "";
                for (String key : lsHt.keySet()) {
                    if (lsHt.get(key).getCode().length() == 8) rootId = key;
                    List c = children.get(lsHt.get(key).getParentId());
                    if (c == null) {
                        c = new ArrayList<Office>();
                        children.put((String) lsHt.get(key).getParentId(), c);
                    }
                    c.add(lsHt.get(key));
                }
                if (StringUtils.isBlank(rootId)) {
                    frameMapper.insertElfRoot(user.getDeptCode(), user.getDeptId());
                    lsHt = frameMapper.bcelfMlLists(pa);
                    children = new HashMap();
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                }
                //System.out.println("-------------"+children);
                HashMap m = toTree(lsHt, children, rootId);
                List mm = new ArrayList();
                mm.add(m);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(mm));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //获取单位工程费目录
    public ResponseBody bcgcfMlLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("CODE", user.getDeptCode().substring(0, 4));

            try {
                HashMap<String, Office> lsHt = frameMapper.bcgcfMlLists(pa);
                HashMap<String, List<Office>> children = new HashMap();
                List<HashMap> bczbList = bczbMapper.getDwGcfByDw(pa);
                for (HashMap hh : bczbList) {
                    String mt = (String) hh.get("ZHZBID");
                    Office ppp = lsHt.get(mt);
                    ppp.setAnyone1((String) hh.get("TITLE"));
                }
                String rootId = "";
                for (String key : lsHt.keySet()) {
                    if (lsHt.get(key).getCode().length() == 8) rootId = key;
                    List c = children.get(lsHt.get(key).getParentId());
                    if (c == null) {
                        c = new ArrayList<Office>();
                        children.put((String) lsHt.get(key).getParentId(), c);
                    }
                    c.add(lsHt.get(key));
                }
                if (StringUtils.isBlank(rootId)) {
                    frameMapper.insertGcfRoot(user.getDeptCode(), user.getDeptId());
                    lsHt = frameMapper.bcgcfMlLists(pa);
                    children = new HashMap();
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                }
                //System.out.println("-------------"+children);
                HashMap m = toTree(lsHt, children, rootId);
                List mm = new ArrayList();
                mm.add(m);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(mm));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //获取单位清单目录
    public ResponseBody bcqdMlLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("CODE", user.getDeptCode().substring(0, 4));

            try {
                HashMap<String, Office> lsHt = frameMapper.bcqdMlLists(pa);
                HashMap<String, List<Office>> children = new HashMap();
                List<HashMap> bczbList = bczbMapper.getDwQdByDw(pa);
                for (HashMap hh : bczbList) {
                    String mt = (String) hh.get("ZHZBID");
                    Office ppp = lsHt.get(mt);
                    ppp.setAnyone1((String) hh.get("TITLE"));
                }
                String rootId = "";
                for (String key : lsHt.keySet()) {
                    if (lsHt.get(key).getCode().length() == 8) rootId = key;
                    List c = children.get(lsHt.get(key).getParentId());
                    if (c == null) {
                        c = new ArrayList<Office>();
                        children.put((String) lsHt.get(key).getParentId(), c);
                    }
                    c.add(lsHt.get(key));
                }
                if (StringUtils.isBlank(rootId)) {
                    frameMapper.insertQdRoot(user.getDeptCode(), user.getDeptId());
                    lsHt = frameMapper.bcqdMlLists(pa);
                    children = new HashMap();
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                }
                //System.out.println("-------------"+children);
                HashMap m = toTree(lsHt, children, rootId);
                List mm = new ArrayList();
                mm.add(m);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(mm));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //获取单位补充指标目录
    public ResponseBody bczbMlLists_bak(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("CODE", user.getDeptCode().substring(0, 4));

            try {
                HashMap<String, Office> lsHt = frameMapper.bczbMlLists(pa);
                HashMap<String, List<Office>> children = new HashMap();
                List<HashMap> bczbList = bczbMapper.getDwBczbByDw(pa);
                for (HashMap hh : bczbList) {
                    String mt = (String) hh.get("ZHZBID");
                    Office ppp = lsHt.get(mt);
                    if (ppp == null) continue;
                    ppp.getOtherproperty().put("O" + (String) hh.get("OILID"), (String) hh.get("ZHDJ"));
                }
                String rootId = "";
                //System.out.println("========"+lsHt);
                for (String key : lsHt.keySet()) {
                    if (lsHt.get(key).getCode().length() == 8) rootId = key;
                    List c = children.get(lsHt.get(key).getParentId());
                    if (c == null) {
                        c = new ArrayList<Office>();
                        children.put((String) lsHt.get(key).getParentId(), c);
                    }
                    c.add(lsHt.get(key));
                }
                if (StringUtils.isBlank(rootId)) {
                    frameMapper.insertBczbRoot(user.getDeptCode(), user.getDeptId());
                    lsHt = frameMapper.bczbMlLists(pa);
                    children = new HashMap();
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                }
                //System.out.println("-------------"+children);
                HashMap m = toTree(lsHt, children, rootId);
                List mm = new ArrayList();
                mm.add(m);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(mm));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    //获取单位补充指标目录
    public ResponseBody bczbMlLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("CODE", user.getDeptCode().substring(0, 4));

            try {
                HashMap<String, Office> lsHt = frameMapper.bczbMlLists(pa);
                HashMap<String, List<Office>> children = new HashMap();
				
				 List<HashMap> bczbList = bczbMapper.getDwBczbByDw(pa); for (HashMap hh :
				 bczbList) { String mt = (String) hh.get("ZHZBID"); Office ppp = lsHt.get(mt);
				 if (ppp == null) continue; ppp.getOtherproperty().put("O" + (String)
				 hh.get("OILID"), (String) hh.get("ZHDJ")); }
				 
                String rootId = "";
                for (String key : lsHt.keySet()) {
                    if (lsHt.get(key).getCode().length() == 8) rootId = key;
                    List c = children.get(lsHt.get(key).getParentId());
                    if (c == null) {
                        c = new ArrayList<Office>();
                        children.put((String) lsHt.get(key).getParentId(), c);
                    }
                    c.add(lsHt.get(key));
                }
                if (StringUtils.isBlank(rootId)) {
                    frameMapper.insertBczbRoot(user.getDeptCode(), user.getDeptId());
                    lsHt = frameMapper.bczbMlLists(pa);
                    children = new HashMap();
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                }
                HashMap m = toTree(lsHt, children, rootId);
                List mContainsMlList = (List)m.get("children");
                orderDwbczbTree(m,mContainsMlList);

                List mm = new ArrayList();
                mm.add(m);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(mm));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    
    /**
     * 单位补充指标的树排序
     * */
    public void orderDwbczbTree(Map mlMap, List mlMapContainsList) {
    	if(mlMapContainsList != null && mlMapContainsList.size() > 0) {
			List newOrderMlList = orderDwbczbMl(mlMapContainsList);
			mlMap.put("children", newOrderMlList);
        	for (int i = 0; i < mlMapContainsList.size(); i++) {
        		HashMap nextMl = (HashMap)mlMapContainsList.get(i);//3级目录
        		List nextMlList = (List)nextMl.get("children");//3级目录包含的多个指标
        		orderDwbczbTree(nextMl,nextMlList);	
    		}
    	}
    }
    
    /**
     * 对List根据Code排序，
     * 生成“根据code有序的” list
     * */
    public List orderDwbczbMl(List mlDataList) {
    	List newMlList = new ArrayList<>();
		List<String> codeList = new ArrayList<String>();
		if(mlDataList != null && mlDataList.size() > 0) {
			for (int i = 0; i < mlDataList.size(); i++) {
				HashMap mlMap = (HashMap)mlDataList.get(i);
				String code = String.valueOf(mlMap.get("code"));
				codeList.add(code);
			}
			Collections.sort(codeList); 
			
			for (int j = 0; j < codeList.size(); j++) {
				String orderCode = codeList.get(j);
				for (int k = 0; k < mlDataList.size(); k++) {
					HashMap mlMap = (HashMap)mlDataList.get(k);
    				String code = String.valueOf(mlMap.get("code"));
    				if(orderCode.equals(code)) {
    					newMlList.add(mlMap);
    				}
				}
			}
		}
		return newMlList;
    }
    
    
    //获取单位补充指标目录
    public ResponseBody vsbzcMlLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("CODE", user.getDeptCode().substring(0, 4));
            pa.put("BBH", condition.getC11());
            try {
                HashMap<String, Office> lsHt = frameMapper.bcsbzcAMlLists(pa);
                HashMap<String, List<Office>> children = new HashMap();
                List<HashMap> bczbList = bczbMapper.getBcsbzcByDw(pa);
                for (HashMap hh : bczbList) {
                    String mt = (String) hh.get("ZHID");
                    Office ppp = lsHt.get(mt);
                    if (ppp == null) continue;
                    //ppp.getOtherproperty().put("O" + (String) hh.get("OILID"), (String) hh.get("CCJG"));//
                    ppp.getOtherproperty().put("O" + (String) hh.get("OILID"), (String) hh.get("GYJG"));
                }
                String rootId = "";
                for (String key : lsHt.keySet()) {
                    if (lsHt.get(key).getCode().length() == 8) rootId = key;
                    List c = children.get(lsHt.get(key).getParentId());
                    if (c == null) {
                        c = new ArrayList<Office>();
                        children.put((String) lsHt.get(key).getParentId(), c);
                    }
                    c.add(lsHt.get(key));
                }
                if (StringUtils.isBlank(rootId)) {
                    frameMapper.insertBcsbzcRoot(user.getDeptCode(), user.getDeptId(), condition.getC11());
                    lsHt = frameMapper.bcsbzcAMlLists(pa);
                    children = new HashMap();
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                }
                HashMap m = toTree2(lsHt, children, rootId);
                
                List mm = new ArrayList();
                mm.add(m);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(mm));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody xtxmMlLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("id", user.getId());
            try {
                HashMap<String, Office> lsHt = frameMapper.xtxmMlLists(pa);
                HashMap<String, List<Office>> children = new HashMap();
                String rootId = user.getId();
                for (String key : lsHt.keySet()) {
                    List c = children.get(lsHt.get(key).getParentId());
                    if (c == null) {
                        c = new ArrayList<Office>();
                        children.put((String) lsHt.get(key).getParentId(), c);
                    }
                    c.add(lsHt.get(key));
                }

                HashMap m = toTree3(lsHt, children, rootId);
                List mm = new ArrayList();
                mm.add(m);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(mm));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody xtxmMlLists2(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            Map pa = new HashMap();
            pa.put("id", condition.getC1());
            //System.out.println("id-----------"+condition.getC1());
            try {
                HashMap<String, Office> lsHt = frameMapper.xtxmMlLists2(pa);
                HashMap<String, List<Office>> children = new HashMap();
                String rootId = condition.getC1();
                for (String key : lsHt.keySet()) {
                    List c = children.get(lsHt.get(key).getParentId());
                    if (c == null) {
                        c = new ArrayList<Office>();
                        children.put((String) lsHt.get(key).getParentId(), c);
                    }
                    c.add(lsHt.get(key));
                }
                HashMap m = toTree3(lsHt, children, rootId);
                List mm = new ArrayList();
                mm.add(m);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(mm));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //获取单位补充设备主材目录
    public ResponseBody bcsbzcMlLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("CODE", user.getDeptCode().substring(0, 4));
            pa.put("BBH", condition.getC11());
            try {
                HashMap<String, Office> lsHt = frameMapper.bcsbzcMlLists(pa);
                HashMap<String, List<Office>> children = new HashMap();
                String rootId = "";
                //System.out.println("========"+lsHt);
                for (String key : lsHt.keySet()) {
                    if (lsHt.get(key).getCode().length() == 8) rootId = key;
                    List c = children.get(lsHt.get(key).getParentId());
                    if (c == null) {
                        c = new ArrayList<Office>();
                        children.put((String) lsHt.get(key).getParentId(), c);
                    }
                    c.add(lsHt.get(key));
                }
                if (StringUtils.isBlank(rootId)) {
                    frameMapper.insertBcsbzcRoot(user.getDeptCode(), user.getDeptId(), condition.getC11());
                    lsHt = frameMapper.bcsbzcMlLists(pa);
                    children = new HashMap();
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                }
                //System.out.println("-------------"+children);
                HashMap m = toTree2(lsHt, children, rootId);
                List mm = new ArrayList();
                mm.add(m);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(mm));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
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
        h.put("rksj", m.getCreateDate());
        h.put("bz", m.getRemarks());
        h.put("type", m.getType());
        h.put("grade", m.getGrade());
        //System.out.println("----------------"+m.getOtherproperty());
        h.putAll(m.getOtherproperty());
        if ("9".equals(m.getType())) {
            h.put("iconCls", "icon-list-alt");
        } else {
            //h.put("iconCls","icon-folder-open");
        }
        List c = null;
        c = new ArrayList();
        h.put("children", c);
        List<Office> cd = children.get(rootId);
        if (cd != null) {
            for (Office i : cd) {
                c.add(toTree(dic, children, (String) i.getId()));
            }
        } else {
        }

        return h;
    }

    public HashMap toTree2(HashMap<String, Office> dic, HashMap<String, List<Office>> children, String rootId) {
        HashMap h = new HashMap();
        Office m = dic.get(rootId);
        h.put("id", m.getId());
        h.put("code", m.getCode().substring(8));
        h.put("text", m.getName());
        h.put("rksj", m.getCreateDate());
        h.put("bz", m.getRemarks());
        h.put("lb", m.getMaster());
        h.put("dw", m.getPhone());
        h.put("pp", m.getFax());
        h.put("xh", m.getEmail());
        h.put("mc", m.getRemarks());
        h.put("type", m.getType());
        //System.out.println("----------------"+m.getOtherproperty());
        h.putAll(m.getOtherproperty());
        if ("10".equals(m.getType())) {
            h.put("iconCls", "icon-list-alt");
        } else {
            //h.put("iconCls","icon-folder-open");
        }
        List c = null;
        c = new ArrayList();
        h.put("children", c);
        List<Office> cd = children.get(rootId);
        if (cd != null) {
            for (Office i : cd) {
                c.add(toTree2(dic, children, (String) i.getId()));
            }
        } else {
        }

        return h;
    }

    public HashMap toTree3(HashMap<String, Office> dic, HashMap<String, List<Office>> children, String rootId) {
        HashMap h = new HashMap();
        Office m = dic.get(rootId);
        if (m == null) return h;
        h.put("id", m.getId());
        h.put("text", m.getName());
        h.put("rksj", m.getCreateDate());
        h.put("bz", m.getRemarks());
        h.put("type", m.getType());
        h.put("fzr", m.getMaster());
        //System.out.println("----------------"+m.getOtherproperty());
        h.putAll(m.getOtherproperty());
        if ("5".equals(m.getType())) {
            h.put("iconCls", "icon-list-alt");
        } else {
            //h.put("iconCls","icon-folder-open");
        }
        List c = null;
        c = new ArrayList();
        h.put("children", c);
        List<Office> cd = children.get(rootId);
        if (cd != null) {
            for (Office i : cd) {
                c.add(toTree3(dic, children, (String) i.getId()));
            }
        } else {
        }

        return h;
    }

    //获取能够添加下级目录的目录
    public ResponseBody officeMLists2(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            try {
                List<Office> lsHt = frameMapper.getOfficeMList2(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody officeMmyLists2(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            try {
                List<Office> lsHt = frameMapper.getOfficeMmyList2(office);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody userLists(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> lsHt = permisionMapper.getUserList(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", permisionMapper.getUserList(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //获取单位管理员的用户列表
    public ResponseBody userListsD(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                //System.out.println(condition.getUserid() + "----------dwdm---" + condition.getDwdm());
                List<HashMap> lsHt = permisionMapper.getUserListD(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", permisionMapper.getUserListD(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody ouruserList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                //System.out.println(condition.getUserid() + "----------dwdm---" + condition.getDwdm());
                List<HashMap> lsHt = permisionMapper.getourUserList(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", permisionMapper.getourUserList(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody userLists2(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> lsHt = permisionMapper.getUserList2(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                //System.out.println("==================" + condition.getId());
                r.put("total", permisionMapper.getUserList2(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody saveDept(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                //String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                String updL = condition.getC6();
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                permisionMapper.saveDeptList(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody deleteDept(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                permisionMapper.deteleDept(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody saveUserList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                //String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                String updL = condition.getC6();
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                //System.out.println("updL-------------" + updL);
                HashMap m = new HashMap();
                m.put("list", h);
                permisionMapper.saveUserList(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody saveUserListD(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
       User user = UserUtils.getUser();
       String deptid = user.getDeptCode().substring(0, 4);
        String strdep = "\"DEPTID\":\""+deptid+"\"";
       
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                //String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                String updL = condition.getC6();
                String insertOrDelete = condition.getC1();
                
                updL = updL.replace("}]", "," + strdep + "}]");
               /* updL+=deptid;*/
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                List<HashMap> resultList = new ArrayList<HashMap>();
                List<HashMap> lsHt = jqsqMapper.getDw3(condition);
                String fpzCode = "";
                for (int i = 0; i < lsHt.size(); i++) {
                	HashMap fpzMap = lsHt.get(i);
                	String fpzName = (String)fpzMap.get("NAME");
                	if("全部用户组".equals(fpzName)) {
                		fpzCode = (String)fpzMap.get("CODE");
                	}
				}
                if(h != null) {
	                for(int i = 0 ; i < h.size(); i++) {
	                	HashMap hashMap = h.get(i);
	                	if("insert".equals(insertOrDelete)) {
	                		hashMap.put("ADMIN", fpzCode);
	                		hashMap.put("ADMINMC", "全部用户组");
	                	}else if("delete".equals(insertOrDelete)) {
	                		hashMap.put("ADMIN", "");
	                		hashMap.put("ADMINMC", "");
	                	}
	                	resultList.add(hashMap);
	                }
                }
                
                /*h.add(pa);*/
                HashMap m = new HashMap();
                m.put("list", resultList);
                permisionMapper.saveUserListD(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody deleteUserList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                permisionMapper.delUserlist(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody qyUserList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                permisionMapper.qyUserlist(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody tyUserList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                permisionMapper.tyUserlist(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody assignUser(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                m.put("id", condition.getId());
                permisionMapper.assignUser(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody assignUserGly(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);	
                m.put("id", condition.getId());
                permisionMapper.assignUserGly(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody jqsqtingy(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                jqsqMapper.saveJqsqtingy(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody jqsqzhux(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                jqsqMapper.saveJqsqzhux(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody jqsqqiy(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                jqsqMapper.saveJqsqqiy(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody xtbbList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> lsHt = bbglMapper.getBbxtlist(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", bbglMapper.getBbxtlist(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody bbxtsave(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                m.put("submitter", UserUtils.getUser().getId());
                bbglMapper.saveBbxt(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody bbxtxf(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                bbglMapper.xfBbxt(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody bbsjxf(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                bbglMapper.xfBbsj(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody xtbbdel(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                bbglMapper.delBbxt(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody sjbbdel(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                bbglMapper.delBbsj(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody sjbbList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> lsHt = bbglMapper.getBbsjlist(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", bbglMapper.getBbsjlist(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody bbsjsave(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                bbglMapper.saveBbsj(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getSjtype(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> zblist = bbglMapper.getSjtypeList(null);
                String zbstr = JsonMapper.getInstance().toJson(zblist);
                rp.setDatas(zbstr);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getXtSjInfo(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取系统更新信息成功", id, rq.getTaskid());
        try {
            try {
                List<HashMap> infos = bbglMapper.getXtSjInfo(params);
                for (HashMap info : infos) {
                    String path = Global.getUserfilesBaseDir()
                            + info.get("SUBMITTER") + "/" + info.get("ID") + "/";
                    String doc = Global.SOFTWAREFILES;
                    List<String> ul = new ArrayList<String>();
                    traverseFolder2(ul, path, doc);
                    info.put("filelist", ul);
                }
                String zbstr = JsonMapper.getInstance().toJson(infos);
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

    public ResponseBody getMenuList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            try {
                //System.out.println("username   1111===="+UserUtils.getUser().getLoginName());
                List<Menu> mmmm = null;
                ObjectMapper objectMapper = new ObjectMapper();
                Menu menu = new Menu();
                try {
                    menu = objectMapper.readValue(rq.getParams(), Menu.class);
                } catch (Exception e) {
                    //e.printStackTrace();

                }
                /*if("test".equals(UserUtils.getUser().getLoginName()))
                      mmmm=frameMapper.findAllList(new Menu());
                else{
                    Menu m = new Menu();
                    m.setUserId(UserUtils.getUser().getId());
                    mmmm = frameMapper.findByUserId(m);
                }*/
                mmmm = frameMapper.findAllList(menu);
                String jdata = JsonMapper.getInstance().toJson(mmmm);
                //System.out.println("==========="+mmmm.size());
                //System.out.println("==========="+jdata);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getMenuById(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                Menu mmmm = frameMapper.findMenuById(condition.getId());
                String jdata = JsonMapper.getInstance().toJson(mmmm);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody deleteMenu(RequestBody rq, Map params, String id) {
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Menu menu = objectMapper.readValue(rq.getParams(), Menu.class);
            //System.out.println("--------------------------menu="+menu);
            try {
                frameMapper.deleteMenu(menu);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody updateMenuSort(RequestBody rq, Map params, String id) {
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, Menu.class);
            List<Menu> h = JsonMapper.getInstance().fromJson(rq.getParams(), javaType);
            //System.out.println("--------------------------menu="+menu);
            HashMap hhh = new HashMap();
            try {
                hhh.put("list", h);
                frameMapper.updateMenuSort(hhh);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody saveMenu(RequestBody rq, Map params, String id) {
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Menu menu = objectMapper.readValue(rq.getParams(), Menu.class);
            //System.out.println("--------------------------menu="+menu);
            try {
                Menu pm = frameMapper.findMenuById(menu.getParentId());
                menu.setParent(pm);
                // 获取修改前的parentIds，用于更新子节点的parentIds
                String oldParentIds = menu.getParentIds();
                // 设置新的父节点串
                menu.setParentIds(menu.getParent().getParentIds() + menu.getParent().getId() + ",");
                // 保存或更新实体
                if (StringUtils.isBlank(menu.getId())) {
                    menu.preInsert();
                    frameMapper.insertMenu(menu);
                } else {
                    menu.preUpdate();
                    frameMapper.updateMenu(menu);
                }
                // 更新子节点 parentIds
                Menu m = new Menu();
                m.setParentIds("%," + menu.getId() + ",%");
                List<Menu> list = frameMapper.findByParentIdsLike(m);
                for (Menu e : list) {
                    e.setParentIds(e.getParentIds().replace(oldParentIds, menu.getParentIds()));
                    frameMapper.updateParentIds(e);
                }
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("保存失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody findAllRole(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            try {
                List<Role> mmmm = null;
                mmmm = frameMapper.findAllRole(new Role());
                String jdata = JsonMapper.getInstance().toJson(mmmm);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getRoleById(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                //System.out.println("condition.getId()  "+condition.getId());
                Role mmmm = frameMapper.findRoleById(condition.getId());
                //System.out.println("mmmm  "+mmmm);
                String jdata = JsonMapper.getInstance().toJson(mmmm);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getRoleByName(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                Role mmmm = frameMapper.getRoleByName(condition.getId());
                String jdata = JsonMapper.getInstance().toJson(mmmm);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody saveRole(RequestBody rq, Map params, String id) {
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap m = objectMapper.readValue(rq.getParams(), HashMap.class);
            String rstr = (String) m.get("role");
            String mstr = (String) m.get("menuList");
            Role role = objectMapper.readValue(rstr, Role.class);

            JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, Menu.class);
            List<Menu> menuList = JsonMapper.getInstance().fromJson(mstr, javaType);
            role.setMenuList(menuList);
            //System.out.println("--------------------------menu="+menu);
            try {
                if (StringUtils.isBlank(role.getId())) {
                    role.preInsert();
                    frameMapper.insertRole(role);
                } else {
                    role.preUpdate();
                    frameMapper.updateRole(role);
                }
                // 更新子节点 parentIds
                frameMapper.deleteRoleMenu(role);
                if (role.getMenuList().size() > 0) {
                    frameMapper.insertRoleMenu(role);
                }
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("保存失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody saveOfficeRs(RequestBody rq, Map params, String id) {
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap m = objectMapper.readValue(rq.getParams(), HashMap.class);
            String rstr = (String) m.get("office");
            String mstr = (String) m.get("menuList");
            Office role = objectMapper.readValue(rstr, Office.class);

            JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, Menu.class);
            List<Menu> menuList = JsonMapper.getInstance().fromJson(mstr, javaType);
            role.setMenuList(menuList);
            //System.out.println("--------------------------menu="+menu);
            try {
                frameMapper.deleteOfficeMenu(role);
                if (role.getMenuList().size() > 0) {
                    frameMapper.insertOfficeMenu(role);
                }
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("保存失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody saveOffice(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            frameMapper.saveOffice(office);
            //System.out.println("--------------------------menu="+menu);
            try {

            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("保存失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody saveOfficer(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            //System.out.println("(((((((((((((((((((((((((((((("+rq.getParams());
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            frameMapper.saveOfficer(office);
            //System.out.println("--------------------------menu="+menu);
            try {

            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("保存失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //保存目录
    public ResponseBody saveOfficem(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            //System.out.println("(((((((((((((((((((((((((((((("+rq.getParams());
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            frameMapper.saveOfficem(office);
            //System.out.println("--------------------------menu="+menu);
            try {

            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("保存失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody saveMypan(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            //System.out.println("(((((((((((((((((((((((((((((("+rq.getParams());
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            frameMapper.saveMypan(office);
            //System.out.println("--------------------------menu="+menu);
            try {

            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("保存失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("保存失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody deleteRole(RequestBody rq, Map params, String id) {
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Role role = objectMapper.readValue(rq.getParams(), Role.class);
            //System.out.println("--------------------------menu="+menu);
            try {
                frameMapper.deleteRole(role);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody findUserByRoleId(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(rq.getParams(), User.class);
            try {
                List<User> mmmm = null;
                mmmm = frameMapper.findUserByRoleId(user);
                String jdata = JsonMapper.getInstance().toJson(mmmm);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody findUserByOfficeId(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office office = objectMapper.readValue(rq.getParams(), Office.class);
            try {
                List<User> mmmm = null;
                mmmm = frameMapper.findUserByOfficeId(office);
                String jdata = JsonMapper.getInstance().toJson(mmmm);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getAllOffice(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<Office> mmmm = null;
                mmmm = frameMapper.getAllOffice();
                String jdata = JsonMapper.getInstance().toJson(mmmm);
                rp.setDatas(jdata);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody deleteOffice(RequestBody rq, Map params, String id) {
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office role = objectMapper.readValue(rq.getParams(), Office.class);
            //System.out.println("--------------------------menu="+menu);
            try {
                frameMapper.deleteOffice(role);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody deleteMypan(RequestBody rq, Map params, String id) {
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office role = objectMapper.readValue(rq.getParams(), Office.class);
            //System.out.println("--------------------------menu="+menu);
            try {
                frameMapper.deleteMypan(role);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody deleteXm(RequestBody rq, Map params, String id) {
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Office role = objectMapper.readValue(rq.getParams(), Office.class);
            //System.out.println("--------------------------menu="+menu);
            try {
                frameMapper.deleteXm(role);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody outrole(RequestBody rq, Map params, String id) {
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(rq.getParams(), User.class);
            //System.out.println("--------------------------userid=" + user.getId() + " role=" + user.getRole().getId());
            try {
                frameMapper.outrole(user);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody outroleGly(RequestBody rq, Map params, String id) {
        //System.out.println("--------------------------saveMenu");
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(rq.getParams(), User.class);
            //System.out.println("--------------------------userid="+user.getId()+" role="+user.getRole().getId());
            try {
                frameMapper.outroleGly(user);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //补充指标
    public ResponseBody getBczbList_bak(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取原始补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", user.getDeptCode().substring(0, 4));
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getYsBczb(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("OID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("OID"), zb);
                    }
                    zb.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    zb.put("PID", (String) m.get("PID"));
                    zb.put("OID", (String) m.get("OID"));
                    zb.put("RKSJ", (String) m.get("RKSJ"));
                    zb.put("USERNAME", (String) m.get("USERNAME"));
                }
                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = bczbMapper.getProjectByScope(scope);

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    o.putAll(projects.get(o.get("PID")));
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                //模糊查询
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyReturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyReturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                datas.put("total", myList.size());
                datas.put("keyReturn", keyReturn);
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

    //补充指标
    public ResponseBody getBczbList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取原始补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", user.getDeptCode().substring(0, 4));
            //pa.put("DEPTID", 1093);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getYsBczb(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("ID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("ID"), zb);
                    }
                    zb.putAll(m);
                }

                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = bczbMapper.getProjectByScope(scope);

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put("PRO_ProjectTitle", (String) m.get("P_PROJECTTITLE"));
                    pro.put("PRO_OilTitle", (String) m.get("P_OILTITLE"));
                    pro.put("PRO_BBHTitle", (String) m.get("P_BBHTITLE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    try {
                        o.putAll(projects.get(o.get("PID")));
                    } catch (Exception e) {
                    }
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                //模糊查询
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyReturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyReturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                datas.put("total", myList.size());
                datas.put("keyReturn", keyReturn);
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
  //获取中心发布指标统计列表
    public ResponseBody xmbczbList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> lsHt = bczbMapper.getxmbczbList(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", bczbMapper.getxmbczbList(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
  //获取系统补充指标列表
    public ResponseBody xtbczbList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> lsHt = bczbMapper.getxtbczbList(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", bczbMapper.getxtbczbList(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
  //获取不充值表应用统计列表
    public ResponseBody bczbyyList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                List<HashMap> lsHt = bczbMapper.getbczbyyList(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", bczbMapper.getbczbyyList(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    //中心补充指标
    public ResponseBody getBczbzxList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取原始补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", condition.getDwdm());
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getYsBczb(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("ID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("ID"), zb);
                    }
                    zb.putAll(m);
                }

                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                
                HashMap<String, String> pMap = new HashMap();
                List<HashMap<String, String>> lsP = new ArrayList<HashMap<String,String>>();
                pMap.putAll(plist);
                for (String k : pMap.keySet()) {
                	List<HashMap> onlyProjectByid = bczbMapper.getOnlyProjectById(k);
                	if(onlyProjectByid.size() > 0) {
                		HashMap resultMap = onlyProjectByid.get(0);
                		String pid = (String)resultMap.get("PID");
                		String oneid = (String)resultMap.get("ID");
                		String p_projecttitle = (String)resultMap.get("P_PROJECTTITLE");
                		List<HashMap> result = bczbMapper.getResultMap(pid);
                		if(result.size() > 0) {
                			HashMap hashMap = result.get(0);
                			hashMap.put("ID", oneid);
                			hashMap.put("PRO_DXGC", p_projecttitle);
                			lsP.add(hashMap);
                		}
                	}
                }
//                List<HashMap> lsP = bczbMapper.getProjectByScope(scope);

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put("PRO_ProjectTitle", (String) m.get("P_PROJECTTITLE"));
                    pro.put("PRO_OilTitle", (String) m.get("P_OILTITLE"));
                    pro.put("PRO_BBHTitle", (String) m.get("P_BBHTITLE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    try {
                        o.putAll(projects.get(o.get("PID")));
                    } catch (Exception e) {
                    }
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                //模糊查询
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyReturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyReturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                datas.put("total", myList.size());
                datas.put("keyReturn", keyReturn);
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

    //补充指标
    public ResponseBody getBczbListxf_bak(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取原始补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", user.getDeptCode().substring(0, 4));
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getYsBczbXf(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("OID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("OID"), zb);
                    }
                    zb.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    zb.put("PID", (String) m.get("PID"));
                    zb.put("OID", (String) m.get("OID"));
                    zb.put("RKSJ", (String) m.get("RKSJ"));
                    zb.put("USERNAME", (String) m.get("USERNAME"));
                    zb.put("FZMC", (String) m.get("FZMC"));
                    zb.put("XFRY", (String) m.get("XFRY"));
                }
                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = bczbMapper.getProjectByScope(scope);
                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    o.putAll(projects.get(o.get("PID")));
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                //模糊查询
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyReturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyReturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                datas.put("total", myList.size());
                datas.put("keyReturn", keyReturn);
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

    public ResponseBody getBczbListxf(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取原始补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", user.getDeptCode().substring(0, 4));
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getYsBczbXf(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("ID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("ID"), zb);
                    }
                    zb.putAll(m);
                }

                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = bczbMapper.getProjectByScope(scope);

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put("PRO_ProjectTitle", (String) m.get("P_PROJECTTITLE"));
                    pro.put("PRO_OilTitle", (String) m.get("P_OILTITLE"));
                    pro.put("PRO_BBHTitle", (String) m.get("P_BBHTITLE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    try {
                        o.putAll(projects.get(o.get("PID")));
                    } catch (Exception e) {
                    }
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                //模糊查询
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyReturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyReturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                datas.put("total", myList.size());
                datas.put("keyReturn", keyReturn);
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

    public ResponseBody getBczbzxListxf(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取原始补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", condition.getDwdm());
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getYsBczbXf(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("ID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("ID"), zb);
                    }
                    zb.putAll(m);
                }

                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = bczbMapper.getProjectByScope(scope);

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put("PRO_ProjectTitle", (String) m.get("P_PROJECTTITLE"));
                    pro.put("PRO_OilTitle", (String) m.get("P_OILTITLE"));
                    pro.put("PRO_BBHTitle", (String) m.get("P_BBHTITLE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    try {
                        o.putAll(projects.get(o.get("PID")));
                    } catch (Exception e) {
                    }
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                //模糊查询
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyReturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyReturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                datas.put("total", myList.size());
                datas.put("keyReturn", keyReturn);
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

    public ResponseBody getYhBczbList_bak(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取原始补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", user.getDeptCode().substring(0, 4));
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getYhBczb(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("OID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("OID"), zb);
                    }
                    zb.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    zb.put("PID", (String) m.get("PID"));
                    zb.put("OID", (String) m.get("OID"));
                    zb.put("RKSJ", (String) m.get("RKSJ"));
                    zb.put("USERNAME", (String) m.get("USERNAME"));
                }
                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = new ArrayList();

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    if (projects.get(o.get("PID")) != null)
                        o.putAll(projects.get(o.get("PID")));
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                datas.put("rows", mdList.subList(fromIndex, toIndex));
                datas.put("total", mdList.size());
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

    public ResponseBody getYhBczbList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", user.getDeptCode().substring(0, 4));
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getYhBczb(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("ID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("ID"), zb);
                    }
                    zb.putAll(m);
                }

                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = new ArrayList();

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put("PRO_ProjectTitle", (String) m.get("P_PROJECTTITLE"));
                    pro.put("PRO_OilTitle", (String) m.get("P_OILTITLE"));
                    pro.put("PRO_BBHTitle", (String) m.get("P_BBHTITLE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    try {
                        o.putAll(projects.get(o.get("PID")));
                    } catch (Exception e) {
                    }
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                //模糊查询
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyReturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyReturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                datas.put("total", myList.size());
                datas.put("keyReturn", keyReturn);
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

    public ResponseBody getYhBczbzxList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", condition.getDwdm());
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getYhBczbzx(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("ID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("ID"), zb);
                    }
                    zb.putAll(m);
                }

                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = new ArrayList();

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put("PRO_ProjectTitle", (String) m.get("P_PROJECTTITLE"));
                    pro.put("PRO_OilTitle", (String) m.get("P_OILTITLE"));
                    pro.put("PRO_BBHTitle", (String) m.get("P_BBHTITLE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    try {
                        o.putAll(projects.get(o.get("PID")));
                    } catch (Exception e) {
                    }
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                //模糊查询
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyReturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyReturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                datas.put("total", myList.size());
                datas.put("keyReturn", keyReturn);
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

    public ResponseBody getDwBczbList_bak(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取原始补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", user.getDeptCode().substring(0, 4));
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getDwBczb(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("OID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("OID"), zb);
                    }
                    zb.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    zb.put("PID", (String) m.get("PID"));
                    zb.put("OID", (String) m.get("OID"));
                    zb.put("OILID", (String) m.get("OILID"));
                    zb.put("RKSJ", (String) m.get("RKSJ"));
                    zb.put("YQNAME", (String) m.get("YQNAME"));
                }
                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = new ArrayList();

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    if (projects.get(o.get("PID")) != null)
                        o.putAll(projects.get(o.get("PID")));
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyreturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyreturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                ;
                datas.put("total", myList.size());

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

    public ResponseBody getDwBczbList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", user.getDeptCode().substring(0, 4));
            pa.put("c1", condition.getC1());
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getDwBczb(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("ID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("ID"), zb);
                    }
                    zb.putAll(m);
                }

                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = new ArrayList();

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put("PRO_ProjectTitle", (String) m.get("P_PROJECTTITLE"));
                    pro.put("PRO_OilTitle", (String) m.get("P_OILTITLE"));
                    pro.put("PRO_BBHTitle", (String) m.get("P_BBHTITLE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    try {
                        o.putAll(projects.get(o.get("PID")));
                    } catch (Exception e) {
                    }
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                //模糊查询
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyReturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyReturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                datas.put("total", myList.size());
                datas.put("keyReturn", keyReturn);
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

    public ResponseBody getZxBczbList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", condition.getDwdm());
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getZxBczb(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("ID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("ID"), zb);
                    }
                    zb.putAll(m);
                }

                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = new ArrayList();

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put("PRO_ProjectTitle", (String) m.get("P_PROJECTTITLE"));
                    pro.put("PRO_OilTitle", (String) m.get("P_OILTITLE"));
                    pro.put("PRO_BBHTitle", (String) m.get("P_BBHTITLE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    try {
                        o.putAll(projects.get(o.get("PID")));
                    } catch (Exception e) {
                    }
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                //模糊查询
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyReturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyReturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                datas.put("total", myList.size());
                datas.put("keyReturn", keyReturn);
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

    public ResponseBody getDwBcElfList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", user.getDeptCode().substring(0, 4));
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getDwBcelf(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("OID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("OID"), zb);
                    }
                    zb.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    zb.put("PID", (String) m.get("PID"));
                    zb.put("OID", (String) m.get("OID"));
                    zb.put("RKSJ", (String) m.get("RKSJ"));
                }
                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = new ArrayList();

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    if (projects.get(o.get("PID")) != null)
                        o.putAll(projects.get(o.get("PID")));
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyreturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyreturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                ;
                datas.put("total", myList.size());

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

    public ResponseBody getDwBcGcfList_bak(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", user.getDeptCode().substring(0, 4));
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getDwBcgcf(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("OID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("OID"), zb);
                    }
                    zb.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    zb.put("PID", (String) m.get("PID"));
                    zb.put("OID", (String) m.get("OID"));
                    zb.put("RKSJ", (String) m.get("RKSJ"));
                }
                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = new ArrayList();

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    if (projects.get(o.get("PID")) != null)
                        o.putAll(projects.get(o.get("PID")));
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyreturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyreturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                datas.put("total", myList.size());
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

    public ResponseBody getDwBcGcfList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            User user = UserUtils.getUser();
            Map pa = new HashMap();
            pa.put("DEPTID", user.getDeptCode().substring(0, 4));
            //System.out.println(pa);
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getDwBcgcf(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("OID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("OID"), zb);
                    }
                    zb.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    zb.put("PID", (String) m.get("PID"));
                    zb.put("OID", (String) m.get("OID"));
                    zb.put("RKSJ", (String) m.get("RKSJ"));
                }
                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = new ArrayList();

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    if (projects.get(o.get("PID")) != null)
                        o.putAll(projects.get(o.get("PID")));
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                List<HashMap> myList = new ArrayList<HashMap>();
                List<HashMap> tmpList = new ArrayList<HashMap>();
                String key = condition.getC1();
                String keyreturn = "";
                if (StringUtils.isNotBlank(key)) {
                    tmpList = mdList;
                    String[] keys = key.split("\\s+");
                    for (String a : keys) {
                        keyreturn += a + "##%%&&**";
                        myList = new ArrayList<HashMap>();
                        for (HashMap m : tmpList) {
                            for (Object k : m.keySet()) {
                                String val = (String) m.get(k);
                                if (StringUtils.isBlank(val)) continue;
                                boolean f = false;
                                if (val.contains(a)) {
                                    f = true;
                                }
                                if (f) {
                                    myList.add(m);
                                    break;
                                }
                            }
                        }
                        tmpList = myList;
                    }
                } else {
                    myList = mdList;
                }
                try {
                    datas.put("rows", myList.subList(fromIndex, toIndex));
                } catch (Exception e) {
                    datas.put("rows", myList);
                }
                datas.put("total", myList.size());
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

    public ResponseBody getBczbByFzList_bak(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取原始补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            Map pa = new HashMap();
            pa.put("c1", condition.getC1());
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getBczbByFz(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("OID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("OID"), zb);
                    }
                    zb.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    zb.put("PID", (String) m.get("PID"));
                    zb.put("OID", (String) m.get("OID"));
                    zb.put("RKSJ", (String) m.get("RKSJ"));
                }
                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = bczbMapper.getProjectByScope(scope);

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    o.putAll(projects.get(o.get("PID")));
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                datas.put("rows", mdList.subList(fromIndex, toIndex));
                datas.put("total", mdList.size());
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
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            Map pa = new HashMap();
            pa.put("c1", condition.getC1());
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getBczbByFz(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("ID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("ID"), zb);
                    }
                    zb.putAll(m);
                }

                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                List<HashMap> lsP = bczbMapper.getProjectByScope(scope);

                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put("PRO_ProjectTitle", (String) m.get("P_PROJECTTITLE"));
                    pro.put("PRO_OilTitle", (String) m.get("P_OILTITLE"));
                    pro.put("PRO_BBHTitle", (String) m.get("P_BBHTITLE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    try {
                        o.putAll(projects.get(o.get("PID")));
                    } catch (Exception e) {
                    }
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                datas.put("rows", mdList.subList(fromIndex, toIndex));
                datas.put("total", mdList.size());
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

    public ResponseBody getBczbByMlList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取补充指标成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);

            Map pa = new HashMap();
            pa.put("c1", condition.getC1());
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getBczbByMl(pa);
                //转换后的指标Map
                HashMap<String, HashMap> objects = new HashMap();
                HashMap<String, String> plist = new HashMap();
                for (HashMap m : bbls) {
                    plist.put((String) m.get("PID"), "project");
                    HashMap zb = (HashMap) objects.get((String) m.get("OID"));
                    if (zb == null) {
                        zb = new HashMap();
                        objects.put((String) m.get("OID"), zb);
                    }
                    zb.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    zb.put("PID", (String) m.get("PID"));
                    zb.put("OID", (String) m.get("OID"));
                    zb.put("RKSJ", (String) m.get("RKSJ"));
                }
                //获取project 范围
                String scope = "''";
                for (String k : plist.keySet()) {
                    scope += ",'" + k + "'";
                }
                //List<HashMap> lsP=bczbMapper.getProjectByScope(scope);
                List<HashMap> lsP = new ArrayList();
                //转换后的project
                HashMap<String, HashMap> projects = new HashMap();
                for (HashMap m : lsP) {
                    HashMap pro = (HashMap) projects.get((String) m.get("ID"));
                    if (pro == null) {
                        pro = new HashMap();
                        projects.put((String) m.get("ID"), pro);
                    }
                    pro.put((String) m.get("OKEY"), (String) m.get("OVALUE"));
                    pro.put("PRO_ID", (String) m.get("ID"));
                    pro.put("PRO_DXGC", (String) m.get("PRO_DXGC"));
                }
                //给指标绑定项目属性
                for (String k : objects.keySet()) {
                    HashMap o = objects.get(k);
                    if (projects.get(o.get("PID")) != null)
                        o.putAll(projects.get(o.get("PID")));
                }
                List<HashMap> mdList = new ArrayList<HashMap>(objects.values());
                LinkedHashMap<String, String> orderby = new LinkedHashMap<>();
                if (StringUtils.isNotBlank(condition.getSort())) {
                    String[] sort = condition.getSort().split(",");
                    for (String s : sort) {
                        if (StringUtils.isBlank(s)) continue;
                        String[] ss = s.split(" ");
                        orderby.put(ss[0], ss[1]);
                    }
                } else {
                    orderby.put("RKSJ", "desc");
                }
                AppUtils.sort(mdList, orderby);
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
                datas.put("rows", mdList.subList(fromIndex, toIndex));
                datas.put("total", mdList.size());
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

    //带项目单项工程信息的格式
    public ResponseBody fzxf2(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            try {
                List<HashMap> lszb = bczbMapper.getZbByFz(condition);
                String xml = "";
                Document doc = DocumentHelper.createDocument();
                Element ls = DocumentHelper.createElement("bcList");
                doc.add(ls);
                HashMap<String, String> projectHt = new HashMap<String, String>();
                //System.out.println("lszb=="+lszb);
                for (HashMap m : lszb) {
                    String root = (String) m.get("INFO");
                    if (StringUtils.isEmpty(root)) continue;
                    if (projectHt.get(root) == null)
                        projectHt.put(root, "''");
                    projectHt.put(root, projectHt.get(root) + ",'" + m.get("BCZBID") + "'");
                }
                //System.out.println("projectHt=="+projectHt);
                for (String k : projectHt.keySet()) {
                    try {
                        Document n = getMapFromDb2(k, projectHt.get(k));
                        ls.add(n.getRootElement());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //bczbXfPath
                String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + bczbXfPath + "/" + condition.getC1();
                xml = XmlUtils.FormatXml(doc);
                FileUtils.writeToFile(saveDirectoryPath, xml, false);
                bczbMapper.updateFzPath(condition.getC1(), saveDirectoryPath);
                Map mail = new HashMap();
                mail.put("FJRID", user.getId());
                mail.put("FJRYHM", user.getLoginName());
                mail.put("FJRXM", user.getUsername());
                mail.put("SJRID", condition.getC2());
                mail.put("ZT", condition.getC3());
                mail.put("LB", "2");
                mail.put("PATH", "/" + condition.getC1());
                mail.put("ID", UUID.randomUUID().toString());
                emailMapper.sendMail(mail);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody fzxf(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            try {
                List<HashMap> lszb = bczbMapper.getZbByFz(condition);
                String xml = "";
                Document doc = DocumentHelper.createDocument();
                Element ls = DocumentHelper.createElement("bcList");
                ls.addAttribute("OilCode", condition.getC5());
                ls.addAttribute("BBH", condition.getC6());
                doc.add(ls);
                for (HashMap m : lszb) {
                    String root = (String) m.get("BCZBID");
                    //xml+=getMapFromDb(root);
                    Document n = getMapFromDb(root);
                    /*if(doc==null) doc=n;
                    else doc.add(n.getRootElement());*/
                    ls.add(n.getRootElement());
                }
                //bczbXfPath
                String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + bczbXfPath + "/" + condition.getC1();
                xml = XmlUtils.FormatXml(doc);
                try{
                	/*xml = compress(xml);*/
                	byte[] b = xml.toString().getBytes("UTF-8");
                	xml = new BASE64Encoder().encode(b);
                }catch (UnsupportedEncodingException e){
                	e.printStackTrace();
                }
                FileUtils.writeToFile(saveDirectoryPath, xml, false);
                bczbMapper.updateFzPath(condition.getC1(), saveDirectoryPath);
                Map mail = new HashMap();
                mail.put("FJRID", user.getId());
                mail.put("FJRYHM", user.getLoginName());
                mail.put("FJRXM", user.getUsername());
                mail.put("SJRID", condition.getC2());
                mail.put("ZT", condition.getC3());
                mail.put("LB", "2");
                mail.put("PATH", "/" + condition.getC1());
                mail.put("ID", UUID.randomUUID().toString());
                emailMapper.sendMail(mail);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody fzxfzx(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "操作成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            try {
                List<HashMap> lszb = bczbMapper.getZbByFz(condition);
                String xml = "";
                Document doc = DocumentHelper.createDocument();
                Element ls = DocumentHelper.createElement("bcList");
                ls.addAttribute("OilCode", condition.getC5());
                ls.addAttribute("BBH", condition.getC6());
                doc.add(ls);
                for (HashMap m : lszb) {
                    String root = (String) m.get("BCZBID");
                    //xml+=getMapFromDb(root);
                    Document n = getMapFromDb(root);
                    /*if(doc==null) doc=n;
                    else doc.add(n.getRootElement());*/
                    ls.add(n.getRootElement());
                }
                //bczbXfPath
                String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + bczbXfPath + "/" + condition.getC1();
                xml = XmlUtils.FormatXml(doc);
                FileUtils.writeToFile(saveDirectoryPath, xml, false);
                bczbMapper.updateFzPath(condition.getC1(), saveDirectoryPath);
                Map mail = new HashMap();
                mail.put("FJRID", user.getId());
                mail.put("FJRYHM", user.getLoginName());
                mail.put("FJRXM", user.getUsername());
                mail.put("SJRID", condition.getC2());
                mail.put("ZT", condition.getC3());
                mail.put("LB", "9");
                mail.put("PATH", "/" + condition.getC1());
                mail.put("ID", UUID.randomUUID().toString());
                emailMapper.sendMail(mail);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public Document getMapFromDb(String root) {
        LinkedHashMap<String, LinkedHashMap> treeM = new LinkedHashMap<>();
        List<LinkedHashMap> treeMm = bczbMapper.getBczbTreeById(root);
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
        try {
            doc = XmlUtils.Map2Xml(docStr);
            xmlstr = XmlUtils.FormatXml(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public Document getMapFromDb_bak(String root) {
        LinkedHashMap<String, LinkedHashMap> treeM = new LinkedHashMap<>();
        List<LinkedHashMap> treeMm = bczbMapper.getBczbTreeById(root);
        for (LinkedHashMap t : treeMm) {
            treeM.put((String) t.get("ID"), t);
        }
        //System.out.println("11 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        List<LinkedHashMap> propertyL = bczbMapper.getBczbProperty(root);
        //System.out.println("22 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String, Map> propertyM = new HashMap<String, Map>();
        for (LinkedHashMap m : propertyL) {
            Map<String, Object> p = propertyM.get((String) m.get("OID"));
            if (p == null) {
                p = new LinkedHashMap<String, Object>();
                propertyM.put((String) m.get("OID"), p);
            }
            if ("@SERVERID".equals((String) m.get("OKEY")))
                p.put((String) m.get("OKEY"), m.get("OID"));
            else p.put((String) m.get("OKEY"), (String) m.get("OVALUE") == null ? "" : (String) m.get("OVALUE"));
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
        //System.out.println("44 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String, Object> docStr = AppUtils.readSql2Map(treeM, tree, root, propertyM);
        Document doc = null;
        String xmlstr = "";
        try {
            doc = XmlUtils.Map2Xml(docStr);
            xmlstr = XmlUtils.FormatXml(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public Document getMapFromDb3_bak(String root) {
        LinkedHashMap<String, LinkedHashMap> treeM = new LinkedHashMap<>();
        List<LinkedHashMap> treeMm = bczbMapper.getYhBczbTreeById(root);
        for (LinkedHashMap t : treeMm) {
            treeM.put((String) t.get("ID"), t);
        }
        //System.out.println("11 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        List<LinkedHashMap> propertyL = bczbMapper.getYhBczbProperty(root);
        //System.out.println("22 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String, Map> propertyM = new HashMap<String, Map>();
        for (LinkedHashMap m : propertyL) {
            Map<String, Object> p = propertyM.get((String) m.get("OID"));
            if (p == null) {
                p = new LinkedHashMap<String, Object>();
                propertyM.put((String) m.get("OID"), p);
            }
            /*if("@SERVERID".equals((String) m.get("OKEY")))
                p.put((String) m.get("OKEY"), m.get("OID"));
            else */
            p.put((String) m.get("OKEY"), (String) m.get("OVALUE") == null ? "" : (String) m.get("OVALUE"));
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
        //System.out.println("44 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String, Object> docStr = AppUtils.readSql2Map(treeM, tree, root, propertyM);
        Document doc = null;
        String xmlstr = "";
        try {
            doc = XmlUtils.Map2Xml(docStr);
            xmlstr = XmlUtils.FormatXml(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
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

    public Document getMapFromDb2(String root, String scope) {
        LinkedHashMap<String, LinkedHashMap> treeM = new LinkedHashMap<>();
        List<LinkedHashMap> treeMm = bczbMapper.getBczbTreeById2(root, scope);
        for (LinkedHashMap t : treeMm) {
            treeM.put((String) t.get("ID"), t);
        }
        //System.out.println("11 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        List<LinkedHashMap> propertyL = bczbMapper.getBczbProperty2(root, scope);
        //System.out.println("22 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String, Map> propertyM = new HashMap<String, Map>();
        for (LinkedHashMap m : propertyL) {
            Map<String, Object> p = propertyM.get((String) m.get("OID"));
            if (p == null) {
                p = new LinkedHashMap<String, Object>();
                propertyM.put((String) m.get("OID"), p);
            }
            if ("@SERVERID".equals((String) m.get("OKEY")))
                p.put((String) m.get("OKEY"), m.get("OID"));
            else p.put((String) m.get("OKEY"), (String) m.get("OVALUE") == null ? "" : (String) m.get("OVALUE"));
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
        //System.out.println("44 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String, Object> docStr = AppUtils.readSql2Map(treeM, tree, root, propertyM);
        Document doc = null;
        String xmlstr = "";
        try {
            doc = XmlUtils.Map2Xml(docStr);
            xmlstr = XmlUtils.FormatXml(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public Document getMapFromDb4(String root) {
        LinkedHashMap<String, LinkedHashMap> treeM = new LinkedHashMap<>();
        List<LinkedHashMap> treeMm = bczbMapper.getDwBczbTreeById(root);
        for (LinkedHashMap t : treeMm) {
            treeM.put((String) t.get("ID"), t);
        }
        //System.out.println("11 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        List<LinkedHashMap> propertyL = bczbMapper.getDwBczbProperty(root);
        //System.out.println("22 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String, Map> propertyM = new HashMap<String, Map>();
        for (LinkedHashMap m : propertyL) {
            Map<String, Object> p = propertyM.get((String) m.get("OID"));
            if (p == null) {
                p = new LinkedHashMap<String, Object>();
                propertyM.put((String) m.get("OID"), p);
            }
            /*if("@SERVERID".equals((String) m.get("OKEY")))
                p.put((String) m.get("OKEY"), m.get("OID"));
            else */
            p.put((String) m.get("OKEY"), (String) m.get("OVALUE") == null ? "" : (String) m.get("OVALUE"));
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
        //System.out.println("44 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String, Object> docStr = AppUtils.readSql2Map(treeM, tree, root, propertyM);
        Document doc = null;
        String xmlstr = "";
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
        String root = "8b7f5824833143a18045b9e620521eb2";
        LinkedHashMap<String, LinkedHashMap> treeM = new LinkedHashMap<>();
        List<LinkedHashMap> treeMm = bczbMapper.getBczbTreeById(root);
        for (LinkedHashMap t : treeMm) {
            treeM.put((String) t.get("ID"), t);
        }
        //System.out.println("11 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        List<LinkedHashMap> propertyL = bczbMapper.getBczbProperty(root);
        //System.out.println("22 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String, Map> propertyM = new HashMap<String, Map>();
        for (LinkedHashMap m : propertyL) {
            Map<String, Object> p = propertyM.get((String) m.get("OID"));
            if (p == null) {
                p = new LinkedHashMap<String, Object>();
                propertyM.put((String) m.get("OID"), p);
            }
            p.put((String) m.get("OKEY"), (String) m.get("OVALUE") == null ? "" : (String) m.get("OVALUE"));
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
        //System.out.println("44 "+new SimpleDateFormat("hh:mm:ss.SSS").format(System.currentTimeMillis()));
        Map<String, Object> docStr = AppUtils.readSql2Map(treeM, tree, root, propertyM);
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
       /* String textFromFile = "";
        User user = UserUtils.getUser();
        try {
            textFromFile = FileUtils.readFileToString(new File("c:/bczb.xml"), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> map = null;
        try {
            map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
            String sql = AppUtils.readMap2Sql2(map, "-1", "", user.getDeptCode().substring(0, 4), (String) params.get("CPUID"), user.getId(), "");
            HashMap h = new HashMap();
            h.put("sql", sql);
            bczbMapper.mergeProject(h);
        } catch (DocumentException e) {
            e.printStackTrace();
        }*/
        return rp;
    }

    //提交到补充指标库
    public ResponseBody yhbczbRk_bak(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        User user = UserUtils.getUser();
        int c = frameMapper.isAdmin(user.getId());
        //System.out.println("user.getUserid()"+user.getId());
        //admin
        if (c > 0) {
            //System.out.println("user.getUserid()-----"+user.getId());
            String textFromFile = "";
            BASE64Decoder decoder = new BASE64Decoder();
            try {
                textFromFile = new String(decoder.decodeBuffer((String) params.get("BCZBDATA")), "UTF-8");

                //textFromFile = StringEscapeUtils.unescapeHtml((String) textFromFile);
                if (StringUtils.isBlank(textFromFile)) return rp;
            /*System.out.println("------------------------");
            String saveDirectoryPath = Global.getConfig("upLoadPath") +"/"+ bczbXfPath+"/55555555";
            FileUtils.writeToFile(saveDirectoryPath,textFromFile,false);*/
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
                    textFromFile = new String(decoder.decodeBuffer((String) params.get("BCZBDATA")), "UTF-8");

                    //textFromFile = StringEscapeUtils.unescapeHtml((String) textFromFile);
                    if (StringUtils.isBlank(textFromFile)) return rp;
            /*System.out.println("------------------------");
            String saveDirectoryPath = Global.getConfig("upLoadPath") +"/"+ bczbXfPath+"/55555555";
            FileUtils.writeToFile(saveDirectoryPath,textFromFile,false);*/
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
                    Document n = getMapFromDb3((String) dd.get("OID"));
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

    //提交到补充指标库
    //用户补充中心指标入库
    public ResponseBody yhbczxzbRk(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        User user = UserUtils.getUser();
        int c = frameMapper.isZx(user.getId());
        if (c > 0) {
            String textFromFile = "";
            BASE64Decoder decoder = new BASE64Decoder();
            try {
                textFromFile = new String(decoder.decodeBuffer((String) params.get("BCZBDATA")), "UTF-8");
                if (StringUtils.isBlank(textFromFile)) return rp;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Object> map = null;
            try {
                String dw = "1093";
                if (StringUtils.isNotBlank(user.getDeptCode())) dw = user.getDeptCode().substring(0, 4);
                map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
                String sql = AppUtils.readMap2Sql4zx(map, "-1", "", dw, (String) rq.getCpu(), user.getId(), "");
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
                    textFromFile = new String(decoder.decodeBuffer((String) params.get("BCZBDATA")), "UTF-8");

                    //textFromFile = StringEscapeUtils.unescapeHtml((String) textFromFile);
                    if (StringUtils.isBlank(textFromFile)) return rp;
            /*System.out.println("------------------------");
            String saveDirectoryPath = Global.getConfig("upLoadPath") +"/"+ bczbXfPath+"/55555555";
            FileUtils.writeToFile(saveDirectoryPath,textFromFile,false);*/
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Map<String, Object> map = null;
                List<HashMap> adm = new ArrayList<HashMap>();
                try {
                    String dw = "1093";//默认临时组
                    if (StringUtils.isNotBlank(user.getDeptCode())) dw = user.getDeptCode().substring(0, 4);
                    adm = frameMapper.getZx();
                    if (adm.size() == 0) {
                        rp.setIssuccess("0");
                        rp.setMessage("获取单位管理员列表失败！");
                        return rp;
                    }
                    map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
                    String sql = AppUtils.readMap2Sql3zx(map, "-1", "", dw, (String) rq.getCpu(), user.getId(), "");
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
                List<HashMap> llll = bczbMapper.getZbzxByServerId(sss);
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
                    mail.put("LB", "8");
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
    //补充指标采集
    public ResponseBody bczbCj(RequestBody rq, Map params, String id) {
        //System.out.println("----------------------------------------------------");
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        try {
            User user = UserUtils.getUser();
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
                logger.error(e.toString());
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return rp;
    }

//用户补充指标入库
    public ResponseBody yhbczbRk(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        User user = UserUtils.getUser();
        int c = frameMapper.isAdmin(user.getId());
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
                	String textFromFile1 = new String(decoder.decodeBuffer((String) params.get("BCZBDATA")), "UTF-8");
                    textFromFile = releaseCompression(textFromFile1);
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
                try{
                	/*xml = compress(xml);*/
                	byte[] b = xml.toString().getBytes("UTF-8");
                	xml = new BASE64Encoder().encode(b);
                }catch (UnsupportedEncodingException e){
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

    //提交到补充二类费指标库
    public ResponseBody yhbcelfRk(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        User user = UserUtils.getUser();
        String textFromFile = "";
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            textFromFile = new String(decoder.decodeBuffer((String) params.get("BCZBDATA")), "UTF-8");
            if (StringUtils.isBlank(textFromFile)) return rp;
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！");
            e.printStackTrace();
        }
        Map<String, Object> map = null;
        try {
            String dw = "1093";
            if (StringUtils.isNotBlank(user.getDeptCode())) dw = user.getDeptCode().substring(0, 4);
            map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
            String sql = AppUtils.readMap2Sql5(map, "-1", "", dw, (String) rq.getCpu(), user.getId());
            HashMap h = new HashMap();
            h.put("sql", sql);
            bczbMapper.mergeProject4(h);
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！");
            e.printStackTrace();
        }

        return rp;
    }

    //提交到单位补充指标储备库
    public ResponseBody dwbccbRk_bak(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        try {
            User user = UserUtils.getUser();
            String textFromFile = "";
            BASE64Decoder decoder = new BASE64Decoder();

            try {
                textFromFile = new String(decoder.decodeBuffer((String) params.get("BCZBDATA")), "UTF-8");

                //textFromFile = StringEscapeUtils.unescapeHtml((String) textFromFile);
                if (StringUtils.isBlank(textFromFile)) return rp;
            /*System.out.println("------------------------");
            String saveDirectoryPath = Global.getConfig("upLoadPath") +"/"+ bczbXfPath+"/55555555";
            FileUtils.writeToFile(saveDirectoryPath,textFromFile,false);*/
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
            /*if (map.get("GeneralInformation") == null) return rp;
            String[] bczbids = ((String) ((Map) map.get("GeneralInformation")).get("@BCZBIDS")).split(",");
            Document doc = DocumentHelper.createDocument();
            Element ls = DocumentHelper.createElement("bcList");
            ls.addAttribute("OilCode", (String) ((Map) map.get("GeneralInformation")).get("@OilCode"));
            //ls.addAttribute("BBH", "3");
            doc.add(ls);
            String sss = "'" + UUID.randomUUID().toString() + "'";
            for (String dd : bczbids) {
                sss += ",'" + dd + "'";

            }
            sss = "(" + sss + ")";
            List<HashMap> llll = bczbMapper.getDwZbByServerId(sss);
            for (HashMap dd : llll) {
                Document n = getMapFromDb4((String) dd.get("OID"));
                ls.add(n.getRootElement());
            }*/

            /*String ii = UUID.randomUUID().toString();
            String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbczbPath + "/" + ii;
            String xml = "";
            try {
                xml = XmlUtils.FormatXml(doc);
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FileUtils.writeToFile(saveDirectoryPath, xml, false);
            Map mail = new HashMap();
            mail.put("FJRID", user.getId());
            mail.put("FJRYHM", user.getLoginName());
            mail.put("FJRXM", user.getUsername());
            mail.put("SJRID", "2BD695B466C2435BA3F6B6258AFBA49E");
            mail.put("ZT", "用户提交补充指标");
            mail.put("LB", "4");
            mail.put("PATH", "/" + ii);
            emailMapper.sendMail(mail);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody dwbccbRk(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        try {
            User user = UserUtils.getUser();
            String textFromFile = "";
            BASE64Decoder decoder = new BASE64Decoder();

            try {
                textFromFile = new String(decoder.decodeBuffer((String) params.get("BCZBDATA")), "UTF-8");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }

    //提交到中心补充指标库
    public ResponseBody zxbccbRk(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        try {
            User user = UserUtils.getUser();
            String textFromFile = "";
            BASE64Decoder decoder = new BASE64Decoder();

            try {
                textFromFile = new String(decoder.decodeBuffer((String) params.get("BCZBDATA")), "UTF-8");
                if (StringUtils.isBlank(textFromFile)) return rp;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Object> map = null;
            try {
                String dw = "1093";
                if (StringUtils.isNotBlank(user.getDeptCode())) dw = user.getDeptCode().substring(0, 4);
                map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
                String sql = AppUtils.readMap2Sql4zx(map, "-1", "", dw, (String) rq.getCpu(), user.getId(), "");
                HashMap h = new HashMap();
                h.put("sql", sql);
                bczbMapper.mergeProject4(h);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getZhzbList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取指标组合列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            //System.out.println(rq.getParams());
            User user = UserUtils.getUser();
            condition.setDwdm(user.getDeptCode().substring(0, 4));
            try {
                List<HashMap> lsHt = bczbMapper.getZhzbList(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", bczbMapper.getZhzbList(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取指标组合失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取指标组合失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getZhzbzxList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取指标组合列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            //System.out.println(rq.getParams());
            User user = UserUtils.getUser();
            condition.setDwdm(user.getDeptCode().substring(0, 4));
            try {
                List<HashMap> lsHt = bczbMapper.getZhzbList(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", bczbMapper.getZhzbList(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取指标组合失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取指标组合失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody getJsrList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取指标组合列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            condition.setDwdm(user.getDeptCode().substring(0, 4));
            try {
                List<HashMap> lsHt = bczbMapper.getJsrList(condition);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(lsHt));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取指标组合失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取指标组合失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody zhsave(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                User user = UserUtils.getUser();
                condition.setUserid(user.getId());
                condition.setDwdm(user.getDeptCode().substring(0, 4));
                bczbMapper.saveBczbZh(condition);
                createOrReloadTreeXml(rp,condition);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody bczbmlsave(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
                try {
                    User user = UserUtils.getUser();
                    condition.setUserid(user.getId());
                    condition.setDwdm(user.getDeptCode().substring(0, 4));
                    String c5 = condition.getC5();
                    condition.setC15(condition.getC1());
                    condition.setC16(condition.getC2());
                    frameMapper.bczbmlsave(condition);//保存指标关系
                    if("yes".equals(c5)) {//若为修改指标
                    	List<String> zbs = bczbMapper.getZbsBymainId(condition);//1、先查询出所此条记录所有的保存指标ID
                    	if(!zbs.isEmpty() && zbs.size() > 0) {
                    		for (int i = 0; i < zbs.size(); i++) {//2、循环遍历  保存指标ID  后根据指标id进行更新
                    			String zbid  = zbs.get(i);
                    			condition.setC11(zbid);
                    			bczbMapper.updateRealZbmc(condition);
							}
                    	}
                    	String formatOne = condition.getC6().replace("\\" ,"");
            			String formatTwo = formatOne.substring(1, formatOne.length());
            			String xmlInfo = formatTwo.substring(0, formatTwo.length()-1);
            			try {
            				createOrUpdateZbXML(xmlInfo);
						} catch (Exception e) {
							rp.setIssuccess("0");
			                rp.setMessage("操作失败！" + e.getMessage());
			                e.printStackTrace();
						}
                    }
                    createOrReloadTreeXml( rp,  condition);
	            } catch (Exception e) {
	                rp.setIssuccess("0");
	                rp.setMessage("操作失败！" + e.getMessage());
	                e.printStackTrace();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        return rp;
    }
    
    public ResponseBody getZbInfo(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        List resultList = new ArrayList();
        ObjectMapper mapper = new ObjectMapper();   
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
                
                try {
                    String c5 = condition.getC5();
                    condition.setC15(condition.getC1());
                    if("yes".equals(c5)) {//若为修改指标
                    	List<String> zbs = bczbMapper.getZbsBymainId(condition);//1、先查询出所此条记录所有的保存指标ID
                    	if(!zbs.isEmpty() && zbs.size() > 0) {
                    		for (int i = 0; i < zbs.size(); i++) {//2、循环遍历  保存指标ID  后根据指标id进行更新
                    			String zbid  = zbs.get(i);
                    			List zbInfo = bczbMapper.getZbInfoById(zbid);
                    			String json = mapper.writeValueAsString(zbInfo.get(0));  
                    			resultList.add(json);
							}
                    		rp.setDatas(resultList.toString());
                    	}
                    }
	            } catch (Exception e) {
	                rp.setIssuccess("0");
	                rp.setMessage("操作失败！" + e.getMessage());
	                e.printStackTrace();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        return rp;
    }

    public ResponseBody bcelfmlsave(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
                try {
                    User user = UserUtils.getUser();
                    condition.setUserid(user.getId());
                    condition.setDwdm(user.getDeptCode().substring(0, 4));
                    frameMapper.bczbmlsave(condition);
                    Map m = new HashMap();
                    m.put("CODE", condition.getDwdm());
                    HashMap<String, Office> lsHt = frameMapper.bcelfMlLists(m);
                    List<HashMap> zbdic = frameMapper.getMlElfAList(m);
                    LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                    HashMap<String, List<Office>> children = new HashMap();
                    Map<String, List<Map>> zbchildren = new HashMap();
                    String rootId = "";
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                    for (HashMap z : zbdic) {
                        String mlid = (String) z.get("ZHZBID");
                        List c = zbchildren.get(mlid);
                        c.add(z);
                    }
                    Map<String, Object> docStr = AppUtils.getMlMap(lsHt, children, rootId, zbchildren);
                    String xml = "";
                    Document doc = XmlUtils.Map2Xml(docStr);
                    String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcelfPath + "/" + rootId;
                    xml = XmlUtils.FormatXml(doc);
                    FileUtils.writeToFile(saveDirectoryPath, xml, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    rp.setIssuccess("0");
                    rp.setMessage("操作失败！" + e.getMessage());
                }
            } catch (Exception e) {
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody bcgcfmlsave(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
                try {
                    User user = UserUtils.getUser();
                    condition.setUserid(user.getId());
                    condition.setDwdm(user.getDeptCode().substring(0, 4));
                    frameMapper.bczbmlsave(condition);
                    Map m = new HashMap();
                    m.put("CODE", condition.getDwdm());
                    HashMap<String, Office> lsHt = frameMapper.bcgcfMlLists(m);
                    List<HashMap> zbdic = frameMapper.getMlGcfAList(m);
                    LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                    HashMap<String, List<Office>> children = new HashMap();
                    Map<String, List<Map>> zbchildren = new HashMap();
                    String rootId = "";
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                    for (HashMap z : zbdic) {
                        String mlid = (String) z.get("ZHZBID");
                        List c = zbchildren.get(mlid);
                        c.add(z);
                    }
                    Map<String, Object> docStr = AppUtils.getMlMap(lsHt, children, rootId, zbchildren);
                    String xml = "";
                    Document doc = XmlUtils.Map2Xml(docStr);
                    String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcgcfPath + "/" + rootId;
                    xml = XmlUtils.FormatXml(doc);
                    FileUtils.writeToFile(saveDirectoryPath, xml, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    rp.setIssuccess("0");
                    rp.setMessage("操作失败！" + e.getMessage());
                }
            } catch (Exception e) {
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody addZb2Fz(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC1());
                String c2 = condition.getC2();
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                Map m = new HashMap();
                m.put("list", h);
                m.put("fzid", c2);
                //System.out.println("6666666666666666"+m);
                bczbMapper.addZb2Fz(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody addElf2Ml(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                User user = UserUtils.getUser();
                condition.setDwdm(user.getDeptCode().substring(0, 4));
                String updL = StringEscapeUtils.unescapeHtml(condition.getC1());
                String c2 = condition.getC2();
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                Map m = new HashMap();
                m.put("list", h);
                m.put("fzid", c2);
                bczbMapper.addElf2Fz(m);
                //System.out.println("6666666666666666"+m);
                {
                    m = new HashMap();
                    m.put("CODE", condition.getDwdm());
                    HashMap<String, Office> lsHt = frameMapper.bcelfMlLists(m);
                    List<HashMap> zbdic = frameMapper.getMlElfAList(m);
                    LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                    HashMap<String, List<Office>> children = new HashMap();
                    Map<String, List<Map>> zbchildren = new HashMap();
                    String rootId = "";
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                    for (HashMap z : zbdic) {
                        String mlid = (String) z.get("ZHZBID");
                        List c = zbchildren.get(mlid);
                        c.add(z);
                    }
                    Map<String, Object> docStr = AppUtils.getMlMap(lsHt, children, rootId, zbchildren);
                    String xml = "";
                    Document doc = XmlUtils.Map2Xml(docStr);
                    String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcelfPath + "/" + rootId;
                    xml = XmlUtils.FormatXml(doc);
                    FileUtils.writeToFile(saveDirectoryPath, xml, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody delElf4Ml(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                User user = UserUtils.getUser();
                condition.setDwdm(user.getDeptCode().substring(0, 4));
                Map m = new HashMap();
                bczbMapper.delElf4Ml(condition);
                {
                    m = new HashMap();
                    m.put("CODE", condition.getDwdm());
                    HashMap<String, Office> lsHt = frameMapper.bcelfMlLists(m);
                    List<HashMap> zbdic = frameMapper.getMlElfAList(m);
                    LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                    HashMap<String, List<Office>> children = new HashMap();
                    Map<String, List<Map>> zbchildren = new HashMap();
                    String rootId = "";
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                    for (HashMap z : zbdic) {
                        String mlid = (String) z.get("ZHZBID");
                        List c = zbchildren.get(mlid);
                        c.add(z);
                    }
                    Map<String, Object> docStr = AppUtils.getMlMap(lsHt, children, rootId, zbchildren);
                    String xml = "";
                    Document doc = XmlUtils.Map2Xml(docStr);
                    String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcelfPath + "/" + rootId;
                    xml = XmlUtils.FormatXml(doc);
                    FileUtils.writeToFile(saveDirectoryPath, xml, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody delGcf4Ml(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                User user = UserUtils.getUser();
                condition.setDwdm(user.getDeptCode().substring(0, 4));
                Map m = new HashMap();
                bczbMapper.delGcf4Ml(condition);
                {
                    m = new HashMap();
                    m.put("CODE", condition.getDwdm());
                    HashMap<String, Office> lsHt = frameMapper.bcgcfMlLists(m);
                    List<HashMap> zbdic = frameMapper.getMlGcfAList(m);
                    LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                    HashMap<String, List<Office>> children = new HashMap();
                    Map<String, List<Map>> zbchildren = new HashMap();
                    String rootId = "";
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                    for (HashMap z : zbdic) {
                        String mlid = (String) z.get("ZHZBID");
                        List c = zbchildren.get(mlid);
                        c.add(z);
                    }
                    Map<String, Object> docStr = AppUtils.getMlMap(lsHt, children, rootId, zbchildren);
                    String xml = "";
                    Document doc = XmlUtils.Map2Xml(docStr);
                    String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcgcfPath + "/" + rootId;
                    xml = XmlUtils.FormatXml(doc);
                    FileUtils.writeToFile(saveDirectoryPath, xml, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody xfElf(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                User user = UserUtils.getUser();
                condition.setDwdm(user.getDeptCode().substring(0, 4));
                Map m = new HashMap();
                //System.out.println("6666666666666666"+m);
                {
                    m = new HashMap();
                    m.put("CODE", condition.getDwdm());
                    HashMap<String, Office> lsHt = frameMapper.bcelfMlLists(m);
                    List<HashMap> zbdic = frameMapper.getMlElfAList(m);
                    LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                    HashMap<String, List<Office>> children = new HashMap();
                    Map<String, List<Map>> zbchildren = new HashMap();
                    String rootId = "";
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                    for (HashMap z : zbdic) {
                        String mlid = (String) z.get("ZHZBID");
                        List c = zbchildren.get(mlid);
                        c.add(z);
                    }
                    Map<String, Object> docStr = AppUtils.getMlMap(lsHt, children, rootId, zbchildren);
                    String xml = "";
                    Document doc = XmlUtils.Map2Xml(docStr);
                    //String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcelfPath + "/" + rootId;
                    String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcelfPath + "/" + user.getDeptId();
                    xml = XmlUtils.FormatXml(doc);
                    FileUtils.writeToFile(saveDirectoryPath, xml, false);
                    m.put("path", "/" + dwbcelfPath + "/" + user.getDeptId());
                    m.put("userid", user.getUserid());
                    m.put("deptid", condition.getDwdm());
                    bczbMapper.xfElf(m);
                }
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody xfGcf(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                User user = UserUtils.getUser();
                condition.setDwdm(user.getDeptCode().substring(0, 4));
                Map m = new HashMap();
                //System.out.println("6666666666666666"+m);
                {
                    m = new HashMap();
                    m.put("CODE", condition.getDwdm());
                    HashMap<String, Office> lsHt = frameMapper.bcgcfMlLists(m);
                    List<HashMap> zbdic = frameMapper.getMlGcfAList(m);
                    LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                    HashMap<String, List<Office>> children = new HashMap();
                    Map<String, List<Map>> zbchildren = new HashMap();
                    String rootId = "";
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                    for (HashMap z : zbdic) {
                        String mlid = (String) z.get("ZHZBID");
                        List c = zbchildren.get(mlid);
                        c.add(z);
                    }
                    Map<String, Object> docStr = AppUtils.getMlMap(lsHt, children, rootId, zbchildren);
                    String xml = "";
                    Document doc = XmlUtils.Map2Xml(docStr);
                    //String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcelfPath + "/" + rootId;
                    String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcgcfPath + "/" + user.getDeptId();
                    xml = XmlUtils.FormatXml(doc);
                    FileUtils.writeToFile(saveDirectoryPath, xml, false);
                    m.put("path", "/" + dwbcgcfPath + "/" + user.getDeptId());
                    m.put("userid", user.getUserid());
                    m.put("deptid", condition.getDwdm());
                    bczbMapper.xfElf(m);
                }
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody addZb2Ml(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC1());
                String c2 = condition.getC2();
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                Map mw = new HashMap();
                mw.put("list", h);
                mw.put("fzid", c2);
                createOrUpdateZbXML(condition.getC1());
                bczbMapper.addZb2Ml(mw);
                createOrReloadTreeXml(rp,condition);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody addZb2MlSbzc(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                bczbMapper.addZb2MlSbzc(condition);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody updSbzc(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                bczbMapper.updSbzc(condition);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody addZb2ZbSbzc(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                bczbMapper.addZb2ZbSbzc(condition);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody addZb2Zb(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC1());
                String c2 = condition.getC2(); //指标关系表中ID
                String c4 = condition.getC4(); //是更新还是
                String c5 = condition.getC5(); //更新的油区ID
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                Map mw = new HashMap();
                mw.put("list", h);
                mw.put("fzid", c2);
                if("updateOilArea".equals(c4)) {
                	bczbMapper.deleteSourceZb(condition);
                	bczbMapper.deleteOldZbRelation(condition);
                }
                bczbMapper.addZb2Zb(mw);
                condition.setC15(condition.getC2());//右侧树中指标ID,根据此ID,查询原指标ID
                condition.setC16(condition.getC3());//将原指标ID  改为此名称
                List<String> zbs = bczbMapper.getZbsBymainId(condition);//1、先查询出所此条记录所有的保存指标ID
            	if(!zbs.isEmpty() && zbs.size() > 0) {
            		for (int i = 0; i < zbs.size(); i++) {//2、循环遍历  保存指标ID  后根据指标id进行更新
            			String zbid = zbs.get(i);
            			condition.setC11(zbid);
            			bczbMapper.updateRealZbmc(condition);
					}
            	}
                //System.out.println("6666666666666666"+m);
                {
                    for (HashMap mmm : h) {
                        String root = (String) mmm.get("ID");
                        LinkedHashMap<String, LinkedHashMap> treeM = new LinkedHashMap<>();
                        List<LinkedHashMap> treeMm = bczbMapper.getDwBczbTreeById(root);
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
                                for (String mmmn : pl) {
                                    mm.put("@" + mmmn, m.get("P_" + mmmn.toUpperCase()) == null ? "" : m.get("P_" + mmmn.toUpperCase()));
                                }
                            else {
                                //System.out.println(m.get("OTYPE"));
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
                        Document doc = DocumentHelper.createDocument();
                        Element ls = DocumentHelper.createElement("DocumentElement");
                        doc.add(ls);
                        //String rootId = (String) propertyM.get(root).get("SERVERID");
                        String rootId = "";
                        if (StringUtils.isBlank(rootId))
                            rootId = root;
                        try {
                            Document zb = XmlUtils.Map2Xml(docStr);
                            ls.add(zb.getRootElement());
                            String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbczbPath + "/" + rootId;
                            String xml = XmlUtils.FormatXml(doc);
                            FileUtils.writeToFile(saveDirectoryPath, xml, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                /*{
                    for (HashMap mmm : h) {

                        String root = (String) mmm.get("ID");
                        LinkedHashMap<String, LinkedHashMap> treeM = new LinkedHashMap<>();
                        List<LinkedHashMap> treeMm = bczbMapper.getDwBczbTreeById(root);
                        for (LinkedHashMap t : treeMm) {
                            treeM.put((String) t.get("ID"), t);
                        }
                        List<LinkedHashMap> propertyL = bczbMapper.getDwBczbProperty(root);
                        Map<String, Map> propertyM = new HashMap<String, Map>();
                        for (LinkedHashMap mnn : propertyL) {
                            Map<String, Object> p = propertyM.get((String) mnn.get("OID"));
                            if (p == null) {
                                p = new LinkedHashMap<String, Object>();
                                propertyM.put((String) mnn.get("OID"), p);
                            }
                            p.put((String) mnn.get("OKEY"), (String) mnn.get("OVALUE") == null ? "" : (String) mnn.get("OVALUE"));
                        }
                        Map<String, List> tree = new LinkedHashMap<String, List>();
                        for (String key : treeM.keySet()) {
                            LinkedHashMap mtt = treeM.get(key);
                            List children = tree.get(mtt.get("PID"));
                            if (children == null) {
                                children = new ArrayList();
                                tree.put((String) mtt.get("PID"), children);
                            }
                            children.add(treeM.get(key));
                        }
                        Map<String, Object> docStr = AppUtils.readSql2Map(treeM, tree, root, propertyM);
                        Document doc = DocumentHelper.createDocument();
                        Element ls = DocumentHelper.createElement("DocumentElement");
                        doc.add(ls);
                        String rootId = (String) propertyM.get(root).get("SERVERID");
                        if (StringUtils.isBlank(rootId))
                            rootId = root;
                        try {
                            Document zb = XmlUtils.Map2Xml(docStr);
                            ls.add(zb.getRootElement());
                            String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbczbPath + "/" + rootId;
                            String xml = XmlUtils.FormatXml(doc);
                            FileUtils.writeToFile(saveDirectoryPath, xml, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }*/
                
                
                createOrReloadTreeXml(rp,condition);
                //原来的设计
                //region
                /*{
                    try {
                        User user = UserUtils.getUser();
                        condition.setUserid(user.getId());
                        condition.setDwdm(user.getDeptCode().substring(0, 4));

                        Map mtt = new HashMap();
                        mtt.put("CODE", condition.getDwdm());
                        HashMap<String, Office> lsHt = frameMapper.bczbMlLists(mtt);
                        List<HashMap> zbdic = frameMapper.getMlZbAList(mtt);
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
                        //System.out.println("-----"+children);
                        Map<String, Object> docStr = AppUtils.getMlMap(lsHt, children, rootId, zbchildren);
                        String xml = "";
                        Document doc = XmlUtils.Map2Xml(docStr);

                        String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbczbPath + "/" + rootId;
                        xml = XmlUtils.FormatXml(doc);
                        FileUtils.writeToFile(saveDirectoryPath, xml, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        rp.setIssuccess("0");
                        rp.setMessage("操作失败！" + e.getMessage());
                    }
                }*/
                //endregion
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody delZb2Fz(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC1());
                String c2 = condition.getC2();
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                Map m = new HashMap();
                m.put("list", h);
                m.put("fzid", c2);
                bczbMapper.delZb2Fz(m);
                createOrReloadTreeXml(rp,condition);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    //补充指标删除
    public ResponseBody delBCZB(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC1());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                Map m = new HashMap();
                m.put("list", h);
                bczbMapper.delBCZB(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody delZb2Ml(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String c1 = StringEscapeUtils.unescapeHtml(condition.getC1());
                String c2 = condition.getC2();
                String isZb = condition.getC3();
                Map m = new HashMap();
                if (StringUtils.isBlank(c1)) {
                    return rp;
                }
                m.put("oilid", c1.substring(1));
                m.put("fzid", c2);
                if("yes".equals(isZb)) {
                	bczbMapper.delZb2Ml(m);
                }else{
                	bczbMapper.delZbml(m);
                }
                
                //原来的设计//region
                createOrReloadTreeXml(rp,condition);
                //endregion
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    
    public ResponseBody mlContainsZb(RequestBody rq, Map params, String id) {
    	 ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
    	 String returnData =  "";
    	try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            
            try {
                String c1 = condition.getC1();
                Map<String,String> parm = Maps.newHashMap();
                if (StringUtils.isBlank(c1)) {
                    return rp;
                }
                parm.put("id", c1);
                List<String> mlTreeList = bczbMapper.getMlTreeListById(parm);
                
                if(mlTreeList.contains("2") && mlTreeList.contains("9")) {
           		 	returnData = "no";
                }else if(mlTreeList.contains("2") && !mlTreeList.contains("9")){
                	returnData = "yesml";
                }else if(!mlTreeList.contains("2") && mlTreeList.contains("9")){
                	returnData = "yeszb";
                }else {
//                	System.out.println("=======Error Info============");
//                	System.out.println("mz and zb type is not right! ");
//                	System.out.println("=======Error Info============");
                }
                rp.setDatas(returnData);
            }catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
    	}catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
    	
    	return rp;
    }
    
    public ResponseBody bczcMlIsContainsZb(RequestBody rq, Map params, String id) {
   	 	ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
   	 	String returnData =  "";
   	 	try {
   	 			ObjectMapper objectMapper = new ObjectMapper();
   	 			Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
	            try {
	               String mlId = condition.getC1();
	               String bbh = condition.getC2();
	               String type = condition.getC3();
	               String parentId = condition.getC4();
	               String code = condition.getC5();
	               Map<String,String> parm = Maps.newHashMap();
	               parm.put("id", mlId);
	               parm.put("bbh", bbh);
	               parm.put("parentId", parentId);
	               Integer bczcNum = null;
	               
	               if("10".equals(type)) {//如果是指标
	            	   bczcNum = frameMapper.getRelationNum(parm);//获取补充主材的数量
	               }else if("2".equals(type)) {//如果是目录
	            	   if(code.length() < 6 ) {
	            		   bczcNum = frameMapper.getMlChildrenZbCountByParentId(parm);
		               }else {
		            	   bczcNum = frameMapper.getMlChildrenZbCountById(parm);
		               }
	               }
	               
	               if(bczcNum > 0) {
	            	   returnData = "no";
	               }else {
	            	   returnData = "yes";
	            	   //执行删除目录和指标的关系
	            	   if("2".equals(type)) {
	            		   if(code.length() < 6 ) {
	            			   frameMapper.cascadeDeleteMlById(parm);
			               }else {
			            	   frameMapper.deleteMlById(parm);
			               }
//	            		   frameMapper.deleteMlRealtion(parm); 
	            	   }else if("10".equals(type)) {
	            		   frameMapper.deleteZbById(parm);
	            	   }
	               }
	               rp.setDatas(returnData);
	           }catch (Exception e) {
	               e.printStackTrace();
	               rp.setIssuccess("0");
	               rp.setMessage("操作失败！" + e.getMessage());
	           }
	   	}catch (Exception e) {
	           rp.setIssuccess("0");
	           rp.setMessage("操作失败！" + e.getMessage());
	           e.printStackTrace();
	    }
   	 	return rp;
   }    
    
    
    public ResponseBody oilAreaIsExist(RequestBody rq, Map params, String id) {
   	 ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
   	 String returnData =  "";
   	try {
           ObjectMapper objectMapper = new ObjectMapper();
           Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
           
           try {
               String c1 = condition.getC1();
               String insertOilArea = condition.getC2();
               Map<String,String> parm = Maps.newHashMap();
               if (StringUtils.isBlank(c1)) {
                   return rp;
               }
               parm.put("zbid", c1);
               List<String> mlTreeList = bczbMapper.getOilAreasByZbId(parm);

               if(mlTreeList.contains(insertOilArea)) {
            	   returnData = "updateOilArea";
               }else {
            	   returnData = "addOilArea";
               }
               rp.setDatas(returnData);
           }catch (Exception e) {
               e.printStackTrace();
               rp.setIssuccess("0");
               rp.setMessage("操作失败！" + e.getMessage());
           }
   	}catch (Exception e) {
           rp.setIssuccess("0");
           rp.setMessage("操作失败！" + e.getMessage());
           e.printStackTrace();
       }
   	
   	return rp;
   }
    
    

    public ResponseBody delElf2Ml(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String c1 = StringEscapeUtils.unescapeHtml(condition.getC1());
                String c2 = condition.getC2();
                Map m = new HashMap();
                if (StringUtils.isBlank(c1)) {
                    return rp;
                }
                m.put("fzid", c2);
                //System.out.println("-------------------"+m);
                bczbMapper.delElf2Ml(m);
                //原来的设计//region
                {
                    try {
                        User user = UserUtils.getUser();
                        condition.setUserid(user.getId());
                        condition.setDwdm(user.getDeptCode().substring(0, 4));
                        Map mtt = new HashMap();
                        mtt.put("CODE", condition.getDwdm());
                        HashMap<String, Office> lsHt = frameMapper.bcelfMlLists(mtt);
                        List<HashMap> zbdic = frameMapper.getMlElfAList(mtt);
                        LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                        HashMap<String, List<Office>> children = new HashMap();
                        Map<String, List<Map>> zbchildren = new HashMap();
                        String rootId = "";
                        for (String key : lsHt.keySet()) {
                            if (lsHt.get(key).getCode().length() == 8) rootId = key;
                            List c = children.get(lsHt.get(key).getParentId());
                            zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                            if (c == null) {
                                c = new ArrayList<Office>();
                                children.put((String) lsHt.get(key).getParentId(), c);
                            }
                            c.add(lsHt.get(key));
                        }
                        for (HashMap z : zbdic) {
                            String mlid = (String) z.get("ZHZBID");
                            List c = zbchildren.get(mlid);
                            c.add(z);
                        }
                        Map<String, Object> docStr = AppUtils.getMlMap(lsHt, children, rootId, zbchildren);
                        String xml = "";
                        Document doc = XmlUtils.Map2Xml(docStr);
                        String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcelfPath + "/" + rootId;
                        xml = XmlUtils.FormatXml(doc);
                        FileUtils.writeToFile(saveDirectoryPath, xml, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        rp.setIssuccess("0");
                        rp.setMessage("操作失败！" + e.getMessage());
                    }
                }
                //endregion
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody delSbzc2Ml(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String c1 = StringEscapeUtils.unescapeHtml(condition.getC1());
                String c2 = condition.getC2();
                Map m = new HashMap();

                m.put("bcid", c2);
                m.put("BBH", condition.getC11());
                //System.out.println("-------------------"+m);
                bczbMapper.delSbzcZB(m);
                String mlId = bczbMapper.getMlidByParam(m);
                bczbMapper.delSbzc2Ml(m);
                m.put("zhid", mlId);
                if(bczbMapper.getMlRelationNum(m) == 0 ) {
                	bczbMapper.delMlWhenNull(m);
                }
                //原来的设计
                //region
                {
                    try {
                        User user = UserUtils.getUser();
                        condition.setUserid(user.getId());
                        condition.setDwdm(user.getDeptCode().substring(0, 4));
                        Map mtt = new HashMap();
                        mtt.put("CODE", condition.getDwdm());
                        mtt.put("BBH", condition.getC11());
                        HashMap<String, Office> lsHt = frameMapper.bcsbzcMlLists(mtt);
                        List<HashMap> zbdic = frameMapper.getMlSbzcAList(mtt);
                        LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                        HashMap<String, List<Office>> children = new HashMap();
                        Map<String, List<Map>> zbchildren = new HashMap();
                        String rootId = "";
                        for (String key : lsHt.keySet()) {
                            if (lsHt.get(key).getCode().length() == 8) rootId = key;
                            List c = children.get(lsHt.get(key).getParentId());
                            zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                            if (c == null) {
                                c = new ArrayList<Office>();
                                children.put((String) lsHt.get(key).getParentId(), c);
                            }
                            c.add(lsHt.get(key));
                        }
                        for (HashMap z : zbdic) {
                            String mlid = (String) z.get("ZHID");
                            List c = zbchildren.get(mlid);
                            c.add(z);
                        }
                        Map<String, Object> docStr = AppUtils.getMlMap(lsHt, children, rootId, zbchildren);
                        String xml = "";
                        Document doc = XmlUtils.Map2Xml(docStr);
                        String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcsbzcPath + "/" + rootId;
                        xml = XmlUtils.FormatXml(doc);
                        FileUtils.writeToFile(saveDirectoryPath, xml, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        rp.setIssuccess("0");
                        rp.setMessage("操作失败！" + e.getMessage());
                    }
                }
                //endregion
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody delFz(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC1());
                String c2 = condition.getC2();
                bczbMapper.delFz(c2);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    //更新
    public ResponseBody gxList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取单位补充指标更新列表成功", id, rq.getTaskid());
        User user = UserUtils.getUser();
        String rootMl = bczbMapper.getBczbRootByCode(user.getDeptCode().substring(0, 4));

        Map pp = new HashMap();
        pp.put("CODE", user.getDeptCode().substring(0, 4));
        List<HashMap> ml = frameMapper.getMlZbAList(pp);
        try {
            Map rrr = new HashMap();
            rrr.put("ML", rootMl);
            rrr.put("BCZB", ml);
            rp.setDatas(JsonMapper.toJsonString(rrr));
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取单位补充指标失败");
        }
        return rp;
    }

    public ResponseBody gxSbzcList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取单位补充补充设备主材更新列表成功", id, rq.getTaskid());
        User user = UserUtils.getUser();
        String rootMl = bczbMapper.getBcSbzcRootByCode(user.getDeptCode().substring(0, 4), "");

        Map pp = new HashMap();
        pp.put("CODE", user.getDeptCode().substring(0, 4));
        List<HashMap> ml = frameMapper.getMlSbzcAList(pp);
        try {
            Map rrr = new HashMap();
            rrr.put("ML", rootMl);
            rrr.put("BCZB", ml);
            rp.setDatas(JsonMapper.toJsonString(rrr));
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取单位补充指标失败");
        }
        return rp;

    }

    //补充设备主材
    public ResponseBody bcsbzcList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取设备主材列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                User user = UserUtils.getUser();
                condition.setDwdm(user.getDeptCode().substring(0, 4));
                List<HashMap> lsHt = bczbMapper.getBcsbzcList(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", bczbMapper.getBcsbzcList(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取设备主材列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取设备主材列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody bcsbzcByMlList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取设备主材列表成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            Map pa = new HashMap();
            pa.put("c1", condition.getC1());
            //params.put("DEPTID", "1045");
            try {
                List<HashMap> bbls = bczbMapper.getBcsbzcByMl(pa);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", bczbMapper.getBcsbzcByMl(pa).size());
                r.put("rows", bbls);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取设备主材列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取设备主材列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody xtxmglmlsave(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
                try {
                    Office office = new Office();
                    office.setId(condition.getC1());
                    office.setName(condition.getC2());
                    office.setRemarks(condition.getC3());
                    office.setParentId(condition.getC4());
                    frameMapper.saveMypan(office);
                } catch (Exception e) {
                    e.printStackTrace();
                    rp.setIssuccess("0");
                    rp.setMessage("操作失败！" + e.getMessage());
                }
            } catch (Exception e) {
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody xtxmsave(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
                try {
                    Office office = new Office();
                    office.setId(condition.getC1());
                    office.setName(condition.getC2());
                    office.setRemarks(condition.getC3());
                    office.setParentId(condition.getC4());
                    frameMapper.saveXtxm(office);
                } catch (Exception e) {
                    e.printStackTrace();
                    rp.setIssuccess("0");
                    rp.setMessage("操作失败！" + e.getMessage());
                }
            } catch (Exception e) {
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody bcsbzcmlsave(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
                try {
                    User user = UserUtils.getUser();
                    condition.setUserid(user.getId());
                    condition.setDwdm(user.getDeptCode().substring(0, 4));
                    frameMapper.bcsbzcmlsave(condition);
                    Map m = new HashMap();
                    m.put("CODE", condition.getDwdm());
                    m.put("BBH", condition.getC21());
                    HashMap<String, Office> lsHt = frameMapper.bcsbzcMlLists(m);
                    List<HashMap> zbdic = frameMapper.getMlSbzcAList(m);
                    LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                    HashMap<String, List<Office>> children = new HashMap();
                    Map<String, List<Map>> zbchildren = new HashMap();
                    String rootId = "";
                    for (String key : lsHt.keySet()) {
                        if (lsHt.get(key).getCode().length() == 8) rootId = key;
                        List c = children.get(lsHt.get(key).getParentId());
                        zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                        if (c == null) {
                            c = new ArrayList<Office>();
                            children.put((String) lsHt.get(key).getParentId(), c);
                        }
                        c.add(lsHt.get(key));
                    }
                    for (HashMap z : zbdic) {
                        String mlid = (String) z.get("ZHID");
                        List c = zbchildren.get(mlid);
                        c.add(z);
                    }
                    Map<String, Object> docStr = AppUtils.getMlMap(lsHt, children, rootId, zbchildren);
                    String xml = "";
                    Document doc = XmlUtils.Map2Xml(docStr);
                    String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbcsbzcPath + "/" + rootId;
                    xml = XmlUtils.FormatXml(doc);
                    FileUtils.writeToFile(saveDirectoryPath, xml, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    rp.setIssuccess("0");
                    rp.setMessage("操作失败！" + e.getMessage());
                }
            } catch (Exception e) {
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody sbzcbbList(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取数据列表", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            try {
                condition.setDwdm(user.getDeptCode().substring(0, 4));
                List<HashMap> lsHt = bbglMapper.getBbsbzclist(condition);
                HashMap r = new HashMap();
                condition.setStart(null);
                r.put("total", bbglMapper.getBbsbzclist(condition).size());
                r.put("rows", lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody bbsbzcsave(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                m.put("dwdm", user.getDeptCode().substring(0, 4));
                m.put("submitter", UserUtils.getUser().getId());
                bbglMapper.saveBbsbzc(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody bbsbzcxf(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                bbglMapper.xfSbzcxt(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody bbsbzcdel(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            String bbh = condition.getC10();
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                bbglMapper.delBbsbzc(m);
                bbglMapper.delBbsbzcMlByBbh(bbh);
                bbglMapper.delBbsbzcZbByBbh(bbh);
                bbglMapper.delBbsbzcRelationByBbh(bbh);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody jc(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "继承成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            User user = UserUtils.getUser();
            try {
                HashMap m = new HashMap();
                m.put("BBH", condition.getC21());
                m.put("DEPTID", user.getDeptCode().substring(0, 4));
                bbglMapper.jcBbsbzc(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
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

    public ResponseBody testNormal(RequestBody rq, Map params, String id) {
        ResponseBody r = new ResponseBody();


        return r;
    }

    /*张*/
    //region
    public  ResponseBody  yqList(RequestBody  rq,  Map  params,  String  id)  {
        ResponseBody  rp  =  new  ResponseBody(params,  "1",  "获取数据列表",  id,  rq.getTaskid());
        try  {
            ObjectMapper  objectMapper  =  new  ObjectMapper();
            Condition  condition  =  objectMapper.readValue(rq.getParams(),  Condition.class);
            try  {
                List<HashMap>  lsHt  =  bbglMapper.getyqlist(condition);
                HashMap  r  =  new  HashMap();
                condition.setStart(null);
                r.put("total",  bbglMapper.getyqlist(condition).size());
                r.put("rows",  lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            }  catch  (Exception  e)  {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！"  +  e.getMessage());
            }
        }  catch  (Exception  e)  {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！"  +  e.getMessage());
            e.printStackTrace();
        }
        return  rp;
    }

    public  ResponseBody  yqbbList(RequestBody  rq,  Map  params,  String  id)  {
        ResponseBody  rp  =  new  ResponseBody(params,  "1",  "获取数据列表",  id,  rq.getTaskid());
        try  {
            ObjectMapper  objectMapper  =  new  ObjectMapper();
            Condition  condition  =  objectMapper.readValue(rq.getParams(),  Condition.class);
            try  {
                List<HashMap>  lsHt  =  bbglMapper.getyqbblist(condition);
                HashMap  r  =  new  HashMap();
                condition.setStart(null);
                r.put("total",  bbglMapper.getyqbblist(condition).size());
                r.put("rows",  lsHt);
                rp.setDatas(com.common.annotation.mapper.JsonMapper.toJsonString(r));
            }  catch  (Exception  e)  {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("获取数据列表失败！"  +  e.getMessage());
            }
        }  catch  (Exception  e)  {
            rp.setIssuccess("0");
            rp.setMessage("获取数据列表失败！"  +  e.getMessage());
            e.printStackTrace();
        }
        return  rp;
    }
    public  ResponseBody  yqsave(RequestBody  rq,  Map  params,  String  id)  {
        ResponseBody  rp  =  new  ResponseBody(params,  "1",  "保存成功！",  id,  rq.getTaskid());
        try  {
            ObjectMapper  objectMapper  =  new  ObjectMapper();
            Condition  condition  =  objectMapper.readValue(rq.getParams(),  Condition.class);
            try  {
                String  updL  =  StringEscapeUtils.unescapeHtml(condition.getC6());
                JavaType  javaType  =  JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class,  HashMap.class);
                List<HashMap>  h  =  JsonMapper.getInstance().fromJson(updL,  javaType);
                HashMap  m  =  new  HashMap();
                m.put("list",  h);
                m.put("submitter",  UserUtils.getUser().getId());
                bbglMapper.saveYq(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody yqbbsave(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "保存成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC6());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                m.put("submitter", UserUtils.getUser().getId());
                bbglMapper.saveYqbb(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    public ResponseBody yqdel(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "删除成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                bbglMapper.delyq(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }

    public ResponseBody yqbbdel(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "删除成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
                String updL = StringEscapeUtils.unescapeHtml(condition.getC2());
                JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
                List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
                HashMap m = new HashMap();
                m.put("list", h);
                bbglMapper.delyqbb(m);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    //endregion


    public static void main(String[] args) {
        try {
            //System.out.println(System.getProperty("java.library.path"));
            //调用方法
            CLibrary.sdtapi.fn_StartBegin();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    
  //补充指标统计采集
    public ResponseBody bczbtjCj(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "获取成功", id, rq.getTaskid());
        try {
            User user = UserUtils.getUser();
            String textFromFile = "";
            BASE64Decoder decoder = new BASE64Decoder();
            try {
            	textFromFile = new String(decoder.decodeBuffer((String) params.get("BCZBDATA")), "UTF-8");
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
    
    /**
     * 生成单位补充指标模块，右侧目录树XML文件
     * */
    public void createOrReloadTreeXml(ResponseBody rp, Condition condition){
            try {
                User user = UserUtils.getUser();
                condition.setUserid(user.getId());
                condition.setDwdm(user.getDeptCode().substring(0, 4));
                Map mtt = new HashMap();
                mtt.put("CODE", condition.getDwdm());
                HashMap<String, Office> lsHt = frameMapper.bczbMlLists(mtt);
                List<HashMap> zbdic = frameMapper.getMlZbAList(mtt);
                LinkedHashMap<String, LinkedHashMap<String, Object>> rs = new LinkedHashMap();
                HashMap<String, List<Office>> children = new HashMap();
                Map<String, List<Map>> zbchildren = new HashMap();
                String rootId = "";
                for (String key : lsHt.keySet()) { 
                    if (lsHt.get(key).getCode().length() == 8) rootId = key;
                    List c = children.get(lsHt.get(key).getParentId());
                    zbchildren.put(lsHt.get(key).getId(), new ArrayList());
                    if (c == null) {
                        c = new ArrayList<Office>();
                        children.put((String) lsHt.get(key).getParentId(), c);
                    }
                    c.add(lsHt.get(key));
                }
                for (HashMap z : zbdic) {
                    String mlid = (String) z.get("ZHZBID");
                    List c = zbchildren.get(mlid);
                    c.add(z);
                }
                Map<String, Object> docStr = AppUtils.getMlMap(lsHt, children, rootId, zbchildren);
                HashMap xmlData = (HashMap)docStr.get("BCZBLocalClass");
                List xmlContainsMlList = (List)((HashMap)xmlData.get("BCZBLocalClasses")).get("BCZBLocalClass");//1级目录
                orderDwbczbTreeXml(xmlData,xmlContainsMlList);
                
                String xml = "";
                Document doc = XmlUtils.Map2Xml(docStr);
                String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbczbPath + "/" + rootId;
                xml = XmlUtils.FormatXml(doc);
                FileUtils.writeToFile(saveDirectoryPath, xml, false);
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
    }
    
    public void orderDwbczbTreeXml(HashMap xmlData,List xmlContainMlList) {
    	if(xmlContainMlList != null && xmlContainMlList.size() > 0) {
			List dwbczbTreeXml = dwbczbTreeXmlList(xmlContainMlList);
            ((HashMap)xmlData.get("BCZBLocalClasses")).put("BCZBLocalClass", dwbczbTreeXml);
        	for (int k = 0; k < xmlContainMlList.size(); k++) {
        		HashMap xmlMap = (HashMap)xmlContainMlList.get(k);//指标目录
        		List xmlMapContainsList = (List)((HashMap)xmlMap.get("BCZBLocalClasses")).get("BCZBLocalClass");
        		orderDwbczbTreeXml(xmlMap,xmlMapContainsList);
        	}
        }
    }
    
    public List dwbczbTreeXmlList(List xmlDataList) {
    	List newResultList = new ArrayList<>();
		List<String> codeList = new ArrayList<String>();
		if(xmlDataList != null && xmlDataList.size() > 0) {
        	for (int i = 0; i < xmlDataList.size(); i++) {
        		HashMap zbxmlMl = (HashMap)xmlDataList.get(i);//指标
        		String code = String.valueOf(zbxmlMl.get("BzCode"));
        		codeList.add(code);
        	}
        	Collections.sort(codeList); 
        	
        	for (int j = 0; j < codeList.size(); j++) {
				String orderCode = codeList.get(j);
				for (int p = 0; p < xmlDataList.size(); p++) {
					HashMap zbMl = (HashMap)xmlDataList.get(p);
    				String code = String.valueOf(zbMl.get("BzCode"));
    				if(orderCode.equals(code)) {
    					newResultList.add(zbMl);
    				}
				}
			}
        }
    	return newResultList;
    }
    
    /**
     * 生成单位补充指标模块，右侧目录树中的指标XML文件
     * */
    public void createOrUpdateZbXML(String xmlInfo) {
    	 String updL = StringEscapeUtils.unescapeHtml(xmlInfo);
         JavaType javaType = JsonMapper.getInstance().getTypeFactory().constructParametricType(List.class, HashMap.class);
         List<HashMap> h = JsonMapper.getInstance().fromJson(updL, javaType);
            for (HashMap mmm : h) {
                String root = (String) mmm.get("ID");
                LinkedHashMap<String, LinkedHashMap> treeM = new LinkedHashMap<>();
                List<LinkedHashMap> treeMm = bczbMapper.getDwBczbTreeById(root);
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
                        for (String mmmn : pl) {
                            mm.put("@" + mmmn, m.get("P_" + mmmn.toUpperCase()) == null ? "" : m.get("P_" + mmmn.toUpperCase()));
                        }
                    else {
                        //System.out.println(m.get("OTYPE"));
                    }
                }
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
                Document doc = DocumentHelper.createDocument();
                Element ls = DocumentHelper.createElement("DocumentElement");
                doc.add(ls);
                String rootId = "";
                if (StringUtils.isBlank(rootId))
                    rootId = root;
                try {
                    Document zb = XmlUtils.Map2Xml(docStr);
                    ls.add(zb.getRootElement());
                    String saveDirectoryPath = Global.getConfig("upLoadPath") + "/" + dwbczbPath + "/" + rootId;
                    String xml = XmlUtils.FormatXml(doc);
                    FileUtils.writeToFile(saveDirectoryPath, xml, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    }
    
    public ResponseBody importBcsbzcExcel(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "导入成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
            	String c1 = condition.getC1();
            	List<String> listData = Arrays.asList(c1.split("&"));
            	String bbh = condition.getC2();
            	String mlParentId = condition.getC3();
            	Map<String,String> codeParam = new HashMap<String, String>();
            	codeParam.put("bbh", bbh);
            	codeParam.put("id", mlParentId);
            	for (int i = 0; i < listData.size(); i++) {
            		String string = listData.get(i);
            		Map<String, String> importData = getStringToMap(string);//导入的数据
            		List<Map<String, String>> imputZbDataList = getImputZbDataList(importData);//获取到指标的数据
            		Map<String, String> mlData = new HashMap<String, String>();//导入的指标数据形成目录
            		Map<String, String> realtionData = new HashMap<String, String>();//导入的指标数据形成目录
            		
            		//编码
            		String bm = bczbMapper.getAddCodeByParam(codeParam);
            		if(StringUtils.isBlank(bm)) {
            			bm = bczbMapper.getNewCodeByParam(codeParam);
            		}
            		
            		String mlid = UUID.randomUUID().toString().replaceAll("-", "");//UUID生成 导入数据的ID
            		mlData.put("id", mlid);
            		mlData.put("bbh", bbh);
            		mlData.put("code", bm);
            		mlData.put("name", importData.get("mc"));
            		mlData.put("parent_id", mlParentId);
            		mlData.put("lb", importData.get("lb"));
            		mlData.put("type", "10");
            		mlData.put("useable", "1");
            		
            		frameMapper.saveBcsbzcMl(mlData);//保存导入的Excel数据到····目录表
            		
            		for (int j = 0; j < imputZbDataList.size(); j++) {//保存指标数据、保存关系数据
            			Map<String, String> zbMap = imputZbDataList.get(j);
            			String zbid = UUID.randomUUID().toString().replaceAll("-", "");//UUID生成 导入数据的ID
            			
            			zbMap.put("bbh", bbh);
            			zbMap.put("id", zbid);
            			zbMap.put("bm", bm);
            			zbMap.put("mc", importData.get("mc"));
            			zbMap.put("xh", importData.get("xh"));
            			zbMap.put("dw", importData.get("dw"));
            			zbMap.put("lb", "设备".equals(importData.get("lb")) ? "SB" : "ZC");

            			Condition condition2 = new Condition();
                		condition2.setC1(zbMap.get("yq"));
                		String yqid = jqsqMapper.getYqidByName(condition2);//油区ID
                		zbMap.put("oilid", yqid);
                		
                		realtionData.put("code", bm);
                		realtionData.put("oilid", yqid);
                		realtionData.put("bbh", bbh);
                		realtionData.put("zhid", mlid);
                		realtionData.put("bcid", zbid);
                		
                		frameMapper.addBcsbzcZb(zbMap);//保存导入的Excel数据到····指标表
                		frameMapper.saveBcsbzcMlZbRealtion(realtionData);//保存导入的Excel数据到····指标-目录关系表
					}
				}
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    
    
    public ResponseBody updateBcsbzcExcel(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "导入成功！", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
            	String c1 = condition.getC1();
            	String bbh = condition.getC2();
            	List<String> listData = Arrays.asList(c1.split("&"));
            	
            	
            	for (int i = 0; i < listData.size(); i++) {
            		String string = listData.get(i);
            		Map<String, String> importData = getStringToMap(string);//导入的数据
            		
            		String bm = importData.get("bm");
            		String mlId = frameMapper.getMlIdByParam(bm,bbh);
            		Map<String, String> mlDataMap = new HashMap<String, String>();
            		List<Map<String, String>> imputZbDataList = getImputZbDataList(importData);//获取到指标的数据
            		for (int j = 0; j < imputZbDataList.size(); j++) {//保存指标数据、保存关系数据
            			Map<String, String> map = imputZbDataList.get(j);
            			Map<String, String> realtionData = new HashMap<String, String>();//导入的指标与目录的关系表
            			//1、获取指标ID，更新数据
            			String zbid = map.get("id");
            			String yq = map.get("yq");
            			Condition condition2 = new Condition();
                		condition2.setC1(map.get("yq"));
                		String yqid = jqsqMapper.getYqidByName(condition2);//油区ID
                		
            			if(StringUtils.isNotBlank(zbid)) {
            				if(idAndYqIsRight(zbid,yqid)) {
            					frameMapper.updateBcsbzcZbById(map);
            				}else {
            					zbid = UUID.randomUUID().toString().replaceAll("-", "");//UUID生成 导入数据的ID
                				map.put("bbh", bbh);
                				map.put("id", zbid);
                				map.put("bm", bm);
                        		map.put("oilid", yqid);
                        		
                        		realtionData.put("code", bm);
                        		realtionData.put("oilid", yqid);
                        		realtionData.put("bbh", bbh);
                        		realtionData.put("zhid", mlId);
                        		realtionData.put("bcid", zbid);
                        		
                        		frameMapper.addBcsbzcZb(map);//新增的油区信息
                        		frameMapper.saveBcsbzcMlZbRealtion(realtionData);//保存导入的Excel数据到····指标-目录关系表
            				}
            			}
//            			else {
//            				zbid = UUID.randomUUID().toString().replaceAll("-", "");//UUID生成 导入数据的ID
//            				map.put("bbh", bbh);
//            				map.put("id", zbid);
//            				map.put("bm", bm);
//                    		map.put("oilid", yqid);
//                    		
//                    		realtionData.put("code", bm);
//                    		realtionData.put("oilid", yqid);
//                    		realtionData.put("bbh", bbh);
//                    		realtionData.put("zhid", mlId);
//                    		realtionData.put("bcid", zbid);
//                    		
//                    		frameMapper.addBcsbzcZb(map);//新增的油区信息
//                    		frameMapper.saveBcsbzcMlZbRealtion(realtionData);//保存导入的Excel数据到····指标-目录关系表
//            			}
            		}
            		
            		mlDataMap.put("id", mlId);
            		mlDataMap.put("lb", importData.get("lb"));
            		mlDataMap.put("mc", importData.get("mc"));
            		//2、更新指标的关联目录
        			frameMapper.updateBcsbzcMlById(mlDataMap);
				}
            } catch (Exception e) {
                e.printStackTrace();
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    
    public boolean idAndYqIsRight(String id, String yqid) {
    	
    	int count = frameMapper.sbzcIsExist(id,yqid);
    	if(count == 0) {
    		return false;
    	}else {
    		return true;
    	}
    }
    
    public static Map<String,String> getStringToMap(String mapStr){
    	String str = mapStr.replace("{", "").replace("}", "").replace(" ","");
        //根据逗号截取字符串数组
        String[] str1 = str.split(",");
        //创建Map对象
        Map<String,String> map = new HashMap<>();
        //循环加入map集合
        for (int i = 0; i < str1.length; i++) {
            //根据":"截取字符串数组
            String[] str2 = str1[i].split("=");
            String[] str3 = {str2[0], ""};
            if(str2.length != 2) {
            	str3[1] = "null";
            	map.put(str3[0],str3[1]);
            }else {
            	 //str2[0]为KEY,str2[1]为值
                map.put(str2[0],str2[1]);
            }
        }
        return map;
    }
    
    public List<Map<String,String>> getImputZbDataList(Map<String, String> importData){
    	List<Map<String,String>> zbDataList = new ArrayList<Map<String,String>>();
    	Map<String, String> slData = new HashMap<String, String>();//胜利油田指标数据
    	Map<String, String> zyData = new HashMap<String, String>();//中原油田指标数据
    	Map<String, String> hnData = new HashMap<String, String>();//河南油田指标数据
    	Map<String, String> jhData = new HashMap<String, String>();//江汉油田指标数据
    	Map<String, String> jsData = new HashMap<String, String>();//江苏油田指标数据
    	Map<String, String> xbData = new HashMap<String, String>();//西北油田指标数据
    	Map<String, String> hbData = new HashMap<String, String>();//华北油田指标数据
    	Map<String, String> xnData = new HashMap<String, String>();//西南油田指标数据
    	Map<String, String> csData = new HashMap<String, String>();//长输油田指标数据
    	
    	Map<String, String> publicData = new HashMap<String, String>();//长输油田指标数据
    	publicData.put("lb", importData.get("lb"));
    	publicData.put("mc", importData.get("mc"));
    	publicData.put("dw", importData.get("dw"));
    	publicData.put("xh", importData.get("xh"));
    	
    	if(!importData.get("gyjg_sl").equals("null")) {
    		slData.put("id", formartNullValue(importData.get("id_sl")));
    		slData.put("pp", formartNullValue(importData.get("pp_sl")));
	    	slData.put("cj", formartNullValue(importData.get("cj_sl")));
	    	slData.put("ccrq", formartNullValue(importData.get("ccrq_sl")));
	    	slData.put("ccjg", formartNullValue(importData.get("ccjg_sl")));
	    	slData.put("gyjg", formartNullValue(importData.get("gyjg_sl")));
	    	slData.put("jgly", formartNullValue(importData.get("jgly_sl")));
	    	slData.put("jggxsj", formartNullValue(importData.get("jggxsj_sl")));
	    	slData.put("jglysm", formartNullValue(importData.get("jglysm_sl")));
	    	slData.put("bz", formartNullValue(importData.get("bz_sl")));
	    	slData.put("yq", "胜利");
	    	slData.putAll(publicData);
	    	zbDataList.add(slData);
    	}
    	
    	if(!importData.get("gyjg_zy").equals("null")) {
    		zyData.put("id", formartNullValue(importData.get("id_zy")));
	    	zyData.put("pp", formartNullValue(importData.get("pp_zy")));
	    	zyData.put("cj", formartNullValue(importData.get("cj_zy")));
	    	zyData.put("ccrq", formartNullValue(importData.get("ccrq_zy")));
	    	zyData.put("ccjg", formartNullValue(importData.get("ccjg_zy")));
	    	zyData.put("gyjg", formartNullValue(importData.get("gyjg_zy")));
	    	zyData.put("jgly", formartNullValue(importData.get("jgly_zy")));
	    	zyData.put("jggxsj", formartNullValue(importData.get("jggxsj_zy")));
	    	zyData.put("jglysm", formartNullValue(importData.get("jglysm_zy")));
	    	zyData.put("bz", formartNullValue(importData.get("bz_zy")));
	    	zyData.put("yq", "中原");
	    	zyData.putAll(publicData);
	    	zbDataList.add(zyData);
    	}
    	
    	if(!importData.get("gyjg_hn").equals("null")) {
    		hnData.put("id", formartNullValue(importData.get("id_hn")));
	    	hnData.put("pp", formartNullValue(importData.get("pp_hn")));
	    	hnData.put("cj", formartNullValue(importData.get("cj_hn")));
	    	hnData.put("ccrq", formartNullValue(importData.get("ccrq_hn")));
	    	hnData.put("ccjg", formartNullValue(importData.get("ccjg_hn")));
	    	hnData.put("gyjg", formartNullValue(importData.get("gyjg_hn")));
	    	hnData.put("jgly", formartNullValue(importData.get("jgly_hn")));
	    	hnData.put("jggxsj", formartNullValue(importData.get("jggxsj_hn")));
	    	hnData.put("jglysm", formartNullValue(importData.get("jglysm_hn")));
	    	hnData.put("bz", formartNullValue(importData.get("bz_hn")));
	    	hnData.put("yq", "河南");
	    	hnData.putAll(publicData);
	    	zbDataList.add(hnData);
    	}
    	
    	if(!importData.get("gyjg_jh").equals("null")) {
    		jhData.put("id", formartNullValue(importData.get("id_jh")));
    		jhData.put("pp", formartNullValue(importData.get("pp_jh")));
        	jhData.put("cj", formartNullValue(importData.get("cj_jh")));
        	jhData.put("ccrq", formartNullValue(importData.get("ccrq_jh")));
        	jhData.put("ccjg", formartNullValue(importData.get("ccjg_jh")));
        	jhData.put("gyjg", formartNullValue(importData.get("gyjg_jh")));
        	jhData.put("jgly", formartNullValue(importData.get("jgly_jh")));
        	jhData.put("jggxsj", formartNullValue(importData.get("jggxsj_jh")));
        	jhData.put("jglysm", formartNullValue(importData.get("jglysm_jh")));
        	jhData.put("bz", formartNullValue(importData.get("bz_jh")));
        	jhData.put("yq", "江汉");
        	jhData.putAll(publicData);
        	zbDataList.add(jhData);
    	}
    	
    	if(!importData.get("gyjg_xb").equals("null")) {
    		jsData.put("id", formartNullValue(importData.get("id_js")));
    		jsData.put("pp", formartNullValue(importData.get("pp_js")));
        	jsData.put("cj", formartNullValue(importData.get("cj_js")));
        	jsData.put("ccrq", formartNullValue(importData.get("ccrq_js")));
        	jsData.put("ccjg", formartNullValue(importData.get("ccjg_js")));
        	jsData.put("gyjg", formartNullValue(importData.get("gyjg_js")));
        	jsData.put("jgly", formartNullValue(importData.get("jgly_js")));
        	jsData.put("jggxsj", formartNullValue(importData.get("jggxsj_js")));
        	jsData.put("jglysm", formartNullValue(importData.get("jglysm_js")));
        	jsData.put("bz", formartNullValue(importData.get("bz_js")));
        	jsData.put("yq", "江苏");
        	jsData.putAll(publicData);
        	zbDataList.add(jsData);
    	}
    	
    	if(!importData.get("gyjg_xb").equals("null")) {
    		xbData.put("id", formartNullValue(importData.get("id_xb")));
    		xbData.put("pp", formartNullValue(importData.get("pp_xb")));
        	xbData.put("cj", formartNullValue(importData.get("cj_xb")));
        	xbData.put("ccrq", formartNullValue(importData.get("ccrq_xb")));
        	xbData.put("ccjg", formartNullValue(importData.get("ccjg_xb")));
        	xbData.put("gyjg", formartNullValue(importData.get("gyjg_xb")));
        	xbData.put("jgly", formartNullValue(importData.get("jgly_xb")));
        	xbData.put("jggxsj", formartNullValue(importData.get("jggxsj_xb")));
        	xbData.put("jglysm", formartNullValue(importData.get("jglysm_xb")));
        	xbData.put("bz", formartNullValue(importData.get("bz_xb")));
        	xbData.put("yq", "西北");
        	xbData.putAll(publicData);
        	zbDataList.add(xbData);
    	}
    	
    	if(!importData.get("gyjg_hb").equals("null")) {
    		hbData.put("id", formartNullValue(importData.get("id_hb")));
	    	hbData.put("pp", formartNullValue(importData.get("pp_hb")));
	    	hbData.put("cj", formartNullValue(importData.get("cj_hb")));
	    	hbData.put("ccrq", formartNullValue(importData.get("ccrq_hb")));
	    	hbData.put("ccjg", formartNullValue(importData.get("ccjg_hb")));
	    	hbData.put("gyjg", formartNullValue(importData.get("gyjg_hb")));
	    	hbData.put("jgly", formartNullValue(importData.get("jgly_hb")));
	    	hbData.put("jggxsj", formartNullValue(importData.get("jggxsj_hb")));
	    	hbData.put("jglysm", formartNullValue(importData.get("jglysm_hb")));
	    	hbData.put("bz", formartNullValue(importData.get("bz_hb")));
	    	hbData.put("yq", "华北");
	    	hbData.putAll(publicData);
	    	zbDataList.add(hbData);
    	}
    	
    	if(!importData.get("gyjg_xn").equals("null")) {
    		xnData.put("id", formartNullValue(importData.get("id_xn")));
	    	xnData.put("pp", formartNullValue(importData.get("pp_xn")));
	    	xnData.put("cj", formartNullValue(importData.get("cj_xn")));
	    	xnData.put("ccrq", formartNullValue(importData.get("ccrq_xn")));
	    	xnData.put("ccjg", formartNullValue(importData.get("ccjg_xn")));
	    	xnData.put("gyjg", formartNullValue(importData.get("gyjg_xn")));
	    	xnData.put("jgly", formartNullValue(importData.get("jgly_xn")));
	    	xnData.put("jggxsj", formartNullValue(importData.get("jggxsj_xn")));
	    	xnData.put("jglysm", formartNullValue(importData.get("jglysm_xn")));
	    	xnData.put("bz", formartNullValue(importData.get("bz_xn")));
	    	xnData.put("yq", "西南");
	    	xnData.putAll(publicData);
	    	zbDataList.add(xnData);
    	}
    	
    	if(!importData.get("gyjg_cs").equals("null")) {
    		csData.put("id", formartNullValue(importData.get("id_cs")));
    		csData.put("pp", formartNullValue(importData.get("pp_cs")));
        	csData.put("cj", formartNullValue(importData.get("cj_cs")));
        	csData.put("ccrq", formartNullValue(importData.get("ccrq_cs")));
        	csData.put("ccjg", formartNullValue(importData.get("ccjg_cs")));
        	csData.put("gyjg", formartNullValue(importData.get("gyjg_cs")));
        	csData.put("jgly", formartNullValue(importData.get("jgly_cs")));
        	csData.put("jggxsj", formartNullValue(importData.get("jggxsj_cs")));
        	csData.put("jglysm", formartNullValue(importData.get("jglysm_cs")));
        	csData.put("bz", formartNullValue(importData.get("bz_cs")));
        	csData.put("yq", "长输");
        	csData.putAll(publicData);
        	zbDataList.add(csData);
    	}
    	return zbDataList;
    }
    
    private Object trim(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResponseBody getExportMlDataByBbh(RequestBody rq, Map params, String id) {
        ResponseBody rp = new ResponseBody(params, "1", "请求成功", id, rq.getTaskid());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Condition condition = objectMapper.readValue(rq.getParams(), Condition.class);
            try {
            	String bbh = condition.getC1();
            	Map<String, String> param = new HashMap<String, String>();
            	param.put("bbh",bbh);
            	List<Map<String, String>> listData = new ArrayList<Map<String,String>>();
            	
            	List<Map<String, String>> exportMlDataList = frameMapper.getExportMlDataByBbh(param);
            	for (int i = 0; i < exportMlDataList.size(); i++) {
            		Map<String, String> mapData = new HashMap<String, String>();
            		
            		Map<String, String> exportMlData = exportMlDataList.get(i);

            		//1、获取Excel中目录的数据列
            		String lb = "SB".equals(exportMlData.get("MASTER")) ? "设备" : "主材";
            		String mc = exportMlData.get("NAME");
            		String code = exportMlData.get("CODE");
            		
            		mapData.put("mc", mc);
            		mapData.put("bm", code);
            		mapData.put("lb", lb);
            		String mlid = exportMlData.get("ID");
            		List<String> exportZbid = frameMapper.getExportZbidByMlid(mlid);
            		for (int j = 0; j < exportZbid.size(); j++) {
            			String zbid = exportZbid.get(j);
            			List<Map<String, String>> exportZbData = frameMapper.getExportZbDataByid(zbid);
            			if(exportZbData != null) {
            				for (int k = 0; k < exportZbData.size(); k++) {
            					//2、获取各个油区指标数据Map
            					Map<String, String> yqDataMap = getExcelData(exportZbData.get(k));
            					 //3、将数据保存至map中
            					mapData.putAll(yqDataMap);
							}
            			}
					}
            		//4、将map放到list中储存  为了生存excel做准备
            		listData.add(mapData);
				}
            	//5、返回list 用于生成excel
            	rp.setDatas(org.apache.commons.lang.StringUtils.join(listData.toArray(), "&"));
            	
            } catch (Exception e) {
                rp.setIssuccess("0");
                rp.setMessage("操作失败！" + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            rp.setIssuccess("0");
            rp.setMessage("操作失败！" + e.getMessage());
            e.printStackTrace();
        }
        return rp;
    }
    
    public Map<String,String> getExcelData(Map<String,String> yqData){
    	Map<String,String> resultMap = new HashMap<String, String>();
    	String yqname = yqData.get("YQNAME");
    	
    	if("胜利".equals(yqname)) {
    		resultMap.put("pp_sl", yqData.get("PP"));
    		resultMap.put("cj_sl", yqData.get("CJMC"));
	    	resultMap.put("ccrq_sl", yqData.get("CCRQ"));
	    	resultMap.put("ccjg_sl", yqData.get("CCJG"));
	    	resultMap.put("gyjg_sl", yqData.get("GYJG"));
	    	resultMap.put("jgly_sl", yqData.get("GYLY"));
	    	resultMap.put("jggxsj_sl", yqData.get("GXSJ"));
	    	resultMap.put("jglysm_sl", yqData.get("LYSM"));
	    	resultMap.put("bz_sl", yqData.get("BZ"));
	    	resultMap.put("id_sl", yqData.get("ID"));
    	}
    	
    	if("中原".equals(yqname)) {
    		resultMap.put("pp_zy", yqData.get("PP"));
    		resultMap.put("cj_zy", yqData.get("CJMC"));
	    	resultMap.put("ccrq_zy", yqData.get("CCRQ"));
	    	resultMap.put("ccjg_zy", yqData.get("CCJG"));
	    	resultMap.put("gyjg_zy", yqData.get("GYJG"));
	    	resultMap.put("jgly_zy", yqData.get("GYLY"));
	    	resultMap.put("jggxsj_zy", yqData.get("GXSJ"));
	    	resultMap.put("jglysm_zy", yqData.get("LYSM"));
	    	resultMap.put("bz_zy", yqData.get("BZ"));
	    	resultMap.put("id_zy", yqData.get("ID"));
    	}
    	
    	if("河南".equals(yqname)) {
    		resultMap.put("pp_hn", yqData.get("PP"));
    		resultMap.put("cj_hn", yqData.get("CJMC"));
	    	resultMap.put("ccrq_hn", yqData.get("CCRQ"));
	    	resultMap.put("ccjg_hn", yqData.get("CCJG"));
	    	resultMap.put("gyjg_hn", yqData.get("GYJG"));
	    	resultMap.put("jgly_hn", yqData.get("GYLY"));
	    	resultMap.put("jggxsj_hn", yqData.get("GXSJ"));
	    	resultMap.put("jglysm_hn", yqData.get("LYSM"));
	    	resultMap.put("bz_hn", yqData.get("BZ"));
	    	resultMap.put("id_hn", yqData.get("ID"));
    	}
    	
    	if("江汉".equals(yqname)) {
    		resultMap.put("pp_jh", yqData.get("PP"));
    		resultMap.put("cj_jh", yqData.get("CJMC"));
	    	resultMap.put("ccrq_jh", yqData.get("CCRQ"));
	    	resultMap.put("ccjg_jh", yqData.get("CCJG"));
	    	resultMap.put("gyjg_jh", yqData.get("GYJG"));
	    	resultMap.put("jgly_jh", yqData.get("GYLY"));
	    	resultMap.put("jggxsj_jh", yqData.get("GXSJ"));
	    	resultMap.put("jglysm_jh", yqData.get("LYSM"));
	    	resultMap.put("bz_jh", yqData.get("BZ"));
	    	resultMap.put("id_jh", yqData.get("ID"));
    	}
    	
    	if("江苏".equals(yqname)) {
    		resultMap.put("pp_js", yqData.get("PP"));
    		resultMap.put("cj_js", yqData.get("CJMC"));
	    	resultMap.put("ccrq_js", yqData.get("CCRQ"));
	    	resultMap.put("ccjg_js", yqData.get("CCJG"));
	    	resultMap.put("gyjg_js", yqData.get("GYJG"));
	    	resultMap.put("jgly_js", yqData.get("GYLY"));
	    	resultMap.put("jggxsj_js", yqData.get("GXSJ"));
	    	resultMap.put("jglysm_js", yqData.get("LYSM"));
	    	resultMap.put("bz_js", yqData.get("BZ"));
	    	resultMap.put("id_js", yqData.get("ID"));
    	}
    	
    	if("西北".equals(yqname)) {
    		resultMap.put("pp_xb", yqData.get("PP"));
    		resultMap.put("cj_xb", yqData.get("CJMC"));
	    	resultMap.put("ccrq_xb", yqData.get("CCRQ"));
	    	resultMap.put("ccjg_xb", yqData.get("CCJG"));
	    	resultMap.put("gyjg_xb", yqData.get("GYJG"));
	    	resultMap.put("jgly_xb", yqData.get("GYLY"));
	    	resultMap.put("jggxsj_xb", yqData.get("GXSJ"));
	    	resultMap.put("jglysm_xb", yqData.get("LYSM"));
	    	resultMap.put("bz_xb", yqData.get("BZ"));
	    	resultMap.put("id_xb", yqData.get("ID"));
    	}
    	
    	if("华北".equals(yqname)) {
    		resultMap.put("pp_hb", yqData.get("PP"));
    		resultMap.put("cj_hb", yqData.get("CJMC"));
	    	resultMap.put("ccrq_hb", yqData.get("CCRQ"));
	    	resultMap.put("ccjg_hb", yqData.get("CCJG"));
	    	resultMap.put("gyjg_hb", yqData.get("GYJG"));
	    	resultMap.put("jgly_hb", yqData.get("GYLY"));
	    	resultMap.put("jggxsj_hb", yqData.get("GXSJ"));
	    	resultMap.put("jglysm_hb", yqData.get("LYSM"));
	    	resultMap.put("bz_hb", yqData.get("BZ"));
	    	resultMap.put("id_hb", yqData.get("ID"));
    	}
    	
    	if("西南".equals(yqname)) {
    		resultMap.put("pp_xn", yqData.get("PP"));
    		resultMap.put("cj_xn", yqData.get("CJMC"));
	    	resultMap.put("ccrq_xn", yqData.get("CCRQ"));
	    	resultMap.put("ccjg_xn", yqData.get("CCJG"));
	    	resultMap.put("gyjg_xn", yqData.get("GYJG"));
	    	resultMap.put("jgly_xn", yqData.get("GYLY"));
	    	resultMap.put("jggxsj_xn", yqData.get("GXSJ"));
	    	resultMap.put("jglysm_xn", yqData.get("LYSM"));
	    	resultMap.put("bz_xn", yqData.get("BZ"));
	    	resultMap.put("id_xn", yqData.get("ID"));
    	}
    	
    	if("长输".equals(yqname)) {
    		resultMap.put("pp_cs", yqData.get("PP"));
    		resultMap.put("cj_cs", yqData.get("CJMC"));
	    	resultMap.put("ccrq_cs", yqData.get("CCRQ"));
	    	resultMap.put("ccjg_cs", yqData.get("CCJG"));
	    	resultMap.put("gyjg_cs", yqData.get("GYJG"));
	    	resultMap.put("jgly_cs", yqData.get("GYLY"));
	    	resultMap.put("jggxsj_cs", yqData.get("GXSJ"));
	    	resultMap.put("jglysm_cs", yqData.get("LYSM"));
	    	resultMap.put("bz_cs", yqData.get("BZ"));
	    	resultMap.put("id_cs", yqData.get("ID"));
    	}
    	resultMap.put("xh", yqData.get("XH"));
    	resultMap.put("dw", yqData.get("DW"));
    	return resultMap;
    }
    public static String formartNullValue(String str) {
		boolean blank = org.apache.commons.lang.StringUtils.isBlank(str);
		if(blank || "null".equals(str)) {
			str = "";
		}
		return str;
	}
}
