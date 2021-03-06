package com.server.mapper;


import com.common.annotation.MyBatisDao;
import com.server.Entity.Condition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

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
public interface BbglMapper {

	/**
	 * 插入一个数据持久化对象
	 *
	 * @param params
	 *            要插入的数据持久化对象
	 * @return 返回影响行数
	 */
	List<HashMap> getBbxtlist(Condition condition);
	List<HashMap> getBbsjlist(Condition condition);
	List<HashMap> getSjtypeList(Condition condition);
	void saveBbxt(HashMap map);
	void saveBbsj(HashMap map);
	void xfBbxt(HashMap params);
	void xfBbsj(HashMap params);
	void delBbxt(HashMap params);
	void delBbsj(HashMap params);
	List<HashMap> getXtSjInfo(Map map);
	List<HashMap> getBbsbzclist(Condition condition);
	void saveBbsbzc(HashMap map);
    void xfSbzcxt(HashMap params);
    void delBbsbzc(HashMap params);
	List<HashMap> getSbzcBbList(Map params);
	void jcBbsbzc(HashMap params);
//region
List<HashMap>  getyqlist(Condition  condition);
	List<HashMap>  getyqbblist(Condition  condition);
	void  saveYq(HashMap  map);
	void  delyq(HashMap  params);
	void  saveYqbb(HashMap  map);
	void  delyqbb(HashMap  params);
	void  delBbsbzcMlByBbh(@Param("bbh") String bbh);
	void  delBbsbzcZbByBbh(@Param("bbh") String bbh);
	void  delBbsbzcRelationByBbh(@Param("bbh") String bbh);
	//endregion
}
