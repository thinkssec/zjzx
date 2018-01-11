package com.server.controller;

import com.common.mapper.JsonMapper;
import com.server.mapper.SysControlMapper;
import com.server.service.AdminService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/11/20.
 */
@Controller
@RequestMapping(value = "${testPath}")
public class TestController {
    private String uploadFolderName="upload";
    @Autowired
    AdminService adminService;
    @Autowired
    SysControlMapper sysControlMapper;
    @RequestMapping("test")
    public String test() {
        return "em/test";
    }



}
