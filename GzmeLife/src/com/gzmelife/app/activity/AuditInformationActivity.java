package com.gzmelife.app.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gzmelife.app.KappAppliction;
import com.gzmelife.app.R;
import com.gzmelife.app.UrlInterface;
import com.gzmelife.app.adapter.AuditInformationAdapter;
import com.gzmelife.app.bean.MyUploadCookbookBean;
import com.gzmelife.app.tools.DateUtil;
import com.gzmelife.app.tools.MyLog;
import com.gzmelife.app.tools.MyLogger;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

@ContentView(R.layout.activity_audit_information)
public class AuditInformationActivity extends BaseActivity {//
	@ViewInject(R.id.tv_title)
	TextView tv_title;
	
	@ViewInject(R.id.layout_noData)
	View layout_noData;
	@ViewInject(R.id.tv_title_left)
	TextView tv_title_left;
	@ViewInject(R.id.lv_data)
	PullToRefreshListView lv_data;
	
	private List<MyUploadCookbookBean> list = new ArrayList<MyUploadCookbookBean>();
	private AuditInformationAdapter adapter;
	
//	private int page = 1;
//	private int pageSize = 10;
	
	private Context context;


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
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		context = this;
		initView();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initView() {
		tv_title.setText("审核信息");
		tv_title_left.setVisibility(View.VISIBLE);
		tv_title_left.setText("个人中心");
		adapter = new AuditInformationAdapter(context, list);
		lv_data.setAdapter(adapter);
		
		lv_data.setMode(Mode.DISABLED);
		lv_data.setOnRefreshListener(new OnRefreshListener2() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				list.clear();
//				page = 1;
				getData();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {
//				page++;
				getData();
			}
		});
		getData();
	}

	private void getData() {
		showDlg();
		RequestParams params = new RequestParams(UrlInterface.URL_MY_UPLOAD_COOKBOOK);
		params.addBodyParameter("userId", KappAppliction.myApplication.getUser().getId());
		x.http().post(params, new CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				refreshingEnd();
				MyLog.i(MyLog.TAG_I_JSON, result);
				try {
					Gson gson = new Gson();
					JSONObject obj = new JSONObject(result);
					if (!isSuccess(obj)) {
						return;
					}
					
					List<MyUploadCookbookBean> listTemp = gson.fromJson(obj.getJSONObject("data")
							.getJSONArray("menubooks").toString(), new TypeToken<List<MyUploadCookbookBean>>(){}.getType());
					if (listTemp.size() == 0) {
//						if (page == 1) {
//							KappUtils.showToast(context, "暂无系统消息");
//						} else {
//							page--;
//							KappUtils.showToast(context, "无更多数据");
//						}
						layout_noData.setVisibility(View.VISIBLE);
						lv_data.setVisibility(View.GONE);
					} else {
						list.addAll(listTemp);
					}
					adapter.notifyDataSetChanged();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				refreshingEnd();
			}

			@Override
			public void onCancelled(CancelledException cex) {
				refreshingEnd();
			}

			@Override
			public void onFinished() {
				refreshingEnd();
			}
		});
	}

	private void refreshingEnd() {
		closeDlg();
		lv_data.getLoadingLayoutProxy().setLastUpdatedLabel("最近更新:" + DateUtil.getCurrentTimeString(0));
		lv_data.onRefreshComplete();
	}

}
