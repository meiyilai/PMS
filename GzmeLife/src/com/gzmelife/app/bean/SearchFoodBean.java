package com.gzmelife.app.bean;

import com.gzmelife.app.tools.MyLogger;

/**
 * 搜索标准食材库的Bean（List每个Item）
 */
public class SearchFoodBean {

    MyLogger HHDLog = MyLogger.HHDLog();

    private String id;
    private String name;
    private String c_id;
    private String c_name;
    private String uid;

    public String getId() {
        HHDLog.e(id);
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        HHDLog.e(name);
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getC_id() {
        HHDLog.e(c_id);
        return c_id;
    }

    public void setC_id(String c_id) {
        this.c_id = c_id;
    }

    public String getC_name() {
        HHDLog.e(c_name);
        return c_name;
    }

    public void setC_name(String c_name) {
        this.c_name = c_name;
    }

    public String getUid() {
        HHDLog.e(uid);
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
