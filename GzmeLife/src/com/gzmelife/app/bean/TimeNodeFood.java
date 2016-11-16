package com.gzmelife.app.bean;

/**
 * Created by HHD on 2016/10/14.
 *
 * 智能自动命名bean
 */

public class TimeNodeFood {

    /** 食材名称 */
    private String name;
    /** 食材重量 */
    private int weight;

    public TimeNodeFood() {
        //
    }

    public TimeNodeFood(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
