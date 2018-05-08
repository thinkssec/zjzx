package com.common.utils;

import com.ckfinder.connector.configuration.ConfigurationFactory;
import com.ckfinder.connector.configuration.ConfigurationPathBuilder;
import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.utils.PathUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2018/3/29.
 */
public class CkFinderPathBuilder extends ConfigurationPathBuilder {
    //public static String path
    public CkFinderPathBuilder(){
        super();
    }
    public String getBaseUrl(HttpServletRequest request) {
       //System.out.println("---------------"+request.getAttribute("path"));
        //SystemAuthorizingRealm.Principal principal = (SystemAuthorizingRealm.Principal) UserUtils.getPrincipal();
        //System.out.println("---------------"+ UserUtils.getPrincipal());
        String baseURL = null;
        try {
            IConfiguration e = ConfigurationFactory.getInstace().getConfiguration();
            baseURL = e.getBaseURL();
            //baseURL+="1/";
            //System.out.println("---------------"+baseURL);
        } catch (Exception var4) {
            var4.printStackTrace();
            baseURL = null;
        }
        if(baseURL == null || baseURL.equals("")) {
            baseURL = super.getBaseUrl(request);
        }
        return PathUtils.addSlashToBeginning(PathUtils.addSlashToEnd(baseURL));
    }

    public String getBaseDir(HttpServletRequest request) {
        String baseDir = null;

        try {
            IConfiguration e = ConfigurationFactory.getInstace().getConfiguration();
            baseDir = e.getBaseDir();
            //baseDir+="1/";
            //System.out.println("================"+baseDir);
        } catch (Exception var4) {
            baseDir = null;
        }

        return baseDir != null && !baseDir.equals("")?baseDir:super.getBaseDir(request);
    }
}
