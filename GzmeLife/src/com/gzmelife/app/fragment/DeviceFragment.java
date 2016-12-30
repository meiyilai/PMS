package com.gzmelife.app.fragment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.espressif.iot.esptouch.demo_activity.EspWifiAdminSimple;
import com.gzmelife.Status.smartPotStatu;
import com.gzmelife.app.KappAppliction;
import com.gzmelife.app.R;
import com.gzmelife.app.activity.AddDevicesActivity;
import com.gzmelife.app.activity.CookBookDetailActivity;
import com.gzmelife.app.activity.DeviceCenterActivity;
import com.gzmelife.app.activity.DeviceDetailActivity;
import com.gzmelife.app.activity.MainActivity;
import com.gzmelife.app.adapter.CookBookAdapter;
import com.gzmelife.app.adapter.DeviceCenterAdapter;
import com.gzmelife.app.bean.DeviceNameAndIPBean;
import com.gzmelife.app.dao.DevicesDAO;
import com.gzmelife.app.device.Config;
import com.gzmelife.app.tools.CountDownTimerUtil;
import com.gzmelife.app.tools.DateUtil;
import com.gzmelife.app.tools.DensityUtil;
import com.gzmelife.app.tools.KappUtils;
import com.gzmelife.app.tools.MyLogger;
import com.gzmelife.app.tools.SharedPreferenceUtil;
import com.gzmelife.app.tools.ShowDialogUtil;
import com.gzmelife.app.tools.WifiUtil;
import com.gzmelife.app.views.TipConfirmView;

/**
 * 界面【设备】
 */
@SuppressLint("InflateParams")
public class DeviceFragment extends Fragment {

    private static MyLogger HHDLog = MyLogger.HHDLog();

    private String TAG = "DeviceFragment";
    private TextView tv_title;
    private RadioButton rb_selfFile;
    private RadioButton rb_downFile;
    private Button btn_titleRight;
    private ImageView iv_titleLeft;
    private ImageView iv_titleRight;
    /** 设备内文件 */
    private ListView lv_file;
    /** 离线设备 */
    private ListView lv_pms;
    /** 指示条 2016 */
    private View view_selfFile;
    /** 指示条 2016 */
    private View view_downFile;
    /** 没有设备时提示界面 2016 */
    private View layout_no_device;
    /** 设备连接时填充界面 2016 */
    private View layout_connected;
    /** 历史连接设备 2016 */
    private View layout_devices;
    /** 警示框（进度条） 2016 */
    private AlertDialog dlg;
    /** 警示框（转圈） 2016 */
    private Dialog pDlg;
    /** true表示录波文件，false表示菜谱文件 */
    public static boolean fileFlag = true;
    /** 曾经连接的过的设备 2016 */
    private List<DeviceNameAndIPBean> deviceList = new ArrayList<DeviceNameAndIPBean>();
    /** 曾经连接的过的设备列表适配器 2016 */
    private DeviceCenterAdapter deviceAdapter;
    /** 当前连接设备名称和IP 2016 */
    private DeviceNameAndIPBean connectDeviceBean;
    /** 菜谱文件列表 */
    private List<String> downFileList = new ArrayList<String>();
    /** 录波文件列表 */
    private List<String> selfFileList = new ArrayList<String>();
    /** 菜谱列表适配器 2016 */
    private CookBookAdapter downAdapter;
    /** 录波文件适配器 2016 */
    private CookBookAdapter selfAdapter;
    /** 保存菜谱的名称 2016 */
    public static String saveFileName;
    /** 当前删除菜谱的名称 2016 */
    private String deleteFileName = "";
    /** 与WiFi建立连接超时时间 */
    private TimeCountOut outTimeWifi;
    /** 与PMS建立连接超时时间 */
    private TimeCountOut outTimePMS;
    /** 标记是否首次连接：true=是首次 */
    private boolean isFirst = true;
    /** 广播 2016 */
    private LocalBroadcastManager broadcastManager;
    /** 设备状态 2016 */
    public String socket = "";
    /** 是否正在下载菜谱：true=是 2016 */
    public boolean downLoadState = false;
    private final int MOBILE_QUERY = 123;
    private final int MOBILE_STOP = 10000;

    /** 标记是否清空两个列表 2016 */
    public static boolean isClearList = false;

    private EffectInVisiableHandler mtimeHandler;

    public boolean isConnect = false;
    private int time = 0;

    private Context context;
    private MainActivity mainActivity;

