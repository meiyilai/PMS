package com.gzmelife.app.device;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.gzmelife.app.bean.DeviceNameAndIPBean;
import com.gzmelife.app.tools.MyLogger;

/**
 * UDP接收局域网智能灶设备 2016
 * @author chenxiaoyan
 *
 */
public class DeviceUtil {

	MyLogger HHDLog = MyLogger.HHDLog();

	/* 发送广播端的socket */
	private DatagramSocket multicastSocket;

	private InetAddress receiveAddress;

	private List<String> deviceIPList;
	private List<DeviceNameAndIPBean> deviceList;

	private OnReceiver onReceiver;

	private Thread thread = null;
	
	private static WifiManager.MulticastLock lock;

	/** UDP接收局域网PMS设备的构造方法 2016 */
	public DeviceUtil(Context context, OnReceiver mOnReceiver) {
		this.onReceiver = mOnReceiver;
		
		deviceIPList = new ArrayList<String>();
		deviceList = new ArrayList<DeviceNameAndIPBean>();
		if (multicastSocket == null) {
			/* 创建socket实例 */
			try {
				multicastSocket = new MulticastSocket(Config.SERVER_HOST_PORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		WifiManager manager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
		this.lock= manager.createMulticastLock("UDPwifi"); 
		
		if (receiveAddress == null) {
			try {
				receiveAddress = InetAddress
						.getByName(Config.localIP);
//				if (!receiveAddress.isMulticastAddress()) {// 测试是否为多播地址
//					try {
//						throw new Exception("请使用多播地址");
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//				try {
//					multicastSocket.setTimeToLive(1);
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
//				multicastSocket.joinGroup(receiveAddress);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}

		if (thread == null) {
			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					byte buf[] = new byte[1024];
					DatagramPacket dp = null;
					dp = new DatagramPacket(buf, buf.length, receiveAddress,
							Config.SERVER_HOST_PORT);
					while (null != multicastSocket && !multicastSocket.isClosed()) {
						try {
							lock.acquire();
							
							multicastSocket.receive(dp);
							String name = new String(buf, 0, dp.getLength()).trim();
							if (name.contains(",")) {
								name = name.substring(0, name.indexOf(","));
							}
							String ip = dp.getAddress().getHostAddress();
							if (!deviceIPList.contains(ip) && name.contains("PMS")) {
								//MyLog.i(MyLog.TAG_I_INFO, "接收到数据:" + name + "," + ip);
								HHDLog.v("UDP模式搜索到的设备：" + "名称=" + name + "，IP地址=" + ip);

								DeviceNameAndIPBean bean = new DeviceNameAndIPBean();
								bean.setIp(ip);//设备的IP地址
								bean.setName(name);//设备的名称
								deviceIPList.add(ip);
								deviceList.add(bean);

								HHDLog.v("存储PMS设备的名称和IP的对象="+bean.toString());
								if (onReceiver != null) {
									onReceiver.refreshData(deviceList);
								}
							}
							
							lock.release();
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}

	public interface OnReceiver {
		/** 获取并保存PMS对象（名称和IP） 2016 */
		void refreshData(List<DeviceNameAndIPBean> list);//失败
	}

	/** 启动UDP搜索PMS设备的线程 2016 */
	public void startSearch() {
		thread.start();
	}

	public void closeSearch() {

		if (multicastSocket != null) {
			multicastSocket.close();
			multicastSocket = null;
		}
	}
}
