package com.gzmelife.app.activity;

import java.util.ArrayList;
import java.util.List;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.espressif.iot.esptouch.demo_activity.EspWifiAdminSimple;
import com.gzmelife.app.KappAppliction;
import com.gzmelife.app.R;
import com.gzmelife.app.adapter.PMSWifiAdapter;
import com.gzmelife.app.device.Config;
import com.gzmelife.app.fragment.DeviceFragment;
import com.gzmelife.app.tools.DateUtil;
import com.gzmelife.app.tools.KappUtils;
import com.gzmelife.app.tools.MyLogger;
import com.gzmelife.app.tools.WifiUtil;

/**
 * 界面【直连模式】左边（添加新设备）
 */
@ContentView(R.layout.activity_add_device_by_pms_wifi)
public class AddDeviceByPMSWifiActivity extends BaseActivity {

    MyLogger HHDLog = MyLogger.HHDLog();

    @ViewInject(R.id.tv_title)
    TextView tv_title;
    @ViewInject(R.id.tv_title_left)
    TextView tv_title_left;

    @ViewInject(R.id.layout_no_data)
    View layout_no_data;

    @ViewInject(R.id.lv_pmsWifi)
    ListView lv_pmsWifi;

    /** 与wifi建立连接 */
    private TimeCountOut outTime;
    /** 与PMS建立连接 */
    private TimeCountOut outtime;
    /** 重连次数（失败一次+1，等于3时，失败3次，给出提示，连接失败） */
    private int failCount = 0;
    /** 缓存PMS设备名称 */
    private String tempName;

    /** 缓存PMS设备列表 2016 */
    private List<ScanResult> wifiList = new ArrayList<ScanResult>();
    /** 显示“PMS_”开头的适配器 2016 */
    private PMSWifiAdapter adapter;

    private Context context;

    //TODO 2016
    /**
     * Socket状态监听
     */
    @Override
    public void success(List<String> cookBookFileList, int status, int progress, int total) {
        HHDLog.v("status=" + status + "，progress=" + progress + "，total=" + total);
        switch (status) {
            case 4:
                handler.sendEmptyMessage(4);
                HHDLog.v("回调码=4，握手成功"+"，status=" + status);
                break;
            case 9:
                handler.sendEmptyMessage(9);
                HHDLog.v("回调码=9，对时功能成功"+"，status=" + status);
                break;
            default:
                break;
        }
    }
    @Override
    public void failure(int flag) {
        HHDLog.v("flag=" + flag);
        handler.sendEmptyMessage(3);
    }
    //TODO

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        context = this;
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        HHDLog.v("界面【直连模式】左边（添加新设备）");

        //TODO 2016
        bindSocketService();
        //TODO 2016

        handler.sendEmptyMessage(Config.MSG_RE_BIND);

