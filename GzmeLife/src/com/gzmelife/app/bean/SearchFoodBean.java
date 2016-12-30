package com.gzmelife.app.bean;

import com.gzmelife.app.tools.MyLogger;

/**
 * 搜索标准食材库的Bean（List每个Item）
 */
public class SearchFoodBean {

    MyLogger HHDLog = MyLogger.HHDLog();

    /** 食材的ID=UID */
    private String id;
    /** 食材的名称 */
    private String name;
    /** 一级分类的ID */
    private String c_id;
    /** 一级分类的名称 */
    private String c_name;
    /** 食材的UID */
    private String uid;

    public String getId() {
        HHDLog.v(id);
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        HHDLog.v(name);
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getC_id() {
        HHDLog.v(c_id);
        return c_id;
    }

    public void setC_id(String c_id) {
        this.c_id = c_id;
    }

    public String getC_name() {
        HHDLog.v(c_name);
        return c_name;
    }

    public void setC_name(String c_name) {
        this.c_name = c_name;
    }

    public String getUid() {
        HHDLog.v(uid);
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
