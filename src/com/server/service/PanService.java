package com.server.service;

import com.common.annotation.mapper.JsonMapper;
import com.common.config.Global;
import com.common.utils.DateUtils;
import com.common.utils.FileUtils;
import com.common.utils.IdGen;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.mapper.SysControlMapper;
import com.server.mapper.UserMapper;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/18.
 */
@Service
@Component
public class PanService {
    @Autowired
    UserMapper userMapper;
    String panPath = Global.getConfig("upLoadPath");

    public ResponseBody createDirectory(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","目录创建成功！",id,rq.getTaskid());
        try{
            HashMap u=userMapper.getUserOne(rq.getUsername());
            String root=(String)u.get("ID");
            String direcotry=(String)params.get("PATH");
            String p=panPath+Global.PAN_BASE_URL+root+direcotry;
            File file = new File(p);
            if (file.exists()) {
                if(file.isDirectory()){
                    rp.setIssuccess("0");
                    rp.setMessage("创建失败！目录已经存在！");
                    return rp;
                }
            }
            System.out.println(panPath+Global.PAN_BASE_URL+root+direcotry);
            FileUtils.createDirectory(p);
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("目录创建失败！");
        }
        return rp;
    }

    public ResponseBody createFile(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","文件创建成功！",id,rq.getTaskid());
        try{
            HashMap u=userMapper.getUserOne(rq.getUsername());
            String root=(String)u.get("ID");
            String direcotry=(String)params.get("PATH");
            String p=panPath+Global.PAN_BASE_URL+root+direcotry;
            String d=panPath+Global.USERFILES_BASE_URL+rq.getAttach();
            File file = new File(p);
            if (file.exists()) {
                if(file.isDirectory()){
                    FileUtils.copyFile(d,
                            p+rq.getAttach().substring(rq.getAttach().lastIndexOf("/")));
                }else{
                    rp.setIssuccess("0");
                    rp.setMessage("文件创建失败！目录不存在！");
                }
            }else{
                rp.setIssuccess("0");
                rp.setMessage("文件创建失败！目录不存在！");
            }

        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("文件创建失败！");
        }
        return rp;
    }
}
