package com.server.service;

import com.common.config.DataSourceContextHolder;
import com.server.mapper.SysControlMapper;
import com.server.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/11/21.
 */
@Service
@Component
public class AdminService{
    @Autowired
    UserMapper userMapper;
    public HashMap getUserOne(String username){
        DataSourceContextHolder.setDbType(DataSourceType.Datasource1);
        HashMap u=userMapper.getUserOne(username);
        return u;
    }

}