    @Override//1
    public void onAttach(Context context) {
        super.onAttach(context);
        HHDLog.v("");
    }
    @Override//2
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HHDLog.v("");
    }
    @Override//3
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HHDLog.v("");
        return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_device, null);
    }
    @Override//4
    public void onActivityCreated(Bundle savedInstanceState) {
        HHDLog.v("");
        super.onActivityCreated(savedInstanceState);
        //Log.i(TAG, "onActivityCreated-->");
        context = this.getActivity();
        mainActivity = (MainActivity) this.getActivity();
        initView();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(KappUtils.ACTION_PMS_STATUS);//20160919发送设备状态的广播
        // intentFilter.addCategory();
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        broadcastManager.registerReceiver(receiver, intentFilter);//20160919接收设备状态的广播
    }
    @Override//5
    public void onStart() {
        super.onStart();
        HHDLog.v("");
    }
    @Override//8
    public void onStop() {
        super.onStop();
        HHDLog.v("");
    }
    @Override//9
    public void onDestroyView() {
        super.onDestroyView();
        HHDLog.v("");
    }
    @Override//10
    public void onDestroy() {
        super.onDestroy();
        HHDLog.v("");
//        if (socketTool != null) {
//            socketTool.closeSocket();
//        }
//        Config.SERVER_HOST_NAME = "";

        broadcastManager.unregisterReceiver(receiver);
        if (outTimePMS != null) {
            outTimePMS.cancel();
            outTimePMS = null;
        }
        if (outTimeWifi != null) {
            outTimeWifi.cancel();
            outTimeWifi = null;
        }
    }
    @Override//11
    public void onDetach() {
        super.onDetach();
        HHDLog.v("");
    }
    @Override//7
    public void onPause() {
        super.onPause();

        HHDLog.v("");
        //TODO 2016
        /** 解绑服务 */
        mainActivity.unbindSocketService();
        //TODO 2016
        //Log.i(TAG, "onPause-->");
    }
    @Override//6
    public void onResume() {
        super.onResume();

        //TODO 2016
        /** 绑定服务 */
        mainActivity.bindSocketService();
        //TODO 2016
        HHDLog.v("界面【设备】");
        handler.sendEmptyMessage(Config.MSG_RE_BIND);
    }

    /** 加载设备、录波、菜谱列表 */
    private void loadList() {
        HHDLog.v("加载设备、录波、菜谱列表");
        //******************************************************************************************
        if (TextUtils.isEmpty(Config.serverHostName)) {   //20160920服务器（PMS设备）地址为空
            HHDLog.v("PMS设备的IP未赋值");
            showDeviceList();//20160920显示设备列表
            if (isFirst) {  //20160920首次连接
                isFirst = false;
                connectDeviceBean = SharedPreferenceUtil.getPmsInfo(context); // 获取上次连接设备，自动连接
                if (connectDeviceBean != null) {//20160914设备名称和ip不为空就连接
                    h.sendEmptyMessage(2);//开始转圈并连接设备（WiFi+Socket）
                }
            }
        } else {HHDLog.v("PMS设备的IP已赋值");
            tv_title.setText(Config.serverHostName);//20160920“标题”改为当前PMS设备名称
            btn_titleRight.setVisibility(View.VISIBLE);//20160920显示右边按钮
            iv_titleRight.setVisibility(View.GONE);//20160920隐藏右边图标
            layout_devices.setVisibility(View.GONE);//20160920隐藏设备列表
            layout_no_device.setVisibility(View.GONE);//20160920隐藏没有设备提示界面
            layout_connected.setVisibility(View.VISIBLE);//20160920显示录波/菜谱文件
            updatePmsStatus();////20160920更新PMS设备状态（左边图标）
            clearList();//20160916清空两个列表
            initSocketTool();//20160920加载Socket工具
            /*if (selfFileList.size() == 0 && downFileList.size() == 0) {//20160920录波&菜谱文件列表长度为0
                HHDLog.v("请求菜谱文件列表总数");
                fileFlag = true;//20160920录波文件
                getPMSSelfFileNum();//20160920获取录波文件数量//
            }*/
            if (selfFileList.size() == 0 && rb_selfFile.isChecked()) {//20160920如果录波列表为0而且为选中状态
                getPMSSelfFileNum();
                HHDLog.v("请求录波文件总数");
               /* final byte[] bufFilePath = {0x00};
                fileFlag = true;
                mainActivity.socketService.splitInstruction(Config.bufGetFileNum, bufFilePath);//20160920获取录波文件数量//*/
            } else if (rb_downFile.isChecked() && downFileList.size() == 0) {//20160920如果菜谱列表为0而且为选中状态
                getPMSDownFileNum();
                HHDLog.v("请求菜谱文件列表总数");
                //fileFlag = false;
            }
        }
        if (Config.flag == 2) {
            smartPotStatu smart = new smartPotStatu();
            if (smart.queryDirty()) {
                MenuFileRefrash();//加载对应菜谱列表
            }
        }
        //******************************************************************************************
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {//20160919接收设备状态的广播
        @Override
        public void onReceive(Context context, Intent intent) {
            HHDLog.v("");
            String action = intent.getAction();
            if (action.equals(KappUtils.ACTION_PMS_STATUS)) {
                updatePmsStatus();
            }
        }
    };

    /** 加载视图 */
    private void initView() {
        HHDLog.v("加载视图");
        //HHDLog.v("");
        tv_title = (TextView) getView().findViewById(R.id.tv_title);
        rb_selfFile = (RadioButton) getView().findViewById(R.id.rb_selfFile);
        rb_downFile = (RadioButton) getView().findViewById(R.id.rb_downFile);
        iv_titleLeft = (ImageView) getView().findViewById(R.id.iv_titleLeft);
        iv_titleRight = (ImageView) getView().findViewById(R.id.iv_titleRight);
        btn_titleRight = (Button) getView().findViewById(R.id.btn_titleRight);
        layout_no_device = getView().findViewById(R.id.layout_no_device);
        layout_connected = getView().findViewById(R.id.layout_connected);
        layout_devices = getView().findViewById(R.id.layout_devices);
        view_selfFile = getView().findViewById(R.id.view_selfFile);
        view_downFile = getView().findViewById(R.id.view_downFile);
        int normal = DensityUtil.dip2px(context, 12);//20160913左右上角按钮应统一封装在Bar上
        iv_titleLeft.setPadding(normal, normal, normal, normal);
        iv_titleRight.setImageResource(R.drawable.icon01);
        lv_file = (ListView) getView().findViewById(R.id.lv_file);
        lv_pms = (ListView) getView().findViewById(R.id.lv_pms);
        deviceAdapter = new DeviceCenterAdapter(context, deviceList);//20160920设置设备中心适配器数据
        lv_pms.setAdapter(deviceAdapter);//20160920添加数据到列表
        lv_pms.setOnItemClickListener(new OnItemClickListener() {//20160919设备列表（短）点击事件
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connectDeviceBean = deviceList.get(position);
                h.sendEmptyMessage(2);
            }
        });
        lv_pms.setOnItemLongClickListener(new OnItemLongClickListener() {//20160919设备列表（长）点击事件
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deletePMSDevice(position);
                return true;
            }
        });

        rb_selfFile.setOnCheckedChangeListener(new OnCheckedChangeListener() {//20160919录波文件点击事件
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    HHDLog.v("点击加载“录波文件”");
                    Config.flag = 1;
                    rb_selfFile.setTextColor(Color.parseColor("#ff5b80"));
                    view_selfFile.setVisibility(View.VISIBLE);
                    lv_file.setAdapter(selfAdapter);
                    selfFileList.clear();
                    mainActivity.showDialog();
                    byte[] bufFilePath = {0x00};//20161212
                    fileFlag = true;
                    selfFileList.clear();
                    if (Config.BUF_GET_FILE_NUM != null && bufFilePath != null && !bufFilePath.equals("") && !Config.BUF_GET_FILE_NUM.equals("")) {
                        try {
                            mainActivity.socketService.splitInstruction(Config.BUF_GET_FILE_NUM, bufFilePath);//TODO F3 00 00 菜谱文件
                        } catch (Exception e) {
                            //
                        }
                    }
                } else {
                    rb_selfFile.setTextColor(Color.parseColor("#3f3f3f"));
                    view_selfFile.setVisibility(View.INVISIBLE);
                }
            }
        });
        rb_downFile.setOnCheckedChangeListener(new OnCheckedChangeListener() {  //20160919菜谱文件点击事件
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Config.flag = 2;
                    HHDLog.v("点击加载“菜谱文件”");
                    rb_downFile.setTextColor(Color.parseColor("#ff5b80"));
                    view_downFile.setVisibility(View.VISIBLE);
                    if (downAdapter == null) {
                        downAdapter = new CookBookAdapter(context, downFileList, new CookBookAdapter.OnReceiver() {
                            @Override
                            public void onDownload(int position) {
                                Message msg = new Message();
                                msg.what = 3;
                                msg.arg1 = position;
                                msg.obj = "1";
                                h.sendMessage(msg);
                            }

                            @Override
                            public void onDelete(int position) {
                                deletePMSFile(1, position);
                            }
                        });
                    }
                    lv_file.setAdapter(downAdapter);
                    downFileList.clear();
                    mainActivity.showDialog();
                    final byte[] bufFilePath = {0x01};
                    fileFlag = false;
                    downFileList.clear();
                    mainActivity.socketService.splitInstruction(Config.BUF_GET_FILE_NUM, bufFilePath);//TODO F3 00 菜谱文件
                } else {
                    rb_downFile.setTextColor(Color.parseColor("#3f3f3f"));
                    view_downFile.setVisibility(View.INVISIBLE);
                }
            }
        });

        selfAdapter = new CookBookAdapter(context, selfFileList,
                new CookBookAdapter.OnReceiver() {
                    @Override
                    public void onDownload(int position) {
                        Message msg = new Message();
                        msg.what = 3;
                        msg.arg1 = position;
                        msg.obj = "2";
                        h.sendMessage(msg);
                    }

                    @Override
                    public void onDelete(int position) {
                        deletePMSFile(0, position);
                    }
                });
        lv_file.setAdapter(selfAdapter);

        btn_titleRight.setText("切换");
        btn_titleRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HHDLog.v("右边按钮");
                startActivity(new Intent(context, DeviceCenterActivity.class));
            }
        });

        iv_titleRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HHDLog.v("右边图标");
                Intent intent = new Intent(context, AddDevicesActivity.class);
                startActivity(intent);
            }
        });

        iv_titleLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HHDLog.v("左边按钮");
                /*Intent intent = new Intent(context, DeviceDetailActivity.class);
                startActivity(intent);*/
                if (!TextUtils.isEmpty(Config.serverHostName)) { // 未连接，点击无效
                    startActivityForResult(new Intent(context, DeviceDetailActivity.class), 101);
                }
            }
        });
    }







