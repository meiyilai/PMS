package com.gzmelife.app.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.gzmelife.app.R;
import com.gzmelife.app.bean.LocalFoodMaterialLevelThree;
import com.gzmelife.app.tools.MyLogger;

/**
 * 我的食材库（遍历显示每个食材）
 */
@SuppressLint("InflateParams")
public class MyCookFoodMaterialChildAdapter extends BaseAdapter {

	MyLogger HHDLog=MyLogger.HHDLog();

	private String TAG="MyCookFoodMaterialChildAdapter";
	private Context context;

	private List<LocalFoodMaterialLevelThree> list;

	private boolean isClickable;

	private OnReceiver onReceiver;

	private int flag;

	private LayoutInflater inflater;

	/** flag 0:返回选择的id，1：返回选择的名字 */
	public MyCookFoodMaterialChildAdapter(Context context, List<LocalFoodMaterialLevelThree> list, int flag, OnReceiver onReceiver) {
		super();
		this.context = context;
		this.list = list;
		this.flag = flag;
		this.onReceiver = onReceiver;

		inflater = LayoutInflater.from(this.context);
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
		final ViewHolder viewHolder;
		if (null == convertView) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_lv_my_foodmaterial_child, null);
			viewHolder.cb_taste = (CheckBox) convertView.findViewById(R.id.cb_taste);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final LocalFoodMaterialLevelThree bean = list.get(position);
		//HHDLog.v("遍历食材名称："+list.get(position).getName()+"_遍历食材UID："+list.get(position).getId()+" "+list.get(position).getUid()+" "+list.get(position).getPid());
		viewHolder.cb_taste.setText(bean.getName());
		viewHolder.cb_taste.setChecked(list.get(position).isChecked());
		viewHolder.cb_taste.setEnabled(true);
		viewHolder.cb_taste.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				list.get(position).setChecked(!list.get(position).isChecked());
				viewHolder.cb_taste.setChecked(list.get(position).isChecked());
				if (onReceiver != null) {
					if (flag == 0) {
						onReceiver.onCheckChange(bean.getId() + "",bean.getUid() + "", list.get(position).isChecked());/** TODO:调用选择事件 */
					} else if (flag == 1) {
						onReceiver.onCheckChange(bean.getName(),bean.getUid() + "", list.get(position).isChecked());/** TODO:调用选择事件 */
					}
				}
				HHDLog.v("点击了“我的食材库”的："+list.get(position).getName());
			}
		});

		return convertView;
	}

	public interface OnReceiver {
		void onCheckChange(String name,String id,  boolean isChecked);
	}

	public static class ViewHolder {
		CheckBox cb_taste;
	}

	public void setClickable(boolean isClickable) {
		this.isClickable = isClickable;
	}

}
