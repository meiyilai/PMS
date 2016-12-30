package com.gzmelife.app.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;

public class RecyclerViewForScrollView extends RecyclerView {

	public RecyclerViewForScrollView(Context context) {
		super(context);
		//
	}

	public RecyclerViewForScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//
	}

	public RecyclerViewForScrollView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
		//
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);

	}

}
