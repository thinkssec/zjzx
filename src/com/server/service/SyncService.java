package com.server.service;

import com.common.annotation.mapper.JsonMapper;
import com.fasterxml.jackson.databind.JavaType;
import com.server.Entity.RequestBody;
import com.server.Entity.ResponseBody;
import com.server.mapper.EmailMapper;
import com.server.mapper.SyncMapper;
import com.server.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/18.
 */
@Service
@Component
public class SyncService {
    @Autowired
    SyncMapper syncMapper;
    @Transactional
    public ResponseBody sycndata(RequestBody rq, Map params, String id){
        ResponseBody rp=new ResponseBody(params,"1","获取指标库成功！",id,rq.getTaskid());
        //System.out.println(params);
        try{
            syncMapper.updsycnt(null);
            String sycnTime=syncMapper.getCurrentTime();
            List<HashMap> zb=syncMapper.getzblist(params);
            List<HashMap> zbml=syncMapper.getzbmllist(params);
            List<HashMap> tj=syncMapper.gettjblist(params);
            HashMap datas=new HashMap();
            datas.put("zb",zb);
            datas.put("zbml",zbml);
            datas.put("tj",tj);
            datas.put("sycnTime",sycnTime);
            rp.setDatas(JsonMapper.toJsonString(datas));
            //System.out.println(rp.getDatas());
        }catch(Exception e){
            e.printStackTrace();
            rp.setIssuccess("0");
            rp.setMessage("获取指标库失败");
        }
        return rp;
    }

}
