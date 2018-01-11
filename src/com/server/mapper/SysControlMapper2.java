package com.server.mapper;

import com.common.annotation.MyBatisDao;
import com.common.annotation.SqliteDao;
import com.server.Entity.MainMapper;
import com.server.Entity.RequestBody;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/10/17.
 */
@MyBatisDao
@SqliteDao
public interface SysControlMapper2 extends MainMapper {
    @SqliteDao
    void attachDb();
    @SqliteDao
    List<HashMap> selectFromTest();
    @SqliteDao
    void insertTesst();
}
