package com.gzmelife.app.activity;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import android.os.Bundle;
import android.webkit.WebSettings.PluginState;
import android.widget.TextView;

import com.gzmelife.app.R;
import com.gzmelife.app.UrlInterface;
import com.gzmelife.app.tools.KappUtils;
import com.gzmelife.app.tools.MyLog;
import com.gzmelife.app.tools.MyLogger;
import com.gzmelife.app.views.ProgressWebView;

import java.util.List;

@ContentView(R.layout.activity_webview)
public class AboutUsActivity extends BaseActivity {//
	@ViewInject(R.id.tv_title)
	TextView tv_title;

	@ViewInject(R.id.webview)
	ProgressWebView webview;

	private String flag;
	private String id;
	private String name;

	// private Context context;
	//TODO 2016
	/** Socket状态监听 */
	@Override
	public void success(List<String> cookBookFileList, int status, int progress, int total) {}
	@Override
	public void failure(int flag) {}
	//TODO 2016
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		// context = this;

		flag = getIntent().getStringExtra("flag");
		id = getIntent().getStringExtra("id");
		MyLog.i(MyLog.TAG_I_INFO,"======id ======" + id);
		try {
			name = getIntent().getStringExtra("name");
		} catch (Exception e) {
			//
		}
		initView();
	}

	MyLogger HHDLog = MyLogger.HHDLog();
	@Override
	protected void onResume() {
		super.onResume();
		HHDLog.v("");
	}

	@SuppressWarnings("deprecation")
	private void initView() {
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setPluginState(PluginState.ON);
		webview.getSettings().setAllowFileAccess(true);

		switch (flag) {

		case KappUtils.FLAG_FAQ_DETAIL_1:
			tv_title.setText("问题详情");
			MyLog.i(MyLog.TAG_I_INFO, "name=====:"+ name);
			webview.loadUrl(UrlInterface.URL_MSG_DETAIL + "?type=3&errorName=" + name);
			MyLog.i(MyLog.TAG_I_INFO, "url=====:"+ UrlInterface.URL_MSG_DETAIL + "?type=3&errorName=" + name);
			break;
		case KappUtils.FLAG_FAQ_DETAIL:
			tv_title.setText("问题详情");
			MyLog.i(MyLog.TAG_I_INFO,"======id ======" + id);
			MyLog.i(MyLog.TAG_I_INFO,"======id ======" + UrlInterface.URL_MSG_DETAIL + "?type=3&id=" + id);
			webview.loadUrl(UrlInterface.URL_MSG_DETAIL + "?type=3&id=" + id);
			break;
		case KappUtils.FLAG_ABOUT_US:
			tv_title.setText("关于我们");
			webview.loadUrl(UrlInterface.URL_MSG_DETAIL + "?type=2");
			break;
		case KappUtils.FLAG_SYSTEM_MSG_DETAIL: // 系统消息
			tv_title.setText("详情");
			MyLog.i(MyLog.TAG_I_INFO,"======id ======" + id);
			webview.loadUrl(UrlInterface.URL_MSG_DETAIL + "?type=4&id=" + id);
			// showDlg();
			// RequestParams params = new
			// RequestParams(UrlInterface.URL_MSG_DETAIL);
			// params.addBodyParameter("type", "4");
			// params.addBodyParameter("id", id);
			// x.http().post(params, new CommonCallback<String>() {
			// @Override
			// public void onSuccess(String result) {
			// closeDlg();
			// try {
			// JSONObject obj = new JSONObject(result);
			// if (!isSuccess(obj)) {
			// return;
			// }
			//
			// webview.loadDataWithBaseURL(null,
			// obj.getJSONObject("data").getString("content"), "text/html",
			// "utf-8", null);
			// } catch (JSONException e) {
			// e.printStackTrace();
			// }
			// }
			//
			// @Override
			// public void onError(Throwable ex, boolean isOnCallback) {
			// closeDlg();
			// }
			//
			// @Override
			// public void onCancelled(CancelledException cex) {
			// closeDlg();
			// }
			//
			// @Override
			// public void onFinished() {
			// closeDlg();
			// }
			// });
			break;
		}
	}
}
