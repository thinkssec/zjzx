package com.server.mapper;

import com.common.annotation.MyBatisDao;
import com.common.annotation.SqlServerDao;
import com.server.Entity.MainMapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/17.
 */
@MyBatisDao
@SqlServerDao
public interface EmailMapper extends MainMapper {
    void sendMail(Map param);
    void replyMail(Map param);
    void sendAttach(Map param);
    void delete(Map param);
    void readed(Map param);
    List<HashMap> getMailList(Map param);
    List<HashMap> getMailAList(Map param);
}
