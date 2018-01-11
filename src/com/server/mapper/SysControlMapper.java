package com.server.mapper;

import com.common.annotation.MyBatisDao;
import com.common.annotation.SqlServerDao;
import com.server.Entity.MainMapper;
import com.server.Entity.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    List<HashMap> getRequestList();
    List<HashMap> getHandleMsgList();
    void doBuzByin(Map param);
    void insertTest();
    HashMap getRequestOne(String id);
}
