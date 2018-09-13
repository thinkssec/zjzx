package com.server.mapper;


import com.common.annotation.MyBatisDao;
import com.server.Entity.Condition;
import org.apache.ibatis.annotations.MapKey;

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

	List<LinkedHashMap> getBczbTreeById(String bczbid);
	List<LinkedHashMap> getOtherTest();
	List<LinkedHashMap> getOtherTest2();
	//获取原始补充指标
	List<HashMap> getYsBczb(Condition c);
}
