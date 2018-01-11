package com.server.service;

import com.server.mapper.SysControlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by Administrator on 2017/11/7.
 */
@Service
@Component
public class RegisterService {
    @Autowired
    SysControlMapper sysControlMapper;

    public void getRequestList(Map param){
        sysControlMapper.insertTest();
    }


}
