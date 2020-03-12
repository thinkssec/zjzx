package com.server.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.common.annotation.MyBatisDao;
import com.common.annotation.SqlServerDao;
import com.server.Entity.MainMapper;
import com.server.Entity.RequestBody;

/**
 * Created by Administrator on 2017/10/17.
 */
@MyBatisDao
@SqlServerDao
public interface SysControlMapper extends MainMapper {
    void insertRequest(RequestBody param);
    void updateRequest(RequestBody param);
    void updateRequest2(RequestBody param);
    void updateRequest3(RequestBody param);
    void updateHdlMsg(RequestBody param);
    void deleteRequest(RequestBody param);
    List<HashMap<String, String>> getRequestList(@Param("startTime")String c1, @Param("endTime")String c2);
    List<HashMap<String, String>> getHandleMsgList();
    void doBuzByin(Map param);
    void insertTest();
    HashMap getRequestOne(String id);
}
