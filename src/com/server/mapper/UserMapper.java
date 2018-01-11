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
public interface UserMapper extends MainMapper {
    HashMap getUserOne(String username);
    void updUserList(HashMap param);
    void delUserList(HashMap param);
    List<HashMap> getUserList(Map params);
    List<HashMap> getDeptList(Map params);
    List<HashMap> validRegstUser(String username);

}
