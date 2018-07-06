/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.common.sys.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import com.server.Entity.Menu;

import java.util.List;

/**
 * 机构Entity
 * @author ThinkGem
 * @version 2013-05-15
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Office extends TreeEntity<Office> {

	private static final long serialVersionUID = 1L;
/*	private Office parent;	// 父级编号*/
	private String parentIds; // 所有父级编号
	private String code; 	// 机构编码
//	private String name; 	// 机构名称
//	private Integer sort;		// 排序
	private String type; 	// 机构类型（1：公司；2：部门；3：小组）
	private String typpe;
	private String typppe;
	private String typpppe;
	private String typppppe;
	private String typpppppe;
	private String typppppppe;

	public String getTyppppe() {
		return typpppe;
	}

	public String getTypppppe() {
		return typppppe;
	}

	public void setTypppppe(String typppppe) {
		this.typppppe = typppppe;
	}

	public String getTyppppppe() {
		return typpppppe;
	}

	public void setTyppppppe(String typpppppe) {
		this.typpppppe = typpppppe;
	}

	public String getTypppppppe() {
		return typppppppe;
	}

	public void setTypppppppe(String typppppppe) {
		this.typppppppe = typppppppe;
	}

	public void setTyppppe(String typpppe) {
		this.typpppe = typpppe;
	}

	private String grade; 	// 机构等级（1：一级；2：二级；3：三级；4：四级）
	private String address; // 联系地址
	private String zipCode; // 邮政编码
	private String master; 	// 负责人
	private String phone; 	// 电话
	private String fax; 	// 传真
	private String email; 	// 邮箱
	private String useable;//是否可用
	private String dwdm;
	private String dwmc;
	private String lxr;
	private String lxrDh;
	private String bz;
	private String sfqy;
	private String isdel;
	private String parentId;
    private String parentName;
	private String isleaf;
	private List<Menu> menuList = Lists.newArrayList();
	public String getParentName() {
		return parentName;
	}

	public List<Menu> getMenuList() {
		return menuList;
	}

	public void setMenuList(List<Menu> menuList) {
		this.menuList = menuList;
	}

	public String getTyppe() {
		return typpe;
	}

	public void setTyppe(String typpe) {
		this.typpe = typpe;
	}

	public String getIsleaf() {
		return isleaf;
	}

	public void setIsleaf(String isleaf) {
		this.isleaf = isleaf;
	}

	public String getTypppe() {
		return typppe;
	}

	public void setTypppe(String typppe) {
		this.typppe = typppe;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	@Override
	public String getParentIds() {
		return parentIds;
	}

	@Override
	public void setParentIds(String parentIds) {
		this.parentIds = parentIds;
	}

	private User primaryPerson;//主负责人
	private User deputyPerson;//副负责人
	private List<String> childDeptList;//快速添加子部门

	public String getDwdm() {
		return dwdm;
	}

	public void setDwdm(String dwdm) {
		this.dwdm = dwdm;
	}

	public String getDwmc() {
		return dwmc;
	}

	public void setDwmc(String dwmc) {
		this.dwmc = dwmc;
	}

	public String getLxr() {
		return lxr;
	}

	public void setLxr(String lxr) {
		this.lxr = lxr;
	}

	@Override
	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getBz() {
		return bz;
	}

	public void setBz(String bz) {
		this.bz = bz;
	}

	public String getSfqy() {
		return sfqy;
	}

	public void setSfqy(String sfqy) {
		this.sfqy = sfqy;
	}

	public String getIsdel() {
		return isdel;
	}

	public void setIsdel(String isdel) {
		this.isdel = isdel;
	}

	public Office(){
		super();
//		this.sort = 30;
		this.type = "2";
	}

	public Office(String id){
		super(id);
	}

	@Override
	public Office getParent() {
		return null;
	}

	@Override
	public void setParent(Office parent) {

	}

	public List<String> getChildDeptList() {
		return childDeptList;
	}

	public void setChildDeptList(List<String> childDeptList) {
		this.childDeptList = childDeptList;
	}

	public String getUseable() {
		return useable;
	}

	public void setUseable(String useable) {
		this.useable = useable;
	}

	public User getPrimaryPerson() {
		return primaryPerson;
	}

	public void setPrimaryPerson(User primaryPerson) {
		this.primaryPerson = primaryPerson;
	}

	public User getDeputyPerson() {
		return deputyPerson;
	}

	public void setDeputyPerson(User deputyPerson) {
		this.deputyPerson = deputyPerson;
	}

//
//	@Length(min=1, max=2000)
//	public String getParentIds() {
//		return parentIds;
//	}
//
//	public void setParentIds(String parentIds) {
//		this.parentIds = parentIds;
//	}


//
//	@Length(min=1, max=100)
//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
//
//	public Integer getSort() {
//		return sort;
//	}
//
//	public void setSort(Integer sort) {
//		this.sort = sort;
//	}
	

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

//	public String getParentId() {
//		return parent != null && parent.getId() != null ? parent.getId() : "0";
//	}
	

	public String toString() {
		return dwmc;
	}

	public String getLxrDh() {
		return lxrDh;
	}

	public void setLxrDh(String lxrDh) {
		this.lxrDh = lxrDh;
	}
}