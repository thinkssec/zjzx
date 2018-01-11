package com.server.Entity;

import java.util.Map;

/**
 * Created by Administrator on 2017/10/17.
 */
public class ResponseBody {
    public String username;//请求用户
    public String ip;//客户端ip
    public String mac;
    public String taskid;//会话ID
    public String subid;//会话子ID
    public String issuccess;//时候成功
    public String message;//提示消息
    public String datas;//返回数据
    public String type;//响应类型 f 带附件的请求
    public String rqstid;//请求id

    public ResponseBody(Map params, String issuccess, String message, String sid,String taskid) {
        this.taskid=taskid;
        this.rqstid=sid;
        this.issuccess = issuccess;
        this.message = message;
    }

    public ResponseBody() {
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDatas() {
        return datas;
    }

    public void setDatas(String datas) {
        this.datas = datas;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getSubid() {
        return subid;
    }

    public void setSubid(String subid) {
        this.subid = subid;
    }

    public String getIssuccess() {
        return issuccess;
    }

    public void setIssuccess(String issuccess) {
        this.issuccess = issuccess;
    }

    public String getMessage() {
        return message;
    }

    public String getRqstid() {
        return rqstid;
    }

    public void setRqstid(String rqstid) {
        this.rqstid = rqstid;
    }
}