//**************************************************************************************************
    /**
     * 监听Socket连接成功状态
     *
     * @param cookBookFileList  菜谱列表（遍历时）
     * @param status            0：默认值（不处理），
     *                          1：下载成功，
     *                          2：下载失败，
     *                          3：下载数据的百分比，
     *                          4：连接成功，
     *                          5：删除文件成功，
     *                          6：获取设备状态成功，
     *                          7：传文件到智能灶成功，
     *                          8：传文件到智能灶的百分比，
     *                          9：对时功能。
     * @param progress          进度条当前值
     * @param total             进度条总进度
     */
    public void success(List<String> cookBookFileList, int status, int progress, int total) {
        HHDLog.v("");
        //if (!mainActivity.socketService.isConnect()) {
        switch (status) {
            case 0:
                HHDLog.v("0不处理");
                mainActivity.closeDlg();
                if (outTimeWifi != null) {
                    outTimeWifi.cancel();
                    outTimeWifi = null;
                }
                if (fileFlag) {
                    selfFileList.clear();
                    if (cookBookFileList != null) {
                        selfFileList.addAll(cookBookFileList);
                    } else {
                        KappUtils.showToast(context, "暂无录波文件");
                    }
                    selfAdapter.notifyDataSetChanged();//20160919更新列表数据
                } else {
                    downFileList.clear();
                    if (cookBookFileList != null) {
                        downFileList.addAll(cookBookFileList);
                    } else {
                    }
                    downAdapter.notifyDataSetChanged();
                }
                //如果不发起终止的消息,每次点击切换都会10秒过后会断开,若发起终止 则进入自动连接时会退出已连接的,要求重新连接
//								mtimeHandler.removeMessages(MOBILE_QUERY);
                break;
            case 1:
                HHDLog.v("1下载成功");
                handler.sendEmptyMessage(1);
                break;
            case 2:
                HHDLog.v("2下载失败");
                break;
            case 3:
                HHDLog.v("3下载数据的百分比");
                ShowDialogUtil.setProgress(progress, total);
                break;
            case 4:
                HHDLog.v("4连接成功");
                handler.sendEmptyMessage(2);//执行对时并更新设备在线图标
                break;
            case 5:
                HHDLog.v("5删除文件成功");
                handler.sendEmptyMessage(5);
                break;
            case 6:
                HHDLog.v("6获取设备状态成功");
                // 发送广播，四个主界面左上角图标变更.首次连接成功查询状态，之后每次心跳成功后发送查询
                Intent intent = new Intent();
                intent.setAction(KappUtils.ACTION_PMS_STATUS);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                Intent mIntent = new Intent("aaa");
                mIntent.putExtra("yaner", "发送广播，相当于在这里传送数据");
                getActivity().sendBroadcast(mIntent);// 发送广播

                break;
            case 9://F2 00
                HHDLog.v("9对时功能成功");
                handler.sendEmptyMessage(9);
                // 发送广播，四个主界面左上角图标变更.首次连接成功查询状态，之后每次心跳成功后发送查询
                //System.out.print("----9对时功能成功----");
                //onResume();//没必要重新绑定服务
                //loadList();//TODO TODO TODO 这里逻辑要处理好（什么情况加载什么列表）
                break;
        }
        if (status != 5 && !TextUtils.isEmpty(deleteFileName)) {
            HHDLog.v("5删除文件失败");
            handler.sendEmptyMessage(3);//删除文件失败
        }
        //}
    }
    /**
     * 监听Socket连接失败状态
     *
     * @param flag  0：默认值，
     *              -1：下载文件大小=0。
     */
    public void failure(int flag) {
        HHDLog.v("");
        //if (!mainActivity.socketService.isConnect()) {
            if (!TextUtils.isEmpty(deleteFileName)) {
                handler.sendEmptyMessage(3);
            }
            switch (flag) {
                case -1:
                    handler.sendEmptyMessage(flag);
                    break;
                default:
                    Config.serverHostName = "";
                    handler.sendEmptyMessage(flag);
                    break;
            }
        //}
    }

