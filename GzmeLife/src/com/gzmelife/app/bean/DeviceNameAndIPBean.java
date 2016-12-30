package com.gzmelife.app.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/** PMS设备对象类 */
@Table(name = "deviceNameAndIPBean")
public class DeviceNameAndIPBean {
	/** 数据库序号 */
	@Column(isId = true, name = "id")
	private int id;
	/** PMS名称 */
	@Column(name = "name")
	private String name;
	/** PMSWiFi名称 */
	@Column(name = "wifiName")
	private String wifiName;
	/** PMS上次IP地址 */
	@Column(name = "ip")
	private String ip;

	/** 设置PMS的名称 2016 */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/** 获取设备的IP地址 2016 */
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getWifiName() {
		return wifiName;
	}
	public void setWifiName(String wifiName) {
		this.wifiName = wifiName;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "ID：" + id + "；名称：" + name + "；WiFi：" + wifiName + "；IP地址：" + ip;
	}	
	
	public boolean isSame(DeviceNameAndIPBean b) {
		return name.equals(b.getName())
			&& wifiName.equals(b.getWifiName()) && ip.equals(b.getIp());
	}
	
	/** PMS名字和wifi名字一样，但是PMS的ip不一样 */ 
	public boolean isSameNameAndWifiName(DeviceNameAndIPBean b) {
		return name.equals(b.getName()) && wifiName.equals(b.getWifiName()) && !ip.equals(b.getIp());
	}
}
