/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.common.utils;

import com.common.realm.StatelessRealm;
import com.common.sys.entity.Role;
import com.common.sys.entity.User;
import com.server.mapper.UserMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import java.util.HashMap;


/**
 * 用户工具类
 * @author ThinkGem
 * @version 2013-12-05
 */
public class UserUtils {

	private static UserMapper userMapper = SpringContextHolder.getBean(UserMapper.class);

	/**
	 * 根据ID获取用户
	 * @param username
	 * @return 取不到返回null
	 */
	public static User get(String username){
		User user = null;
		if (user ==  null){
			//user = userDao.get(id);
			HashMap us=userMapper.getUserOne(username);
			if(us!=null){
				user=new User();
				/*user.setLoginName((String)u.get("LOGINNAME"));
				user.setName((String)u.get("USERNAME"));
				user.setId((String)u.get("USERID"));
				user.setPassword((String)u.get("PWD"));*/
				user.setId((String)us.get("USERID"));
				user.setLoginName((String)us.get("LOGINNAME"));
				user.setName((String)us.get("USERNAME"));
				user.setPassword((String)us.get("PWD"));
				user.setLoginFlag("1");
				user.setMobile((String)us.get("YDDH"));
				user.setPhone((String)us.get("GDDH"));
				user.setDeptMc((String)us.get("DW"));
				user.setDeptId((String)us.get("DEPTID"));
				user.setDeptMc((String)us.get("DEPTNAME"));
				user.setDeptCode((String)us.get("CODE"));
				user.setParentId((String)us.get("PARENTID"));
				user.setParentName((String)us.get("PARENTNAME"));
				user.setBzxx((String)us.get("BZXX"));
				user.setEmail((String)us.get("EMAIL"));
				user.setTopDeptid((String)us.get("RT"));
				user.setTopDeptname((String)us.get("RTNAME"));
				//user.setRoleList(userMapper.getRoleList(user.getId()));
				if(!"1".equals(user.getId()))
					user.setPermissionList(userMapper.getPermissionList(user.getId()));
				else{
					user.setPermissionList(userMapper.getPermissionListAll(user.getId()));
				}
			}
			if (user == null){
				return null;
			}

		}
		return user;
	}
	/**
	 * 获取当前用户
	 * @return 取不到返回 new User()
	 */
	public static User getUser(){
		//StatelessRealm.Principal principal = getPrincipal();
		String principal = getPrincipal2();
		if (principal!=null){
			User user = get(principal);
			if (user != null){
				return user;
			}
			return new User();
		}
		// 如果没有登录，则返回实例化空的User对象。
		return new User();
	}

	
	/**
	 * 获取授权主要对象
	 */
	public static Subject getSubject(){
		return SecurityUtils.getSubject();
	}
	
	/**
	 * 获取当前登录者对象
	 */
	public static StatelessRealm.Principal getPrincipal(){
		try{
			Subject subject = SecurityUtils.getSubject();

			StatelessRealm.Principal principal = (StatelessRealm.Principal)subject.getPrincipal();
			if (principal != null){
				return principal;
			}
//			subject.logout();
		}catch (UnavailableSecurityManagerException e) {
			
		}catch (InvalidSessionException e){
			
		}
		return null;
	}
	public static String getPrincipal2(){
		try{
			Subject subject = SecurityUtils.getSubject();
			//StatelessRealm.Principal principal = getPrincipal();
			String principal = (String)subject.getPrincipal();
			if (principal != null){
				return principal.toString();
			}
//			subject.logout();
		}catch (UnavailableSecurityManagerException e) {

		}catch (InvalidSessionException e){

		}
		return null;
	}
	
	public static Session getSession(){
		try{
			Subject subject = SecurityUtils.getSubject();
			Session session = subject.getSession(false);
			if (session == null){
				session = subject.getSession();
			}
			if (session != null){
				return session;
			}
//			subject.logout();
		}catch (InvalidSessionException e){
			
		}
		return null;
	}
	
	// ============== User Cache ==============
	
	public static Object getCache(String key) {
		return getCache(key, null);
	}
	
	public static Object getCache(String key, Object defaultValue) {
//		Object obj = getCacheMap().get(key);
		Object obj = getSession().getAttribute(key);
		return obj==null?defaultValue:obj;
	}

	public static void putCache(String key, Object value) {
//		getCacheMap().put(key, value);
		getSession().setAttribute(key, value);
	}

	public static void removeCache(String key) {
//		getCacheMap().remove(key);
		getSession().removeAttribute(key);
	}
	
//	public static Map<String, Object> getCacheMap(){
//		Principal principal = getPrincipal();
//		if(principal!=null){
//			return principal.getCacheMap();
//		}
//		return new HashMap<String, Object>();
//	}
	
}