//**************************************************************************************************

    private void connectPMS() {//20160919连接设备
        HHDLog.v("连接PMS设备");
        //Log.i(TAG, "connectPMS-->");
        // 拿到PMS信息，判断当前网络下是否有那个WiFi，有的话则连接上WiFi，然后判断PMS的ip是否存在，然后与PMS连接
        boolean isOpenWifi = WifiUtil.openWifi(context);//20160919标记WiFi开关状态
        if (!isOpenWifi) {
            while (!WifiUtil.isEnable(context)) {//20160919wifi未开启
                HHDLog.v("开启WiFi中");
                try {
                    Thread.sleep(1500);//20160919暂停执行
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            WifiUtil.startScan();//20160919扫描WiFi
            HHDLog.v("扫描WiFi");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //20160919在设备WiFi局域网（获取WiFi信息）
        List<WifiConfiguration> wifiTempList = WifiUtil.getWifiConfigurationList();//20160919已连过WiFi

        boolean isExist = false;//20160919标记wifi是否存在
        for (int i = 0; i < wifiTempList.size(); i++) {
            WifiConfiguration wifiConfiguration = wifiTempList.get(i);//获取已连接过WiFi信息
            HHDLog.v("获取已连接过的WiFi="+wifiConfiguration.SSID);
            String str = wifiConfiguration.SSID.substring(1, wifiConfiguration.SSID.length() - 1);//20160919去掉WiFi名称的引号部分

            //20160919通过直接连接
            if (str.equals(connectDeviceBean.getWifiName())) { // 上次连接的WiFi和当前可以连接WiFi对比
                HHDLog.v("对比WiFi="+str+"<->"+connectDeviceBean.getWifiName());
                isExist = true;
                WifiUtil.connectWifi(wifiConfiguration, true);//20160919连接选定网络，禁用其他网络被
                //Log.i(TAG, "connectPMS-->WifiUtil.connectWifi(wifiConfiguration, true)-->wifiName:;" + connectDeviceBean.getWifiName());
                //Log.i(TAG, "h.sendEmptyMessage(0);");
                h.sendEmptyMessage(0);  //15秒钟内提示连接WiFi成功//获取IP并10秒钟内无指令提示连接失败
                break;
            }

            //20160919通过路由器连接
            if (i == wifiTempList.size() - 1) {
                List<ScanResult> wifiScanList = WifiUtil.getWifiList();
                for (int j = 0; j < wifiScanList.size(); j++) {
                    ScanResult scanResult = wifiScanList.get(j);
                    HHDLog.v("扫描到的WiFi="+scanResult.SSID);
                    if (scanResult.SSID.equals(connectDeviceBean.getWifiName()) && scanResult.capabilities.contains("[ESS]")) {//20160919contains：是否包含//ESS：扩展服务集（ESS）
                        isExist = true;
                        WifiUtil.connectWifi(scanResult.SSID, true);
                        HHDLog.v("连接WiFi");
                        HHDLog.v("WiFi是否相同=" + scanResult.SSID+"<->"+connectDeviceBean.getWifiName()+"<->"+scanResult.capabilities.contains("[ESS]"));
                        h.sendEmptyMessage(0);
                        break;
                    }
                }
            }
        }

        if (!isExist) {
            KappUtils.showToast(context, "WiFi：" + connectDeviceBean.getWifiName() + "不在范围内或已被清除配置信息");
            mainActivity.closeDlg();
            HHDLog.v("不在范围内");
        }
    }

    /**
     * （长）点击删除设备
     * @param position list标记
     */
    private void deletePMSDevice(int position) {//20160919（长）点击删除设备
        HHDLog.v("（长）点击删除设备");
        final DeviceNameAndIPBean bean = deviceList.get(position);//20160919列表上选中的设备信息
        TipConfirmView.showConfirmDialog(context, "是否确认删除Wifi为\"" + bean.getWifiName() + "\"网络下的设备\"" + bean.getName() + "\"？", new OnClickListener() {
            @Override
            public void onClick(View v) {
                TipConfirmView.dismiss();
                if (new DevicesDAO().deleteDeviceById(bean.getId())) {//20160919根据数据库PMS的Id删除设备
                    showDeviceList();
                    DeviceNameAndIPBean bean2 = SharedPreferenceUtil.getPmsInfo(context);//20160919存储在SP上的设备信息
                    if (bean.getName() != null && bean2.getName() != null) {//20160919
                        if (bean.getName().equals(bean2.getName())//20160919对比设备名称（选中对比SP）
                                && bean.getIp().equals(bean2.getIp())//20160919对比设备IP
                                && bean.getWifiName().equals(bean2.getWifiName())) //20160919对比设备wifi名
                        {
                            bean2 = new DeviceNameAndIPBean();
                            SharedPreferenceUtil.setPmsInfo(context, bean2);//20160919存储SP的设备信息置空
                            KappUtils.showToast(context, "删除成功");
                        }
                    }

                } else {
                    KappUtils.showToast(context, "删除失败");
                }
            }
        });
    }

    /**
     * 删除选中菜谱
     * @param flag 标记是录波或菜谱列表
     * @param position list标记
     */
    private void deletePMSFile(final int flag, int position) {
        HHDLog.v("");
        final String fileName;
        if (flag == 0) {
            fileName = selfFileList.get(position);
        } else if (flag == 1) {
            fileName = downFileList.get(position);
        } else {
            KappUtils.showToast(context, "flag传值错误");
            return;
        }
        TipConfirmView.showConfirmDialog(context, "是否确认删除文件-\"" + fileName
                + "\"？", new OnClickListener() {
            @Override
            public void onClick(View v) {
                TipConfirmView.dismiss();
                deleteFileName = fileName;
                byte[] arr = null;
                try {
                    arr = deleteFileName.getBytes("gb2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                mainActivity.showDialog();
                if (flag == 0) {
                    //socketTool.splitInstruction(Config.bufDelSelfFile, arr);
                    mainActivity.socketService.splitInstruction(Config.BUF_DEL_SELF_FILE, arr);
                } else if (flag == 1) {
                    //socketTool.splitInstruction(Config.bufDelDownFile, arr);
                    mainActivity.socketService.splitInstruction(Config.BUF_DEL_DOWN_FILE, arr);
                }
            }
        });
    }

    Handler h = new Handler(new Callback() {
        @Override
        public boolean handleMessage(final Message msg) {
            switch (msg.what) {
                case 0://20160914自动扫描连接设备（//通过直接连接或路由器连接）// TODO 已检代码
                    HHDLog.v("0");
                    OnEvent onEvent = new OnEvent() {
                        @Override
                        public void onTick(long millisUntilFinished) {//20160919发起倒计时（millisUntilFinished倒计时的剩余时间）
                            //MyLog.d("onTick ." + millisUntilFinished);
                            if (WifiUtil.getWifiInfo() != null//20160919连接网络信息不为空
                                    && connectDeviceBean != null//20160919设备的名称和IP不为空
                                    && !TextUtils.isEmpty(new EspWifiAdminSimple(context).getWifiConnectedSsid())//20160919SSID不为空//获取活跃状态或在建立中WiFi的SSID
                                    && WifiUtil.getWifiInfo().getSSID().equals("\"" + connectDeviceBean.getWifiName() + "\"")) {    //20160919连接的WiFi和设备的WiFi相同
                                //KappUtils.showToast(context, "连接WiFi成功");
                                HHDLog.v("连接WiFi成功，耗时=" + millisUntilFinished);
                                h.sendEmptyMessage(1);//20160919获取IP并10秒钟内无指令提示连接失败
                            }
                        }

                        @Override
                        public void onFinish() {//20160919倒计时结束后会调用onFinish，倒计时结束后需要执行的操作
                            Log.i(TAG, "h-->0-->" + "onFinish .");
                            if (WifiUtil.getWifiInfo() != null
                                    && connectDeviceBean != null
                                    && !TextUtils.isEmpty(new EspWifiAdminSimple(context).getWifiConnectedSsid())
                                    && WifiUtil.getWifiInfo().getSSID().equals("\"" + connectDeviceBean.getWifiName() + "\"")) {
                                //KappUtils.showToast(context, "连接WiFi成功");
                                HHDLog.v("连接WiFi成功，耗时=");
                                iv_titleLeft.setImageResource(R.drawable.icon04);//20160919图标
                                Log.i(TAG, "h.sendEmptyMessage(1);");
                                h.sendEmptyMessage(1);//20160919获取IP并10秒钟内无指令提示连接失败
                            } else {
                                if (connectDeviceBean != null) {//20160919倒计时结束后没获取到所需信息则连接失败
                                    KappUtils.showToast(context, "连接WiFi失败");
                                    connectDeviceBean = null;
                                }
                            }
                        }
                    };
                    outTimeWifi = new TimeCountOut(1000 * 15, 1000, onEvent);//20160919重新发起计时
                    outTimeWifi.start();
                    break;// TODO 已检代码
                case 1: //20160919获取本机IP// TODO 已检代码
                    HHDLog.v("1");
                    //Log.i(TAG, "h-->1-->");
                    KappUtils.getLocalIP(context);//20160919获取本机IP
                    if (outTimeWifi != null) {
                        outTimeWifi.cancel();//20160919取消计时
                        outTimeWifi = null;
                    }
                    Config.serverHostIp = connectDeviceBean.getIp();//20160919给设备赋值真正IP地址
                    HHDLog.v("PMS的IP="+Config.serverHostIp);
                    outTimePMS = new TimeCountOut(10 * 1000, 1000, new OnEvent() { // 倒计时10秒，10秒内没有指令与PMS连接成功，则给出失败提示，且去掉转圈
                        @Override
                        public void onFinish() {
                            handler.sendEmptyMessage(0);//20160914提示连接失败(10秒钟后没指令提示连接失败)
                        }

                        @Override
                        public void onTick(long millisUntilFinished) {}
                    });

                    initSocketTool();//20160919加载连接设备的数据
                    //socketTool.firstConnect(); // 首次连接：连接Socket、发F8 00……
                    mainActivity.socketService.firstConnect(); // TODO 1
                    outTimePMS.start();//20160919开始倒计时
                    break;// TODO 已检代码
                case 2: //20160919点击设备列表连接设备
                    Log.i(TAG, "h-->2-->");
                    mainActivity.showDialog();//20160919显示转圈
                    connectPMS();//20160919连接设备
                    break;
                case 3:
                    Log.i(TAG, "h-->3-->");
                    /** 20161010暂停下载 */
                    pDlg = ShowDialogUtil.getShowDialog(mainActivity, R.layout.dialog_progressbar_2, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, 0, 0, false, View.VISIBLE);
                    String s = (String) msg.obj;
                    if ("1".equals(s)) {
                        downloadFromPMS(1, msg.arg1);
                    } else if ("2".equals(s)) {
                        downloadFromPMS(0, msg.arg1);
                    }
                    break;
            }
            return false;
        }
    });

//    /** 显示警示框 */
//    public void showDialog() {//TODO 已检代码
//        HHDLog.v("显示警示框");
//        if (null != this && null != dlg && !dlg.isShowing()) {
//            dlg.show();
//        } else if (null != this && null == dlg) {
//            dlg = ShowDialogUtil.getShowDialog(mainActivity, R.layout.dialog_progressbar, 0, 0, true);
//        }
//    }//TODO 已检代码
//
//    /** 关闭警示框 */
//    public void closeDlg() {//TODO 已检代码
//        HHDLog.v("关闭警示框");
//        if (null != this && null != dlg && dlg.isShowing()) {
//            dlg.dismiss();
//        }
//    }//TODO 已检代码

    /** 关闭（进度条）警示框 */
    private void closePDlg() {//TODO 已检代码
        HHDLog.v("关闭（进度条）警示框");
        if (pDlg != null && pDlg.isShowing()) {
            pDlg.dismiss();
        }
    }//TODO 已检代码

    Handler handler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == -1 && pDlg != null && pDlg.isShowing()) {
                KappUtils.showToast(context, "文件不存在或其他异常，获取到文件大小为0");
            }
            switch (msg.what) {
                case -1:// TODO 已检代码
                    break;// TODO 已检代码
                case 0: //20160914连接失败业务 TODO 已检代码
                    if (pDlg != null && pDlg.isShowing()) {
                        KappUtils.showToast(context, "文件下载失败");
                    }
                    if (connectDeviceBean != null) {
                        if (outTimePMS != null) {
                            outTimePMS.cancel();
                            outTimePMS = null;
                        }
                        HHDLog.v("与PMS的指令连接失败,清除connectDeviceBean");
                        Config.isConnect = false;//20160913设备离线
                        KappUtils.showToast(context, "与PMS的指令连接失败");
                        mainActivity.closeDlg();
                        closePDlg();
                        connectDeviceBean = null;//20160919连接设备名称和IP置空
                        Config.serverHostName = "";//20160919服务器名称置空
                        updatePmsStatus();//20160919更新离（在）线图标
                    }
                    if (TextUtils.isEmpty(Config.serverHostName)) {
                        showDeviceList();//20160919显示（之前连接过）设备列表
                    }
                    if (dlg != null)
                        dlg.dismiss();
                    break;// TODO 已检代码
                case 1://20160914下载成功业务 TODO 已检代码
                    closePDlg();
                    KappUtils.showToast(context, "下载成功");
                    downLoadState = false;
                    Intent intent = new Intent(context, CookBookDetailActivity.class);
                    intent.putExtra("filePath", DeviceFragment.saveFileName);
                    DeviceFragment.saveFileName = "";
                    startActivity(intent);
                    break;// TODO 已检代码
                case 2: // PMS连接成功//TODO 已检代码
                    //KappUtils.showToast(context, "与PMS连接成功");
                    HHDLog.v("H2：握手成功");
                    try {
                        HHDLog.e("给PMS名称");
                        if (connectDeviceBean != null) {
                            Config.serverHostName = connectDeviceBean.getName();
                            connectDeviceBean = null;
                        }
                        //socketTool.splitInstruction(Config.bufSetTime, new DateUtil().getCurrentTime());
                        mainActivity.socketService.splitInstruction(Config.BUF_SET_TIME, new DateUtil().getCurrentTime());//F2 00 TODO 应该延迟0.2秒
                    } catch (Exception e) {
                        //
                    }
                    break;//TODO 已检代码
                case 3: // 删除文件失败 TODO 已检代码
                    deleteFileName = "";
                    KappUtils.showToast(context, "文件删除失败");
                    break;//TODO 已检代码
                case 5: // 删除文件成功 TODO 已检代码
                    deleteFileName = "";
                    KappUtils.showToast(context, "文件删除成功");
                    if (rb_selfFile.isChecked()) {
                        getPMSSelfFileNum();
                    } else {
                        getPMSDownFileNum();
                        System.out.print("----请求菜谱文件总数110----");
                    }
                    break;//TODO 已检代码
                case 9://TODO 20161223 加
                    HHDLog.v("H4：握手成功");
                    HHDLog.v("H9：对时成功");
                    if (outTimeWifi != null) {
                        outTimeWifi.cancel();
                        outTimeWifi = null;
                    }
                    KappUtils.showToast(context, "连接成功");
                    KappAppliction.state = 1;
                    Config.isConnect = true;//20160913设备在线
                    updatePmsStatus();
                    loadList();
                    break;
                case Config.MSG_RE_BIND:
                    mainActivity.bindSocketService();
                    handler.sendEmptyMessage(Config.MSG_CHECK_BIND);
                    break;
                case Config.MSG_CHECK_BIND:
                    if (mainActivity.socketService == null) {//判断下确保已经绑定服务
                        handler.sendEmptyMessage(Config.MSG_RE_BIND);
                        HHDLog.w("判断是否已经绑定服务" + (mainActivity.socketService == null));
                    } else {
                        loadList();
                    }
                    break;
            }
            return false;
        }
    });

    /** 显示（之前连接过）设备列表 */
    private void showDeviceList() { //20160919显示（之前连接过）设备列表  // TODO 已检代码块
        HHDLog.v("显示（之前连接过）设备列表");
        tv_title.setText("我的设备中心");//20160919“标题”改变为“我的设备中心”
        btn_titleRight.setVisibility(View.GONE);//20160919隐藏“保存”按钮
        iv_titleRight.setVisibility(View.VISIBLE);//20160920右上角图标看见
        layout_connected.setVisibility(View.GONE);//20160920隐藏录波/菜谱文件
        updatePmsStatus();//20160920更新PMS设备（图标）状态
        List<DeviceNameAndIPBean> deviceListTemp = new DevicesDAO().getAllDevices();//20160920查询所有PMS设备

        if (deviceListTemp == null || deviceListTemp.size() == 0) { // 本地没有设备数据
            HHDLog.v("本地数据库没有PMS设备");
            layout_no_device.setVisibility(View.VISIBLE);//20160920显示没有设备界面
            layout_devices.setVisibility(View.GONE);//20160920隐藏设备列表界面
        } else {
            layout_no_device.setVisibility(View.GONE);
            layout_devices.setVisibility(View.VISIBLE);
            deviceList.clear();//20160920清空设备列表
            deviceList.addAll(deviceListTemp);//20160920加载设备列表数据
            HHDLog.v("本地数据库的PMS设备个数为="+String.valueOf(deviceListTemp.size()));
            deviceAdapter.notifyDataSetChanged();//20160920更新设备中心适配器数据
        }
    }   // TODO 已检代码块

    /**
     * 下载选中菜谱
     * @param flag 标记是录波或菜谱列表
     * @param position list标记
     */
    private void downloadFromPMS(int flag, int position) {
        HHDLog.v("下载选中菜谱");
        try {
            if (flag == 0) { // PMS内的录波
                saveFileName = selfFileList.get(position);
            } else { // 1
                saveFileName = downFileList.get(position);
            }
            downLoadState = true;
            byte[] arr = null;
            try {
                arr = saveFileName.getBytes("gb2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            byte[] bs = new byte[arr.length + 1]; // 菜谱
            for (int i = 0; i < arr.length + 1; i++) {
                if (i == 0) {
                    if (flag == 0) {
                        bs[i] = 0x00;
                    } else {
                        bs[i] = 0x01;
                    }
                } else {
                    bs[i] = arr[i - 1];
                }
            }
            //socketTool.splitInstruction(Config.bufFileLenth, bs);
            mainActivity.socketService.splitInstruction(Config.BUF_FILE_LENTH, bs);//F4 00
        } catch (Exception e) {
            closePDlg();
        }
    }

    /** 清空两个列表 */
    private void clearList() {  // TODO 已检代码块
        HHDLog.v("清空两个列表");
        //Log.i(TAG, "clearList-->");
        if (isClearList) {
            isClearList = false;
            //socketTool = null;// TODO
            rb_selfFile.setChecked(true);//20160916录波文件为选中状态
            selfFileList.clear();//20160916清空录波文件列表
            downFileList.clear();//20160916清空菜谱文件列表

            DeviceNameAndIPBean bean = new DeviceNameAndIPBean();
            bean.setName(Config.serverHostName);
            bean.setWifiName(new EspWifiAdminSimple(context).getWifiConnectedSsid());
            bean.setIp(Config.serverHostIp);
            // 更新上次所连接的设备信息
            SharedPreferenceUtil.setPmsInfo(context, bean);
            new DevicesDAO().save(bean); // 若本地（20160919数据库）没有该连接数据，则新增保存
        }
    }   // TODO 已检代码块

    /** 更新PMS设备状态//应该改到success方法中（类似音乐播放器的播放按钮状态） */
    private void updatePmsStatus() {    //20160919更新PMS设备状态// TODO 已检代码块
        HHDLog.v("更新PMS设备状态");
        Log.i(TAG, "updatePmsStatus-->");
        if (TextUtils.isEmpty(Config.serverHostName)) {//20160914服务器地址为空则为离线状态
            iv_titleLeft.setImageResource(R.drawable.icon05);//20160914离线图标
        } else {
            iv_titleLeft.setImageResource(R.drawable.icon04);//20160914在线图标
        }
    }   // TODO 已检代码块

    /** 请求菜谱列表总数 */
    private void getPMSDownFileNum() {  //TODO 已检代码
        HHDLog.v("请求菜谱列表总数");
        fileFlag = false;//20160920显示录波文件列表
        final byte[] bufFilePath = {0x01};//20160920查询录波文件列表F3 01
        downFileList.clear();//20160920清空录波文件列表
        mainActivity.socketService.splitInstruction(Config.BUF_GET_FILE_NUM, bufFilePath);//20160920发送指令（“F3 01”查询文件数量）
    }//TODO 已检代码

    /** 请求录波列表总数 */
    private void getPMSSelfFileNum() { // 录波文件 TODO 已检代码
        HHDLog.v("请求录波列表总数");
        fileFlag = true;//20160920录波文件
        final byte[] bufFilePath = {0x00};//20160920数据长度
        selfFileList.clear();//20160920清空录波文件列表
        mainActivity.socketService.splitInstruction(Config.BUF_GET_FILE_NUM, bufFilePath);//20160920“F3 00”获取录波文件数量
    }//TODO 已检代码

    /** 时间计时器 */
    public class TimeCountOut extends CountDownTimerUtil {
        private OnEvent onEvent;
        public TimeCountOut(long millisInFuture, long countDownInterval, OnEvent onEvent2) {
            super(millisInFuture, countDownInterval);
            HHDLog.v("");
            this.onEvent = onEvent2;
        }
        @Override
        public void onFinish() {
            HHDLog.v("");
            mainActivity.closeDlg();
            if (onEvent != null) {
                onEvent.onFinish();//20160919倒计时结束后会调用onFinish，倒计时结束后需要执行的操作
            }
        }
        @Override
        public void onTick(long millisUntilFinished) {
            if (onEvent != null) {
                onEvent.onTick(millisUntilFinished);
                //HHDLog.v("时间计时器="+millisUntilFinished);
            }
        }
    }

    /** 自定义接口回调 */
    public interface OnEvent {//20160919触发事件
        void onFinish();//20160919倒计时结束后会调用onFinish，倒计时结束后需要执行的操作
        void onTick(long millisUntilFinished);//20160919发起倒计时（millisUntilFinished倒计时的剩余时间）
    }

    /** 接收上个界面的数据 */
    @Override
    public void onActivityResult(int arg0, int arg1, Intent arg2) {
        HHDLog.v("");
        if (arg1 == getActivity().RESULT_OK) {
            int position;
            switch (arg0) {
                case 101:
                    socket = arg2.getStringExtra("socket");
                    System.out.print("----socket----" + socket);
                    break;

                default:
                    //
                    break;
            }
        }
        super.onActivityResult(arg0, arg1, arg2);
    }

    /**
     * Fragment是否被隐藏
     * @param hidden 隐藏状态
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        HHDLog.v("隐藏状态=" + hidden);
        super.onHiddenChanged(hidden);
        //Log.i(TAG, "onHiddenChanged-->" + String.valueOf(hidden));
        if (hidden) {
            //
        } else {
            if (Config.flag == 2) {
                smartPotStatu smart = new smartPotStatu();
                if (smart.queryDirty()) {
                    MenuFileRefrash();//加载相应菜谱文件
                }
            }
        }
    }

    /** 加载相应菜谱文件 */
    private void MenuFileRefrash() {
        HHDLog.v("加载相应菜谱文件");
        rb_downFile.setTextColor(Color.parseColor("#ff5b80"));
        view_downFile.setVisibility(View.VISIBLE);
        if (downAdapter == null) {
            downAdapter = new CookBookAdapter(context, downFileList, new CookBookAdapter.OnReceiver() {
                @Override
                public void onDownload(int position) {
                    Message msg = new Message();
                    msg.what = 3;
                    msg.arg1 = position;
                    msg.obj = "1";
                    h.sendMessage(msg);
                    //downloadFromPMS(1, position);
                }

                @Override
                public void onDelete(int position) {
                    deletePMSFile(1, position);
                }
            });
        }

        lv_file.setAdapter(downAdapter);
        downFileList.clear();
        mainActivity.showDialog();

        final byte[] bufFilePath = {0x01};
        fileFlag = false;
        downFileList.clear();
        mainActivity.socketService.splitInstruction(Config.BUF_GET_FILE_NUM, bufFilePath);//F3 00

        /*new Thread(new Runnable() {//TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
            @Override
            public void run() {
                Looper.prepare();
                socketTool.closeSocket();
                socketTool.initClientSocket();
                socketTool.startHeartTimer();
                final byte[] bufFilePath = {0x01};
                fileFlag = false;
                downFileList.clear();
                //socketTool.splitInstruction(Config.bufGetFileNum, bufFilePath);
                mainActivity.socketService.splitInstruction(Config.bufGetFileNum, bufFilePath);//F3 00
                // getPMSDownFileNum();
                Looper.loop();
            }
        }).start();*/

    }


    /** 重新连接 */
    private class EffectInVisiableHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            HHDLog.v("");
            switch (msg.what) {
                case MOBILE_QUERY:
                    //
                    break;
                case MOBILE_STOP:
                    mtimeHandler.removeMessages(MOBILE_QUERY);
                    break;
            }
        }
    }
//**************************************************************************************************

    /**以下方法没用到******************************************************************************/
    /** 重置时间 */
    public void resetTime() {
        HHDLog.v("重置时间");
        mtimeHandler.removeMessages(MOBILE_QUERY);
        Message msg = mtimeHandler.obtainMessage(MOBILE_QUERY);
        mtimeHandler.sendMessageDelayed(msg, 10000);
    }
    private void getInfo() {
        PackageManager pm = getContext().getPackageManager();
        boolean permission = (PackageManager.FEATURE_WIFI.equals(pm.checkPermission("android.permission.ACCESS_WIFI_STATE", "packageName"))
        );
        if (permission) {
            connectPMS();
        } else {
            KappUtils.showToast(context, "没有WiFi权限");
        }
    }

    // TODO 已弃用
    /** 相当于绑定服务=初始化SocketTool */
    private void initSocketTool() {
        HHDLog.v("");
    }
    // TODO 已弃用

    /**
     * WiFi是否连接状态
     * @param context 当前上下文
     * @return 状态
     */
    public static boolean isWifiConnected(Context context) {
        HHDLog.v("WiFi是否连接状态");
        Log.i("DeviceFragment", "isWifiConnected-->");
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }

        return false;
    }
    //**************************************************************************************************

}