package com.gzmelife.app.activity;

import java.util.List;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.demo_activity.EspWifiAdminSimple;
import com.gzmelife.app.KappAppliction;
import com.gzmelife.app.R;
import com.gzmelife.app.bean.DeviceNameAndIPBean;
import com.gzmelife.app.device.Config;
import com.gzmelife.app.device.DeviceUtil;
import com.gzmelife.app.fragment.DeviceFragment;
import com.gzmelife.app.tools.DateUtil;
import com.gzmelife.app.tools.KappUtils;
import com.gzmelife.app.tools.MyLogger;

/**
 * 界面【无线路由器】左边（添加新设备）
 */
@ContentView(R.layout.activity_add_device_by_wifi_router)
public class AddDeviceByWifiRouterActivity extends BaseActivity implements OnClickListener {//

	MyLogger HHDLog = MyLogger.HHDLog();

	@ViewInject(R.id.tv_title)
	TextView tv_title;
	@ViewInject(R.id.et_ssid)
	EditText et_ssid;
	@ViewInject(R.id.et_pwd)
	EditText et_pwd;
	@ViewInject(R.id.tv_title_left)
	TextView tv_title_left;
	private DeviceUtil deviceUtil;
	/** WiFi管理模块 */
	private EspWifiAdminSimple mWifiAdmin;
	private Context context;

