package com.gzmelife.app.activity;

import java.util.List;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gzmelife.app.R;
import com.gzmelife.app.adapter.PMSErrorAdapter;
import com.gzmelife.app.device.Config;
import com.gzmelife.app.fragment.DeviceFragment;
import com.gzmelife.app.tools.KappUtils;
import com.gzmelife.app.tools.MyLog;
import com.gzmelife.app.tools.MyLogger;
import com.gzmelife.app.views.ListViewForScrollView;

/**
 * 界面【设备状态】左边（设备）
 */
@ContentView(R.layout.activity_device_detail)
public class DeviceDetailActivity extends BaseActivity {//

	private MyLogger HHDLog = MyLogger.HHDLog();

	@ViewInject(R.id.bt_startStop)
	private Button bt_startStop;
	@ViewInject(R.id.bt_increase)
	private Button bt_increase;
	@ViewInject(R.id.bt_decrease)
	private Button bt_decrease;
	@ViewInject(R.id.bt_confirm)
	private Button bt_confirm;

	@ViewInject(R.id.tv_title)
	private TextView tv_title;
	@ViewInject(R.id.tv_v)
	TextView tv_v;
	@ViewInject(R.id.tv_a)
	TextView tv_a;
	@ViewInject(R.id.tv_w)
	TextView tv_w;
	@ViewInject(R.id.tv_setW)
	TextView tv_setW;
	@ViewInject(R.id.tv_pmsTemp)
	TextView tv_pmsTemp;
	@ViewInject(R.id.tv_setTemp)
	TextView tv_setTemp;
	@ViewInject(R.id.tv_roomTemp)
	TextView tv_roomTemp;
	@ViewInject(R.id.tv_status)
	TextView tv_status;
	@ViewInject(R.id.tv_title_left)
	TextView tv_title_left;
	@ViewInject(R.id.lv_error)
	ListViewForScrollView lv_error;

	private Context context;
	/** 显示错误代码的适配器 2016 */
	private PMSErrorAdapter adapter;
	/** 标记是否刷新界面（没刷新不能关闭界面） 2016 */
	private boolean state = false;
	/** 刷新界面的时间控制器 21061227 */
	private RefreshTime refreshTime;
	/** 累计时间（一秒增加一次） */
	private int refreshTimer = 0;

	//TODO 2016
	/** Socket状态监听 */
	@Override
	public void success(List<String> cookBookFileList, int status, int progress, int total) {
		//HHDLog.v("status=" + status);
		System.out.println("");
		if (status == 6) {
			handler.sendEmptyMessage(0);//刷新界面
		} else if (status == 11) {
			HHDLog.v("遥控功能回调成功，应该刷新显示状态");
			refreshTimer = 0;
			handler.sendEmptyMessage(7);
		}
	}
	@Override
	public void failure(int flag) {
		HHDLog.v("获取设备状态失败=" + flag);
		handler.sendEmptyMessage(1);
	}
	//TODO 2016
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		context = this;
		initView();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		HHDLog.v("界面【设备状态】左边（设备）");

		//TODO 2016
		bindSocketService();
		//TODO 2016
		handler.sendEmptyMessage(Config.MSG_RE_BIND);
		if (refreshTime != null) {//TODO
			refreshTime.cancel();
			refreshTime = null;
		}
		refreshTime = new RefreshTime(Long.MAX_VALUE, 1 * 1000);
		refreshTime.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		HHDLog.v("");

		//TODO 2016
		unbindSocketService();
		//TODO 2016

