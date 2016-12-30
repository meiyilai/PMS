package com.gzmelife.app.activity;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import com.gzmelife.app.R;
import com.gzmelife.app.UrlInterface;
import com.gzmelife.app.tools.MyLogger;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.List;

@ContentView(R.layout.activity_protocol)
public class ProtocolActivity extends BaseActivity {//
	@ViewInject(R.id.web)
	WebView web;
	@ViewInject(R.id.tv_title)
	TextView tv_title;
	@ViewInject(R.id.tv_title_left)
	TextView tv_title_left;


	MyLogger HHDLog = MyLogger.HHDLog();
	@Override
	protected void onResume() {
		super.onResume();
		HHDLog.v("");
	}

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
		tv_title.setText("用户协议");
		tv_title_left.setVisibility(View.VISIBLE);
		tv_title_left.setText("注册");
		web.getSettings().setJavaScriptEnabled(true);
		web.loadUrl(UrlInterface.URL_HOST + "/webview.jsp?type=1");
	}

}
