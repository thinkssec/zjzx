/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.common.servlet;

import com.ckfinder.connector.ConnectorServlet;
import com.common.config.Global;
import com.common.realm.StatelessRealm;
import com.common.utils.FileUtils;
import com.common.utils.UserUtils;
import org.apache.shiro.session.Session;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CKFinderConnectorServlet
 * @author ThinkGem
 * @version 2014-06-25
 */
public class CKFinderConnectorServlet extends ConnectorServlet {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		prepareGetResponse(request, response, false);
		super.doGet(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		prepareGetResponse(request, response, true);
		super.doPost(request, response);
	}
	
	private void prepareGetResponse(final HttpServletRequest request,
                                    final HttpServletResponse response, final boolean post) throws ServletException {
		String type = request.getParameter("type");
		String command=request.getParameter("command");
		String principal = "";
		String relationId="";
		String bbh="";
		String yq="";
		relationId=request.getParameter("ID");
		principal=request.getParameter("USERID");
		bbh=request.getParameter("BBH");
		yq=request.getParameter("YQ");


		// 初始化时，如果startupPath文件夹不存在，则自动创建startupPath文件夹
		if ("Init".equals(command)){
			request.setAttribute("path",type);

			String startupPath = request.getParameter("startupPath");// 当前文件夹可指定为模块名
			String path="";
			if (startupPath!=null){
				String[] ss = startupPath.split(":");
				if (ss.length==2){
					String realPath = Global.getUserfilesBaseDir()
							+ principal + "/" +relationId+"/"+ ss[0] + ss[1];
					//path=realPath;
					FileUtils.createDirectory(FileUtils.path(realPath));
				}
			}
			//System.out.println("---------------------"+type);
			if("software".equals(type)){
				path=Global.getUserfilesBaseDir()
						+ principal +"/"+relationId+ "/software";
				//System.out.println(path);
				//FileUtils.createDirectory(FileUtils.path(path+"/BASICDATA"));
				FileUtils.createDirectory(FileUtils.path(path+"/bqgl"));
				FileUtils.createDirectory(FileUtils.path(path+"/configXml"));
				FileUtils.createDirectory(FileUtils.path(path+"/de"));
				FileUtils.createDirectory(FileUtils.path(path+"/es"));
				FileUtils.createDirectory(FileUtils.path(path+"/image"));
				FileUtils.createDirectory(FileUtils.path(path+"/ja"));
				FileUtils.createDirectory(FileUtils.path(path+"/ru"));
				FileUtils.createDirectory(FileUtils.path(path+"/workspace"));
				FileUtils.createDirectory(FileUtils.path(path+"/xml"));
			}else if("data".equals(type)){
				path=Global.getUserfilesBaseDir()
						+ principal +"/" +relationId+ "/data";
				FileUtils.createDirectory(FileUtils.path(path+"/ZX/ZB/")+bbh+"/"+yq+"/"+"DW");
				FileUtils.createDirectory(FileUtils.path(path+"/ZX/ZB/")+bbh+"/"+yq+"/"+"DX");
				FileUtils.createDirectory(FileUtils.path(path+"/ZX/ZB/")+bbh+"/"+yq+"/"+"FX");
				FileUtils.createDirectory(FileUtils.path(path+"/ZX/JG/")+bbh+"/"+yq);
				FileUtils.createDirectory(FileUtils.path(path+"/ZX/ELF/")+bbh+"/"+yq);
				//FileUtils.createDirectory(FileUtils.path(path+"/DW"));
			}
		}
		// 快捷上传，自动创建当前文件夹，并上传到该路径
		else if ("QuickUpload".equals(command) && type!=null){
			String currentFolder = request.getParameter("currentFolder");// 当前文件夹可指定为模块名
			String realPath = Global.getUserfilesBaseDir()
					+ principal + "/" +relationId+ "/"+ type + (currentFolder != null ? currentFolder : "");
			FileUtils.createDirectory(FileUtils.path(realPath));
		}
		//System.out.println("------------------------");
//		for (Object key : request.getParameterMap().keySet()){
//			System.out.println(key + ": " + request.getParameter(key.toString()));
//		}
	}
	
}
