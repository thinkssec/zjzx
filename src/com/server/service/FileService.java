package com.server.service;

import com.common.config.Global;
import com.common.utils.DateUtils;
import com.common.utils.FileUtils;
import com.common.utils.IdGen;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.mapper.SysControlMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.ibatis.thread.PropertiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/18.
 */
@Service
@Component
public class FileService {

    String curProjectPath = Global.getConfig("upLoadPath");;
    @Autowired
    SysControlMapper sysControlMapper;
    public String upLoad(RequestBody rq, Map params, String id, CommonsMultipartFile file) {
        ResponseBody r = new ResponseBody();
        //String saveDirectoryPath = curProjectPath + "/" + uploadFolderName;
        String filepath="/"+ DateUtils.getDate("yyyy-MM-dd")+"//"+ IdGen.uuid();
        String saveDirectoryPath = curProjectPath +"/"+ Global.USERFILES_BASE_URL+filepath;
        FileUtils.createDirectory(saveDirectoryPath);
        File saveDirectory = new File(saveDirectoryPath);

        System.out.println("开始接受 "+new Date());
        String fileName="";
        if (!file.isEmpty()) {
            fileName= file.getOriginalFilename();
            String fileExtension = FilenameUtils.getExtension(fileName);
            try {
                file.transferTo(new File(saveDirectory, fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("接受完毕  "+new Date()+"  "+saveDirectoryPath+"//"+fileName);
        return "/"+filepath+"//"+fileName;
    }

}
