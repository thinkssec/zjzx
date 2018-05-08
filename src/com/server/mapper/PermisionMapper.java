package com.server.mapper;



import com.common.annotation.MyBatisDao;
import com.server.Entity.Condition;

import java.util.HashMap;
import java.util.List;

@MyBatisDao
public interface PermisionMapper {
    /**
     *
     */

    List<HashMap> getDeptList(Condition condition);
    void saveDeptList(HashMap condition);
    void deteleDept(HashMap condition);

    List<HashMap> getUserList(Condition condition);
    void saveUserList(HashMap condition);
    void delUserlist(HashMap condition);


}
