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

@ContentView(R.layout.activity_protocol)
public class ProtocolActivity extends BaseActivity {
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
	@Override
	protected void onCreate(Bundle arg0) {
		/***/
		super.onCreate(arg0);
		tv_title.setText("用户协议");
		tv_title_left.setVisibility(View.VISIBLE);
		tv_title_left.setText("注册");
		web.getSettings().setJavaScriptEnabled(true);
		web.loadUrl(UrlInterface.URL_HOST + "/webview.jsp?type=1");
	}

}
