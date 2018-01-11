package com.server.mapper;

import com.common.annotation.MyBatisDao;
import com.common.annotation.SqlServerDao;
import com.server.Entity.MainMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/17.
 */
@MyBatisDao
@SqlServerDao
public interface AnonMapper extends MainMapper {
    public HashMap selectRegistInf(Map m);
    public void insertRegistInf(Map m);

}
