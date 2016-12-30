package com.gzmelife.app.device;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import com.gzmelife.app.KappAppliction;
import com.gzmelife.app.activity.CheckUpdateActivity;
import com.gzmelife.app.activity.CookBookDetailActivity;
import com.gzmelife.app.fragment.DeviceFragment;
import com.gzmelife.app.tools.DataUtil;
import com.gzmelife.app.tools.FileUtils;
import com.gzmelife.app.tools.KappUtils;
import com.gzmelife.app.tools.MyLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.gzmelife.app.device.Config.BUF_DOWN_FILE_CANCEL;
import static com.gzmelife.app.device.Config.BUF_FILE_CANCEL;


/**
 * Socket服务组件
 */
public class SocketService extends Service {

    MyLogger HHDLog = MyLogger.HHDLog();

    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private Context context;
    private Activity activity;
    /** 监听Socket状态 */
    private OnReceiver receiver;
    /** 计时器 */
    private static HeartTimeCount heartTimer;

    /** 当前是否正在发送命令：true=发送状态 */
    private boolean isSendCMD = false;
    /** 若指令发送不成功，3S后重发指令*/
    private int timeCnt = 0;
    /** 用于存储重发一帧数据 */
    private byte[] bufLastTemp;
    /** 记录重发次数，若超过3次则进行重连的操作*/
    private int MaxReCnt = 0;
    /** 是否超时（大于9秒）：false=没超时 */
    private boolean RecTimeOut = false;
    /** 标记接收数据的总长度 */
    private int num = 0;
    /** 校验数据时缓存一帧数据 */
    private byte[] bufTemp = new byte[256 * 256];
    /** 指令发送是否成功、空闲状态、进行指令重发或心跳：false=非空闲状态 */
    private boolean ConFalg = false;
    /** PMS中录波文件列表总数 */
    private int fileNum = 0;
    /** 标记PMS中录波文件列表第几页 */
    private int frmIndex = 0;
    /** 标记PMS中录波文件列表最大一页 */
    private int maxIndex = 0;
    /** 发送的文件的总大小 */
    private int numDownZie = 0;
    /** 已经发到PMS的大小 */
    private int numDownNow = 0;
    // private int numUpZie = 0; // 上传到手机来的文件的大小
    /** 手机已经接收的大小 */
    private int numUpNow = 0;
    /** 请求文件的长度 */
    private byte[] bufRecFile;
    /** 缓存传到PMS的文件 */
    private byte[] bufSendFile = new byte[10 * 1024 * 1024];
    /** 一次最大发送到PMS的大小 */
    private int MaxPacket = 2 * 1024;
    /** PMS中菜谱文件列表 */
    private List<String> downFileList = new ArrayList<String>();
    /** PMS中录波文件列表 */
    private List<String> selfFileList = new ArrayList<String>();
    /** 缓存（进行到）帧数的byte数组 */
    private byte[] bufACK = {0x00, 0x00};
    /** 连接状态：true=连接成功（收到F8 00，不必重连）、false=未连接（初次连接、心跳重连） 2016 */
    private boolean isConnected = false;
    /** 三次重连与指令的机会 若三次后还失败（isConnected=false且不再自动连接）*/
    private int connectTimes = 3;
    /** 是否在心跳 */
    private boolean startHeart = false;


    public SocketService() {}
    /** 调用getSocketService()，获得SocketService */
    public class SocketBinder extends Binder {
        public SocketService getSocketService() {
            return SocketService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        HHDLog.i("Socket服务被绑定");
        return new SocketBinder();//调用onBind可以获得SocketBinder对象
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //socket = new Socket();
        HHDLog.i("Socket服务被创建...");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeSocket();
        HHDLog.v("销毁服务");
    }

    /** 自定义接口回调（菜谱列表、连接状态、监听进度条等） */
    public interface OnReceiver {//OnSocketListen
        /**
         * 连接成功
         * @param cookBookFileList  菜谱列表（遍历时）
         * @param status            0：默认值（不处理），
         *                          1：下载成功，
         *                          2：下载失败，
         *                          3：下载百分比，
         *                          4：连接成功，
         *                          5：删除文件成功，
         *                          6：获取设备状态成功，
         *                          7：传文件到智能灶成功，
         *                          8：传文件到智能灶的百分比，
         *                          9：对时功能。
         * @param progress          进度条当前值
         * @param total             进度条总进度
         */
        void onSuccess(List<String> cookBookFileList, int status, int progress, int total);

        /**
         * 连接失败
         * @param flag  0：默认值，
         *              -1：下载文件大小=0。
         */
        void onFailure(int flag);
    }
    /** 设值自定义接口回调 */
    public void setReceiver(OnReceiver receiver) {
        this.receiver = receiver;
    }

    /** 守护线程（检查socket状态）更新设备状态 */// TODO

    //*****************************************************************
    /** 生产者消费者设计模式（MSG=消费的对象） */
    class Message {
        private byte[] msg;
        private boolean flag = true;//用于标记发送和接收
        public Message() {
            super();
        }
        public Message(byte[] msg) {
            this.msg = msg;
        }
        public byte[] getMsg() {
            return msg;
        }
        public void setMsg(byte[] msg) {
            this.msg = msg;
        }

