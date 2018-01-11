package com.common.servlet;


import com.common.config.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.util.UriUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 查看CK上传的图片
 *
 * @author ThinkGem
 * @version 2014-06-25
 */
public class SoftWareUpgradeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void fileOutputStream(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String filepath = req.getRequestURI();
        int index = filepath.indexOf(Global.SOFTWAREFILES_BASE_URL);
        if (index >= 0) {
            filepath = filepath.substring(index + Global.SOFTWAREFILES_BASE_URL.length());
        }
        try {
            filepath = UriUtils.decode(filepath, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            logger.error(String.format("解释文件路径失败，URL地址为%s", filepath), e1);
        }
        File file = new File(Global.getUserfilesBaseDir() + Global.SOFTWAREFILES_BASE_URL + filepath);
        //System.out.println(Global.getUserfilesBaseDir() + Global.SOFTWAREFILES_BASE_URL + filepath);

        FileCopyUtils.copy(new FileInputStream(file), resp.getOutputStream());
        resp.setHeader("Content-Type", "application/octet-stream");
        return;

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        fileOutputStream(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        fileOutputStream(req, resp);
    }
}
