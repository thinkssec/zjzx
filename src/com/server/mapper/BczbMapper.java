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
	void bqzd(HashMap map);
	void insertBczb1(HashMap map);
	void insertBczb2(HashMap map);
	void mergeProject3(HashMap map);
	void mergeProject4(HashMap map);
	List<LinkedHashMap> getBczbProperty(String bczbid);
	List<LinkedHashMap> getBczbProperty2(@Param("rootId") String rootId,@Param("scope") String scope);
	List<LinkedHashMap> getBczbTreeById(String bczbid);
	List<LinkedHashMap> getYhBczbTreeById(String bczbid);
	List<LinkedHashMap> getDwBczbTreeById(String bczbid);
	List<LinkedHashMap> getBczbTreeById2(@Param("rootId") String rootId,@Param("scope") String scope);
	List<LinkedHashMap> getOtherTest();
	List<LinkedHashMap> getOtherTest2();
	//获取原始补充指标
	List<HashMap> getYsBczb(Map c);
	List<HashMap> getxmbczbList(Condition condition);
	List<HashMap> getxtbczbList(Condition condition);
	List<HashMap> getbczbyyList(Condition condition);
	List<HashMap> getYsBczbXf(Map c);
	List<HashMap> getDwBczbByDw(Map c);
	List<HashMap> getDwElfByDw(Map c);
	List<HashMap> getDwGcfByDw(Map c);
	List<HashMap> getDwQdByDw(Map c);
	List<HashMap> getBcsbzcByDw(Map c);
	List<HashMap> getZbByServerId(@Param("SCOPE") String scope);
	List<HashMap> getZbzxByServerId(@Param("SCOPE") String scope);
	List<HashMap> getDwZbByServerId(@Param("SCOPE") String scope);
	List<HashMap> getYhBczb(Map c);
	List<HashMap> getYhBczbzx(Map c);
	List<HashMap> getDwBczb(Map c);
	List<HashMap> getZxBczb(Map c);
	List<HashMap> getDwBcelf(Map c);
	List<HashMap> getDwBcgcf(Map c);
    List<HashMap> getBczbByFz(Map c);
	List<HashMap> getBczbByMl(Map c);
	List<HashMap> getBcsbzcByMl(Map c);
	List<HashMap> getProjectByScope(@Param("scope") String scope);
	
	List<HashMap> getOnlyProjectById(@Param("id") String id);
	List<HashMap> getResultMap(@Param("pid") String pid);
	
	List<HashMap> getZhzbList(Condition params);
	List<HashMap> getJsrList(Condition params);
	List<HashMap> getZbByFz(Condition params);
	void updateFzPath(@Param("id") String id,@Param("path") String path);
	void saveBczbZh(Condition params);
	void addZb2Fz(Map params);
	void addElf2Fz(Map params);
	void delElf4Ml(Condition params);
	void delGcf4Ml(Condition params);
	void addZb2Ml(Map params);
	void addZb2MlSbzc(Condition params);
    void updSbzc(Condition params);
	void addZb2Zb(Map params);
    void addZb2ZbSbzc(Condition params);
	void delZb2Fz(Map params);
	void delBCZB(Map params);
	void delFz(@Param("ID") String id);
	
	void delZb2Ml(Map m);
	 List<String> getMlTreeListById(Map m);
	void delZbml(Map m);
	void delElf2Ml(Map m);
    void delSbzc2Ml(Map m);
    void delSbzcZB(Map<String,String> m);
	List<LinkedHashMap> getYhBczbProperty(String bczbid);
	List<LinkedHashMap> getDwBczbProperty(String bczbid);
//补充设备主材
    List<HashMap> getBcsbzcList(Condition params);
    String getBczbRootByCode(@Param("CODE") String code);
	String getBcSbzcRootByCode(@Param("CODE") String code,@Param("BBH") String bbh);

	void xfElf(Map m);
	void xfGcf(Map m);
	void changshi(List list);
    List<HashMap> getDic();
    void updateRealZbmc(Condition m);
    List<String> getZbsBymainId(Condition m);
    List<String> getOilAreasByZbId(Map<String,String> m);
    void deleteSourceZb(Condition m);
    void deleteOldZbRelation(Condition m);
    List getZbInfoById(String zbid);
    String getAddCodeByParam(Map<String,String> m);
    String getNewCodeByParam(Map<String,String> m);
    String getMlidByParam(Map<String,String> m);
    int getMlRelationNum(Map<String,String> m);
    void delMlWhenNull(Map<String,String> m);
}