		if (refreshTime != null) {
			refreshTime.cancel();
			refreshTime = null;
		}
	}

	/** 2016获取PMS设备的当前状态信息 */
	private void getDeviceInfo() {
		//HHDLog.v("socketService是否为空=" + (socketService == null));
		socketService.splitInstruction(Config.BUF_STATUS, null);
		state = false;
		HHDLog.e("获取设备状态 F7 00");
	}
	
	private void initView() {
		tv_title.setText("设备状态");
		tv_title_left.setVisibility(View.VISIBLE);
		tv_title_left.setText("设备");

		//bt_startStop bt_increase  bt_decrease bt_confirm
		bt_startStop.setOnClickListener(new OnClickListener() {//启/停
			@Override
			public void onClick(View view) {
				socketService.splitInstruction(Config.BUF_ON_OFF, null);
				Drawable pressed = context.getResources().getDrawable(R.drawable.icon_bt_control_pressed);
				bt_startStop.setBackground(pressed);
			}
		});
		bt_increase.setOnClickListener(new OnClickListener() {//+
			@Override
			public void onClick(View view) {
				socketService.splitInstruction(Config.BUF_IN_POWER, null);
			}
		});
		bt_decrease.setOnClickListener(new OnClickListener() {//-
			@Override
			public void onClick(View view) {
				socketService.splitInstruction(Config.BUF_DE_POWER, null);
			}
		});
		bt_confirm.setOnClickListener(new OnClickListener() {//确定
			@Override
			public void onClick(View view) {
				socketService.splitInstruction(Config.BUF_CONFIRM, null);
			}
		});


		tv_title.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				HHDLog.v("");
				//if (state == true) {
				if (state) {
					Intent intent = new Intent();
					intent.putExtra("socket", "socket");
					setResult(RESULT_OK, intent);
					DeviceFragment d = new DeviceFragment();
					d.socket = "socket";
					DeviceDetailActivity.this.finish();
					//System.out.print("----收到设备状态----");
				} else {
					Toast.makeText(DeviceDetailActivity.this, "请稍后", Toast.LENGTH_SHORT).show();
					//System.out.print("----正在请求设备状态----");
					HHDLog.v("正在获取设备状态");
				}
			}
		});
	}

	Handler handler = new android.os.Handler(new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					HHDLog.e("H0填充数据");
					System.out.println("");
					state = true;
					tv_v.setText(Config.PMS_V);
					tv_a.setText(Config.PMS_A);
					tv_w.setText(Config.PMS_SetW);
					tv_pmsTemp.setText(Config.PMS_Temp);
					tv_roomTemp.setText(Config.PMS_IGBT);
					tv_setTemp.setText(Config.PMS_SetTemp);
					tv_setW.setText(Config.PMS_W);
					tv_status.setText(Config.PMS_Status);

					Drawable blue = context.getResources().getDrawable(R.drawable.design_blue_point);//2016 加
					Drawable orange = context.getResources().getDrawable(R.drawable.design_orange_point);
					Drawable red = context.getResources().getDrawable(R.drawable.design_red_point);
					Drawable on = context.getResources().getDrawable(R.drawable.icon_bt_on);
					Drawable off = context.getResources().getDrawable(R.drawable.icon_bt_off);

					if ("关机".equals(Config.PMS_Status)) {//.trim()
						tv_status.setBackground(red);
						bt_startStop.setBackground(off);
					} else if ("开机".equals(Config.PMS_Status)) {
						tv_status.setBackground(blue);
						bt_startStop.setBackground(on);
					} else if ("待机".equals(Config.PMS_Status)) {
						tv_status.setBackground(orange);
						bt_startStop.setBackground(off);
					} else if ("暂停".equals(Config.PMS_Status)) {
						tv_status.setBackground(orange);
						//bt_startStop.setBackground(off);
					} //2016 加

					//Config.PMS_ERRORS.add("0");
					adapter = new PMSErrorAdapter(context, Config.PMS_Errors, new PMSErrorAdapter.OnReceiver() {
						@Override
						public void onClick(String position) {
							Intent intent = new Intent(context, AboutUsActivity.class);
							intent.putExtra("flag", KappUtils.FLAG_FAQ_DETAIL_1);
							//intent.putExtra("id", list.get(position - 1).getId());
							intent.putExtra("name", position);
							MyLog.i(MyLog.TAG_I_INFO, "position=====:" + position);
							startActivity(intent);
						}
					});
					lv_error.setAdapter(adapter);
					// 发送广播，四个主界面左上角图标变更.首次连接成功查询状态，之后每次心跳成功后发送查询
					Intent intent = new Intent();
					intent.setAction(KappUtils.ACTION_PMS_STATUS);
					context.sendBroadcast(intent);
					//t.interrupt();
					//System.out.print("----请求设备状态成功----");
					HHDLog.v("获取设备状态成功");
					/*// 无指令时5秒刷新一次界面
					new Handler().postDelayed(new Runnable() {
						public void run() {
							handler.sendEmptyMessage(7);
							HHDLog.e("测试刷新频率");
						}
					}, 5*1000);*/


					break;
				case 1:
					KappUtils.showToast(context, "获取设备状态失败");
					break;
				case 7://F7 00
					//HHDLog.v("2");
					getDeviceInfo();
					break;
				case Config.MSG_RE_BIND:
					showDlg();
					bindSocketService();
					handler.sendEmptyMessage(Config.MSG_CHECK_BIND);
					HHDLog.v("再次绑定服务");
					break;
				case Config.MSG_CHECK_BIND:
					if (socketService == null) {
						handler.sendEmptyMessage(Config.MSG_RE_BIND);
						HHDLog.w("判断是否已经绑定服务=" + (socketService == null));
					} else {
						HHDLog.w("判断是否已经绑定服务=" + (socketService == null));
						getDeviceInfo();
						closeDlg();
					}
					break;
			}
			return false;
		}
	});

	class RefreshTime extends CountDownTimer {
		public RefreshTime(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			HHDLog.e("启动刷新界面时间");
		}

		@Override
		public void onTick(long l) {
			refreshTimer++;
			HHDLog.w("累计秒数=" + refreshTimer);
			if (refreshTimer >= 5) {
				handler.sendEmptyMessage(7);
				refreshTimer = 0;
				//HHDLog.e("5秒");
			}
		}

		@Override
		public void onFinish() {

		}
	}
}

