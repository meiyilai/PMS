package com.gzmelife.app.tools;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiUtil {
	
	static MyLogger HHDLog = MyLogger.HHDLog();
	
	private static WifiManager mWm;

	/** 打开wifi开关，且返回之前的wifi开关状态 */
	public static boolean openWifi(Context context) {
		HHDLog.v("");
		boolean result;
		mWm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		result = mWm.isWifiEnabled();
		if (!result) {
			HHDLog.v("");
			mWm.setWifiEnabled(true);
		}
		return result;
	}

	/** 获取当前已连接上的WiFi的信息 2016 */
	public static WifiInfo getWifiInfo() {
		HHDLog.v("");
		return mWm.getConnectionInfo();
	}

	/** 扫描WiFi网络 2016 */
	public static void startScan() {
		HHDLog.v("");
		mWm.startScan();
	}

	/**
	 * 连接到配置的指定WiFi 2016
	 *
	 * @param ssid				指定的WiFi名称
	 * @param disableOthers	是否禁用其他WiFi
	 * @return					连接成功返回true
	 */
	public static boolean connectWifi(String ssid, Boolean disableOthers) {
		HHDLog.v("");
		MyLog.d("ssid=" + ssid);
		WifiConfiguration config = createWifiConfiguration(ssid);
//		mWm.addNetwork(config);
		return mWm.enableNetwork(mWm.addNetwork(config), disableOthers);
	}

	/** 连接到配置好的指定指定（ID从配置信息中获取）WiFi 2016 */
	public static boolean connectWifi(WifiConfiguration config, Boolean disableOthers) {
		HHDLog.v("");
		MyLog.d("ssid=" + config.SSID);
		return mWm.enableNetwork(config.networkId, disableOthers);
	}

	/** 获取当前WiFi状态 2016  */
	public static int getWifiStatus(Context context) {
		HHDLog.v("");
		mWm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return mWm.getWifiState();
	}

	/** 获取扫描到的所有WiFi的结果 2016 */
	public static List<ScanResult> getWifiList() {
		HHDLog.v("");
//		List<ScanResult> wifiResult = mWm.getScanResults();
		return mWm.getScanResults();
	}

	/** 获取网络连接的状态（得到配置好的网络连接） 2016 */
	public static List<WifiConfiguration> getWifiConfigurationList() {
		HHDLog.v("");
		return mWm.getConfiguredNetworks();
	}

	/** 创建一个WiFi并配置网络信息 2016 */
	public static WifiConfiguration createWifiConfiguration(String SSID/*, String Password, int Type*/) {
		HHDLog.v("");
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";

		WifiConfiguration tempConfig = isExsits(SSID);
		if (tempConfig != null) {
			HHDLog.v("");
			mWm.removeNetwork(tempConfig.networkId);
		}

//          if(Type == 1) {HHDLog.v(""); //WIFICIPHER_NOPASS
//               config.wepKeys[0] = "";
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//               config.wepTxKeyIndex = 0;
//          }
//          if(Type == 2) {HHDLog.v(""); //WIFICIPHER_WEP
//              config.hiddenSSID = true;
//              config.wepKeys[0]= "\""+Password+"\"";
//              config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//              config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//              config.wepTxKeyIndex = 0;
//          }
//          if(Type == 3) {HHDLog.v(""); //WIFICIPHER_WPA
//	          config.preSharedKey = "\""+Password+"\"";
//	          config.hiddenSSID = true;
//	          config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//	          config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//	          config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//	          config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//	          //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//	          config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//	          config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//	          config.status = WifiConfiguration.Status.ENABLED;
//          }
		return config;
	}

	/** 是否已存在WiFi 2016 */
	private static WifiConfiguration isExsits(String SSID) {
		HHDLog.v("");
		List<WifiConfiguration> existingConfigs = mWm.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			HHDLog.v("");
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				HHDLog.v("");
				return existingConfig;
			}
		}
		return null;
	}

	/** 判断WiFi 是否可用 2016 */
	public static boolean isEnable(Context context) {
		HHDLog.v("");
		mWm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return mWm.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
	}
}
