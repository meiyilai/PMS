package com.gzmelife.app.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gzmelife.app.R;
import com.gzmelife.app.bean.DeviceNameAndIPBean;
import com.gzmelife.app.tools.MyLog;

/**
 * 设备列表Adapter_界面【添加设备】左边（设备中心）
 */
@SuppressLint("InflateParams")
public class AddDeviceAdapter extends BaseAdapter {

	private List<DeviceNameAndIPBean> list;
	
	private LayoutInflater inflater;
	private Context context;
	
	private OnReceiver onReceiver;

    /** 设备列表Adapter构造方法_界面【添加设备】左边（设备中心） */
	public AddDeviceAdapter(Context context, List<DeviceNameAndIPBean> list, OnReceiver onReceiver) {
		this.context = context;
		this.list = list;
		this.onReceiver = onReceiver;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		MyLog.d(MyLog.TAG_D, "--->position=" + position);
		final ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_lv_add_device, null);
			holder = new ViewHolder();
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.view_gap = convertView.findViewById(R.id.view_gap);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (position == list.size() - 1) {
			holder.view_gap.setVisibility(View.GONE);
		} else {
			holder.view_gap.setVisibility(View.VISIBLE);
		}
		final DeviceNameAndIPBean bean = list.get(position);
		holder.tv_name.setText("我的智能灶-" + bean.getName());
		
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onReceiver != null) {
					onReceiver.onClick(position);
				}
			}
		});
		
		return convertView;
	}

    /** 20161212响应点击事件（自定义接口回调） */
	public interface OnReceiver {
		void onClick(int position);
	}
	
	class ViewHolder {
		TextView tv_name;
		View view_gap;
	}

}
