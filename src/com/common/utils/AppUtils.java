package com.common.utils;

import java.util.*;

/**
 * Created by Administrator on 2018/1/11.
 */
public class AppUtils {
    public static int deptStep=4;
    public static String getParentDept(String deptid){
        if(!StringUtils.isNotBlank(deptid))return "('abcd')";
        StringBuffer depts=new StringBuffer("");
        depts.append("(");
        depts.append("'"+deptid+"'");
        int s=deptid.length();
        int t=s/deptStep-1;
        for(int i=1;i<=t;i++){
            depts.append(",'"+deptid.substring(0,deptid.length()-i*deptStep)+"'");

        }
        depts.append(")");
        return depts.toString();
    }
    public static void sort(List<HashMap> dlist, LinkedHashMap<String,String> orderby){
        if(orderby==null) return;
        for(final String k:orderby.keySet()){
            final String v=orderby.get(k);
            Collections.sort(dlist, new Comparator<HashMap>() {
                public int compare(HashMap u1, HashMap u2) {
                    Object a1=u1.get(k);
                    Object a2=u2.get(k);
                    if(a1==null) a1=new String("");
                    if(a2==null) a2=new String("");
                    if("desc".equals(v)){
                        return ((String)a2).compareTo(((String)a1)); //升序
                    }
                    else{
                        return ((String)a1).compareTo(((String)a2)); //升序
                    }
                }
            });
        }
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
                String deptid=(String)((Map<String, Object>) node).get("@DEPTID");
                String pkey = id + cpuid + userid;
                sql.append(" v_deptid:="+deptid+";");
                sql.append(" delete from APP_OPROPERTY where oid in (select id from APP_OBJECT_BCZBTREE connect by prior id=pid start with PKEY='" + pkey + "');");
                sql.append(" delete from APP_OBJECT_BCZBTREE where id in (select id from APP_OBJECT_BCZBTREE connect by prior id=pid start with PKEY='" + pkey + "');");
                //String uuid = UUID.randomUUID().toString();
                String uuid = IdGen.uuid();
                sql.append("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,PKEY,LB,DEPTID) VALUES ('" + uuid + "','" + pid + "','" + key + "','" + pkey + "','1','"+deptid+"');");
                for (String okey : ((Map<String, Object>) node).keySet()) {
                    Object property = ((Map<String, Object>) node).get(okey);
                    if (property instanceof String)
                        sql.append(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + okey.substring(1) + "','" + property.toString() + "',v_deptid);");
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
                    sql.append(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + key.substring(1) + "','" + node.toString() + "',v_deptid);");
                }
                else {
                    sql.append("insert into APP_OBJECT_BCZBTREE(ID,PID,OTYPE,LB,DEPTID) VALUES ('" + uuid + "','" + pid + "','" + key + "','1',v_deptid);");
                    for (String okey : ((Map<String, Object>) node).keySet()) {
                        Object property = ((Map<String, Object>) node).get(okey);
                        if (property instanceof String)
                            sql.append(" insert into APP_OPROPERTY(OID,OKEY,OVALUE,ODEPTID) VALUES ('" + uuid + "','" + okey.substring(1) + "','" + property.toString() + "',v_deptid);");
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
    public static Map<String,Object> readSql2Map(HashMap<String,LinkedHashMap> objdic,Map<String,List> tree,String root,Map<String,Map> propertyM){
        Map<String,Object> doc =new LinkedHashMap<String,Object>();
        Map<String,Object> obj=(Map<String,Object>)propertyM.get(root);
        if(obj==null) obj=new LinkedHashMap<String,Object>();
        doc.put((String)((LinkedHashMap)objdic.get(root)).get("OTYPE"),obj);
        List childreno=tree.get(root);
        if(childreno==null) return doc;
        for(Object c : childreno){
            if(obj.get((String)((Map)c).get("OTYPE"))==null)
                obj.put((String)((Map)c).get("OTYPE"),readSql2Map(objdic,tree,(String)((Map)c).get("ID"),propertyM).get((String)((Map)c).get("OTYPE")));
            else{
                if(obj.get((String)((Map)c).get("OTYPE")) instanceof List){
                    List<Object> ccc=(List)obj.get((String)((Map)c).get("OTYPE"));
                    ccc.add(readSql2Map(objdic,tree,(String)((Map)c).get("ID"),propertyM).get((String)((Map)c).get("OTYPE")));
                }else{
                    List<Object> ccc=new ArrayList<Object>();
                    ccc.add(obj.get((String)((Map)c).get("OTYPE")));
                    obj.put((String)((Map)c).get("OTYPE"),ccc);
                    ccc.add(readSql2Map(objdic,tree,(String)((Map)c).get("ID"),propertyM).get((String)((Map)c).get("OTYPE")));
                }
            }
        }
        return doc;
    }

    public static void main(String[] argv){
        System.out.println(AppUtils.getParentDept(null));
    }
}