	//TODO 2016
	/** Socket状态监听 */
	@Override
	public void success(List<String> cookBookFileList, int status, int progress, int total) {
		switch (status) {
			case 4:
				HHDLog.e("《7》");
				HHDLog.v("回调码=4，握手成功");
				deviceUtil = new DeviceUtil(context, new DeviceUtil.OnReceiver() {
					@Override
					public void refreshData(List<DeviceNameAndIPBean> _list) {
						Message msg = new Message();
						msg.what = 4;
						msg.obj = _list;
						handler.sendMessage(msg);
					}
				});
				deviceUtil.startSearch();
				break;
			case 9:
				HHDLog.e("《8》");
				HHDLog.v("回调码=9，对时功能成功");
				handler.sendEmptyMessage(9);
				break;
			default:
				break;
		}
	}
	@Override
	public void failure(int flag) {
		handler.sendEmptyMessage(-9);
	}
	//TODO 2016
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		context = this;
		mWifiAdmin = new EspWifiAdminSimple(this);
		HHDLog.e("《1》");
		initView();
		getDevices();
	}

	@Override
	protected void onResume() {
		super.onResume();
		HHDLog.v("界面【无线路由器】左边（添加新设备）");

		//TODO 2016
		bindSocketService();
		//TODO 2016
		handler.sendEmptyMessage(Config.MSG_RE_BIND);
	}

	@Override
	protected void onPause() {
		super.onPause();

		//TODO 2016
		unbindSocketService();
		//TODO 2016
	}

	private void initView() {
		HHDLog.e("《2》");
		tv_title.setText("无线路由器");
		et_ssid.setText(mWifiAdmin.getWifiConnectedSsid());//2016显示当前连接WiFi的名称
		tv_title_left.setVisibility(View.VISIBLE);
		tv_title_left.setText("添加新设备");
	}

	/** 获取局域网内所有设备（为提高中途切换WiFi时提高成功率） 2016 *///TODO 最终应该实现动态监听切换WiFi，如有切换WiFi才触发此函数
	private void getDevices() {
		HHDLog.e("《3》");
		//showDlg();//TODO
		if (!TextUtils.isEmpty(new EspWifiAdminSimple(this).getWifiConnectedSsid())) {
			KappUtils.getLocalIP(context);
			deviceUtil = new DeviceUtil(context, new DeviceUtil.OnReceiver() {
				@Override
				public void refreshData(List<DeviceNameAndIPBean> _list) {
					//list.clear();
					//list.addAll(_list);
					handler.sendEmptyMessage(0);
				}
			});
			deviceUtil.startSearch();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_confirm:
				HHDLog.e("《4》点击“配置”");
				if (et_pwd.getText().toString().equals("")) {
					KappUtils.showToast(context, "请输入WiFi密码");
				} else {
					String apSsid = mWifiAdmin.getWifiConnectedSsid();//WiFi名称
					String apBssid = mWifiAdmin.getWifiConnectedBssid();//手机MAC
					String apPassword = et_pwd.getText().toString();//WiFi密码//.toString().trim()
					String isSsidHiddenStr = "NO";//不是隐藏的WiFi
					new EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword, isSsidHiddenStr, "1");
				}
				break;
			case R.id.tv_tip:
				HHDLog.v("点击“配置指南”");
				startActivity(new Intent(context, RouterTipActivity.class));
				break;
		}
	}

	private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {
		private IEsptouchTask mEsptouchTask;
		/** without the lock, if the user tap confirm and cancel quickly enough,the bug will arise. the reason is follows:
		 * 当没有锁，如果用户点击确认和取消足够快，该错误就会出现。原因如下：
		 *
		 * 0. task is starting created, but not finished
		 * 0.任务开始创建，但未完成
		 *
		 * 1. the task is cancel for the task hasn't been created, it do nothing
		 * 1任务被取消了，任务没有被创建，什么也没有做
		 *
		 * 2. task is created
		 * 创建任务
		 *
		 * 3. Oops, the task should be cancelled, but it is running
		 * 3.哎呀，这个任务应该被取消，但它运行*/

		/** 同步时充当锁的对象 */
		private final Object mLock = new Object();

		/** 当后台任务执行之前，开始调用此方法 */
		@Override
		protected void onPreExecute() {
			HHDLog.v("后台任务执行之前转圈");
			showDlg();
		}

		/** 执行后台任务 */
		@Override
		protected List<IEsptouchResult> doInBackground(String... params) {
			HHDLog.e("《5》");
			HHDLog.v("执行后台任务");
			socketService.closeSocket();//TODO 2016 加
			int taskResultCount = -1;
			synchronized (mLock) {
				String apSsid = params[0];//WiFi名称
				String apBssid = params[1];//路由器MAC
				String apPassword = params[2];//WiFi密码
				String isSsidHiddenStr = params[3];//不是隐藏的WiFi
				String taskResultCountStr = params[4];//1
				boolean isSsidHidden = false;
				HHDLog.e("WiFi名称=" + apSsid + "，路由器MAC=" + apBssid + "，WiFi密码=" + apPassword + "，不是隐藏的WiFi=" + isSsidHiddenStr);
				if (isSsidHiddenStr.equals("YES")) {
					isSsidHidden = true;
				}
				taskResultCount = Integer.parseInt(taskResultCountStr);
				mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, isSsidHidden, context);
				//Esptouch是上海乐鑫开发的一键配置wifi上网的技术
				// mEsptouchTask.setEsptouchListener(myListener);
			}
			List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);//执行结果
			return resultList;
		}

		/** 当后台任务执行结束，此方法将会被调用 */
		@Override
		protected void onPostExecute(List<IEsptouchResult> result) {
			HHDLog.e("《6》");
			HHDLog.v("后台任务执行结束");
			IEsptouchResult firstResult = result.get(0);
			/** check whether the task is cancelled and no results received
			 *  检查任务是否被取消和没有收到任何结果 */
			if (!firstResult.isCancelled()) {
				int count = 0;
				/** max results to be displayed, if it is more than maxDisplayCount,just show the count of redundant ones
				 * 要显示的最大结果，如果它大于最大显示数，只显示多余的计数 */
				final int maxDisplayCount = 1;
				/** the task received some results including cancelled while executing before receiving enough results
				 * 这项任务收到了一些结果，包括取消，而执行之前收到足够的结果 */
				if (firstResult.isSuc()) {	//检查是否esptouch任务执行成功
					// StringBuilder sb = new StringBuilder();
					for (IEsptouchResult resultInList : result) {
						// sb.append("Esptouch success, bssid = "+ resultInList.getBssid()+ ",InetAddress = "+ resultInList.getInetAddress().getHostAddress() + "\n");
						Config.serverHostIp = resultInList.getInetAddress().getHostAddress();//SERVER_HOST_IP=192.168.4.1
						HHDLog.v("配置到的PMS的IP=" + resultInList.getInetAddress().getHostAddress());
						//HHDLog.v("配置到的PMS的名称=" + resultInList.getInetAddress().getHostName());
						//HHDLog.v("配置到名称=" + resultInList.getInetAddress().getCanonicalHostName());
						//HHDLog.v("配置到名称=" + resultInList.getInetAddress().getAddress());
						//KappUtils.showToast(context, "PMS的ip：" + Config.SERVER_HOST_IP);
						count++;
						if (count >= maxDisplayCount) {
							break;
						}
					}
					// 2016 WiFi连接成功，跳到成功界面
					socketService.firstConnect();//TODO 加
					//socketService.splitInstruction(Config.bufConnect, null);//TODO
				} else {
					closeDlg();
					//KappUtils.showToast(context, "配置失败");
					KappUtils.showToast(context, "连接失败");
				}
			}
		}
	}

	Handler handler = new android.os.Handler(new Callback() {
		@SuppressWarnings("unchecked")
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case 4:
					HHDLog.v("H4：握手成功");
					/*deviceUtil = new DeviceUtil(context, new DeviceUtil.OnReceiver() {
						@Override
						public void refreshData(List<DeviceNameAndIPBean> _list) {
							for (DeviceNameAndIPBean bean : _list) {
								HHDLog.e("测试网络中已经连接设备="+bean.toString());//
								if (bean.getIp().equals(Config.SERVER_HOST_IP)) {
									Config.SERVER_HOST_NAME = bean.getName();
									socketService.splitInstruction(Config.bufSetTime, new DateUtil().getCurrentTime());
									break;
								}
								//Config.SERVER_HOST_NAME = "未匹配到SSID";
								Config.SERVER_HOST_NAME = "PMS_123456";
								socketService.splitInstruction(Config.bufSetTime, new DateUtil().getCurrentTime());
							}
						}
					});
					deviceUtil.startSearch();*/
					HHDLog.e("给PMS名称");
					List<DeviceNameAndIPBean> _list = (List<DeviceNameAndIPBean>) msg.obj;
					for (DeviceNameAndIPBean bean : _list) {
						if (bean.getIp().equals(Config.serverHostIp)) {
							Config.serverHostName = bean.getName();
							socketService.splitInstruction(Config.BUF_SET_TIME, new DateUtil().getCurrentTime());
							break;
						}
					}
					break;
				case 9:
					HHDLog.e("《9》");
					HHDLog.v("H9：对时成功");

					KappUtils.showToast(context, "连接成功");
					closeDlg();
					KappAppliction.state = 1;
					DeviceFragment.isClearList = true;
					startActivity(new Intent(context, MainActivity.class));
					AddDeviceByWifiRouterActivity.this.finish();
					break;
				case 0:
					HHDLog.v("0");
					//closeDlg();//TODO
					break;
				case -9:
					HHDLog.v("-9");
					KappUtils.showToast(context, "与PMS连接失败");
					KappAppliction.state = 2;
					closeDlg();
					break;
				case Config.MSG_RE_BIND:
					bindSocketService();
					handler.sendEmptyMessage(Config.MSG_CHECK_BIND);
					break;
				case Config.MSG_CHECK_BIND:
					if (socketService == null) {//判断下确保已经绑定服务
						handler.sendEmptyMessage(Config.MSG_RE_BIND);
						HHDLog.w("判断是否已经绑定服务=" + (socketService == null));
					} else {
						//loadList();
						HHDLog.w("判断是否已经绑定服务=" + (socketService == null));
					}
					break;
			}
			return false;
		}
	});

	/** 重写back键事件（关闭转圈或者关闭界面） 2016 */
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		HHDLog.w("");
//		if (keyCode == KeyEvent.KEYCODE_BACK /*&& event.getRepeatCount() == 0*/) {//event.getRepeatCount() == 0 /** 防止点击过快 */
//			HHDLog.w("重写back键事件（关闭转圈或者关闭界面），是否转圈="+isShowingDlg());
//			if (isShowingDlg()) {
//				closeDlg();
//			} else {
//				AddDeviceByWifiRouterActivity.this.finish();
//			}
//		}
//		return super.onKeyDown(keyCode, event);
//	}
}
