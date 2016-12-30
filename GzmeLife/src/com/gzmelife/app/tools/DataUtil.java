package com.gzmelife.app.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtil {
	/** 若number<10,在前面加0 */
	public static String addZeroBeforeNumber(int number) {
		return number > 9 ? "" + number : "0" + number;
	}
	
	/**
	 * 根据B单位的文件大小，得到M单位的格式，取一位小数
	 */
	public static String getFileSizeOfMBFromByte(long size) {
		size /= 1024; // 得到KB
		double result = 1.0 * size / 1024;
		result = (double)(Math.round(result * 10)) / 10;
		return result + "";
	}

	/** 根据传参判定字符串是否包含中文：false=不包含*/
	public final static boolean isHaveChinese(String s){
		String regEx = "[\\u4e00-\\u9fa5]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(s);
		while (m.find()) {
			return true;
		}
		return false;
	}

	/** 根据传参判定字符串是否为空字符串：false=空*/
	public final static boolean isnotnull(Object object) {
		if (object == null) {
			return false;
		}
		if (object.toString().trim().equals("")) {
			return false;
		}
		return true;

	}
	/**
     * 描述：手机号格式验证.
     *
     * @param str 指定的手机号码字符串
     * @return 是否为手机号码格式:是为true，否则false
     */
 	public static Boolean isMobileNo(String str) {
 		Boolean isMobileNo = false;
 		try {
			Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9])|(17[7]))\\d{8}$");
			Matcher m = p.matcher(str);
			isMobileNo = m.matches();
		} catch (Exception e) {
			e.printStackTrace();
		}
 		return isMobileNo;
 	}
 	
 	public static byte getIpEndByte(int i) {
//		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
//				+ "." + (i >> 24 & 0xFF);
		return (byte) (i >> 24 & 0xFF);
	}
	
 	public static String getIp(int i) {
 		String ip = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
 		MyLog.i("ip=" + ip);
		return ip;
	}

	/**
	 * byte转十进制
	 * @param b 传过来的数据
	 * @return
     */
 	public static int hexToTen(byte b) {
 		return b & 0xff;
//    	int temp = b;
//		if (temp < 0) { 
//			temp += 256;
//		}
//		return temp;
    }
/*	public static int hexToTen(byte b) {
		return b & 0xff;
//    	int temp = b;
//		if (temp < 0) {
//			temp += 256;
//		}
//		return temp;
	}*/

	/**
	 * 16进制字符串转二进制字符串
	 *
	 * @param hexString 哈希字符串
	 * @return          二进制字符串
	 */
	public static String hexString2binaryString(String hexString) {
		if (hexString == null || hexString.length() % 2 != 0)
			return "";
		String bString = "", tmp;
		for (int i = 0; i < hexString.length(); i++) {
			tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hexString.substring(i, i + 1), 16));
			bString += tmp.substring(tmp.length() - 4);
		}
		return bString;
	}

	/**
	 * 字节转哈希字符串
	 *
	 * @param b 需要转换的byte
	 * @return  相应的String
	 */
	public static String byte2HexString(byte b) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HexCode[(b >>> 4) & 0x0f]);
		buffer.append(HexCode[b & 0x0f]);
		return buffer.toString();
	}

	/***
	 * 字节转哈希字符串
	 *
	 * @param b 需要转的字节
	 * @return  哈希字符串
	 */
	public static String bytetoHexString(byte b) {
		String result = Integer.toHexString(b & 0xFF);
		if (result.length() == 1) {
			result = '0' + result;
		}
		return result;
	}

	/** 哈希表 */
	private static char[] HexCode = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	/**
	 * 字节转整数
	 *
	 * @param res   需要转换的字节数组
	 * @return      整数
	 */
	public static int bytes2int(byte[] res) {// 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000
		int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00) | ((res[2] << 24) >>> 8) | (res[3] << 24);// “|” 表示按位或
		return targets;
	}

	/**
	 * 字节数组转对象// bytearray to object
	 *
	 * @param bytes 需要转换的字节数组
	 * @return      对象
	 */
	public static Object byte2Object(byte[] bytes) {
		Object obj = null;
		try {
			ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
			ObjectInputStream oi = new ObjectInputStream(bi);
			obj = oi.readObject();
			bi.close();
			oi.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * 对象转字节数组// object to bytearray
	 *
	 * @param obj   需要转换的对象
	 * @return      相应的字节数组
	 */
	public static byte[] object2Byte(java.lang.Object obj) {
		byte[] bytes = null;
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(obj);
			bytes = bo.toByteArray();
			bo.close();
			oo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bytes;
	}

}
