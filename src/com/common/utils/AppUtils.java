package com.common.utils;

import com.common.sys.entity.Office;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Administrator on 2018/1/11.
 */
public class AppUtils {
    public static int deptStep = 4;

    public static String getParentDept(String deptid) {
        if (!StringUtils.isNotBlank(deptid)) return "('abcd')";
        StringBuffer depts = new StringBuffer("");
        depts.append("(");
        depts.append("'" + deptid + "'");
        int s = deptid.length();
        int t = s / deptStep - 1;
        for (int i = 1; i <= t; i++) {
            depts.append(",'" + deptid.substring(0, deptid.length() - i * deptStep) + "'");

        }
        depts.append(")");
        return depts.toString();
    }

    public static void sort(List<HashMap> dlist, LinkedHashMap<String, String> orderby) {
        if (orderby == null) return;
        for (final String k : orderby.keySet()) {
            final String v = orderby.get(k);
            Collections.sort(dlist, new Comparator<HashMap>() {
                public int compare(HashMap u1, HashMap u2) {
                    Object a1 = u1.get(k);
                    Object a2 = u2.get(k);
                    if (a1 == null) a1 = new String("");
                    if (a2 == null) a2 = new String("");
                    if ("desc".equals(v)) {
                        return ((String) a2).compareTo(((String) a1)); //升序
                    } else {
                        return ((String) a1).compareTo(((String) a2)); //升序
                    }
                }
            });
        }
    }
    public static String readMap2Sql5(Map<String, Object> map, String pid,String pk,String deptid,String cpuid,String userid) {
        StringBuffer sql = new StringBuffer("");
        for (String key : map.keySet()) {
            //Map<String, Object> node = (Map<String, Object>) map.get(key);
            Object node = map.get(key);
            if("DocumentElement".equals(key) && node instanceof Map){
                String id = (String) ((Map<String, Object>) node).get("@ProjectVersionID");
                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql5(child1, "","",deptid,cpuid,userid));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql5(child1, "","",deptid,cpuid,userid));
                        }
                    }
                }
            }if("OtherCost".equals(key) && node instanceof Map){
                String typeName = (String) ((Map<String, Object>) node).get("@TypeName");
                String usedSpecdName = (String) ((Map<String, Object>) node).get("@UsedSpecdName");
                String pkey = typeName+usedSpecdName + cpuid + userid;
                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql5(child1, "",pkey,deptid,cpuid,userid));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql5(child1, "",pkey,deptid,cpuid,userid));
                        }
                    }
                }
            }
            else {
                String uuid = IdGen.uuid();
                if(StringUtils.isBlank(pid)) pid="-1";
                if (node instanceof String) {
                    sql.append("insert into APP_OPROPERTY_YHBCELF(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(key.substring(1)) + "','" + decodeSpecialChars(node.toString()) + "','"+deptid+"');");
                } else {
                    sql.append("insert into APP_OBJECT_YHBCELF(ID,PID,OTYPE,LB,DEPTID,USERID) VALUES ('" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1','"+deptid+"','"+userid+"');");
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String)
                            sql.append(" insert into APP_OPROPERTY_YHBCELF(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"');");
                        else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            sql.append(readMap2Sql5(child1, uuid,"",deptid,cpuid,userid));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                sql.append(readMap2Sql5(child1, uuid,"",deptid,cpuid,userid));
                            }
                        }
                    }
                }
            }
        }
        return sql.toString();
    }
    public static String readMap2Sql4_bak(Map<String, Object> map, String pid,String pk,String deptid,String cpuid,String userid,String oilid) {
        StringBuffer sql = new StringBuffer("");
        for (String key : map.keySet()) {
            //Map<String, Object> node = (Map<String, Object>) map.get(key);
            Object node = map.get(key);
            if("GeneralInformation".equals(key) && node instanceof Map){
                String id = (String) ((Map<String, Object>) node).get("@ProjectVersionID");
                String oilCode=(String) ((Map<String, Object>) node).get("@OilCode");
                /*String cpuid = (String) ((Map<String, Object>) node).get("@CpuID");
                String userid = (String) ((Map<String, Object>) node).get("@UserID");
                String deptid = (String) ((Map<String, Object>) node).get("@DeptID");*/
                String pkey = id + cpuid + userid;
                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql4(child1, "",pkey,deptid,cpuid,userid,oilCode));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql4(child1, "",pkey,deptid,cpuid,userid,oilCode));
                        }
                    }
                }

            }
            else if ("Project".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ProjectID");
                String pkey = pk+id;

                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String){

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql4(child1, "",pkey,deptid,cpuid,userid,oilid));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql4(child1, "",pkey,deptid,cpuid,userid,oilid));
                        }
                    }
                }
            } else {
                String uuid = IdGen.uuid();
                //if(map.get("@SERVERID")!=null&&!"".equals((String)map.get("@SERVERID"))) uuid=map.get("@SERVERID").toString();
                if(StringUtils.isBlank(pid)) pid="-1";
                if (node instanceof String) {
                    sql.append(" insert into APP_OPROPERTY_DWCBBCZB(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(key.substring(1)) + "','" + decodeSpecialChars(node.toString()) + "','"+deptid+"');");
                } else {
                    sql.append("insert into APP_OBJECT_DWCBBCZB(ID,PID,OTYPE,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1','"+deptid+"','"+userid+"','"+oilid+"');");
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String)
                            sql.append(" insert into APP_OPROPERTY_DWCBBCZB(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"');");
                        else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            sql.append(readMap2Sql4(child1, uuid,"",deptid,cpuid,userid,oilid));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                sql.append(readMap2Sql4(child1, uuid,"",deptid,cpuid,userid,oilid));
                            }
                        }
                    }
                }
            }
        }
        return sql.toString();
    }
    public static String readMap2Sql4(Map<String, Object> map, String pid,String pk,String deptid,String cpuid,String userid,String oilid) {
        StringBuffer ll=new StringBuffer("");
        for (String key : map.keySet()) {
            Object node = map.get(key);
            if("GeneralInformation".equals(key) && node instanceof Map){
                String id = (String) ((Map<String, Object>) node).get("@ProjectVersionID");
                String oilCode=(String) ((Map<String, Object>) node).get("@OilCode");
                String pkey = id + cpuid + userid;
                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        ll.append(readMap2Sql4(child1, "",pkey,deptid,cpuid,userid,oilCode));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            ll.append(readMap2Sql4(child1, "",pkey,deptid,cpuid,userid,oilCode));
                        }
                    }
                }
            }
            else if ("Project".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ProjectID");
                String pkey = pk+id;
                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String){
                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        ll.append(readMap2Sql4(child1, "",pkey,deptid,cpuid,userid,oilid));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            ll.append(readMap2Sql4(child1, "",pkey,deptid,cpuid,userid,oilid));
                        }
                    }
                }
            } else {
                String uuid = IdGen.uuid();
                if(StringUtils.isBlank(pid)) pid="-1";
                if (node instanceof String) {
                    //sql.append(" insert into APP_OPROPERTY_DWCBBCZB(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(key.substring(1)) + "','" + decodeSpecialChars(node.toString()) + "','"+deptid+"');");
                } else {
                    String bef="insert into APP_OBJECT_DWCBBCZB(ord,ID,PID,OTYPE,LB,DEPTID,USERID,OILID";
                    String aft=" VALUES (SEQ_NODEORD.NEXTVAL,'" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1','"+deptid+"','"+userid+"','"+oilid+"'";
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String) {
                            bef=bef+",P_"+decodeSpecialChars(okey.substring(1));
                            aft=aft+",'"+decodeSpecialChars(property.toString())+"'";
                        }else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            ll.append(readMap2Sql4(child1, uuid,"",deptid,cpuid,userid,oilid));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                ll.append(readMap2Sql4(child1, uuid,"",deptid,cpuid,userid,oilid));
                            }
                        }
                    }
                    ll.append(bef+")" +aft+");");
                }
            }
        }
        return ll.toString();
    }
    public static String readMap2Sql4zx(Map<String, Object> map, String pid,String pk,String deptid,String cpuid,String userid,String oilid) {
        StringBuffer ll=new StringBuffer("");
        for (String key : map.keySet()) {
            Object node = map.get(key);
            if("GeneralInformation".equals(key) && node instanceof Map){
                String id = (String) ((Map<String, Object>) node).get("@ProjectVersionID");
                String oilCode=(String) ((Map<String, Object>) node).get("@OilCode");
                String pkey = id + cpuid + userid;
                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        ll.append(readMap2Sql4(child1, "",pkey,deptid,cpuid,userid,oilCode));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            ll.append(readMap2Sql4(child1, "",pkey,deptid,cpuid,userid,oilCode));
                        }
                    }
                }
            }
            else if ("Project".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ProjectID");
                String pkey = pk+id;
                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String){
                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        ll.append(readMap2Sql4(child1, "",pkey,deptid,cpuid,userid,oilid));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            ll.append(readMap2Sql4(child1, "",pkey,deptid,cpuid,userid,oilid));
                        }
                    }
                }
            } else {
                String uuid = IdGen.uuid();
                if(StringUtils.isBlank(pid)) pid="-1";
                if (node instanceof String) {
                    //sql.append(" insert into APP_OPROPERTY_DWCBBCZB(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(key.substring(1)) + "','" + decodeSpecialChars(node.toString()) + "','"+deptid+"');");
                } else {
                    String bef="insert into APP_OBJECT_ZXCBBCZB(ord,ID,PID,OTYPE,LB,DEPTID,USERID,OILID";
                    String aft=" VALUES (SEQ_NODEORD.NEXTVAL,'" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1','"+deptid+"','"+userid+"','"+oilid+"'";
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String) {
                            bef=bef+",P_"+decodeSpecialChars(okey.substring(1));
                            aft=aft+",'"+decodeSpecialChars(property.toString())+"'";
                        }else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            ll.append(readMap2Sql4(child1, uuid,"",deptid,cpuid,userid,oilid));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                ll.append(readMap2Sql4(child1, uuid,"",deptid,cpuid,userid,oilid));
                            }
                        }
                    }
                    ll.append(bef+")" +aft+");");
                }
            }
        }
        return ll.toString();
    }

    public static String readMap2Sql3_bak(Map<String, Object> map, String pid,String pk,String deptid,String cpuid,String userid,String oilid) {
        StringBuffer sql = new StringBuffer("");
        for (String key : map.keySet()) {
            //Map<String, Object> node = (Map<String, Object>) map.get(key);
            Object node = map.get(key);
            if("GeneralInformation".equals(key) && node instanceof Map){
                String id = (String) ((Map<String, Object>) node).get("@ProjectVersionID");
                String oilCode=(String) ((Map<String, Object>) node).get("@OilCode");
                /*String cpuid = (String) ((Map<String, Object>) node).get("@CpuID");
                String userid = (String) ((Map<String, Object>) node).get("@UserID");
                String deptid = (String) ((Map<String, Object>) node).get("@DeptID");*/
                String pkey = id + cpuid + userid;
                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql3(child1, "",pkey,deptid,cpuid,userid,oilCode,""));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql3(child1, "",pkey,deptid,cpuid,userid,oilCode,""));
                        }
                    }
                }

            }
            else if ("Project".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ProjectID");
                String pkey = pk+id;

                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String){

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql3(child1, "",pkey,deptid,cpuid,userid,oilid,""));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql3(child1, "",pkey,deptid,cpuid,userid,oilid,""));
                        }
                    }
                }
            } else {
                String uuid = IdGen.uuid();
                //if(map.get("@SERVERID")!=null&&!"".equals((String)map.get("@SERVERID"))) uuid=map.get("@SERVERID").toString();
                if(StringUtils.isBlank(pid)) pid="-1";
                if (node instanceof String) {
                    sql.append(" insert into APP_OPROPERTY_YHBCZB(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(key.substring(1)) + "','" + decodeSpecialChars(node.toString()) + "','"+deptid+"');");
                } else {
                    sql.append("insert into APP_OBJECT_YHBCZB(ID,PID,OTYPE,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1','"+deptid+"','"+userid+"','"+oilid+"');");
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String)
                            sql.append(" insert into APP_OPROPERTY_YHBCZB(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"');");
                        else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            sql.append(readMap2Sql3(child1, uuid,"",deptid,cpuid,userid,oilid,""));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                sql.append(readMap2Sql3(child1, uuid,"",deptid,cpuid,userid,oilid,""));
                            }
                        }
                    }
                }
            }
        }
        return sql.toString();
    }
    public static String readMap2Sql3(Map<String, Object> map, String pid,String pk,String deptid,String cpuid,String userid,String oilid,String bz) {
        StringBuffer sql = new StringBuffer("");
        for (String key : map.keySet()) {
            //Map<String, Object> node = (Map<String, Object>) map.get(key);
            Object node = map.get(key);
            if("GeneralInformation".equals(key) && node instanceof Map){
                String id = (String) ((Map<String, Object>) node).get("@ProjectVersionID");
                String oilCode=(String) ((Map<String, Object>) node).get("@OilCode");
                String pkey = id + cpuid + userid;
                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql3(child1, "",pkey,deptid,cpuid,userid,oilCode,bz));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql3(child1, "",pkey,deptid,cpuid,userid,oilCode,bz));
                        }
                    }
                }

            }
            else if ("Project".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ProjectID");
                String pkey = pk+id;
                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String){

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql3(child1, "",pkey,deptid,cpuid,userid,oilid,bz));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql3(child1, "",pkey,deptid,cpuid,userid,oilid,bz));
                        }
                    }
                }
            } else {
                String uuid = IdGen.uuid();
                //if(map.get("@SERVERID")!=null&&!"".equals((String)map.get("@SERVERID"))) uuid=map.get("@SERVERID").toString();
                if(StringUtils.isBlank(pid)) pid="-1";
                if (node instanceof String) {
                } else {
                    String bef="insert into APP_OBJECT_YHBCZB(ord,ID,PID,OTYPE,LB,DEPTID,USERID,OILID, BZ";
                    String aft=" VALUES (SEQ_NODEORD.NEXTVAL,'" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1','"+deptid+"','"+userid+"','"+oilid+"','"+bz+"'";
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String) {
                            bef=bef+",P_"+decodeSpecialChars(okey.substring(1));
                            aft=aft+",'"+decodeSpecialChars(property.toString())+"'";
                        }else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            sql.append(readMap2Sql3(child1, uuid,"",deptid,cpuid,userid,oilid,bz));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                sql.append(readMap2Sql3(child1, uuid,"",deptid,cpuid,userid,oilid,bz));
                            }
                        }
                    }
                    sql.append(bef+")" +aft+");");
                }
            }
        }
        return sql.toString();
    }
    public static String readMap2Sql3zx(Map<String, Object> map, String pid,String pk,String deptid,String cpuid,String userid,String oilid) {
        StringBuffer sql = new StringBuffer("");
        for (String key : map.keySet()) {
            //Map<String, Object> node = (Map<String, Object>) map.get(key);
            Object node = map.get(key);
            if("GeneralInformation".equals(key) && node instanceof Map){
                String id = (String) ((Map<String, Object>) node).get("@ProjectVersionID");
                String oilCode=(String) ((Map<String, Object>) node).get("@OilCode");
                String pkey = id + cpuid + userid;
                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql3(child1, "",pkey,deptid,cpuid,userid,oilCode,""));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql3(child1, "",pkey,deptid,cpuid,userid,oilCode,""));
                        }
                    }
                }

            }
            else if ("Project".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ProjectID");
                String pkey = pk+id;
                String uuid = IdGen.uuid();
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String){

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql3(child1, "",pkey,deptid,cpuid,userid,oilid,""));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql3(child1, "",pkey,deptid,cpuid,userid,oilid,""));
                        }
                    }
                }
            } else {
                String uuid = IdGen.uuid();
                //if(map.get("@SERVERID")!=null&&!"".equals((String)map.get("@SERVERID"))) uuid=map.get("@SERVERID").toString();
                if(StringUtils.isBlank(pid)) pid="-1";
                if (node instanceof String) {
                } else {
                    String bef="insert into APP_OBJECT_YHBCZXZB(ord,ID,PID,OTYPE,LB,DEPTID,USERID,OILID ";
                    String aft=" VALUES (SEQ_NODEORD.NEXTVAL,'" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1','"+deptid+"','"+userid+"','"+oilid+"'";
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String) {
                            bef=bef+",P_"+decodeSpecialChars(okey.substring(1));
                            aft=aft+",'"+decodeSpecialChars(property.toString())+"'";
                        }else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            sql.append(readMap2Sql3(child1, uuid,"",deptid,cpuid,userid,oilid,""));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                sql.append(readMap2Sql3(child1, uuid,"",deptid,cpuid,userid,oilid,""));
                            }
                        }
                    }
                    sql.append(bef+")" +aft+");");
                }
            }
        }
        return sql.toString();
    }
    public static String readMap222Sql2(Map<String, Object> map, String pid,String pk,String deptid,String cpuid,String userid,String oilid) {
        StringBuffer sql = new StringBuffer("");
        for (String key : map.keySet()) {
            //Map<String, Object> node = (Map<String, Object>) map.get(key);
            Object node = map.get(key);
            if("GeneralInformation".equals(key) && node instanceof Map){
                String id = (String) ((Map<String, Object>) node).get("@ProjectVersionID");
                String oilCode=(String) ((Map<String, Object>) node).get("@OilCode");
                /*String cpuid = (String) ((Map<String, Object>) node).get("@CpuID");
                String userid = (String) ((Map<String, Object>) node).get("@UserID");
                String deptid = (String) ((Map<String, Object>) node).get("@DeptID");*/
                String pkey = id + cpuid + userid;
                sql.append("begin ");
                sql.append(" select count(1),id into v_count,v_infoid from APP_OBJECT_BCZBTREE where pkey='"+decodeSpecialChars(pkey)+"' group by id;");
                sql.append(" exception when others then null; end; ");
                sql.append(" if v_count=0 then begin ");
                String uuid = IdGen.uuid();
                sql.append(" v_infoid:='"+uuid+"';");
                sql.append("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,PKEY,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "','" + pid + "','" +decodeSpecialChars( key) + "','" + decodeSpecialChars(pkey) + "','1','" + deptid + "','"+userid+"','"+oilCode+"');");
                sql.append("v_cc:=v_cc+1;");
                sql.append("end; end if;");
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                        sql.append(" if v_count=0 then insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "',v_deptid); end if;");

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilCode));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilCode));
                        }
                    }
                }

            }
            else if ("Project".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ProjectID");
                String pkey = pk+id;
                sql.append(" begin ");
                sql.append(" select count(1) into v_cdxgc from APP_OBJECT_BCZBTREE where nvl(zt,0)>0 and pkey='"+decodeSpecialChars(pkey)+"';");
                sql.append(" if v_cdxgc =0 then ");
                sql.append(" begin ");
                sql.append(" delete from APP_OPROPERTY where oid in (select id from APP_OBJECT_BCZBTREE connect by prior id=pid start with PKEY='" + decodeSpecialChars(pkey) + "');");
                sql.append(" delete from APP_OBJECT_BCZBTREE where id in (select id from APP_OBJECT_BCZBTREE connect by prior id=pid start with PKEY='" + decodeSpecialChars(pkey) + "');");
                //String uuid = UUID.randomUUID().toString();
                String uuid = IdGen.uuid();
                sql.append("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,PKEY,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "',v_infoid,'" + decodeSpecialChars(key) + "','" + decodeSpecialChars(pkey) + "','1','" + deptid + "','"+userid+"','"+oilid+"');");
                sql.append("v_cc:=v_cc+1;");
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                        sql.append(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "',v_deptid);");
                        sql.append("v_cc:=v_cc+1;");
                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql2(child1, uuid,pkey,"",cpuid,userid,oilid));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql2(child1, uuid,pkey,"",cpuid,userid,oilid));
                        }
                    }
                }
                sql.append(" end;");
                sql.append(" end if;");
                sql.append(" end;");
            } else {
                sql.append("if v_cc mod 500 =0 then commit; end if;");
                String uuid = IdGen.uuid();
                //if(map.get("@SERVERID")!=null&&!"".equals((String)map.get("@SERVERID"))) uuid=map.get("@SERVERID").toString();
                if (node instanceof String) {
                    sql.append(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(key.substring(1)) + "','" + decodeSpecialChars(node.toString()) + "',v_deptid);");
                    sql.append("v_cc:=v_cc+1;");
                } else {
                    sql.append("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1',v_deptid,'"+userid+"','"+oilid+"');");
                    sql.append("v_cc:=v_cc+1;");
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String) {
                            sql.append(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "',v_deptid);");
                            sql.append("v_cc:=v_cc+1;");
                        }else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            sql.append(readMap2Sql2(child1, uuid,"","",cpuid,userid,oilid));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                sql.append(readMap2Sql2(child1, uuid,"","",cpuid,userid,oilid));
                            }
                        }
                    }
                }
            }
        }
        return sql.toString();
    }
    public static HashMap<String,List> readMap2Sql2(Map<String, Object> map, String pid,String pk,String deptid,String cpuid,String userid,String oilid) {
        HashMap<String,List> ll=new HashMap();
        if(ll.get("a")==null) ll.put("a",new ArrayList());
        if(ll.get("b")==null) ll.put("b",new ArrayList());
        List aList=ll.get("a");
        List bList=ll.get("b");
        for (String key : map.keySet()) {
            //Map<String, Object> node = (Map<String, Object>) map.get(key);
            Object node = map.get(key);
            if("GeneralInformation".equals(key) && node instanceof Map){
                String id = (String) ((Map<String, Object>) node).get("@ProjectVersionID");
                String oilCode=(String) ((Map<String, Object>) node).get("@OilCode");
                /*String cpuid = (String) ((Map<String, Object>) node).get("@CpuID");
                String userid = (String) ((Map<String, Object>) node).get("@UserID");
                String deptid = (String) ((Map<String, Object>) node).get("@DeptID");*/
                String pkey = id + cpuid + userid;
                aList.add("begin ");
                aList.add(" select count(1),id into v_count,v_infoid from APP_OBJECT_BCZBTREE where pkey='"+decodeSpecialChars(pkey)+"' group by id;");
                aList.add(" exception when others then null; end; ");
                aList.add(" if v_count=0 then begin ");
                String uuid = IdGen.uuid();
                aList.add(" v_infoid:='"+uuid+"';");
                aList.add("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,PKEY,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "','" + pid + "','" +decodeSpecialChars( key) + "','" + decodeSpecialChars(pkey) + "','1','" + deptid + "','"+userid+"','"+oilCode+"');");
                aList.add("end; end if;");
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                        aList.add(" if v_count=0 then insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"'); end if;");

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        HashMap<String,List> www=readMap2Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilCode);
                        aList.addAll(www.get("a"));
                        bList.addAll(www.get("b"));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            HashMap<String,List> www=readMap2Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilCode);
                            aList.addAll(www.get("a"));
                            bList.addAll(www.get("b"));
                        }
                    }
                }

            }
            else if ("Project".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ProjectID");
                String pkey = pk+id;
                aList.add(" begin ");
                aList.add(" select count(1) into v_cdxgc from APP_OBJECT_BCZBTREE where nvl(zt,0)>0 and pkey='"+decodeSpecialChars(pkey)+"';");
                aList.add(" if v_cdxgc =0 then ");
                aList.add(" begin ");
                aList.add(" delete from APP_OPROPERTY where oid in (select id from APP_OBJECT_BCZBTREE connect by prior id=pid start with PKEY='" + decodeSpecialChars(pkey) + "');");
                aList.add(" delete from APP_OBJECT_BCZBTREE where id in (select id from APP_OBJECT_BCZBTREE connect by prior id=pid start with PKEY='" + decodeSpecialChars(pkey) + "');");
                //String uuid = UUID.randomUUID().toString();
                String uuid = IdGen.uuid();
                aList.add("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,PKEY,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "',v_infoid,'" + decodeSpecialChars(key) + "','" + decodeSpecialChars(pkey) + "','1','" + deptid + "','"+userid+"','"+oilid+"');");
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                        aList.add(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"');");
                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        HashMap<String,List> www=readMap2Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilid);
                        aList.addAll(www.get("a"));
                        bList.addAll(www.get("b"));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            HashMap<String,List> www=readMap2Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilid);
                            aList.addAll(www.get("a"));
                            bList.addAll(www.get("b"));
                        }
                    }
                }
                aList.add(" end;");
                aList.add(" end if;");
                aList.add(" end;");
            } else {
                String uuid = IdGen.uuid();
                //if(map.get("@SERVERID")!=null&&!"".equals((String)map.get("@SERVERID"))) uuid=map.get("@SERVERID").toString();
                if (node instanceof String) {
                    bList.add(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(key.substring(1)) + "','" + decodeSpecialChars(node.toString()) + "','"+deptid+"');");
                } else {
                    bList.add("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1','"+deptid+"','"+userid+"','"+oilid+"');");
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String) {
                            bList.add(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"');");
                        }else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            HashMap<String,List> www=readMap2Sql2(child1, uuid,"",deptid,cpuid,userid,oilid);
                            aList.addAll(www.get("a"));
                            bList.addAll(www.get("b"));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                HashMap<String,List> www=readMap2Sql2(child1, uuid,"",deptid,cpuid,userid,oilid);
                                aList.addAll(www.get("a"));
                                bList.addAll(www.get("b"));
                            }
                        }
                    }
                }
            }
        }
        return ll;
    }

    public static HashMap<String,List> readMap2222Sql2(Map<String, Object> map, String pid,String pk,String deptid,String cpuid,String userid,String oilid) {
        HashMap<String,List> ll=new HashMap();
        if(ll.get("a")==null) ll.put("a",new ArrayList());
        if(ll.get("b1")==null) ll.put("b1",new ArrayList());
        if(ll.get("b2")==null) ll.put("b2",new ArrayList());
        List aList=ll.get("a");
        List bList1=ll.get("b1");
        List bList2=ll.get("b2");
        for (String key : map.keySet()) {
            //Map<String, Object> node = (Map<String, Object>) map.get(key);
            Object node = map.get(key);
            if("GeneralInformation".equals(key) && node instanceof Map){
                String id = (String) ((Map<String, Object>) node).get("@ProjectVersionID");
                String oilCode=(String) ((Map<String, Object>) node).get("@OilCode");
                /*String cpuid = (String) ((Map<String, Object>) node).get("@CpuID");
                String userid = (String) ((Map<String, Object>) node).get("@UserID");
                String deptid = (String) ((Map<String, Object>) node).get("@DeptID");*/
                String pkey = id + cpuid + userid;
                aList.add("begin ");
                aList.add(" select count(1),id into v_count,v_infoid from APP_OBJECT_BCZBTREE where pkey='"+decodeSpecialChars(pkey)+"' group by id;");
                aList.add(" exception when others then null; end; ");
                aList.add(" if v_count=0 then begin ");
                String uuid = IdGen.uuid();
                aList.add(" v_infoid:='"+uuid+"';");
                aList.add("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,PKEY,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "','" + pid + "','" +decodeSpecialChars( key) + "','" + decodeSpecialChars(pkey) + "','1','" + deptid + "','"+userid+"','"+oilCode+"');");
                aList.add("end; end if;");
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                        aList.add(" if v_count=0 then insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"'); end if;");

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        HashMap<String,List> www=readMap2222Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilCode);
                        aList.addAll(www.get("a"));
                        bList1.addAll(www.get("b1"));
                        bList2.addAll(www.get("b2"));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            HashMap<String,List> www=readMap2222Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilCode);
                            aList.addAll(www.get("a"));
                            bList2.addAll(www.get("b2"));
                            bList1.addAll(www.get("b1"));
                        }
                    }
                }

            }
            else if ("Project".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ProjectID");
                String pkey = pk+id;
                aList.add(" begin ");
                aList.add(" select count(1) into v_cdxgc from APP_OBJECT_BCZBTREE where nvl(zt,0)>0 and pkey='"+decodeSpecialChars(pkey)+"';");
                aList.add(" if v_cdxgc =0 then ");
                aList.add(" begin ");
                aList.add(" delete from APP_OPROPERTY where oid in (select id from APP_OBJECT_BCZBTREE connect by prior id=pid start with PKEY='" + decodeSpecialChars(pkey) + "');");
                aList.add(" delete from APP_OBJECT_BCZBTREE where id in (select id from APP_OBJECT_BCZBTREE connect by prior id=pid start with PKEY='" + decodeSpecialChars(pkey) + "');");
                //String uuid = UUID.randomUUID().toString();
                String uuid = IdGen.uuid();
                aList.add("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,PKEY,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "',v_infoid,'" + decodeSpecialChars(key) + "','" + decodeSpecialChars(pkey) + "','1','" + deptid + "','"+userid+"','"+oilid+"');");
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                        aList.add(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"');");
                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        HashMap<String,List> www=readMap2222Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilid);
                        aList.addAll(www.get("a"));
                        bList2.addAll(www.get("b2"));
                        bList1.addAll(www.get("b1"));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            HashMap<String,List> www=readMap2222Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilid);
                            aList.addAll(www.get("a"));
                            bList2.addAll(www.get("b2"));
                            bList1.addAll(www.get("b1"));
                        }
                    }
                }
                aList.add(" end;");
                aList.add(" end if;");
                aList.add(" end;");
            } else {
                String uuid = IdGen.uuid();
                //if(map.get("@SERVERID")!=null&&!"".equals((String)map.get("@SERVERID"))) uuid=map.get("@SERVERID").toString();
                if (node instanceof String) {
                    //bList.add(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(key.substring(1)) + "','" + decodeSpecialChars(node.toString()) + "','"+deptid+"');");
                    HashMap ppp=new HashMap();
                    ppp.put("OID",uuid);
                    ppp.put("OKEY",key.substring(1));
                    ppp.put("OVALUE",node.toString());
                    ppp.put("ODEPTID",deptid);
                    bList1.add(ppp);
                } else {
                    //bList.add("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1','"+deptid+"','"+userid+"','"+oilid+"');");
                    HashMap ppp=new HashMap();
                    ppp.put("ID",uuid);
                    ppp.put("PID",pid);
                    ppp.put("OTYPE",key);
                    ppp.put("LB","1");
                    ppp.put("DEPTID",deptid);
                    ppp.put("USERID",userid);
                    ppp.put("OILID",oilid);
                    bList2.add(ppp);
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String) {
                            //bList.add(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"');");
                            HashMap pppp=new HashMap();
                            pppp.put("OID",uuid);
                            pppp.put("OKEY",okey.substring(1));
                            pppp.put("OVALUE",property.toString());
                            pppp.put("ODEPTID",deptid);
                            bList1.add(pppp);
                        }else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            HashMap<String,List> www=readMap2222Sql2(child1, uuid,"",deptid,cpuid,userid,oilid);
                            aList.addAll(www.get("a"));
                            bList2.addAll(www.get("b2"));
                            bList1.addAll(www.get("b1"));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                HashMap<String,List> www=readMap2222Sql2(child1, uuid,"",deptid,cpuid,userid,oilid);
                                aList.addAll(www.get("a"));
                                bList2.addAll(www.get("b2"));
                                bList1.addAll(www.get("b1"));
                            }
                        }
                    }
                }
            }
        }
        return ll;
    }
    public static void findDB(Map<String, Object> map){
    	ArrayList list1  = new ArrayList<String>();
    	for (String key : map.keySet()) {
    		list1.add(key);
    	}
    	System.out.println(list1.toString());
    }
    public static HashMap<String,List> readMap2Sql22(Map<String, Object> map, String pid,String pk,String deptid,String cpuid,String userid,String oilid) {
    	findDB(map);
        HashMap<String,List> ll=new HashMap();
        if(ll.get("a")==null) ll.put("a",new ArrayList());
        if(ll.get("b")==null) ll.put("b",new ArrayList());
        List aList=ll.get("a");
        List bList=ll.get("b");
        for (String key : map.keySet()) {
            //Map<String, Object> node = (Map<String, Object>) map.get(key);
            Object node = map.get(key);
            if("GeneralInformation".equals(key) && node instanceof Map){
                String id = (String) ((Map<String, Object>) node).get("@ProjectVersionID");
                String oilCode=(String) ((Map<String, Object>) node).get("@OilCode");
                /*String cpuid = (String) ((Map<String, Object>) node).get("@CpuID");
                String userid = (String) ((Map<String, Object>) node).get("@UserID");
                String deptid = (String) ((Map<String, Object>) node).get("@DeptID");*/
                String pkey = id + cpuid + userid;
                aList.add("begin ");
                aList.add(" select count(1),id into v_count,v_infoid from APP_OBJECT_BCZBTREE where pkey='"+decodeSpecialChars(pkey)+"' group by id;");
                aList.add(" exception when others then null; end; ");
                aList.add(" if v_count=0 then begin ");
                String uuid = IdGen.uuid();
                aList.add(" v_infoid:='"+uuid+"';");
                aList.add("end; end if;");
                String bef="insert into APP_OBJECT_BCZBTREE(ord,id,PID,OTYPE,PKEY,LB,DEPTID,USERID,OILID";
                String aft=" VALUES (SEQ_NODEORD.NEXTVAL,'" + uuid + "','" + pid + "','" +decodeSpecialChars( key) + "','" + decodeSpecialChars(pkey) + "','1','" + deptid + "','"+userid+"','"+oilCode+"'";

                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                        bef=bef+",P_"+decodeSpecialChars(okey.substring(1));
                        aft=aft+",'"+decodeSpecialChars(property.toString())+"'";
                        //ll.append(" if v_count=0 then insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"'); end if;");

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        HashMap<String,List> www=readMap2Sql22(child1, uuid,pkey,deptid,cpuid,userid,oilCode);
                        aList.addAll(www.get("a"));
                        bList.addAll(www.get("b"));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            HashMap<String,List> www=readMap2Sql22(child1, uuid,pkey,deptid,cpuid,userid,oilCode);
                            aList.addAll(www.get("a"));
                            bList.addAll(www.get("b"));
                        }
                    }
                }
                aList.add("if v_count=0 then "+bef+")" +aft+"); end if;");
            }
            else if ("Project".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ProjectID");
                String pkey = pk+id;
                aList.add(" begin ");
                aList.add(" select count(1) into v_cdxgc from APP_OBJECT_BCZBTREE where nvl(zt,0)>0 and pkey='"+decodeSpecialChars(pkey)+"';");
                aList.add(" if v_cdxgc =0 then ");
                aList.add(" begin ");
                aList.add(" delete from APP_OBJECT_BCZBTREE where id in (select id from APP_OBJECT_BCZBTREE connect by prior id=pid start with PKEY='" + decodeSpecialChars(pkey) + "');");
                //String uuid = UUID.randomUUID().toString();
                String uuid = IdGen.uuid();
                String bef="insert into APP_OBJECT_BCZBTREE(ord,id,PID,OTYPE,PKEY,LB,DEPTID,USERID,OILID";
                String aft=" VALUES (SEQ_NODEORD.NEXTVAL,'" + uuid + "',v_infoid,'" + decodeSpecialChars(key) + "','" + decodeSpecialChars(pkey) + "','1','" + deptid + "','"+userid+"','"+oilid+"'";
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                        bef=bef+",P_"+decodeSpecialChars(okey.substring(1));
                        aft=aft+",'"+decodeSpecialChars(property.toString())+"'";
                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        HashMap<String,List> www=readMap2Sql22(child1, uuid,pkey,deptid,cpuid,userid,oilid);
                        aList.addAll(www.get("a"));
                        bList.addAll(www.get("b"));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            HashMap<String,List> www=(readMap2Sql22(child1, uuid,pkey,deptid,cpuid,userid,oilid));
                            aList.addAll(www.get("a"));
                            bList.addAll(www.get("b"));
                        }
                    }
                }
                aList.add(bef+")" +aft+");");
                aList.add(" end;");
                aList.add(" end if;");
                aList.add(" end;");
            } else {
                String uuid = IdGen.uuid();
                String bef="insert into APP_OBJECT_BCZBTREE(ord,id,PID,OTYPE,LB,DEPTID,USERID,OILID";
                String aft=" VALUES (SEQ_NODEORD.NEXTVAL,'" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1','"+deptid+"','"+userid+"','"+oilid+"'";
                for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String) {
                            bef=bef+",P_"+decodeSpecialChars(okey.substring(1));
                            aft=aft+",'"+decodeSpecialChars(property.toString())+"'";
                        }else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            HashMap<String,List> www=(readMap2Sql22(child1, uuid,"",deptid,cpuid,userid,oilid));
                            aList.addAll(www.get("a"));
                            bList.addAll(www.get("b"));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                HashMap<String,List> www=(readMap2Sql22(child1, uuid,"",deptid,cpuid,userid,oilid));
                                aList.addAll(www.get("a"));
                                bList.addAll(www.get("b"));
                            }
                        }
                    }
                bList.add(bef+")" +aft+");");
            }
        }
        return ll;
    }
    public static String readMap2Sql(Map<String, Object> map, String pid) {
        StringBuffer sql = new StringBuffer("");
        for (String key : map.keySet()) {
            //Map<String, Object> node = (Map<String, Object>) map.get(key);
            Object node = map.get(key);
            if ("dxgc".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ID");
                String cpuid = (String) ((Map<String, Object>) node).get("@CPUID");
                String userid = (String) ((Map<String, Object>) node).get("@USERID");
                String deptid = (String) ((Map<String, Object>) node).get("@DEPTID");
                String pkey = id + cpuid + userid;
                sql.append(" v_deptid:=" + deptid + ";");
                sql.append(" delete from APP_OPROPERTY where oid in (select id from APP_OBJECT_BCZBTREE connect by prior id=pid start with PKEY='" + decodeSpecialChars(pkey) + "');");
                sql.append(" delete from APP_OBJECT_BCZBTREE where id in (select id from APP_OBJECT_BCZBTREE connect by prior id=pid start with PKEY='" + decodeSpecialChars(pkey) + "');");
                //String uuid = UUID.randomUUID().toString();
                String uuid = IdGen.uuid();
                sql.append("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,PKEY,LB,DEPTID) VALUES ('" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','" + decodeSpecialChars(pkey) + "','1','" + deptid + "');");
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String)
                        sql.append(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "',v_deptid);");
                    else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        sql.append(readMap2Sql(child1, uuid));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            sql.append(readMap2Sql(child1, uuid));
                        }
                    }
                }
            } else {
                String uuid = IdGen.uuid();
                if (node instanceof String) {
                    sql.append(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(key.substring(1)) + "','" + decodeSpecialChars(node.toString()) + "',v_deptid);");
                } else {
                    sql.append("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,LB,DEPTID) VALUES ('" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1',v_deptid);");
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String)
                            sql.append(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "',v_deptid);");
                        else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            sql.append(readMap2Sql(child1, uuid));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                sql.append(readMap2Sql(child1, uuid));
                            }
                        }
                    }
                }
            }
        }
        return sql.toString();
    }

    public static Map<String, Object> readSql2Map(HashMap<String, LinkedHashMap> objdic, Map<String, List> tree, String root, Map<String, Map> propertyM) {
        Map<String, Object> doc = new LinkedHashMap<String, Object>();
        Map<String, Object> obj = (Map<String, Object>) propertyM.get(root);
        if (obj == null) obj = new LinkedHashMap<String, Object>();
        //System.out.println("-------------------------000 "+root);
        doc.put((String) ((LinkedHashMap) objdic.get(root)).get("OTYPE"), obj);
        List childreno = tree.get(root);
        if (childreno == null) return doc;
        for (Object c : childreno) {
            if (obj.get((String) ((Map) c).get("OTYPE")) == null)
                obj.put((String) ((Map) c).get("OTYPE"), readSql2Map(objdic, tree, (String) ((Map) c).get("ID"), propertyM).get((String) ((Map) c).get("OTYPE")));
            else {
                if (obj.get((String) ((Map) c).get("OTYPE")) instanceof List) {
                    List<Object> ccc = (List) obj.get((String) ((Map) c).get("OTYPE"));
                    ccc.add(readSql2Map(objdic, tree, (String) ((Map) c).get("ID"), propertyM).get((String) ((Map) c).get("OTYPE")));
                } else {
                    List<Object> ccc = new ArrayList<Object>();
                    ccc.add(obj.get((String) ((Map) c).get("OTYPE")));
                    obj.put((String) ((Map) c).get("OTYPE"), ccc);
                    ccc.add(readSql2Map(objdic, tree, (String) ((Map) c).get("ID"), propertyM).get((String) ((Map) c).get("OTYPE")));
                }
            }
        }
        return doc;
    }

    public static Map<String, Object> readSql22Map(HashMap<String, LinkedHashMap> objdic, Map<String, List> tree, String root, Map<String, Map> propertyM) {
        Map<String, Object> doc = new LinkedHashMap<String, Object>();
        Map<String, Object> obj = (Map<String, Object>) propertyM.get(root);
        if (obj == null) obj = new LinkedHashMap<String, Object>();
        //System.out.println("-------------------------000 "+root);
        doc.put((String) ((LinkedHashMap) objdic.get(root)).get("OTYPE"), obj);
        List childreno = tree.get(root);
        if (childreno == null) {
            doc.put("#text",obj.get("P_TEXT")==null?"":obj.get("P_TEXT"));
            return doc;
        }
        for (Object c : childreno) {
            if (obj.get((String) ((Map) c).get("OTYPE")) == null)
                obj.put((String) ((Map) c).get("OTYPE"), readSql22Map(objdic, tree, (String) ((Map) c).get("ID"), propertyM).get((String) ((Map) c).get("OTYPE")));
            else {
                if (obj.get((String) ((Map) c).get("OTYPE")) instanceof List) {
                    List<Object> ccc = (List) obj.get((String) ((Map) c).get("OTYPE"));
                    ccc.add(readSql22Map(objdic, tree, (String) ((Map) c).get("ID"), propertyM).get((String) ((Map) c).get("OTYPE")));
                } else {
                    List<Object> ccc = new ArrayList<Object>();
                    ccc.add(obj.get((String) ((Map) c).get("OTYPE")));
                    obj.put((String) ((Map) c).get("OTYPE"), ccc);
                    ccc.add(readSql22Map(objdic, tree, (String) ((Map) c).get("ID"), propertyM).get((String) ((Map) c).get("OTYPE")));
                }
            }
        }
        return doc;
    }


    public static String decodeSpecialChars(String content)
    {
        String afterDecode = content.replaceAll("'", "''");
        afterDecode = afterDecode.replaceAll("&", "'||chr(38)||'");
        return afterDecode;
    }


    /**
     * 将树结构生成Map方便格式化为XML
     * 
     * */
    public static Map<String, Object> getMlMap(HashMap<String, Office> objdic, Map<String, List<Office>> tree, String root, Map<String, List<Map>> zbM) {
        Map<String, Object> doc = new LinkedHashMap<String, Object>();
        Office r=objdic.get(root);
        if(r!=null) {
            Map m = new HashMap();
            String mlcode =  r.getCode();
            String deptCode = mlcode.substring(0, 8);
            if(StringUtils.isNotBlank(mlcode) && "undefined" != mlcode ) {
            	mlcode = mlcode.replace(deptCode, "B");
            }
            m.put("Level",  r.getGrade() == null ? "" : r.getGrade());
            m.put("Code",   r.getId()    == null ? "" : mlcode);
            m.put("BzCode", r.getId()    == null ? "" : mlcode);
            m.put("Text",   r.getName()  == null ? "" : r.getName());
            List ccc=new ArrayList();
            List ddd=new ArrayList();
            HashMap eee=new HashMap();
            m.put("BCZBLocalClasses",eee);
            eee.put("BCZBLocalClass",ccc);
            HashMap fff=new HashMap();
            m.put("BCZBLocals",fff);
            fff.put("BCZBLocal",ddd);
            if(tree.get(root)!=null){
                List<Office> lll=tree.get(root);
                for(Office o:lll){
                    ccc.add(getMlMap(objdic,tree,o.getId(),zbM).get("BCZBLocalClass"));
                }
            }
            if(zbM.get(root)!=null){
                List<Map> lll=zbM.get(root);
                for(Map o:lll){
                    Map sss=new HashMap();
                    String code =  (String)o.get("CODE");
                    System.out.println("=========================");
                    System.out.println("code=" + code);
                    System.out.println("=========================");
                    if(StringUtils.isNoneBlank(code) && "undefined" != code ) {
                    	code = code.replace(deptCode, "B");
                    }
                    Object object = o.get("CODE");
                    sss.put("BM",   o.get("BM")    == null ? "" : o.get("BM"));
                    sss.put("CODE", o.get("CODE")  == null ? "" : o.get("CODE"));
                    sss.put("OILID",o.get("OILID") == null ? "" : o.get("OILID"));
                    sss.put("YSBM", o.get("CODE")  == null ? "" : code);
                    sss.put("TITLE",o.get("TITLE") == null ? "" : o.get("TITLE"));
                    /*Map ttt=new HashMap();
                    ttt.put("BCZBLocal",sss);*/
                    ddd.add(sss);
                }
            }
            doc.put("BCZBLocalClass",m);
        }
        return doc;
    }
    public static Map<String, Object> getSbzcMlMap(HashMap<String, Office> objdic, Map<String, List<Office>> tree, String root, Map<String, List<Map>> zbM) {
        Map<String, Object> doc = new LinkedHashMap<String, Object>();
        Office r=objdic.get(root);
        if(r!=null) {
        	String mlcode =  r.getCode();
            String deptCode = mlcode.substring(0, 8);
            if(StringUtils.isNotBlank(mlcode) && "undefined" != mlcode ) {
            	mlcode = mlcode.replace(deptCode, "B");
            }
        	
            Map m = new HashMap();
            m.put("Level", r.getGrade()==null?"":r.getGrade());
            m.put("Code", r.getId()==null?"":mlcode);//r.getId()
            m.put("BzCode", r.getId()==null?"": mlcode);//r.getCode()
            m.put("Text", r.getName()==null?"":r.getName());
            List ccc=new ArrayList();
            List ddd=new ArrayList();
            HashMap eee=new HashMap();
            m.put("BCSBZCClasses",eee);
            eee.put("BCSBZCClass",ccc);
            HashMap fff=new HashMap();
            m.put("BCSBZCs",fff);
            fff.put("BCSBZC",ddd);
            if(tree.get(root)!=null){
                List<Office> lll=tree.get(root);
                for(Office o:lll){
                    ccc.add(getSbzcMlMap(objdic,tree,o.getId(),zbM).get("BCSBZCClass"));
                }
            }
            if(zbM.get(root)!=null){
                List<Map> lll=zbM.get(root);
                for(Map o:lll){
                    Map sss=new HashMap();
                    String bm = String.valueOf(o.get("BM"));
                    if(StringUtils.isNotBlank(bm) && "undefined" != bm ) {
                    	bm = bm.replace(deptCode, "B");
                    }
                    sss.put("BM",o.get("BM")==null?"":bm);
                    sss.put("BzCode",o.get("BM")==null?"":bm);
                    sss.put("OILID",o.get("OILID")==null?"":o.get("OILID"));
                    sss.put("MC",o.get("MC")==null?"":o.get("MC"));
                    sss.put("DW",o.get("DW")==null?"":o.get("DW"));
                    sss.put("UpdateTime",o.get("UPDATETIME")==null?"":o.get("UPDATETIME"));
                    sss.put("PP",o.get("PP")==null?"":o.get("PP"));
                    sss.put("XH",o.get("XH")==null?"":o.get("XH"));
                    sss.put("GYS",o.get("CJMC")==null?"":o.get("CJMC"));
                    sss.put("CCTime",o.get("CCRQ")==null?"":o.get("CCRQ"));
                    sss.put("CCJG",o.get("CCJG")==null?"":o.get("CCJG"));
                    sss.put("GYJG",o.get("GYJG")==null?"":o.get("GYJG"));
                    sss.put("JGLYSM",o.get("LYSM")==null?"":o.get("LYSM"));
                    sss.put("BZ",o.get("BZ")==null?"":o.get("BZ"));
                    sss.put("LB",o.get("LBB")==null?"":o.get("LBB"));
                    //sss.put("TITLE",o.get("TITLE")==null?"":o.get("MC"));
                    /*Map ttt=new HashMap();
                    ttt.put("BCZBLocal",sss);*/
                    ddd.add(sss);
                }
            }
            doc.put("BCSBZCClass",m);
        }
        return doc;
    }

    public static void main(String[] argv) {
        String textFromFile = "";

        try {
            textFromFile = FileUtils.readFileToString(new File("c:/bczb.xml"), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> map = null;
        try {
            map = XmlUtils.Xml2MapWithAttr(textFromFile, true);
            String i="";
        } catch (Exception e) {

        }
    }
    
    
    
    public static HashMap<String,List> readbczbMapSql(Map<String, Object> map, String pid,String pk,String deptid,String cpuid,String userid,String oilid) {
        HashMap<String,List> ll=new HashMap();
        if(ll.get("a")==null) ll.put("a",new ArrayList());
        if(ll.get("b")==null) ll.put("b",new ArrayList());
        List aList=ll.get("a");
        List bList=ll.get("b");
        for (String key : map.keySet()) {
            //Map<String, Object> node = (Map<String, Object>) map.get(key);
            Object node = map.get(key);
            if("GeneralInformation".equals(key) && node instanceof Map){
                String id = (String) ((Map<String, Object>) node).get("@ProjectVersionID");
                String oilCode=(String) ((Map<String, Object>) node).get("@OilCode");
                /*String cpuid = (String) ((Map<String, Object>) node).get("@CpuID");
                String userid = (String) ((Map<String, Object>) node).get("@UserID");
                String deptid = (String) ((Map<String, Object>) node).get("@DeptID");*/
                String pkey = id + cpuid + userid;
                aList.add("begin ");
                aList.add(" select count(1),id into v_count,v_infoid from APP_OBJECT_BCZBTREE_TJ where pkey='"+decodeSpecialChars(pkey)+"' group by id;");
                aList.add(" exception when others then null; end; ");
                aList.add(" if v_count=0 then begin ");
                String uuid = IdGen.uuid();
                aList.add(" v_infoid:='"+uuid+"';");
                aList.add("insert into APP_OBJECT_BCZBTREE_TJ(ID,PID,OTYPE,PKEY,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "','" + pid + "','" +decodeSpecialChars( key) + "','" + decodeSpecialChars(pkey) + "','1','" + deptid + "','"+userid+"','"+oilCode+"');");
                aList.add("end; end if;");
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                        aList.add(" if v_count=0 then insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"'); end if;");

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        HashMap<String,List> www=readMap2Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilCode);
                        aList.addAll(www.get("a"));
                        bList.addAll(www.get("b"));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            HashMap<String,List> www=readMap2Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilCode);
                            aList.addAll(www.get("a"));
                            bList.addAll(www.get("b"));
                        }
                    }
                }

            }
            else if ("Project".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ProjectID");
                String pkey = pk+id;
                aList.add(" begin ");
                aList.add(" select count(1) into v_cdxgc from APP_OBJECT_BCZBTREE_TJ where nvl(zt,0)>0 and pkey='"+decodeSpecialChars(pkey)+"';");
                aList.add(" if v_cdxgc =0 then ");
                aList.add(" begin ");
                aList.add(" delete from APP_OPROPERTY where oid in (select id from APP_OBJECT_BCZBTREE_TJ connect by prior id=pid start with PKEY='" + decodeSpecialChars(pkey) + "');");
                aList.add(" delete from APP_OBJECT_BCZBTREE_TJ where id in (select id from APP_OBJECT_BCZBTREE_TJ connect by prior id=pid start with PKEY='" + decodeSpecialChars(pkey) + "');");
                //String uuid = UUID.randomUUID().toString();
                String uuid = IdGen.uuid();
                aList.add("insert into APP_OBJECT_BCZBTREE_TJ(ID,PID,OTYPE,PKEY,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "',v_infoid,'" + decodeSpecialChars(key) + "','" + decodeSpecialChars(pkey) + "','1','" + deptid + "','"+userid+"','"+oilid+"');");
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                        aList.add(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"');");
                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        HashMap<String,List> www=readMap2Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilid);
                        aList.addAll(www.get("a"));
                        bList.addAll(www.get("b"));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            HashMap<String,List> www=readMap2Sql2(child1, uuid,pkey,deptid,cpuid,userid,oilid);
                            aList.addAll(www.get("a"));
                            bList.addAll(www.get("b"));
                        }
                    }
                }
                aList.add(" end;");
                aList.add(" end if;");
                aList.add(" end;");
            } else {
                String uuid = IdGen.uuid();
                //if(map.get("@SERVERID")!=null&&!"".equals((String)map.get("@SERVERID"))) uuid=map.get("@SERVERID").toString();
                if (node instanceof String) {
                    bList.add(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(key.substring(1)) + "','" + decodeSpecialChars(node.toString()) + "','"+deptid+"');");
                } else {
                    bList.add("insert into APP_OBJECT_BCZBTREE_TJ(ID,PID,OTYPE,LB,DEPTID,USERID,OILID) VALUES ('" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1','"+deptid+"','"+userid+"','"+oilid+"');");
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String) {
                            bList.add(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"');");
                        }else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            HashMap<String,List> www=readMap2Sql2(child1, uuid,"",deptid,cpuid,userid,oilid);
                            aList.addAll(www.get("a"));
                            bList.addAll(www.get("b"));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                HashMap<String,List> www=readMap2Sql2(child1, uuid,"",deptid,cpuid,userid,oilid);
                                aList.addAll(www.get("a"));
                                bList.addAll(www.get("b"));
                            }
                        }
                    }
                }
            }
        }
        System.out.println("开始打印sql："+aList.toString());
        return ll;
    }
    public static HashMap<String,List> insertBczbTjSql(Map<String, Object> map, String pid,String pk,String deptid,String cpuid,String userid,String oilid) {
        HashMap<String,List> ll=new HashMap();
        if(ll.get("a")==null) ll.put("a",new ArrayList());
        if(ll.get("b")==null) ll.put("b",new ArrayList());
        List aList=ll.get("a");
        List bList=ll.get("b");
        for (String key : map.keySet()) {
            //Map<String, Object> node = (Map<String, Object>) map.get(key);
            Object node = map.get(key);
            if("GeneralInformation".equals(key) && node instanceof Map){
                String id = (String) ((Map<String, Object>) node).get("@ProjectVersionID");
                String oilCode=(String) ((Map<String, Object>) node).get("@OilCode");
                /*String cpuid = (String) ((Map<String, Object>) node).get("@CpuID");
                String userid = (String) ((Map<String, Object>) node).get("@UserID");
                String deptid = (String) ((Map<String, Object>) node).get("@DeptID");*/
                String pkey = id + cpuid + userid;
                aList.add("begin ");
                aList.add(" select count(1),id into v_count,v_infoid from APP_ZBXX where pkey='"+decodeSpecialChars(pkey)+"' group by id;");
                aList.add(" exception when others then null; end; ");
                aList.add(" if v_count=0 then begin ");
                String uuid = IdGen.uuid();
                aList.add(" v_infoid:='"+uuid+"';");
                aList.add("end; end if;");
                String bef="insert into APP_ZBXX(ord,id,PID,OTYPE,PKEY,LB,DEPTID,USERID,OILID";
                String aft=" VALUES (SEQ_NODEORD2.NEXTVAL,'" + uuid + "','" + pid + "','" +decodeSpecialChars( key) + "','" + decodeSpecialChars(pkey) + "','1','" + deptid + "','"+userid+"','"+oilCode+"'";

                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                        bef=bef+",P_"+decodeSpecialChars(okey.substring(1));
                        aft=aft+",'"+decodeSpecialChars(property.toString())+"'";
                        //ll.append(" if v_count=0 then insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + decodeSpecialChars(okey.substring(1)) + "','" + decodeSpecialChars(property.toString()) + "','"+deptid+"'); end if;");

                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        HashMap<String,List> www=insertBczbTjSql(child1, uuid,pkey,deptid,cpuid,userid,oilCode);
                        aList.addAll(www.get("a"));
                        bList.addAll(www.get("b"));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            HashMap<String,List> www=insertBczbTjSql(child1, uuid,pkey,deptid,cpuid,userid,oilCode);
                            aList.addAll(www.get("a"));
                            bList.addAll(www.get("b"));
                        }
                    }
                }
                aList.add("if v_count=0 then "+bef+")" +aft+"); end if;");
            }
            else if ("Project".equals(key) && node instanceof Map) {
                String id = (String) ((Map<String, Object>) node).get("@ProjectID");
                String pkey = pk+id;
                aList.add(" begin ");
                aList.add(" select count(1) into v_cdxgc from APP_ZBXX where nvl(zt,0)>0 and pkey='"+decodeSpecialChars(pkey)+"';");
                aList.add(" if v_cdxgc =0 then ");
                aList.add(" begin ");
                aList.add(" delete from APP_ZBXX where id in (select id from APP_ZBXX connect by prior id=pid start with PKEY='" + decodeSpecialChars(pkey) + "');");
                //String uuid = UUID.randomUUID().toString();
                String uuid = IdGen.uuid();
                String bef="insert into APP_ZBXX(ord,id,PID,OTYPE,PKEY,LB,DEPTID,USERID,OILID";
                String aft=" VALUES (SEQ_NODEORD2.NEXTVAL,'" + uuid + "',v_infoid,'" + decodeSpecialChars(key) + "','" + decodeSpecialChars(pkey) + "','1','" + deptid + "','"+userid+"','"+oilid+"'";
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String) {
                        bef=bef+",P_"+decodeSpecialChars(okey.substring(1));
                        aft=aft+",'"+decodeSpecialChars(property.toString())+"'";
                    }else if (property instanceof Map) {
                        Map<String, Object> child1 = new HashMap<String, Object>();
                        child1.put(okey, (Map<String, Object>) property);
                        HashMap<String,List> www=insertBczbTjSql(child1, uuid,pkey,deptid,cpuid,userid,oilid);
                        aList.addAll(www.get("a"));
                        bList.addAll(www.get("b"));
                    } else if (property instanceof List) {
                        for (int i = 0; i < ((List) property).size(); i++) {
                            /*if((Map<String, Object>) ((List) property).get(i) instanceof Map)
                            else*/
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, ((List) property).get(i));
                            HashMap<String,List> www=(insertBczbTjSql(child1, uuid,pkey,deptid,cpuid,userid,oilid));
                            aList.addAll(www.get("a"));   
                            bList.addAll(www.get("b"));
                        }
                    }
                }
                aList.add(bef+")" +aft+");");
                aList.add(" end;");
                aList.add(" end if;");
                aList.add(" end;");
            } else {
                String uuid = IdGen.uuid();
                String bef="insert into APP_ZBXX(ord,id,PID,OTYPE,LB,DEPTID,USERID,OILID";
                String aft=" VALUES (SEQ_NODEORD2.NEXTVAL,'" + uuid + "','" + pid + "','" + decodeSpecialChars(key) + "','1','"+deptid+"','"+userid+"','"+oilid+"'";
                for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String) {
                            bef=bef+",P_"+decodeSpecialChars(okey.substring(1));
                            aft=aft+",'"+decodeSpecialChars(property.toString())+"'";
                        }else if (property instanceof Map) {
                            Map<String, Object> child1 = new HashMap<String, Object>();
                            child1.put(okey, (Map<String, Object>) property);
                            HashMap<String,List> www=(insertBczbTjSql(child1, uuid,"",deptid,cpuid,userid,oilid));
                            aList.addAll(www.get("a"));
                            bList.addAll(www.get("b"));
                        } else if (property instanceof List) {
                            for (int i = 0; i < ((List) property).size(); i++) {
                                Map<String, Object> child1 = new HashMap<String, Object>();
                                child1.put(okey, ((List) property).get(i));
                                HashMap<String,List> www=(insertBczbTjSql(child1, uuid,"",deptid,cpuid,userid,oilid));
                                aList.addAll(www.get("a"));
                                bList.addAll(www.get("b"));
                            }
                        }
                    }
                bList.add(bef+")" +aft+");");
            }
        }
        return ll;
    }
}
