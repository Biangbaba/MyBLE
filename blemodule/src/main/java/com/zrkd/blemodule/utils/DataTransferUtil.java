package com.zrkd.blemodule.utils;

import java.text.SimpleDateFormat;

public class DataTransferUtil {
    /**
     * 得到当前的日期时间(Y - M - D H:M:S)
     *
     * @param date
     * @return
     */
    public static String getYMDHMSDate(long date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }
    /**
    * @description 将十六进制的年月日时分秒转换成yyyy-MM-dd HH:mm:ss格式
    * @param hexDate
    * @return
    */
    public static String transferToYMDHMSDate(String hexDate){
        if (hexDate.length()!=14){
            LogUtil.d("传入数据长度:"+hexDate.length()+";有误,无法转换");
            return "传入数据长度有误,无法转换";
        }
        String date=hexDate.replaceAll(" ","");
        String hexYear=date.substring(0,4);
        String hexMonth=date.substring(4,6);
        String hexDay=date.substring(6,8);
        String hexHour=date.substring(8,10);
        String hexMinute=date.substring(10,12);
        String hexSecond=date.substring(12);
        String YMDHMSDate=transferHexYearToString(hexYear)+"-"+hexToInt(hexMonth)+"-"+hexToInt(hexDay)+" "+hexToInt(hexHour)+":"+hexToInt(hexMinute)+":"+hexToInt(hexSecond);
        return YMDHMSDate;
    }

    /**
     * @param hexDate 十六进制的年份
     * @return dateString 年份字符串
     * @description 将2字节的十六进制字符串转化成十进制字符串，主要用于年份转换
     */
    public static String transferHexYearToString(String hexDate) {
        String realHexYear = hexDate.replaceAll(" ", "");
        StringBuffer sb=new StringBuffer();
        for (int i=0;i<4;i++){
            sb.append("0"+realHexYear.substring(i,i+1));
        }
        int dateString = 0;
        if (realHexYear.length() != 4) {
            return "The length is invalid";
        } else {
            byte[] year = new byte[4];
            year = HexUtil.hexStringToBytes(String.valueOf(sb));
            dateString = year[2] * 16 * 16 * 16 + year[3] * 16 * 16 + year[0] * 16 + year[1];
            LogUtil.d("年份：" + dateString);
            return String.valueOf(dateString);
        }
    }
    /**
    * @description 将一字节的十六进制（即类似1f）转换成十进制
    * @param hexString
    * @return
    */
    public static String hexToInt(String hexString) {
        if (hexString.length()!=2){
            LogUtil.d("转换出错："+hexString);
            return "转换出错："+hexString;
        }
        byte[] data=HexUtil.hexStringToBytes(hexString);
        String decimalString =String.valueOf(data[0]);
        return decimalString;
    }
    /**
    * @description 将4字节的十六进制转化成十进制，主要用于步数转换
    * @param hexString 十六进制字符串
    * @return
    */
    public static String stepTransfer(String hexString){
        String data=hexString.replaceAll(" ","");
        StringBuffer sb=new StringBuffer();
        for (int i=0;i<8;i++){
            sb.append("0"+data.substring(i,i+1));
        }
        double stepCount = 0;
        if (data.length() != 8) {
            return "The length:"+data.length()+" is invalid";
        } else {
            byte[] bytes = new byte[8];
            bytes = HexUtil.hexStringToBytes(String.valueOf(sb));
            for (int i=0;i<8;i++){
                if (i%2==0){
                    stepCount+=bytes[i]*Math.pow(16,i+1);
                }else {
                    stepCount+=bytes[i]*Math.pow(16,i-1);
                }
            }
            LogUtil.d("步数：" + (int)stepCount);
            return String.valueOf((int)stepCount);
        }
    }
}
