package com.gzmelife.app.device;

import java.util.ArrayList;
import java.util.List;


/**
 * 设备相关
 * @author chenxiaoyan
 *
 */
public class Config {

	/** F1指令回调（控制功能） 20161227 */
	public static final int MSG_F1 = 1;
	/** F2指令回调（对时） 20161227 */
	public static final int MSG_F2 = 2;
	/** F3指令回调（遍历文件） 20161227 */
	public static final int MSG_F3 = 3;
	/** F4指令回调（上召录波文件） 20161227 */
	public static final int MSG_F4 = 4;
	/** F5指令回调（下发录波文件） 20161227 */
	public static final int MSG_F5 = 5;
	/** F6指令回调（删除【录波、菜谱】文件） 20161227 */
	public static final int MSG_F6 = 6;
	/** F7指令回调（查询状态） 20161227 */
	public static final int MSG_F7 = 7;
	/** F8指令回调（连接确认、繁忙状态、心跳报文、断开连接） 20161227 */
	public static final int MSG_F8 = 8;
	/**  默认回调（不用处理） 20161227 */
	public static final int MSG_DEFAULT = 0;
	/**  Socket失败回调 20161227 */
	public static final int MSG_FAIL = -1;
	/** 重新绑定SocketServic（解决有时还没绑定就开始调用问题） 20161227 */
	public static final int MSG_RE_BIND = -11;
	/** 检查绑定情况绑定SocketServic（解决有时还没绑定就开始调用问题） 20161227 */
	public static final int MSG_CHECK_BIND = -22;


	/** 当前心跳的时间 20161010 */
	public static int timeCntHeart = 0;
	/** 菜谱-》PMS文件总长度 20161010 */
	public static int numDownZie = -1;
	/** 菜谱-》PMS进度长度 20161010 */
	public static int numDownNow = -1;
	/** 缓存一个文件数据 20161010 */
	public static byte[] bufSendFile = new byte[10 * 1024 * 1024];
	/** 帧标记 20161010 */
	public static int frmIndex = 0;
	/** 其他用户正在发指令：false=不是别人的指令 20161010 */
	public static boolean isOtherInstruction = false;
	/** 20161009取消文件传输：true=取消 20161010*/
	public static boolean cancelTransfer = false;


	/** 标记显示录波还是菜谱文件：1=录波文件；2=菜谱文件 20161227 */
	public static int flag = 0;
	/** 本地文件位置 */
	public static int position = 0;
	/** 设备连接状态：false=离线*/
	public static boolean isConnect = false;
	/** 推送的自定消息ID */
	public static String id = null;
	/** 菜谱名称 */
	public static String cookbookName = null;
	public static String NewName = null;
	/** PMS原始的IP 2016 */
	public static final String SERVER_HOST_DEFAULT_IP = "192.168.4.1";
	/** PMS连接成功后的IP 2016 */
	public static String serverHostIp = "192.168.4.1";
	/** PMS的端口号 2016 */
	public static final int SERVER_HOST_PORT = 50000;
	//	public static final int SERVER_HOST_PORT = 9898;
	/** 服务器名称（PMS设备的SSID） 2016 */
	public static String serverHostName = "";
	/** 手机ip前三位，最后一位固定255 */
	public static String broadcastIp = "224.0.0.255"; //
	/** 手机本地IP */
	public static String localIP = "224.0.0.1";
	/** 客户端IP的最后一个字节 */
	public static byte clientPort = -1; //


	// F0 00 发送/响应帧 配置WiFi模块
	/** F1 00 发送/响应帧 启停 */
	public static final byte[] BUF_ON_OFF = { (byte) 0xF1, 0x00 };
	/** F1 01 发送/响应帧 功率减 */
	public static final byte[] BUF_DE_POWER = { (byte) 0xF1, 0x01 };
	/** F1 02 发送/响应帧 功率加 */
	public static final byte[] BUF_IN_POWER = { (byte) 0xF1, 0x02 };
	/** F1 03 发送/响应帧 录波 */
	public static final byte[] bufRecorder = { (byte) 0xF1, 0x03 };
	/** F1 04 发送/响应帧 录波反演 */
	public static final byte[] bufInversion = { (byte) 0xF1, 0x04 };
	/** F1 04 发送/响应帧 确定 */
	public static final byte[] BUF_CONFIRM = { (byte) 0xF1, 0x07 };