        /** 接收数据（生产者） */
        public synchronized void receiveMessage() {
            if (flag){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            {/** 具体接收业务 */
                byte[] resultTemp = new byte[512 * 3];
                int len = -1;
                try {
                    if (input != null || !socket.isClosed() || socket.isConnected()) {
                        len = input.read(resultTemp);
                    } else {
                        //
                    }
                    if (len == -1) {
                        //
                    } else {
                        /** 缓存Socket一次接过来的数据 */
                        byte[] result = new byte[len];
                        for (int i = 0; i < len; i++) {
                            result[i] = resultTemp[i];
                        }

                        HHDLog.i("读进【"+DataUtil.byte2HexString(result[3])+" "+DataUtil.byte2HexString(result[4])+"】---------------------------数据长度="+result.length);
                        System.out.println("\r\n接收---------------------------------------------------------------------------");
                        for (int i = 0; i < result.length; i++) {
                            if (i == 3) {
                                System.out.print("【 ");
                            }
                            if (i == 5) {
                                System.out.print("】 | ");
                            }
                            if (i == 6) {
                                System.out.print("| ");
                            }
                            System.out.print(DataUtil.byte2HexString(result[i]) + " ");
                        }
                        System.out.println("\r\n接收---------------------------------------------------------------------------");
                        System.out.println(" ");

                        android.os.Message msg = new android.os.Message();
                        msg.obj = result;//20161027把接收的数据封装为消息对象
                        checkDataHandler.sendMessage(msg);
                        //HHDLog.i("送数据进行校验");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            flag=true;
            this.notify();
        }

        /**
         * 发送数据（消费者）
         *
         * @param msg 待发送的数据
         */
        public synchronized void sendMessage(byte[] msg) {
            if (!flag){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.setMsg(msg);
            {/** 具体发送业务 */

//                if (){
//
//                }

                if (socket == null || output == null || socket.isClosed()) {
                    if (socket == null) {
                        //
                    } else {
                        //
                    }
                    if (receiver != null) {
                        receiver.onFailure(0);
                        HHDLog.v("检测不能接收PMS状态");
                    }
                    return;
                }
                try {
                    for (int i = 0; i < msg.length; i++) {
                        //
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                try {
                    if (msg != null) {
                        output.write(msg);
                        output.flush();

                        System.out.println("\r\n发送---------------------------------------------------------------------------");
                        for (int i = 0; i < msg.length; i++) {
                            if (i == 3) {
                                System.out.print("【 ");
                            }
                            if (i == 5) {
                                System.out.print("】 | ");
                            }
                            if (i == 6) {
                                System.out.print("| ");
                            }
                            System.out.print(DataUtil.byte2HexString(msg[i]) + " ");
                        }
                        System.out.println("\r\n发送---------------------------------------------------------------------------");
                        System.out.println(" ");
                        HHDLog.i("写出【"+DataUtil.byte2HexString(msg[3])+" "+DataUtil.byte2HexString(msg[4])+"】数据长度="+msg.length);

                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            flag=false;
            this.notify();
        }

    }

    /** Socket接收数据线程 */
    class ReceiveRunnable implements Runnable {
        private Message message;
        public ReceiveRunnable(Message msg) {
            this.message = msg;
        }
        @Override
        public void run() {
//            if (地址不是我的) {

//            }
            /** 地址是我的 */
            {
                message.receiveMessage();
            }
        }
    }

    /** Socket发送数据线程 */
    class SendRunnable implements Runnable {
        private Message message;
        public SendRunnable(Message msg) {
            this.message = msg;
        }
        @Override
        public void run() {

//            if (地址不是我的) {

//            }
            /** 地址是我的 */
            {
                message.sendMessage(message.msg);
            }
        }
    }

    /**
     * 封装发送帧数据功能（发送后启动接收线程）
     * 用到地方：发指令、数据指令、重发指令和数据指令
     *
     * @param frameData 帧数据（拼接好的数据）
     */
    private void sendFrame(byte[] frameData) {
        Message msg = new Message(frameData);
        ReceiveRunnable receiveRunnable = new ReceiveRunnable(msg);
        Thread receiveThread = new Thread(receiveRunnable);
        receiveThread.start();

        SendRunnable sendRunnable = new SendRunnable(msg);
        Thread sendThread = new Thread(sendRunnable);
        sendThread.start();
    }

    /** 判断socket在不在线 true=连接 */
    public boolean isConnect() {
        try {
            socket.sendUrgentData(0xFF);//测试是否连接
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
       /* if (socket==null){
            HHDLog.i("socket==null");
            return false;
        }
        if (socket.isConnected()) {
            return true;
        }*/

        return true;
    }
    /** 判断Socket是否为连接状态 */
    public boolean isSocketConnected(){
        if (socket != null) {
            return socket.isConnected();
        }
        return false;
    }

    /** 判断Socket是否为空：false=null */
    public boolean isNullSocket(){
        HHDLog.i("判断Socket是否为空，false=null");
        if (socket != null){
            return true;
        }
        return false;
    }

    /** 初始化客户端Socket */
    public void initClientSocket() {
        //System.out.println("初始化Socket...");
        //HHDLog.i("初始化Socket...");
        try {
            socket = new Socket(Config.serverHostIp, Config.SERVER_HOST_PORT);
            output = socket.getOutputStream();
            input = socket.getInputStream();
            HHDLog.i("创建输入、输出流、创建Socket=" + Config.serverHostIp + "，" + Config.SERVER_HOST_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 首次进入“设备”界面时调用（三次连接机会） */
    public void firstConnect() {
        //HHDLog.i("初次连接");
        isConnected = false;
        connectTimes = 3;
        connectHandler.sendEmptyMessage(0);
    }

    /** 关闭Socket连接 */
    public void closeSocket() {
        HHDLog.i("关闭Socket...");
        try {
            if (output != null) {
                output.close();
                output = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
            if (heartTimer != null) {
                heartTimer.cancel();
                heartTimer = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分析（PMS-》手机）数据，匹配相应的指令
     *
     * @param buf 接到的帧数据
     * @param num 帧数据的下标
     */
    private void matching(byte[] buf, int num) {
        //HHDLog.i("匹配相应的指令");
        int len = DataUtil.hexToTen(bufTemp[1]) + DataUtil.hexToTen(bufTemp[2]) * 256;
        ConFalg = true;
        isSendCMD = false;
        /** 客户端地址匹配 */
        switch (buf[3]) {
            case (byte) 0xF1:/** F1 00遥控功能 */
                if (receiver != null) {
                    receiver.onSuccess(null, 11, 0, 0);
                    HHDLog.v("收到遥控功能");
                }
                /*if (buf[4] == (byte) 0x00) {//启停
                } else if (buf[4] == (byte) 0x01) {//-
                } else if (buf[4] == (byte) 0x02) {//+
                } else if (buf[4] == (byte) 0x07) {//确定
                }*/
                break;
            case (byte) 0xF2:/** F2 00对时功能 */
                HHDLog.i("匹配【"+DataUtil.byte2HexString(buf[3])+" "+DataUtil.byte2HexString(buf[4])+"】--》F2 00对时功能");
                if (buf[4] == (byte) 0x00) {
                    if (receiver != null) {
                        receiver.onSuccess(null, 9, 0, 0);
                    }
                }
                break;
            case (byte) 0xF3: /** F3 00菜谱文件数量；F3 01菜谱文件列表；F3 02遍历列表完毕 */
                HHDLog.i("匹配【"+DataUtil.byte2HexString(buf[3])+" "+DataUtil.byte2HexString(buf[4])+"】--》F3 00菜谱文件数量；F3 01菜谱文件列表；F3 02遍历列表完毕");
                if (buf[4] == 0x00) { // 得到文件数目
                    fileNum = DataUtil.hexToTen(bufTemp[6]) + DataUtil.hexToTen(bufTemp[7]) * 256;
                    if (DeviceFragment.fileFlag) {
                        selfFileList.clear();
                    } else {
                        downFileList.clear();
                    }
                    if (fileNum > 0) {
                        frmIndex = 1;
                        maxIndex = fileNum / 25;
                        if ((fileNum % 25) > 0) {
                            maxIndex++;
                        }
                        ACK(frmIndex);
                        splitInstruction(Config.BUF_LIST_FILE, bufACK);
                    } else {
                        if (receiver != null) {
                            receiver.onSuccess(null, 0, 0, 0);
                        }
                    }
                } else if (buf[4] == 0x01) { // 得到文件名称
                    String filename = "";
                    for (int index = 8; index < num - 40; index += 40) { // 9->8
                        String aa = "";
                        try {
                            aa = new String(buf, index, 40, "gbk");
                        } catch (UnsupportedEncodingException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            filename = aa.replace("\0", "");
                        } catch (Exception e) {
                            filename = aa;
                        }
                        if (DeviceFragment.fileFlag) {
                            selfFileList.add(filename);
                        } else {
                            downFileList.add(filename);
                        }
                    }
                    if (DeviceFragment.fileFlag) {
                        if (selfFileList.size() < fileNum) { // 加帧判断
                            frmIndex++;
                            ACK(frmIndex);
                            splitInstruction(Config.BUF_LIST_FILE, bufACK);
                        } else {
                            splitInstruction(Config.BUF_LIST_FILE_OVER, null);
                        }
                    } else {
                        if (downFileList.size() < fileNum) { // 加帧判断
                            frmIndex++;
                            ACK(frmIndex);
                            splitInstruction(Config.BUF_LIST_FILE, bufACK);
                        } else {
                            splitInstruction(Config.BUF_LIST_FILE_OVER, null);
                        }
                    }
                } else if (buf[4] == 0x02) { // 遍历完成
                    if (DeviceFragment.fileFlag) {
                        if (receiver != null) {
                            receiver.onSuccess(selfFileList, 0, 0, 0);
                        }
                    } else {
                        if (receiver != null) {
                            receiver.onSuccess(downFileList, 0, 0, 0);
                        }
                    }
                }
                break;
            case (byte) 0xF4:/** F4 00获取菜谱大小；F4 01上召（PMS->手机）菜谱；F4 02发送结束；F4 03中断传输 */
                HHDLog.i("匹配【"+DataUtil.byte2HexString(buf[3])+" "+DataUtil.byte2HexString(buf[4])+"】--》F4 00获取菜谱大小；F4 01上召（PMS->手机）菜谱；F4 02发送结束；F4 03中断传输");
                if (buf[4] == 0x01) { // 得到文件数据
                    if (Config.cancelTransfer) {/** 21061009取消传输 */
                        splitInstruction(BUF_FILE_CANCEL, null);
                        return;
                    }
                    downloadCookbook(buf);
                } else if (buf[4] == 0x00) { // 得到文件长度
                    int fileLen = DataUtil.hexToTen(buf[6]) + DataUtil.hexToTen(buf[7]) * 256
                            + DataUtil.hexToTen(buf[8]) * 256 * 256 + DataUtil.hexToTen(buf[9]) * 256 * 256 * 256;
                    if (fileLen == 0) {
                        if (receiver != null) {
                            receiver.onFailure(-1);
                            HHDLog.v("检测不能接收PMS状态");
                        }
                        return;
                    }
                    numUpNow = 0;
                    bufRecFile = new byte[fileLen];
                    frmIndex = 1;
                    maxIndex = fileLen / MaxPacket;
                    if ((fileLen % MaxPacket) > 0) {
                        maxIndex++;
                    }
                    ACK(frmIndex);
                    splitInstruction(Config.BUF_FILE_ACK, bufACK);
                } else if (buf[4] == 0x02) {
                    DeviceFragment.saveFileName = FileUtils.PMS_FILE_PATH + FileUtils.getFileName(DeviceFragment.saveFileName);
                    FileUtils.writeTextToFile(DeviceFragment.saveFileName, bufRecFile);
                    if (receiver != null) {
                        receiver.onSuccess(null, 1, 0, 0);
                    } else {
                        receiver.onFailure(0);
                        HHDLog.v("检测不能接收PMS状态");
                    }
                }  else if (buf[4] == 0x03) {/** 20161009收到中断确认 */
                    Config.cancelTransfer = false;
                }
                break;
            case (byte) 0xF5:/** F5 00获取菜谱大小；F5 01下发（手机->PMS）菜谱；F5 02发送结束；F5 03中断传输 */
                HHDLog.i("匹配【"+DataUtil.byte2HexString(buf[3])+" "+DataUtil.byte2HexString(buf[4])+"】--》F5 00获取菜谱大小；F5 01下发（手机->PMS）菜谱；F5 02发送结束；F5 03中断传输");
                if (buf[4] == 0x00) { // 发送文件大小和文件名，得到确认
                    if (buf[6] == 0x01) {
                        Config.numDownNow = 0;
                        Config.frmIndex = 1;
                        uploadFile( Config.frmIndex);
                    } else if (buf[6] == 0x00) {
                        if (receiver != null) {
                            receiver.onFailure(50000);
                            HHDLog.v("检测不能接收PMS状态");
                        }
                    }
                } else if (buf[4] == 0x01) { // 发送文件一帧，得到确认
                    if (buf[6] == 0x01) {
                        if (Config.cancelTransfer) {/** 21061009取消传输 */
                            splitInstruction(BUF_DOWN_FILE_CANCEL, null);
                            return;
                        }
                        Config.frmIndex++;
                        if (receiver != null) {
                            receiver.onSuccess(null, 8, Config.numDownNow, Config.numDownZie);
                        } else {
                            //
                        }
                        if (Config.numDownZie > Config.numDownNow) {
                            uploadFile(Config.frmIndex);
                        } else {
                            splitInstruction(Config.BUF_DOWN_FILE_STOP, null);
                        }
                    } else if (buf[6] == 0x00) {
                        uploadFile(Config.frmIndex);
                    }
                } else if (buf[4] == 0x02) {
                    if (receiver != null) {
                        receiver.onSuccess(null, 7, 0, 0);
                    } else {
                        //
                    }
                }
                else if (buf[4] == 0x03) {/** 20161009收到中断确认 */
                    Config.cancelTransfer = false;
                }
                break;
            case (byte) 0xF6:/** F6 00删除录波文件；F6 01删除菜谱文件 */
                HHDLog.i("匹配【"+DataUtil.byte2HexString(buf[3])+" "+DataUtil.byte2HexString(buf[4])+"】--》F6 00删除录波文件；F6 01删除菜谱文件");
                if (buf[4] == (byte) 0x00) {
                    if (buf[6] == 0x01) {
                        if (receiver != null) {
                            receiver.onSuccess(null, 5, 0, 0);
                        }
                    } else {
                        if (receiver != null) {
                            receiver.onFailure(0);
                            HHDLog.v("检测不能接收PMS状态");
                        }
                    }
                } else if (buf[4] == (byte) 0x01) {
                    if (buf[6] == 0x01) {
                        if (receiver != null) {
                            receiver.onSuccess(null, 5, 0, 0);
                        }
                    } else {
                        if (receiver != null) {
                            receiver.onFailure(0);
                            HHDLog.v("检测不能接收PMS状态");
                        }
                    }
                }
                break;
            case (byte) 0xF7:/** F7 00查询状态：功率、温度等 */

                HHDLog.i("匹配【"+DataUtil.byte2HexString(buf[3])+" "+DataUtil.byte2HexString(buf[4])+"】--》F7 00查询状态：功率、温度等");
                if (buf[4] == (byte) 0x00) {
                    //Math.floor(x*10d)/10//保留小数点后一位，且不四舍五入//new java.text.DecimalFormat("#.00").format(3.1415926)//随意位数，且四舍五入

                    //Config.SYSTEM_A = Math.floor(((DataUtil.hexToTen(buf[6]) + 256 * DataUtil.hexToTen(buf[7])) * 1650.0 / 48803.38944) * 10d) / 10 + " A";
                    Config.PMS_A = String.valueOf(
                            Math.floor(((DataUtil.hexToTen(buf[6]) + 256 * DataUtil.hexToTen(buf[7])) / 1000.00) * 10d) / 10
                    ) + "A";

                    Config.PMS_V = String.valueOf(
                            ((DataUtil.hexToTen(buf[8]) + 256 * DataUtil.hexToTen(buf[9])) / 10.0)
                    ) + "V";

                    Config.PMS_W = String.valueOf(
                            ((DataUtil.hexToTen(buf[6]) + 256 * DataUtil.hexToTen(buf[7])) / 1000) * ((DataUtil.hexToTen(buf[8]) + 256 * DataUtil.hexToTen(buf[9])) / 10)
                    ) + "W";
                    //HHDLog.v(((DataUtil.hexToTen(buf[6]) + 256 * DataUtil.hexToTen(buf[7])) / 1000)+"，"+((DataUtil.hexToTen(buf[8]) + 256 * DataUtil.hexToTen(buf[9])) / 10));

                    Config.PMS_Temp = String.valueOf(
                            Math.floor(((DataUtil.hexToTen(buf[10]) + 256 * DataUtil.hexToTen(buf[11])) / 100.0) * 10d) / 10
                    ) + "℃";

                    Config.PMS_IGBT = String.valueOf(
                            Math.floor(((DataUtil.hexToTen(buf[12]) + 256 * DataUtil.hexToTen(buf[13])) / 100.0) * 10d) / 10
                    ) + "℃";

                        switch (DataUtil.hexToTen(buf[14])) {
                            case 0:
                                Config.PMS_Status = "关机";//POWEROFF
                                break;
                            case 1:
                                Config.PMS_Status = "开机";//POWERON
                                break;
                            case 2:
                                Config.PMS_Status = "待机";//POWERSTANDBY
                                break;
                            case 3:
                                Config.PMS_Status = "暂停";//POWERHALT
                                break;
                        }

                        Config.PMS_SetW = String.valueOf(
                                DataUtil.hexToTen(buf[15]) * 10
                        ) + "W";

                    try {
                        Config.PMS_Errors.clear();

                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(DataUtil.byte2HexString(buf[18]));
                        stringBuffer.append(DataUtil.byte2HexString(buf[17]));
                        stringBuffer.append(DataUtil.byte2HexString(buf[16]));
                        //HHDLog.i("拼接16进制=" + stringBuffer);
                        String errors = DataUtil.hexString2binaryString(stringBuffer.toString());
                        //HHDLog.i("16进制转换2进制=" + errors);
                        StringBuffer sbReverse = new StringBuffer(errors);
                        sbReverse = sbReverse.reverse();//反转
                        //HHDLog.i("反转16进制=" + sbReverse.toString());
                        char[] c = sbReverse.toString().toCharArray();
                        for (int i = 0; i < c.length; i++) {
                            if (c[i] == '1') {
                                //HHDLog.i("第" + i + "位有错误");
                                Config.PMS_Errors.add((i) + "");
                            }
                        }

                            //Config.ERROR_CODE = ((DataUtil.hexToTen(buf[16]) + 256 * DataUtil.hexToTen(buf[17]) + 256 * 256 * DataUtil.hexToTen(buf[18])) / 10.0) + "";
                            /*StringBuffer sb = new StringBuffer();//启汇
                            for (int i = 18; i > 15; i--) {
                                String hex = Integer.toHexString(buf[i]);// 2016 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式。
                                String r = "";
                                //String sIhex = String.valueOf(ihex);

                                try {
                                    int ihex = Integer.parseInt(hex);// 2016 将字符串参数作为有符号的十进制整数进行解析。
                                    if (ihex < 10) {
                                        r = String.format("%02d", ihex); // 2016 使用指定的格式字符串和参数返回一个格式化字符串。
                                    } else {
                                        r = ihex + "";
                                    }
                                    HHDLog.i("ihex=" + ihex);
                                } catch (Exception e) {
                                    HHDLog.v("匹配指令4-1-1时捕获到的异常=" + e);
                                    e.printStackTrace();
                                }

                                sb.append(r);
                            }

                            String result = DataUtil.hexString2binaryString(sb.toString());
                            StringBuffer sBuf = new StringBuffer();

                            for (int i = result.length(); i > 0; i--) {
                                sBuf.append(result.substring(i - 1, i));
                            }
                            for (int i = 0; i < result.length(); i++) {
                                if (sBuf.substring(i, i + 1).equals("1")) {
                                    //Config.PMS_Errors.add((i) + "");
                                    HHDLog.i("启汇=" + ((i) + ""));
                                }
                            }*/

                    } catch (Exception e) {
                        HHDLog.v("匹配指令4时捕获到的异常=" + e);
                        e.printStackTrace();
                    }

                    Config.PMS_SetTemp = String.valueOf(
                            Math.floor(DataUtil.hexToTen(buf[19]) * 10d) / 10
                    ) + "℃";
                    //HHDLog.v("设定温度=" + Config.SET_TEMP);
                    Config.PMS_ClientNum = String.valueOf(
                            DataUtil.hexToTen(buf[20])
                    ) + "";

                    if (receiver != null) {
                        receiver.onSuccess(null, 6, 0, 0);
                    }
                } else {
                    if (receiver != null) {
                        receiver.onFailure(0);
                    }
                }
                break;
            case (byte) 0xF8:/** F8 00连接确认；F8 01PMS繁忙；F8 02心跳报文；F8 03断开连接 */
                HHDLog.i("匹配【"+DataUtil.byte2HexString(buf[3])+" "+DataUtil.byte2HexString(buf[4])+"】--》F8 00连接确认；F8 01PMS繁忙；F8 02心跳报文；F8 03断开连接");
                if (buf[4] == (byte) 0x00) { // 连接确认报文，回复PMS的MAC
                    isConnected = true;//TODO 只有一个确认连接
                    startHeartTimer();//TODO 只有一个开始心跳
                    if (receiver != null) {
                        receiver.onSuccess(null, 4, 0, 0);
                        //splitInstruction(Config.bufSetTime, new DateUtil().getCurrentTime());//对时2016
                        //HHDLog.i("握手成功，准备对时");
                    }
                } else if (buf[4] == (byte) 0x01) {
                    //TODO PMS处于繁忙状态，请稍等
                } else if (buf[4] == (byte) 0x02) {
                    // 心跳报文
                }
                break;
            default:
                HHDLog.i("匹配不到指令");
                //System.out.println("匹配不到指令");
                break;
        }
    }

    /**
     * 拼接指令帧数据（带数据）
     *
     * @param functionCode  功能码
     * @param data          数据
     */
    public void splitInstruction(byte[] functionCode, byte[] data) {
        //HHDLog.i("发指令...");
        if (data != null) {
            int addNum = 0;
            int i = 0;
            int len = functionCode.length + data.length + 1;// PMS格式长度+数据长度+校验码
            byte[] bufTemp = new byte[len + 4];/*Lotus 2016-07-27 修改长度*/
            bufTemp[0] = (byte) 0xA5;
            bufTemp[1] = (byte) (len % 256);
            bufTemp[2] = (byte) ((len / 256) % 256);
            bufTemp[3] = functionCode[0];
            bufTemp[4] = functionCode[1];
            if (Config.clientPort == -1) {
                return;
            }
            bufTemp[5] = Config.clientPort;
            for (i = 6; i < len + 3; i++) {
                bufTemp[i] = data[i - 6];
            }
            for (i = 1; i < len + 3; i++) {
                addNum += bufTemp[i];
            }
            bufTemp[len + 3] = (byte) (addNum % 256);
            try {
                //sendMessage(bufTemp);
                sendFrame(bufTemp);
                timeCnt = 0;
                Config.timeCntHeart = 0;
                isSendCMD = true;
                //HHDLog.i("心跳时间为0表示上传清空="+Config.timeCntHeart);
            } catch (Exception e) {
                e.printStackTrace();
            }
            bufLastTemp = new byte[bufTemp.length];
            for (i = 0; i < bufTemp.length; i++) {
                bufLastTemp[i] = bufTemp[i];
            }
        } else {
            int addNum = 0;
            byte[] bufTemp = new byte[functionCode.length + 5];
            bufTemp[0] = (byte) 0xA5;
            bufTemp[1] = (byte) (functionCode.length % 256 + 1);
            bufTemp[2] = (byte) (functionCode.length / 256);
            for (int i = 0; i < functionCode.length; i++) {
                bufTemp[i + 3] = functionCode[i];
            }
            bufTemp[functionCode.length + 3] = Config.clientPort;
            for (int i = 1; i < functionCode.length + 4; i++) {
                addNum += bufTemp[i];
            }
            bufTemp[functionCode.length + 4] = (byte) (addNum % 256);
            try {
                bufLastTemp = new byte[bufTemp.length];
                for (int i = 0; i < bufTemp.length; i++) {
                    bufLastTemp[i] = bufTemp[i];
                }
                bufLastTemp = new byte[bufTemp.length];
                //sendMessage(bufTemp);
                sendFrame(bufTemp);
                timeCnt = 0;
                Config.timeCntHeart = 0;
                isSendCMD = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** 校验数据 */
    Handler checkDataHandler = new Handler(new Handler.Callback() {
        /**
         * 校验数据的正确性
         *
         * @param msg 需要校验的数据
         * @return 是否正确
         */
        @Override
        public boolean handleMessage(android.os.Message msg) {
            //HHDLog.i("校验数据");
            byte[] result = (byte[]) msg.obj;










            try {
                /** 标记再次校验（先替换A5再进行校验）：false=首次 2016 */
                boolean isAgain = false;
                /** Socket接收的总长度（有可能是两帧） 2016 */
                int receiveLen = 0;
                receiveLen = result.length;
                /** 解析出来的长度L=功能码+子功能码+数据域长度（避免多帧数据重叠） 20161101 */
                int frameLen = (DataUtil.hexToTen(result[1]) + DataUtil.hexToTen(result[2]) * 256) + 4;
                //HHDLog.i("接收的总长度（有可能是两帧）=" + receiveLen + "，帧里面正确的长度" + frameLen);
                /** 校验算术和 2016 */
                int checkSUM = 0;
                /** 标记校验开始、结束：false=结束 2016 */
                boolean isCheck = true;

                while (isCheck) {
                    if (isAgain) {
                        if (result[0] == (byte) 0xA5) { //是A5开头
                            if (frameLen >= num) {  //限制接收的长度为有效长度
                                if (num < 3) {//拼接前三
                                    bufTemp[num] = result[num];
                                    num++;
                                } else if (num == 3) {  //拼接四
                                    //len = DataUtil.hexToTen(bufTemp[1]) + DataUtil.hexToTen(bufTemp[2]) * 256;
                                    bufTemp[num] = result[num];
                                    num++;
                                } else if (num > 3) {   //拼接四后面
                                    if (num < frameLen + 3 && num < result.length) {    //解析不到最后一位时
                                        bufTemp[num] = result[num];
                                        num++;
                                    } else {    //解析到最后一位时
                                        if (num < result.length) {
                                            bufTemp[num] = result[num];
                                            num++;
                                        }
                                        for (int i = 1; i < num - 1; i++) { //解析完成开始校验
                                            int temp = bufTemp[i];
                                            if (temp < 0) {
                                                temp += 256;
                                            }
                                            checkSUM += temp;
                                        }
                                        if (DataUtil.hexToTen(bufTemp[num - 1]) == (checkSUM % 256)) {  //对比校验码
                                            if (frameLen == num) {
                                                matching(bufTemp, num);
                                            }
                                            isCheck = false;
                                            return true;
                                        } else {    //校验不通过结束校验
                                            if (frameLen > 0) {
                                                isCheck = false;
                                            }
                                        }
                                        if (frameLen > 0) { //不开始再次校验
                                            isAgain = false;
                                            num = 0;
                                        }
                                    }
                                }
                            } else {
                                //
                            }
                        } else {
                            HHDLog.i("不是A5开头，是=" + DataUtil.byte2HexString(result[0]));
                        }
                    } else {    //首次校验，先替换A5
                        /** 缓存地址码（A5）：用于替换 2016 */
                        byte addressCode = (byte) 0xA5;
                        /** 取出并缓存原地址码 */
                        byte addressCodeTemp = result[0];
                        //HHDLog.v("原地址码（接收到的首位）=" + DataUtil.byte2HexString(addressCodeTemp));
                        if (addressCodeTemp == addressCode) {
                            num = 0;
                            bufTemp[num] = addressCodeTemp;
                            isAgain = true;
                            num++;
                            timeCnt = 0;//TODO
                            Config.timeCntHeart = 0;//TODO 清零心跳，位置应该统一到输入输出流
                            RecTimeOut = false;//recovery恢复
                        } else {
                            //HHDLog.v("原地址码（接收到的首位）不为A5=" + DataUtil.byte2HexString(addressCodeTemp));
                        }
                    }
                }
            } catch (Exception e) {
                HHDLog.v("校验数据时捕获到的异常=" + e);
                e.printStackTrace();
                Config.serverHostName = "";
                if (receiver != null) {
                    receiver.onFailure(0);
                    HHDLog.v("检测不能接收PMS状态");
                }
                ConFalg = false;
                return false;
            }
            return false;
        }
    });

    /** 检查Socket连接状态连接 */
    Handler connectHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    if (!isConnected && connectTimes > 0) {
                        connectTimes--;
                        connectHandler.sendEmptyMessage(1);//检查重连剩余次数
                        HHDLog.i("是否Socket连接="+isSocketConnected());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
                                //if (!isSocketConnected()) {
                                    closeSocket();
                                    initClientSocket();
                                    splitInstruction(Config.BUF_CONNECT, null);//F8 00
                                //}
                                /*new Handler().postDelayed(new Runnable() {  *//** 延迟2秒执行此线程 *//*
                                @Override
                                public void run() {
                                    HHDLog.i("重复关闭、连接、发指令，是否已经连接=" + isConnected + "，剩下连接次数=" + connectTimes);
                                    connectHandler.sendEmptyMessage(0);
                                }
                                }, 2000);*/
                                Looper.loop();
                            }
                        }).start();
                    } else {
                        if (!isConnected) {
                            if (receiver != null) {
                                receiver.onFailure(0);
                                HHDLog.v("检测不能接收PMS状态");
                            }
                            KappAppliction.state = 2;
                            KappUtils.showToast(context, "与PMS连接已经断开");
                        }
                    }
                    break;
                case 1:
//                    HHDLog.i("重连剩余次数：" + connectTimes);
                    //TODO 判断socket为空
                    //HHDLog.v("发送0xFF检查Socket="+isConnect());
                    break;
            }
            return false;
        }
    });


    /** 心跳计时类（重发指令、心跳） */
    class HeartTimeCount extends CountDownTimer {
        /**
         * 倒计时构造方法
         *
         * @param millisInFuture    从开始调用start()到倒计时完成并onFinish()方法被调用的毫秒数（单位毫秒）
         * @param countDownInterval 接收onTick(long)回调的间隔时间（单位毫秒）
         */
        HeartTimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        /** 倒计时完成时被调用   */
        @Override
        public void onFinish() {
            //
        }

        /**
         * 固定间隔被调用（不会在之前一次调用完成前被调用）
         * @param millisUntilFinished 倒计时剩余时间
         *
         * start ()：启动倒计时
         */
        @Override
        public void onTick(long millisUntilFinished) {
            try {
                if (!TextUtils.isEmpty(Config.serverHostName) && socket != null && socket.isConnected()) {
                    timeCnt++;
                    //HHDLog.i("一秒跳一次=" + timeCnt);
                    if (timeCnt > 9) {
                        RecTimeOut = true;
                        timeCnt = 0;
                    }
                    if (ConFalg) {
                        if (RecTimeOut && isSendCMD) {// 如果是发送超时等，三次重发机会，否则尝试重连
                            MaxReCnt++;
                            //sendMessage(bufLastTemp);
                            sendFrame(bufLastTemp);

                            isSendCMD = true;
                            timeCnt = 0;
                            RecTimeOut = false;
                            if (MaxReCnt > 2) {
                                MaxReCnt = 0;
                                ConFalg = false;
                                KappUtils.showToast(context, "操作失败，进行重连");
                                Config.serverHostName = "";
                                connectTimes = 3;
                                isConnected = false;
                                connectHandler.sendEmptyMessage(0);
                            }
                        }
                        Config.timeCntHeart++;/** 心跳计时，30S无操作发送心跳指令 */
                        //HHDLog.v("当前心跳时间（timeCntHeart）="+Config.timeCntHeart);
                        if (Config.timeCntHeart >= 30) {
                            splitInstruction(Config.BUF_HEARTBEAT, null);
                            //Config.timeCntHeart = 0;
                        }
                    }
                }
            } catch (Exception e) {
                //
            }
        }
    }

    /** F4 01
     * PMS（菜谱）-》手机（菜谱）
     *
     * @param buf 每帧数据
     */
    private void downloadCookbook(byte[] buf) {
        int fileLen = DataUtil.hexToTen(buf[1]) + DataUtil.hexToTen(buf[2]) * 256 - 9;//200160928//fileLen：长度L：功能码、子功能码以及数据域的长度之和
        int allFileLen = DataUtil.hexToTen(buf[6]) + DataUtil.hexToTen(buf[7]) * 256 + DataUtil.hexToTen(buf[8]) * 256 * 256 + DataUtil.hexToTen(buf[9]) * 256 * 256 * 256;/** 20160920新增加的 */
        for (int i = 0; i < 1037; i++) {
        }
        for (int i = 0; i < fileLen; i++) {
            bufRecFile[numUpNow + i] = buf[i + 12];//20160928//bufRecFile：当前菜谱文件总长度
        }
        for (int j = 0; j < bufRecFile.length; j++) {
        }
        numUpNow += fileLen;// 40380 40124//20160928//fileLen：进度总长度（int）
        if (receiver != null) {
            receiver.onSuccess(null, 3, numUpNow, bufRecFile.length);
        }
        if (numUpNow == bufRecFile.length) {//20160928需要修改：帧序号=文件的长度/每帧大小
            splitInstruction(Config.BUF_FILE_STOP, null);
        } else {
            frmIndex++;
            ACK(frmIndex);
            splitInstruction(Config.BUF_FILE_ACK, bufACK);//frmIndex（int）=帧序号=bufACK（byte）
        }
    }

    /** F5 01
     * 手机-》PMS
     *
     * @param index 第几帧
     */
    private void uploadFile(int index) {
        int lenth = Config.numDownZie - Config.numDownNow;// 所需下载长度为剩下的长度 但不大于最大请求长度
        if (lenth > (MaxPacket)) {
            lenth = (MaxPacket);
        }
        byte[] bufR = new byte[lenth + 2];
        bufR[0] = (byte) (index % 256);
        bufR[1] = (byte) (index / 256);
        for (int i = 0; i < lenth; i++) {
            bufR[i + 2] = Config.bufSendFile[(index - 1) * MaxPacket + i];//            bufR[i + 2] = bufSendFile[(index - 1) * MaxPacket + i];
        }
        splitInstruction(Config.BUF_DOWN_FILE_DATA, bufR);
        Config.numDownNow = Config.numDownNow + lenth;
    }

    /** F5 00 手机（菜谱）-》PMS（菜谱）；获取文件名称、大小 */
    public void uploadCookbookInfo() {
        String fileName;
        File file;
        //if (CookBookDetailActivity.stat == false) {
        if (!CookBookDetailActivity.stat) {
            file = new File(CookBookDetailActivity.filePath);
            fileName = CookBookDetailActivity.filePath.substring(CookBookDetailActivity.filePath.lastIndexOf('/'));// 文件名长度的限制
        } else {
            file = new File(CookBookDetailActivity.newFilePath);
            fileName = CookBookDetailActivity.newFilePath.substring(CookBookDetailActivity.newFilePath.lastIndexOf('/'));// 文件名长度的限制
        }
        if (fileName.startsWith("\\")) {
            fileName = fileName.replace("\\", "");
        }
        if (fileName.startsWith("/")) {
            fileName = fileName.replace("/", "");
        }
        if (fileName.length() > 39) {
            Looper.prepare();
            KappUtils.showToast(context, "文件名称超长，请重新选择");
            Looper.loop();
            if (receiver != null) {
                receiver.onFailure(0);
                HHDLog.v("检测不能接收PMS状态");
            }
            return;
        }
        byte[] bufFile = FileUtils.getBytesFromFile(file);// 要发送的文件数据
        int check = 0;
        bufSendFile = new byte[bufFile.length];
        Config.bufSendFile = new byte[bufFile.length];//
        for (int i = 0; i < bufFile.length; i++) {
            bufSendFile[i] = bufFile[i];
            Config.bufSendFile[i] = bufFile[i];//
            check += bufFile[i];
        }
        byte[] arr = null;// 文件名的字节长度
        try {
            arr = fileName.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] bufFileInfo = new byte[arr.length + 8];/* Lotus 2016-07-27 因为文件名称长度问题,需要给8位,跟文档上面4位不符*/// 所传参数，文件名长度+文件长度（均为字节长度）
        bufFileInfo[0] = (byte) (bufFile.length % 256);
        bufFileInfo[1] = (byte) ((bufFile.length / 256) % 256);
        bufFileInfo[2] = (byte) ((bufFile.length / 256 / 256) % 256);
        bufFileInfo[3] = (byte) ((bufFile.length / 256 / 256 / 256) % 256);
        bufFileInfo[4] = (byte) (check % 256);
        bufFileInfo[5] = (byte) ((check / 256) % 256);
        bufFileInfo[6] = (byte) ((check / 256 / 256) % 256);
        bufFileInfo[7] = (byte) ((check / 256 / 256 / 256) % 256);
        numDownNow = 0;
        numDownZie = (int) bufFile.length;
        Config.numDownZie = (int) bufFile.length;//
        for (int i = 0; i < arr.length; i++) {
            bufFileInfo[i + 8] = arr[i];
        }
        ConFalg = false;
        splitInstruction(Config.BUF_DOWN_FILE_INFO, bufFileInfo);
    }

    /** F5 00 手机（固件）-》PMS（固件） */
    public void uploadFirmwareInfo() {
        String fileName;
        File file;
        file = new File(CheckUpdateActivity.result1.getPath());
        // 文件名长度的限制
        int index = CheckUpdateActivity.result1.getPath().lastIndexOf('/');
        if (index > 0) {
            index = index + 1;
        }
        fileName = CheckUpdateActivity.result1.getPath().substring(index);
        if (fileName.length() > 39) {
            Looper.prepare();
            KappUtils.showToast(context, "文件名称超长，请重新选择");
            Looper.loop();
            if (receiver != null) {
                receiver.onFailure(0);
                HHDLog.v("检测不能接收PMS状态");
            }
            return;
        }
        byte[] bufFile = FileUtils.getBytesFromFile(file);// 要发送的文件数据
        int check = 0;
        bufSendFile = new byte[bufFile.length];
        //Config.bufSendFile = new byte[bufFile.length];//
        for (int i = 0; i < bufFile.length; i++) {
            bufSendFile[i] = bufFile[i];
            //Config.bufSendFile[i] = bufFile[i];//
            check += bufFile[i];
        }

        // 文件名的字节长度
        byte[] arr = null;
        try {
            arr = fileName.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 所传参数，文件名长度+文件长度（均为字节长度）
        byte[] bufFileInfo = new byte[arr.length + 8];
        bufFileInfo[0] = (byte) (bufFile.length % 256);
        bufFileInfo[1] = (byte) ((bufFile.length / 256) % 256);
        bufFileInfo[2] = (byte) ((bufFile.length / 256 / 256) % 256);
        bufFileInfo[3] = (byte) ((bufFile.length / 256 / 256 / 256) % 256);
        bufFileInfo[4] = (byte) (check % 256);
        bufFileInfo[5] = (byte) ((check / 256) % 256);
        bufFileInfo[6] = (byte) ((check / 256 / 256) % 256);
        bufFileInfo[7] = (byte) ((check / 256 / 256 / 256) % 256);
        numDownNow = 0;
        numDownZie = (int) bufFile.length;
        //Config.numDownZie = (int) bufFile.length;//
        for (int i = 0; i < arr.length; i++) {
            bufFileInfo[i + 8] = arr[i];
        }
        ConFalg = false;
        splitInstruction(Config.BUF_DOWN_FILE_INFO, bufFileInfo);
    }

    /**  启动心跳计时 */
    private void startHeartTimer() {
        HHDLog.v("收到F8 00——》开始心跳");
        if (heartTimer!=null){//20161028HHD
            heartTimer.cancel();
            heartTimer=null;
        }
        heartTimer = new HeartTimeCount(Long.MAX_VALUE, 1000);
        heartTimer.start();
        startHeart = true;//TODO 这里可去掉
        ConFalg = true;
    }

    /**
     * 将帧序号转化为byte数组
     *
     * @param index 帧数整型
     */
    private void ACK(int index) {
        bufACK[0] = (byte) (index % 256);
        bufACK[1] = (byte) (index / 256);
    }
	
    //******************************************************************
    /** 判断是否正在心跳 */
    public boolean isStartHeartTimer() {
        return startHeart;
    }
    public void send(byte[] frameData){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < frameData.length; i++) {
            sb.append(DataUtil.byte2HexString(frameData[i]) + " ");
        }
        try {
            output.write(frameData);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HHDLog.i("发送=" + sb + "，数据长度=" + frameData.length);
        //System.out.println("发送=" + sb + "，数据长度=" + frameData.length);
    }
    public void receive(){
        byte[] resultTemp = new byte[512 * 3];
        int len = -1;
        try {
            len = input.read(resultTemp);
            if (len != -1){
                byte[] result = new byte[len];
                for (int i = 0; i < len; i++) {
                    result[i] = resultTemp[i];
                }
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < result.length; i++) {
                    sb.append(DataUtil.byte2HexString(result[i]) + " ");
                }
                HHDLog.i("接收=" + sb + "，数据长度=" + result.length);
                //System.out.println("接收=" + sb + "，数据长度=" + result.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
