//package com.gzmelife.app.device;
//
//import com.gzmelife.app.tools.CountDownTimerUtil;
//import com.gzmelife.app.tools.MyLogger;
//
///**
// * Created by HHD on 2016/11/2.
// */
//
// /**
// * 重新发送指令、唤醒指令的计时类
// */
//public class ResendTimeCount extends CountDownTimerUtil {
//
//     MyLogger HHDLog = MyLogger.HHDLog();
//
//    /**
//     * 重新发送指令、唤醒指令的计时
//     *
//     * @param millisInFuture 倒计时的总数
//     * @param countDownInterval 间隔多少微秒调用一次onTick方法
//     */
//    public ResendTimeCount(long millisInFuture, long countDownInterval) {
//        super(millisInFuture, countDownInterval);
//        HHDLog.e(" ");
//    }
//
//    @Override
//    public void onTick(long millisUntilFinished) {
//        //HHDLog.e(" ");
//        if (Config.resendTime % 4==0){
//            //HHDLog.e("测试重发心跳是否正常");
//        }
//        if (Config.resendTime==1){
//            if (Config.真发_250后){
//                /** TODO:1.8 超时（300后） */
//                HHDLog.d("1.8 超时（300后）");
//                /** TODO:1.9 改isFirst（false） */
//                Config.isFirst=false;
//                HHDLog.d("1.9 改isFirst（false）="+Config.isFirst);
//                /** TODO:1.10 重（真）发相同指令 */
//                sendFrame(Config.bufLastTemp);
//                //HHDLog.e("1.10 重（真）发相同指令="+ byte2HexString(bufLastTemp[3]) + " " + byte2HexString(bufLastTemp[4]));
//            }
//        }
//        if (Config.resendTime==5){
//            if (Config.唤醒_1250后){
//                //HHDLog.e("唤醒 F8 00");
//                Config.isFirst=false;
//                connectHandler.sendEmptyMessage(0);//重新握手连接
//            }
//        }
//        if (Config.resendTime==17 || Config.resendTime==29 || Config.resendTime==41){
//            if (Config.不再重发_3次){
//                //HHDLog.e("第一秒、二秒、三秒重发指令（重发三次）");
//                Config.isFirst=false;
//                sendFrame(Config.bufLastTemp);
//            }
//        }
//
//        if (Config.resendTime % 10 == 0) {
//            //
//        }
//
//        Config.resendTime++;
//    }
//
//    @Override
//    public void onFinish() {
//        HHDLog.e(" ");
//        //
//    }
//}