        //接收PSM数据**********************************************************************************
//		socketTool.receiveMessage();
        //接收PSM数据**********************************************************************************
    }

    @Override
    protected void onPause() {
        super.onPause();

        //TODO 2016
        unbindSocketService();
        //TODO 2016

        if (outTime != null) {
            outTime.cancel();
            outTime = null;
        }
        if (outtime != null) {
            outtime.cancel();
            outtime = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*if (socketTool != null) {
            socketTool.closeSocket();
            socketTool = null;
        }*/
    }

    private void initView() {
        tv_title.setText("直连模式");
        tv_title_left.setVisibility(View.VISIBLE);
        tv_title_left.setText("添加新设备");
        adapter = new PMSWifiAdapter(context, wifiList, new PMSWifiAdapter.OnReceiver() {
            @Override
            public void onClick(final int position) {
                HHDLog.v("《1》点击="+wifiList.get(position).SSID);
                if (!WifiUtil.isEnable(context)) {
                    KappUtils.showToast(context, "WiFi被关闭，请开启后重试");
                    return;
                }

                showDlg();
                WifiUtil.connectWifi(wifiList.get(position).SSID, true);
                outTime = new TimeCountOut(1000 * 15, 1000, new OnEvent() {
                    @Override
                    public void onFinish() {
                        HHDLog.v("（要全为真）WiFi打开状态=" + (WifiUtil.getWifiInfo() != null) + "->" + WifiUtil.getWifiInfo()
                                + "。连接的WiFi是否不为空=" + (!TextUtils.isEmpty(new EspWifiAdminSimple(context).getWifiConnectedSsid())) + "->" + new EspWifiAdminSimple(context).getWifiConnectedSsid()
                                + "。WiFi是否和点击的相等=" + (WifiUtil.getWifiInfo().getSSID().equals("\"" + wifiList.get(position).SSID + "\"")) + "->" + WifiUtil.getWifiInfo().getSSID() + ("\"" + wifiList.get(position).SSID + "\""));
                        if (WifiUtil.getWifiInfo() != null
                                && !TextUtils.isEmpty(new EspWifiAdminSimple(context).getWifiConnectedSsid())
                                && WifiUtil.getWifiInfo().getSSID().equals("\"" + wifiList.get(position).SSID + "\"")) {
                            //KappUtils.showToast(context, "连接WiFi成功");
                            tempName = wifiList.get(position).SSID;
                            handler.sendEmptyMessage(2);
                        } else {
                            handler.sendEmptyMessage(1);
                        }
                    }

                    @Override
                    public void onTick(long millisUntilFinished) {
                        HHDLog.v("《2》每秒连接一次="+millisUntilFinished);
                        HHDLog.v("（要全为真）WiFi打开状态=" + (WifiUtil.getWifiInfo() != null)
                                + "。连接的WiFi是否不为空=" + (!TextUtils.isEmpty(new EspWifiAdminSimple(context).getWifiConnectedSsid())) + "->" + new EspWifiAdminSimple(context).getWifiConnectedSsid()
                                + "。WiFi是否和点击的相等=" + (WifiUtil.getWifiInfo().getSSID().equals("\"" + wifiList.get(position).SSID + "\"")) + "->" + WifiUtil.getWifiInfo().getSSID() + ("\"" + wifiList.get(position).SSID + "\""));

                        //TODO 出现BUG模块
                        if (WifiUtil.getWifiInfo() != null
                                && !TextUtils.isEmpty(new EspWifiAdminSimple(context).getWifiConnectedSsid())
                                && WifiUtil.getWifiInfo().getSSID().equals("\"" + wifiList.get(position).SSID + "\"")) {

                            //KappUtils.showToast(context, "连接WiFi成功");
                            HHDLog.v("连接WiFi成功");
                            HHDLog.v("《3》连接WiFi成功="+millisUntilFinished);
                            /*outTime.cancel();
                            outTime = null;*/

                            tempName = wifiList.get(position).SSID;
                            HHDLog.v("缓存WiFi名=" + tempName);
                            handler.sendEmptyMessage(2);
                        }
                    }
                });
                outTime.start();
            }
        });
        lv_pmsWifi.setAdapter(adapter);

        showDlg();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                while (!WifiUtil.isEnable(context)) {
                    HHDLog.v("线程_WiFi没打开，正在打开...");
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                WifiUtil.startScan();
                HHDLog.v("线程_扫描WiFi");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<ScanResult> wifiTempList = WifiUtil.getWifiList();

                for (int i = 0; i < wifiTempList.size(); i++) {
                    ScanResult bean = wifiTempList.get(i);
                    if (bean.capabilities.equals("[ESS]") && bean.SSID.startsWith("PMS_")) { // 无密码，且PMS_开头
                        wifiList.add(bean);
                        HHDLog.v("线程_加载WiFi适配器");
                    }
                }
                handler.sendEmptyMessage(0);
                Looper.loop();
            }
        }).start();
    }

    Handler handler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HHDLog.v("H0");
                    closeDlg();
                    if (wifiList.size() == 0) {
                        layout_no_data.setVisibility(View.VISIBLE);
                        lv_pmsWifi.setVisibility(View.GONE);
                        HHDLog.v("线程_没有WiFi");
                    } else {
                        layout_no_data.setVisibility(View.GONE);
                        lv_pmsWifi.setVisibility(View.VISIBLE);

                        adapter.notifyDataSetChanged();
                        HHDLog.v("线程_加载WiFi列表");
                    }
                    //TODO 已检代码
                    break;
                case 1:
                    HHDLog.v("H-4");
                    HHDLog.v("《4》连接失败");
                    wifiList.clear();
                    List<ScanResult> wifiTempList = WifiUtil.getWifiList();
                    for (int i = 0; i < wifiTempList.size(); i++) {
                        ScanResult bean = wifiTempList.get(i);
                        if (bean.capabilities.equals("[ESS]") && bean.SSID.startsWith("PMS1_")) { // 无密码，且PMS_开头
                            wifiList.add(bean);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    KappAppliction.state = 2;
                    KappUtils.showToast(context, "连接失败，请重新试");
                    break;
                case 2:
                    HHDLog.v("H-2");
                    HHDLog.v("《4》连接WiFi后");
                    if (outTime != null) {
                        outTime.cancel();
                        outTime = null;
                    }
                    showDlg();
                    failCount = 0;
                    Config.serverHostIp = Config.SERVER_HOST_DEFAULT_IP;
                    // 连接成功，跳到成功界面
                /*if (socketTool == null) {
                    socketTool = new SocketTool(context, new SocketTool.OnReceiver() {
                                @Override
                                public void onSuccess(List<String> cookBookFileList, int flag, int now, int all) {
                                    Config.SERVER_HOST_NAME = tempName;
                                    handler.sendEmptyMessage(1);
                                }

                                @Override
                                public void onFailure(int flag) {
                                    handler.sendEmptyMessage(3);
                                }
                            });
                }*/
                    // 倒计时10秒，10秒内没有指令与PMS连接成功，则给出提示，且去掉转圈
                    outtime = new TimeCountOut(10 * 1000, 1000, new OnEvent() {
                        @Override
                        public void onFinish() {
                            failCount = 2;
                            handler.sendEmptyMessage(3);
                        }
                        @Override
                        public void onTick(long millisUntilFinished) {
                            //MyLog.d("PMS指令连接倒计时：" + millisUntilFinished);
                            HHDLog.v("Socket连接PMS设备每秒计时="+ millisUntilFinished);
                            if (millisUntilFinished / 1000 % 3 == 0) {
                            /*new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    socketTool.initClientSocket();
                                    socketTool.splitInstruction(Config.bufConnect, null);*/
                                socketService.closeSocket();//TODO
                                socketService.firstConnect(); // 根据不同的ip，建立不同的socket
                                /*}
                            }).start();*/
                            }
                        }
                    });
                    outtime.start();
                    break;
                case 3:
                    HHDLog.v("3");
                    failCount++;
                    if (failCount == 3) {
                        KappAppliction.state = 2;
                        KappUtils.showToast(context, "连接失败，请重新试");
                        closeDlg();
                        if (outtime != null) {
                            outtime.cancel();
                            outtime = null;
                        }
                    }
                    break;
                case 4:
                    HHDLog.v("H4：握手成功");
                    HHDLog.e("给PMS名称");
                    Config.serverHostName = tempName;
                    socketService.splitInstruction(Config.BUF_SET_TIME, new DateUtil().getCurrentTime());
                case 9://对时成功
                    HHDLog.v("H9：对时成功");
                    if (outtime != null) {
                        outtime.cancel();
                        outtime = null;
                    }
                    KappUtils.showToast(context, "连接成功");
                    closeDlg();
                    KappAppliction.state=1;
                    DeviceFragment.isClearList = true;
                    startActivity(new Intent(context, MainActivity.class));
                    AddDeviceByPMSWifiActivity.this.finish();
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

                default:
                    HHDLog.v("H没有匹配");
                    break;
            }
            return false;
        }
    });
}
