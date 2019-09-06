package com.server.controller;

import com.common.mapper.JsonMapper;
import com.server.mapper.SysControlMapper;
import com.server.service.AdminService;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/11/20.
 */
@Controller
@RequestMapping(value = "${emPath}")
public class AdminController{
    private String uploadFolderName="upload";
    @Autowired
    AdminService adminService;
    @Autowired
    SysControlMapper sysControlMapper;
    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping("index")
    public String login(String username, String password) {
        /*HashMap u=adminService.getUserOne(username);
        if(u==null)
            return "em/sysLogin";
        else{
            if(u.get("PASSWORD").equals(password)){
                return "em/index";
            }
        }*/
        return "em/index";
    }

    @RequestMapping("getRequestList")
    @ResponseBody
    public String getRequestList(@RequestParam String  c1, @RequestParam String c2) {
        List<HashMap> u=sysControlMapper.getRequestList(c1,c2);
        String jdata= JsonMapper.getInstance().toJson(u);
        return jdata;
    }

    @RequestMapping("getHandleMsgList")
    @ResponseBody
    public String getHandleMsgList() {
        List<HashMap> u=sysControlMapper.getHandleMsgList();
        String jdata= JsonMapper.getInstance().toJson(u);
        return jdata;
    }

    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping("sjbbgl")
    public String sjbbgl(String username, String password) {
        return "../../static/ckfinder/ckfinderSj";
    }

    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping("xtbbgl")
    public String xtbbgl(String username, String password) {
        return "../../static/ckfinder/ckfinderCx";
    }

    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping("test")
    public String test(String username, String password) {
        return "../../static/ckfinder/frame";
    }
/*  @RequestMapping("upLoad")
    @ResponseBody
    public String upLoad(String requestBody,@RequestParam("file") CommonsMultipartFile file,
                         HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        //清除上次上传进度信息
        String curProjectPath = session.getServletContext().getRealPath("/");
        String saveDirectoryPath = curProjectPath + "/" + uploadFolderName;
        File saveDirectory = new File(saveDirectoryPath);
        System.out.println(saveDirectoryPath);
        System.out.println(file);
        System.out.println(requestBody);
        // 判断文件是否存在
        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            String fileExtension = FilenameUtils.getExtension(fileName);

            try {
                file.transferTo(new File(saveDirectory, fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "";
    }*/


}
