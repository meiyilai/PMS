package com.gzmelife.app.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gzmelife.app.R;
import com.gzmelife.app.activity.NetCookBookDetailActivity;
import com.gzmelife.app.bean.CookBookMenuBookStepsBean;
import com.gzmelife.app.tools.MyLog;
import com.gzmelife.app.tools.MyLogger;

/**
 * 编辑菜谱Adapter（菜谱详情、编辑菜谱界面都不执行）
 */
public class CookBookStepAdapter extends BaseAdapter {

	MyLogger HHDLog= MyLogger.HHDLog();

	Context context;
	ViewHolder viewHolder;
	// List<TimeNode> listTimeNode;
	boolean isEdt;
	private List<CookBookMenuBookStepsBean> cookBookMenuBookStepsBeanList = new ArrayList<CookBookMenuBookStepsBean>();

	/** 食材名称&重量 */
	public ArrayList<HashMap<String, Long>> foodNameAndWgt = new ArrayList<>();

	public CookBookStepAdapter(Context context, List<CookBookMenuBookStepsBean> cookBookMenuBookStepsBeanList) {
		this.context = context;
		this.cookBookMenuBookStepsBeanList = cookBookMenuBookStepsBeanList;
	}

	public boolean isEdt() {
		return isEdt;
	}

	public void setEdt(boolean isEdt) {
		this.isEdt = isEdt;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return cookBookMenuBookStepsBeanList == null ? 0
				: cookBookMenuBookStepsBeanList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		//MyLog.d("position=" + position);
		//HHDLog.v("position=" + position);
		// final TimeNode timeNode = listTimeNode.get(position);
		final CookBookMenuBookStepsBean bean = cookBookMenuBookStepsBeanList
				.get(position);

		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.layout_cook_book_step, null);
			viewHolder.tv_name = (TextView) convertView
					.findViewById(R.id.tv_name);
			viewHolder.tv_time = (TextView) convertView
					.findViewById(R.id.tv_time);
			viewHolder.tv_content = (TextView) convertView
					.findViewById(R.id.tv_content);
			viewHolder.iv_add = (ImageView) convertView
					.findViewById(R.id.iv_add);
			viewHolder.iv_edt = (ImageView) convertView
					.findViewById(R.id.iv_edt);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (isEdt) {
			viewHolder.iv_add.setVisibility(View.VISIBLE);
			viewHolder.iv_edt.setVisibility(View.VISIBLE);
		} else {
			viewHolder.iv_add.setVisibility(View.GONE);
			viewHolder.iv_edt.setVisibility(View.GONE);
		}
		viewHolder.iv_add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((NetCookBookDetailActivity) context).edtStep(null, 0, 0,
						false, position);
			}
		});
		viewHolder.iv_edt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int startTime;
				int endTime;
				if (position == 0) {
					startTime = 0;
				} else {
					startTime = Integer.parseInt(cookBookMenuBookStepsBeanList
							.get(position - 1).getMinute()
							+ cookBookMenuBookStepsBeanList.get(position - 1)
									.getSecond());
				}
				if (position == cookBookMenuBookStepsBeanList.size() - 1) {

					endTime = startTime + (5 * 60);
				} else {
					endTime = Integer.parseInt(cookBookMenuBookStepsBeanList
							.get(position + 1).getMinute()
							+ cookBookMenuBookStepsBeanList.get(position + 1)
									.getSecond());
				}
				((NetCookBookDetailActivity) context).edtStep(bean, startTime,
						endTime, true, position);
			}
		});

		viewHolder.tv_name.setText(String.valueOf(position + 1));
		SimpleDateFormat format = new SimpleDateFormat("mm’ss”");
		// viewHolder.tv_time.setText(cookBookMenuBookStepsBeanList.get(position).getMinute()+"’"+cookBookMenuBookStepsBeanList.get(position)+"”");

		/*if (!cookBookMenuBookStepsBeanList.get(position).getMinute().equals("")
				&& cookBookMenuBookStepsBeanList.get(position).getMinute() != null
				&& !cookBookMenuBookStepsBeanList
						.get(position)
						.getSecond()
						.equals("&&cookBookMenuBookStepsBeanList.get(position).getSecond()!=null")) {
			viewHolder.tv_time.setText(format.format(new Date(Integer
					.parseInt(cookBookMenuBookStepsBeanList.get(position)
							.getMinute()
							+ cookBookMenuBookStepsBeanList.get(position)
									.getSecond()) * 1000)));
		} else {
			viewHolder.tv_time.setText("");
		}*/

		if(!TextUtils.isEmpty(bean.getMinute())
				&&!TextUtils.isEmpty(bean.getSecond())){
			viewHolder.tv_time.setText(bean.getMinute()+"分"+bean.getSecond()+"秒");
		}else {
			viewHolder.tv_time.setText("");
		}

		StringBuffer fb = new StringBuffer();//20161114
		String[] singleFood = bean.getFoods().split(";");
		for (int i = 0; i < singleFood.length; i++) {
			HHDLog.v(singleFood[i]);
			String foodName = null;//食材名称
			String weight = null;//重量
			Pattern p = Pattern.compile("\\u007C(.*)\\u007C");//（\u007C=|）
			Matcher m = p.matcher(singleFood[i]);
			while (m.find()) {
				foodName = m.group(1);
			}
			weight = singleFood[i].substring(singleFood[i].lastIndexOf("|") + 1);//后缀
			if (foodName != null) {
				fb.append(foodName);
				fb.append(" ");
				fb.append(weight);
				fb.append(" g；");
			}
		}
		HHDLog.v(fb);
		viewHolder.tv_content.setText("步骤描述：" + bean.getDescribes() + " \n\n食材：" + fb);//20161114

		//String name = bean.getFoods().replace("0|", " ").replace("|", " ");
		//// String newStr = name.substring(name.indexOf("\0"),name.length());
		//// String result = name.substring(0, name.indexOf(";"));
		//viewHolder.tv_content.setText("步骤描述：" + bean.getDescribes() + " \n\n食材：" + name.replace("；", " g；"));
		return convertView;
	}

	class ViewHolder {
		TextView tv_name, tv_time, tv_content;//20161114步骤序号，步骤开始时间，步骤内容
		ImageView iv_add, iv_edt;//添加步骤按钮，编辑步骤按钮
	}

}
