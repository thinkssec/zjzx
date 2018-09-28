package com.server.mapper;


import com.common.annotation.MyBatisDao;
import com.server.Entity.Condition;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>[appRgcbGlq]数据访问接口</b>
 * 
 * <p>
 * 注意:此文件自动生成-禁止手工修改
 * </p>
 * 
 * @author @blue
 * @date 2017-06-20
 */
@MyBatisDao
public interface BczbMapper {
	void mergeProject(HashMap map);
	List<LinkedHashMap> getBczbProperty(String bczbid);
	List<LinkedHashMap> getBczbProperty2(@Param("rootId") String rootId,@Param("scope") String scope);
	List<LinkedHashMap> getBczbTreeById(String bczbid);
	List<LinkedHashMap> getBczbTreeById2(@Param("rootId") String rootId,@Param("scope") String scope);
	List<LinkedHashMap> getOtherTest();
	List<LinkedHashMap> getOtherTest2();
	//获取原始补充指标
	List<HashMap> getYsBczb(Map c);
    List<HashMap> getBczbByFz(Map c);
	List<HashMap> getProjectByScope(@Param("scope") String scope);
	List<HashMap> getZhzbList(Condition params);
	List<HashMap> getJsrList(Condition params);
	List<HashMap> getZbByFz(Condition params);
	void updateFzPath(@Param("id") String id,@Param("path") String path);
	void saveBczbZh(Condition params);
	void addZb2Fz(Map params);
	void delZb2Fz(Map params);
}
