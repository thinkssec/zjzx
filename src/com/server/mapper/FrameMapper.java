package com.server.mapper;

import com.common.annotation.MyBatisDao;
import com.common.sys.entity.Office;
import com.common.sys.entity.Role;
import com.common.sys.entity.User;
import com.server.Entity.Condition;
import com.server.Entity.MainMapper;
import com.server.Entity.Menu;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/17.
 */
@MyBatisDao
public interface FrameMapper extends MainMapper {
    public Map<String, Object> getPermission(String key);
    public List<Menu> findByUserId(Menu menu);
    public List<Menu> findAllList(Menu entity);
    public Menu findMenuById( String id);
    public Role findRoleById( String id);
    public Role getRoleByName( String name);
    public int insertMenu(Menu entity);
    public int updateMenu(Menu entity);
    public int deleteMenu(Menu entity);
    public List<Menu> findByParentIdsLike(Menu menu);
    public int updateParentIds(Menu menu);
    public int updateMenuSort(HashMap m);
    public List<Role> findAllRole(Role entity);
    public void panMCreate(Map m);
    public void panDCreate(Map m);
    public void panMUpdate(Map m);
    public void panMDelete(Map m);
    public void panDDelete(Map m);
    public void panMXmDelete(Map m);
    public void panDXmDelete(Map m);
    public void panMUpload(Map m);
    public void panDUpload(Map m);
    public List<HashMap> getPanMList(Map m);
    public List<HashMap> getPanDList(Map m);
    public List<HashMap> getPanMXmList(Map m);
    public List<HashMap> getPanDXmList(Map m);
    public int insertRole(Role entity);
    public int updateRole(Role entity);

    public int deleteRoleMenu(Role entity);
    public int deleteOfficeMenu(Office entity);
    public int insertRoleMenu(Role entity);
    public int insertOfficeMenu(Office entity);
    public void deleteRole(Role role);
    public void deleteOffice(Office office);
    public void deleteMypan(Office office);
    public void deleteXm(Office office);
    public void outrole(User user);
    public void outroleGly(User user);
    public List<User> findUserByRoleId(User user);
    public List<User> findUserByOfficeId(Office office);
    List<Office> getOfficeList(Office office);
    List<Office> getOfficeRList(Office office);
    List<Office> getOfficeDList(Office office);
    List<Office> getOfficeMList(Office office);
    List<Office> getOfficeMyList(Office office);
    List<Office> xmglLists(Office office);
    List<Office> xtxmglLists(Office office);
    List<Office> xmglListsD(Office office);
    @MapKey("id")
    HashMap<String,Office> bczbMlLists(Map office);
    @MapKey("id")
    HashMap<String,Office> bcelfMlLists(Map office);
    @MapKey("id")
    HashMap<String,Office> bcsbzcAMlLists(Map office);
    @MapKey("id")
    HashMap<String,Office> bczbMlListsDt(Map office);
    @MapKey("id")
    HashMap<String,Office> bcsbzcMlLists(Map office);
    @MapKey("id")
    HashMap<String,Office> xtxmMlLists(Map office);
    @MapKey("id")
    HashMap<String,Office> xtxmMlLists2(Map office);
    List<Office> getOfficeMList2(Office office);
    List<Office> getOfficeMmyList2(Office office);
    public List<Office> getAllOffice();
    public Office getOfficeById(Office office);
    public Office getMypanById(Office office);
    public List<Menu> getOfficeMenuById(Office office);
    public void saveOffice(Office office);
    public void saveOfficer(Office office);
    public void saveOfficem(Office office);
    public void saveMypan(Office office);
    public void saveXtxm(Office office);
    void insertBczbRoot(@Param("CODE") String deptCode,@Param("DEPTID")String deptid);
    void insertBcsbzcRoot(@Param("CODE") String deptCode,@Param("DEPTID")String deptid,@Param("BBH")String bbh);
    void insertElfRoot(@Param("CODE") String deptCode,@Param("DEPTID")String deptid);
    void bczbmlsave(Condition condition);
    void bcsbzcmlsave(Condition condition);
    List<HashMap> getMlZbAList(Map m);
    List<HashMap> getMlElfAList(Map m);
    List<HashMap> getMlSbzcAList(Map m);
    List<HashMap> getMlSbzcAList2(Map m);
    List<HashMap> getAdminByCode(@Param("CODE") String code);
    String getCodeByCpu(@Param("CPU") String CPU);
    String getOilByCpu(@Param("CPU") String CPU);

    int isAdmin(@Param("USERID") String userid);
}
