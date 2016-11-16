package com.gzmelife.app.bean;

import com.gzmelife.app.tools.MyLogger;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;


/**
 * 标准食材库（我的食材库）三级食材（name、Uid、weight（重量）、pid（一级的ID）、id（本地数据库））
 */
@Table(name = "localFoodMaterialLevelThree")
public class LocalFoodMaterialLevelThree {

	MyLogger HHDLog = MyLogger.HHDLog();

	@Column(isId = true, name = "id")
	private int fsId;//食材在数据库的顺序
	@Column(name = "name")
	private String fsName;//食材的名称
	@Column(name = "uid")
	private String uid;//食材的UID
	@Column(name = "weight")
	private int weight;//食材的重量
	@Column(name = "pid")
	private int pid; // 一级的ID
	private boolean isChecked;
	
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	public int getPid() {
		return pid;
	}
	public void setPid(int pid) {
		this.pid = pid;
	}
	public int getId() {
		return fsId;
	}
	public LocalFoodMaterialLevelThree setId(int id) {
		this.fsId = id;
		HHDLog.e(id+"="+fsId);
		return this;
	}
	
	public String getName() {
		return fsName;
	}
	
	public LocalFoodMaterialLevelThree setName(String name) {
		this.fsName = name;
		HHDLog.e(name+"="+fsName);
		return this;
	}

	public String getUid() {
		return uid;
	}
	public LocalFoodMaterialLevelThree setUid(String uid) {
		this.uid = uid;
		HHDLog.e(uid+"="+ this.uid);
		return this;
	}

	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
}
