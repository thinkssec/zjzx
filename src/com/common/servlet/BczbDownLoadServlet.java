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
public class BczbDownLoadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void fileOutputStream(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String filepath = req.getRequestURI();
        try {
            filepath = UriUtils.decode(filepath, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            logger.error(String.format("解释文件路径失败，URL地址为%s", filepath), e1);
        }
        File file = new File(Global.getConfig("upLoadPath") + filepath);
        //System.out.println(Global.getUserfilesBaseDir() + Global.USERFILES_BASE_URL + filepath);
        try {
            FileCopyUtils.copy(new FileInputStream(file), resp.getOutputStream());
            resp.setHeader("Content-Type", "application/octet-stream");
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            req.setAttribute("exception", new FileNotFoundException("请求的文件不存在"));
            req.getRequestDispatcher("/WEB-INF/views/error/404.jsp").forward(req, resp);
        }

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
