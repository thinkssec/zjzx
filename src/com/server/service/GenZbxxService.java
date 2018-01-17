package com.server.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2018/1/16.
 */
@Service
@Component
public class GenZbxxService {
    public String GetZBXXXXX_XML(String ZBBM, String BBH, String OILID)
    {
        try
        {
           /* String ZBID = GetZBID(ZBBM, BBH, OILID);
            //分项指标只返回分项指标一条记录，禁止返回下级详情
            //test             
            String sql1 = "select * from tzgs_tjb t where t.bb='" + BBH + "' and t.oilid='" + OILID + "' and t.zbid='" + ZBID + "' and t.bm='" + ZBBM + "'";
            DataTable dt_fx = dbop.Select(sql1);
            String xml = "";
            if (dt_fx.Rows[0]["objcosttype"].ToString() == "ctSectionProjectIndex")
            {
                xml= GetFXZBXXXX_XML(ZBBM, BBH, OILID);

            }
            else if (dt_fx.Rows[0]["objcosttype"].ToString() == "ctUnitProjectIndex")
            {
                xml= GetDWZBXXXX_XML(ZBBM, BBH, OILID);

            }
            else if (dt_fx.Rows[0]["objcosttype"].ToString() == "ctIndividualProjectIndex")
            {
                xml= GetDXZBXXXX_XML(ZBBM, BBH, OILID);
            }
            if (!String.IsNullOrEmpty(xml))
            {
                return  CommonMethod.GetXMLDoc(xml);
            }*/
            return "";
        }
        catch(Exception e)
        {
            return "";
        }
    }
}
