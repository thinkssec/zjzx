package com.server.mapper;

import com.common.annotation.MyBatisDao;
import com.server.Entity.MainMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/17.
 */
@MyBatisDao
public interface FrameMapper extends MainMapper {
public Map<String, Object> getPermission(String key);
}
