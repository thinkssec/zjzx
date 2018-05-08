package com.server.mapper;

import com.common.annotation.MyBatisDao;
import com.server.Entity.Condition;


import java.util.HashMap;
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
public interface JqsqMapper {

	/**
	 * 插入一个数据持久化对象
	 *            要插入的数据持久化对象
	 * @return 返回影响行数
	 */
	List<HashMap> getJqsq(Condition condition);
	List<HashMap> getJqsqpz(Condition condition);
	List<HashMap> getJqsqwpz(Condition condition);
	void saveJqsqty(HashMap condition);
	void saveJqsqbty(HashMap condition);
	void saveJqsqdel(HashMap condition);
	List<HashMap> getDw(Condition condition);
	List<HashMap> getYq(Condition condition);
    void saveJqsqpz(HashMap map);
    HashMap isRegist(Map map);
	HashMap isRegist2(Map map);
    void regist(Map params);

	void saveJqsqtingy(HashMap m);

	void saveJqsqzhux(HashMap m);

	void saveJqsqqiy(HashMap m);
}
