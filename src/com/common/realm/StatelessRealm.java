package com.common.realm;


import com.common.sys.entity.User;
import com.common.utils.Des;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * <p>User: sky
 * <p>Date: 14-2-26
 * <p>Version: 1.0
 */
public class StatelessRealm extends AuthorizingRealm {
    @Override
    public boolean supports(AuthenticationToken token) {
        //仅支持StatelessToken类型的Token
        return token instanceof StatelessToken;
    }
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        //根据用户名查找角色，请根据需求实现
        String username = (String) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo authorizationInfo =  new SimpleAuthorizationInfo();
        authorizationInfo.addRole("admin");
        return authorizationInfo;
    }
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        StatelessToken statelessToken = (StatelessToken) token;
        String username = statelessToken.getUsername();
        String key = "11111111";//根据用户名获取密钥（和客户端的一样）
        //在服务器端生成客户端参数消息摘要
        String serverDigest = null;
        try {
            //serverDigest = URLEncoder.encode( new String(Des.encrypt("11111111".getBytes(),key),"UTF-8"));
            //serverDigest =  Des.encrypt2("11111111");
            //serverDigest = new String(Des.encrypt("11111111".getBytes("UTF-8"),key),"UTF-8");
            //System.out.println("wwww"+serverDigest);
            serverDigest = "11111111";
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(key);
        //System.out.println(statelessToken.getClientDigest());
        //System.out.println(serverDigest);
        //然后进行客户端消息摘要和服务器端消息摘要的匹配
        return new SimpleAuthenticationInfo(
                username,
                serverDigest,
                getName());
        /*User user=null;
        return new SimpleAuthenticationInfo(
                new Principal(user, token.isMobileLogin()),
                serverDigest,
                getName());*/
    }

    private String getKey(String username) {//得到密钥，此处硬编码一个
        return "admin";
    }

    public static class Principal implements Serializable {

        private static final long serialVersionUID = 1L;

        private String id; // 编号
        private String loginName; // 登录名
        private String name; // 姓名
        private boolean mobileLogin; // 是否手机登录
        private String relationId;

//		private Map<String, Object> cacheMap;

        public Principal(User user, boolean mobileLogin) {
            this.id = user.getId();
            this.loginName = user.getLoginName();
            this.name = user.getName();
        }
        public String getRelationId() {
            return relationId;
        }

        public void setRelationId(String relationId) {
            this.relationId = relationId;
        }
        public String getId() {
            return id;
        }

        public String getLoginName() {
            return loginName;
        }

        public String getName() {
            return name;
        }


//		@JsonIgnore
//		public Map<String, Object> getCacheMap() {
//			if (cacheMap==null){
//				cacheMap = new HashMap<String, Object>();
//			}
//			return cacheMap;
//		}

        @Override
        public String toString() {
            return id;
        }

    }
}
