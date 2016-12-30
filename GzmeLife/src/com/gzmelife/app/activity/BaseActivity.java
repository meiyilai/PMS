package com.gzmelife.app.activity;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.gzmelife.app.R;
import com.gzmelife.app.device.SocketService;
import com.gzmelife.app.tools.CountDownTimerUtil;
import com.gzmelife.app.tools.KappUtils;
import com.gzmelife.app.tools.MyLogger;
import com.gzmelife.app.tools.ShowDialogUtil;
import com.gzmelife.app.tools.SystemBarUtils;

import java.util.List;

public abstract class BaseActivity extends FragmentActivity {

	MyLogger HHDLog = MyLogger.HHDLog();

	/** 转圈警示框2016 */
	private AlertDialog dlg;

	public Context context;

	public SocketService socketService;//TODO 2016
	/** 标记是否绑定服务2016 */
	public boolean isBound = false;
	/** 组件连接服务2016 */
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			SocketService.SocketBinder socketBinder = (SocketService.SocketBinder) service;
			socketService = socketBinder.getSocketService();
			socketService.setReceiver(onReceiver);//TODO
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {//服务出现问题
			socketService = null;
			isBound = false;
		}
	};
	/** 绑定服务 */
	public void bindSocketService(){
		HHDLog.w("绑定前判断是否已经绑定=" + isBound);
		if (!isBound) {
			Intent intent = new Intent(this, SocketService.class);
			bindService(intent, conn, Context.BIND_AUTO_CREATE);
			isBound = true;
			HHDLog.w("执行绑定后判断是否已经绑定="+isBound);
		}
		/** TODO 绑定成功后判断是否连接Socket，如果没连接则 */
	}
	/** 解绑服务 */
	public void unbindSocketService(){
		if (isBound) {
			unbindService(conn);
			isBound = false;
		}
	}
	/** Socket状态监听 */
	private SocketService.OnReceiver onReceiver = new SocketService.OnReceiver() {
		@Override
		public void onSuccess(List<String> cookBookFileList, int status, int progress, int total) {
			success(cookBookFileList, status, progress, total);//模板设计模式
		}
		@Override
		public void onFailure(int flag) {
			failure(flag);//把具体实现延迟到子类
		}
	};
	/**
	 * 抽象Socket连接成功
	 *
	 * @param cookBookFileList  菜谱列表（遍历时）
	 * @param status            0：默认值（不处理），
	 *                          1：下载成功，
	 *                          2：下载失败，
	 *                          3：下载数据的百分比，
	 *                          4：连接成功，
	 *                          5：删除文件成功，
	 *                          6：获取设备状态成功，
	 *                          7：传文件到智能灶成功，
	 *                          8：传文件到智能灶的百分比，
	 *                          9：对时功能。
	 * @param progress          进度条当前值
	 * @param total             进度条总进度
	 */
	public abstract void success(List<String> cookBookFileList, int status, int progress, int total);
	/**
	 * 抽象Socket连接失败
	 *
	 * @param flag  0：默认值，
	 *              -1：下载文件大小=0。
	 */
	public abstract void failure(int flag);
	//TODO 2016

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		 x.view().inject(this);
		SystemBarUtils.applyKitKatTranslucency(this);
		context = getApplicationContext();
	}

	/** 关闭基Activity 2016 */
	public void back(View v) {
		this.finish();
	}

	/** 显示转圈警示框（不可以取消） 2016 */
	public void showDlg() {
		HHDLog.v("显示转圈");
		if (null != this && null != dlg && !dlg.isShowing()) {
			dlg.show();
		} else if (null != this && null == dlg) {
			dlg = ShowDialogUtil.getShowDialog(this, R.layout.dialog_progressbar, 0, 0, false);
		}
	}

	/** 显示转圈警示框（可以取消） 2016 */
	public void showDialog() {
		HHDLog.v("显示警示框（可以取消）");
		if (null != this && null != dlg && !dlg.isShowing()) {
			dlg.show();
		} else if (null != this && null == dlg) {
			dlg = ShowDialogUtil.getShowDialog(this, R.layout.dialog_progressbar, 0, 0, true);
		}
	}

	/** 转圈警示框状态：true=活动状态 2016 */
	public boolean isShowingDlg(){
		if (null != this && null != dlg && dlg.isShowing()) {
			return true;
		}
		return false;
	}

	/** 关闭转圈警示框 2016 */
	public void closeDlg() {
		HHDLog.v("关闭转圈");
		if (null != this && null != dlg && dlg.isShowing()) {
			dlg.dismiss();
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			View v = getCurrentFocus();
			if (isShouldHideInput(v, ev)) {
				hideSoftInput(v.getWindowToken());
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * @param v
	 * @param event
	 * @return
	 */
	private boolean isShouldHideInput(View v, MotionEvent event) {
		if (v != null && (v instanceof EditText)) {
			int[] l = { 0, 0 };
			v.getLocationInWindow(l);
			int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
			if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param token
	 */
	private void hideSoftInput(IBinder token) {
		if (token != null) {
			InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			im.hideSoftInputFromWindow(token,
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public class TimeCountOut extends CountDownTimerUtil {
		private OnEvent onEvent;

		public TimeCountOut(long millisInFuture, long countDownInterval, OnEvent onEvent2) {
			super(millisInFuture, countDownInterval);
			this.onEvent = onEvent2;
		}

		@Override
		public void onFinish() {
			if (dlg.isShowing()) {
				// KappUtils.showToast(BaseActivity.this, "超时，请重试");
				closeDlg();
			}
			if (onEvent != null) {
				onEvent.onFinish();
			}
		}

		@Override
		public void onTick(long millisUntilFinished) {
			if (onEvent != null) {
				onEvent.onTick(millisUntilFinished);
			}
		}
	}

	public interface OnEvent {
		void onFinish();

		void onTick(long millisUntilFinished);
	}

	/** 后台返回成功，则继续；否则给出提示 */
	public boolean isSuccess(JSONObject obj) {
		try {
			if (!obj.getString("status").equals("10001")) {
				String errorMsg = obj.getString("msg");
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = "后台错误";
				}
				KappUtils.showToast(context, errorMsg);
				return false;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}

	public String getEditTextString(EditText et) {
		if (et == null) {
			return "";
		}
		return et.getText().toString().trim();
	}
}
