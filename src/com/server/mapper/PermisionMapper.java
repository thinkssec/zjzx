package com.server.mapper;



import com.common.annotation.MyBatisDao;
import com.common.sys.entity.Office;
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
    List<HashMap> getUserListD(Condition condition);
    List<HashMap> getUserList2(Condition condition);
    void saveUserList(HashMap condition);
    void saveUserListD(HashMap condition);
    void delUserlist(HashMap condition);
    void qyUserlist(HashMap condition);
    void tyUserlist(HashMap condition);
    void assignUser(HashMap condition);
    void assignUserGly(HashMap condition);


}
