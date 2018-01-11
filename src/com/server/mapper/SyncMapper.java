package com.server.mapper;

import com.common.annotation.MyBatisDao;
import com.common.annotation.SqlServerDao;
import com.server.Entity.MainMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/17.
 */
@MyBatisDao
@SqlServerDao
public interface SyncMapper extends MainMapper {
    public List<HashMap> getzblist(Map m);
    public List<HashMap> getzbmllist(Map m);
    public List<HashMap> gettjblist(Map m);
    public void updsycnt(Map m);
    public String getCurrentTime();
}