	/** F2 00 发送/响应帧 对时功能 */
	public static final byte[] BUF_SET_TIME = { (byte) 0xF2, 0x00 };

	/** F3 00 发送/响应帧 获取录波文件数量 遍历文件 */
	public static final byte[] BUF_GET_FILE_NUM = { (byte) 0xF3, 0x00 };
	/** F3 01 发送/响应帧 查询录波文件列表 */
	public static final byte[] BUF_LIST_FILE = { (byte) 0xF3, 0x01 };
	/** F3 01 发送/响应帧 查询录波文件列表结束 */
	public static final byte[] BUF_LIST_FILE_OVER = { (byte) 0xF3, 0x02 };

	/** F4 00 发送/响应帧 获取录波文件大小 上召录波文件*/
	public static final byte[] BUF_FILE_LENTH = { (byte) 0xF4, 0x00 };
	/** F4 01：发送/响应帧 上召录波数据//下载（上送）录波数据 */
	public static final byte[] BUF_FILE_ACK = { (byte) 0xF4, 0x01 };// ok
	/** F4 02：发送/响应帧 录波发送结束 */
	public static final byte[] BUF_FILE_STOP = { (byte) 0xF4, 0x02 };
	/** F4 02：发送/响应帧 中断录波传输 */
	public static final byte[] BUF_FILE_CANCEL = { (byte) 0xF4, 0x03 };

	/** F5 00 发送/确认帧 下发录波文件大小 下发录波文件 */
	public static final byte[] BUF_DOWN_FILE_INFO = { (byte) 0xF5, 0x00 };
	/** F5 01 发送/确认帧 下发录波数据 */
	public static final byte[] BUF_DOWN_FILE_DATA = { (byte) 0xF5, 0x01 };
	/** F5 02 发送/确认帧 数据发送结束 */
	public static final byte[] BUF_DOWN_FILE_STOP = { (byte) 0xF5, 0x02 };
	/** F5 02 发送/确认帧 数据发送结束 */
	public static final byte[] BUF_DOWN_FILE_CANCEL = { (byte) 0xF5, 0x03 };

	/** F6 00 发送/确认帧 删除装置生成录波文件操作 删除文件 */
	public static final byte[] BUF_DEL_SELF_FILE = { (byte) 0xF6, 0x00 };
	/** F6 01 发送/确认帧 删除APP下载菜谱文件操作 */
	public static final byte[] BUF_DEL_DOWN_FILE = { (byte) 0xF6, 0x01 };

	/** F7 00 发送/响应帧 查询状态，包括功率、温度等 */
	public static final byte[] BUF_STATUS = { (byte) 0xF7, 0x00 };

	/** F8 00 发送/响应帧 连接确认报文，回复PMS的MAC */
	public static final byte[] BUF_CONNECT = { (byte) 0xF8, 0x00 };
	// 01 发送/响应帧 非主客户端抢占控制权，成为主客户端。 无效，只能连接一个手机，后面连接的自动得到控制权
	//	public static byte[] bufGrap = { (byte) 0xF8, 0x01 };
	/** F8 02 发送/响应帧 心跳报文 */
	public static final byte[] BUF_HEARTBEAT = { (byte) 0xF8, 0x02 };


