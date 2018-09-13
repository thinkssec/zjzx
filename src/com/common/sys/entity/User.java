/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.common.sys.entity;

import com.common.config.Global;
import com.common.utils.Collections3;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 用户Entity

 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends DataEntity<User> {

	private static final long serialVersionUID = 1L;
	private String loginName;// 登录名
	private String password;// 密码
	private String deptId;// 单位ID
	private String oilId;// 油区ID
	private String isDel;
	private String no;		// 工号
	private String name;	// 姓名
	private String phone;	// 电话
	private String mobile;	// 手机
	private String userType;// 用户类型
	private String loginFlag;	// 是否允许登陆

	private String deptCode;
	private String topDeptid;
	private String topDeptname;
	private String parentId;
	private String parentName;
	private Role role;
	private String deptMc;
	private List<HashMap> roleList = Lists.newArrayList(); // 拥有角色列表
	private HashMap permissionList = new HashMap();
    private	String	userid	;
    private	String	loginname	;
    private	String	username	;
    private	String	dw	;
    private	String	gddh	;
    private	String	yddh	;
    private	String	email	;
    private	String	bzxx	;
    private	String	pwd	;
    private	String	upwd	;
    private	String	sfqy	;
    private	String	fpz	;
    private	String	createtime	;
    private	String	isdel	;
	private String filepath;
    public boolean hasPermission(String p){
		boolean f=false;
		if(this.permissionList==null) f=false;
		else if(this.permissionList.get(p)!=null) f=true;
		return f;
	}
    public String getUserid() {
        return userid;
    }

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getTopDeptid() {
		return topDeptid;
	}

	public void setTopDeptid(String topDeptid) {
		this.topDeptid = topDeptid;
	}

	public String getTopDeptname() {
		return topDeptname;
	}

	public void setTopDeptname(String topDeptname) {
		this.topDeptname = topDeptname;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }


	public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDw() {
        return dw;
    }

    public void setDw(String dw) {
        this.dw = dw;
    }

    public String getGddh() {
        return gddh;
    }

    public void setGddh(String gddh) {
        this.gddh = gddh;
    }

    public String getYddh() {
        return yddh;
    }

    public void setYddh(String yddh) {
        this.yddh = yddh;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getUpwd() {
        return upwd;
    }

    public void setUpwd(String upwd) {
        this.upwd = upwd;
    }

    public String getSfqy() {
        return sfqy;
    }

    public void setSfqy(String sfqy) {
        this.sfqy = sfqy;
    }

    public String getFpz() {
        return fpz;
    }

    public void setFpz(String fpz) {
        this.fpz = fpz;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getIsdel() {
        return isdel;
    }

    public void setIsdel(String isdel) {
        this.isdel = isdel;
    }

    public HashMap getPermissionList() {
		return permissionList;
	}

	public void setPermissionList(HashMap permissionList) {
		this.permissionList = permissionList;
	}

	public User() {
		super();
		this.loginFlag = Global.YES;
	}
	
	public User(String id){
		super(id);
	}

	public User(String id, String loginName){
		super(id);
		this.loginName = loginName;
	}
	public String getLoginFlag() {
		return loginFlag;
	}

	public void setLoginFlag(String loginFlag) {
		this.loginFlag = loginFlag;
	}
	/**
	 * 用户拥有的角色名称字符串, 多个角色名称用','分隔.
	 */
	public String getRoleNames() {
		return Collections3.extractToString(roleList, "name", ",");
	}
	
	public boolean isAdmin(){
		return isAdmin(this.id);
	}
	
	public static boolean isAdmin(String id){
		return id != null && "1".equals(id);
	}
	
	@Override
	public String toString() {
		return id;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getDeptId() {
		return deptId;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	public String getOilId() {
		return oilId;
	}

	public void setOilId(String oilId) {
		this.oilId = oilId;
	}

	public String getIsDel() {
		return isDel;
	}

	public void setIsDel(String isDel) {
		this.isDel = isDel;
	}

	public List<HashMap> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<HashMap> roleList) {
		this.roleList = roleList;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getBzxx() {
		return bzxx;
	}

	public void setBzxx(String bzxx) {
		this.bzxx = bzxx;
	}

	public String getDeptMc() {
		return deptMc;
	}

	public void setDeptMc(String deptMc) {
		this.deptMc = deptMc;
	}
}