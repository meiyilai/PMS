package com.gzmelife.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.espressif.iot.esptouch.demo_activity.EspWifiAdminSimple;
import com.gzmelife.app.KappAppliction;
import com.gzmelife.app.R;
import com.gzmelife.app.adapter.AddDeviceAdapter;
import com.gzmelife.app.bean.DeviceNameAndIPBean;
import com.gzmelife.app.device.Config;
import com.gzmelife.app.device.DeviceUtil;
import com.gzmelife.app.fragment.DeviceFragment;
import com.gzmelife.app.tools.DateUtil;
import com.gzmelife.app.tools.KappUtils;
import com.gzmelife.app.tools.MyLogger;
import com.gzmelife.app.views.ListViewForScrollView;

/**
 * 界面【添加设备】左边（设备中心）
 */
public class AddDevicesActivity extends BaseActivity {

    MyLogger HHDLog = MyLogger.HHDLog();

    private TextView tv_title;
    private TextView tv_number;
    private TextView tv_title_left;
    private Button bt_addDevice;
    private DeviceUtil deviceUtil;
    private ListViewForScrollView lv_data;
    /** 存储UDP扫描到的结果 2016 */
    private List<DeviceNameAndIPBean> list = new ArrayList<DeviceNameAndIPBean>();
    private AddDeviceAdapter adapter;
    private DeviceNameAndIPBean selectBean;
    private TimeCountOut outtime;
    private Context context;

    //TODO 2016
    /** Socket状态监听 */
    @Override
    public void success(List<String> cookBookFileList, int status, int progress, int total) {
        switch (status) {
            case 4:
                handler.sendEmptyMessage(4);
                HHDLog.w("回调码=4，握手成功");
                break;
            case 9:
                handler.sendEmptyMessage(9);
                HHDLog.w("回调码=9，对时功能成功");
                break;
            default:
                break;
        }
    }

    @Override
    public void failure(int flag) {
        closeDlg();
        HHDLog.e("连接失败回调码=" + flag);
        Looper.prepare();
        //KappUtils.showToast(context, "与PMS连接中断");
        KappUtils.showToast(context, "连接失败，请重连...");//TODO 像DF那样设置超时才触发
        KappAppliction.state = 2;
        Looper.loop();
    }
    //TODO 2016

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_add_device);
        context = this;
        initView();
        getDevices();
        lv_data = (ListViewForScrollView) findViewById(R.id.lv_data);
        adapter = new AddDeviceAdapter(context, list, new AddDeviceAdapter.OnReceiver() {
            @Override
            public void onClick(final int position) {
                selectBean = list.get(position);
                Config.serverHostIp = selectBean.getIp();
                HHDLog.w("点击设备列表" + "；选中名称=" + selectBean.getName() + "；IP=" + selectBean.getIp());
                showDlg();
                socketService.closeSocket();
                socketService.firstConnect();
                outtime = new TimeCountOut(10 * 1000, 1000, null);
                outtime.start();
            }
        });
        lv_data.setAdapter(adapter);
        tv_number = (TextView) findViewById(R.id.tv_number);
        bt_addDevice = (Button) findViewById(R.id.bt_addDevice);
        bt_addDevice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(AddDevicesActivity.this, AddNewDeviceActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        HHDLog.v("界面【添加设备】左边（设备中心）");
        //TODO 2016
        bindSocketService();
        //TODO 2016
        handler.sendEmptyMessage(Config.MSG_RE_BIND);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //TODO 2016
        unbindSocketService();
        //TODO 2016

        if (deviceUtil != null) {
            deviceUtil.closeSearch();
        }
        if (outtime != null) {
            outtime.cancel();
        }
    }

    private void initView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText("添加设备");
        tv_title_left = (TextView) findViewById(R.id.tv_title_left);
        tv_title_left.setVisibility(View.VISIBLE);
        tv_title_left.setText("设备中心");
    }

    /** 获取局域网内所有设备（连接WiFi的状况下） */
    private void getDevices() {
        if (!TextUtils.isEmpty(new EspWifiAdminSimple(this).getWifiConnectedSsid())) {
            KappUtils.getLocalIP(context);
            deviceUtil = new DeviceUtil(context, new DeviceUtil.OnReceiver() {
                @Override
                public void refreshData(List<DeviceNameAndIPBean> _list) {
                    list.clear();
                    list.addAll(_list);
                    handler.sendEmptyMessage(0);
                }
            });
            deviceUtil.startSearch();
        }
    }

    Handler handler = new android.os.Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    tv_number.setText("局域网内已发现设备 (" + list.size() + ")");//没有失败应该显示一个哭脸并加载另一个跳转页面
                    adapter.notifyDataSetChanged();
                    break;
                case 4:
                    HHDLog.v("H4：握手成功");
                    HHDLog.e("给PMS名称");
                    Config.serverHostName = selectBean.getName();
                    socketService.splitInstruction(Config.BUF_SET_TIME, new DateUtil().getCurrentTime());
                    break;
                case 9:
                    HHDLog.v("H9：对时成功");
                    KappUtils.showToast(context, "连接成功");
                    closeDlg();
                    KappAppliction.state = 1;
                    DeviceFragment.isClearList = true;
                    startActivity(new Intent(context, MainActivity.class));
                    AddDevicesActivity.this.finish();
                    break;
                case Config.MSG_RE_BIND:
                    bindSocketService();
                    handler.sendEmptyMessage(Config.MSG_CHECK_BIND);
                    break;
                case Config.MSG_CHECK_BIND:
                    if (socketService == null) {//判断下确保已经绑定服务
                        handler.sendEmptyMessage(Config.MSG_RE_BIND);
                        HHDLog.w("判断是否已经绑定服务=" + (socketService == null));
                    } else {
                        //loadList();
                        HHDLog.w("判断是否已经绑定服务=" + (socketService == null));
                    }
                    break;
            }
            return false;
        }
    });

}