	/** 电流 */
	public static String PMS_A = "0.00";
	/** 电压 */
	public static String PMS_V = "000.0";
	/** 当前功率 */
	public static String PMS_W = "0000W";
	/** 设定功率 */
	public static String PMS_SetW = "0000W";
	/** 当前锅温 */
	public static String PMS_Temp = "000.0℃";
	/** IGBT（室内）温度 */
	public static String PMS_IGBT = "000.0℃";
	/** 开机（工作）状态 */
	public static String PMS_Status = "XX";
	/** 设定（锅温）温度 */
	public static String PMS_SetTemp = "000.0℃";
	/** 客户端数量 */
	public static String PMS_ClientNum = "0";
	/** 错误代码 */
	public static String ERROR_CODE = "XXX";
	/** 智能灶错误信息 */
	public static List<String> PMS_Errors = new ArrayList<String>();
	public static String[] PMS_ErrorDesc = {
			"IGBT温度过高",		//0x0001（1）
			"IGBT过压保护",		//0x0002（2）
			"供电电压异常",		//0x0004（3）
			"过流保护",			//0x0008（4）
			"未检测到锅具",		//0x0010（5）
			"线圈过热保护",		//0x0020（6）
			"控制板通讯异常",	//0x0040（7）
			"干锅保护",			//0x0080（8）
			"长时间无脉冲",		//0x0100（9）
			"浪涌保护",			//0x0200（10）
			"SPIFlash故障",		//0x0400（11）
			"人机板串口异常",	//0x0800（12）
			//警告代码
			"测温元件异常",		//0x1000（13）
			"存储卡异常",		//0x2000（14）
			"称重元件异常",		//0x4000（15）
	};

	/*public static String[] errorDesc = {//20162028
			"  IGBT温度过高  ",		//0x0001
			"  IGBT过压保护  ",		//0x0002
			"  供电电压异常  ",		//0x0004
			"    过流保护    ",		//0x0008
			"  未检测到锅具  ",		//0x0010
			"  线圈过热保护  ",		//0x0020
			" 控制板通讯异常 ",		//0x0040
			"    干锅保护    ",		//0x0080
			"  长时间无脉冲  ",		//0x0100
			"  浪涌保护      ",		//0x0200
			"  SPI Flash故障 ",		//0x0400
			" 人机板串口异常 ",		//0x0800
			//警告代码
			"  测温元件异常  ",		//0x1000
			"  存储卡异常    ",		//0x2000
			"  称重元件异常  ",		//0x4000
	};*/

	/*public static int[] errorCode = {
			0x0001,// IGBT过温
			0x0002,// IGBT过压
			0x0004,// 系统电压异常
			0x0008,// 电流异常
			0x0010,// 无锅
			0x0020,// 线圈过热
			0x0040,// IR(红外测温) Err
			0x0080,// SD错误
			0x0100,// 反演暂时停机
			0x0200 // 串口接收数据错误
	};*/

	public static int[] errorCode = {
			0x0001,//"  IGBT温度过高  ",
			0x0002,//"  IGBT过压保护  ",
			0x0004,//"  供电电压异常  ",
			0x0008,//"    过流保护    ",
			0x0010,//"  未检测到锅具  ",
			0x0020,//"  线圈过热保护  ",
			0x0040,//" 控制板通讯异常 ",
			0x0080,//"    干锅保护    ",
			0x0100,//"  长时间无脉冲  ",
			0x0200,//"  浪涌保护      ",
			0x0400,//"  SPI Flash故障 ",
			0x0800,//" 人机板串口异常 ",
			//警告代码
			0x1000,//"  测温元件异常  ",
			0x2000,//"  存储卡异常    ",
			0x4000,//"  称重元件异常  ",
	};

	public static String[] errorCode_str = {
			"0x0001",//"  IGBT温度过高  ",
			"0x0002",//"  IGBT过压保护  ",
			"0x0004",//"  供电电压异常  ",
			"0x0008",//"    过流保护    ",
			"0x0010",//"  未检测到锅具  ",
			"0x0020",//"  线圈过热保护  ",
			"0x0040",//" 控制板通讯异常 ",
			"0x0080",//"    干锅保护    ",
			"0x0100",//"  长时间无脉冲  ",
			"0x0200",//"  浪涌保护      ",
			"0x0400",//"  SPI Flash故障 ",
			"0x0800",//" 人机板串口异常 ",
			//警告代码
			"0x1000",//"  测温元件异常  ",
			"0x2000",//"  存储卡异常    ",
			"0x4000",//"  称重元件异常  ",
	};

	/*public static String[] errorDesc = { "IGBT过温", "IGBT过压", "系统电压异常", "电流异常", "无锅",
			"线圈过热", "人机串口接收异常", "干锅保护", "过流", "浪涌保护" , "SPI Flash故障", "人机串口发送异常", "IR ERR", "SD卡错误", "称重元件异常"};*/
}
