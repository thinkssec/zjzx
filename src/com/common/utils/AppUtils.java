package com.common.utils;

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
    public static void main(String[] argv){
        System.out.println(AppUtils.getParentDept(null));
    }
}
