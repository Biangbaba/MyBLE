package com.zrkd.blemodule.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 字符串转字节工具类
 */
public class HexUtil {

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static String toHexString(String value) {
        int ve = Integer.parseInt(value);
        String hv = Integer.toHexString(ve);
        if (hv.length() < 2) {
            hv = "0"+hv;
        }
        return hv;
    }


    public static String bytesToHexString(byte[] b) {
        if (b.length == 0) {
            return null;
        }
        StringBuilder sbs = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            int value = b[i] & 0xFF;
            sbs.append(value+"  ");
            String hv = Integer.toHexString(value);
            if (hv.length() < 2) {
                sb.append(0);
            }
            sb.append(hv);
        }
//        LogUtils.e("蓝牙传输过来的数据是============="+sbs);
//        LogUtils.e("转换成十六进制后的数据是============="+sb);
        return sb.toString();
    }


    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private static byte charToByte2(char c) {
        return (byte) "0123456789abcdef".indexOf(c);
    }

    /**
     * 用于建立十六进制字符的输出的小写字符数组
     */
    private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * 用于建立十六进制字符的输出的大写字符数组
     */
    private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data
     *            byte[]
     * @return 十六进制char[]
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data
     *            byte[]
     * @param toLowerCase
     *            <code>true</code> 传换成小写格式 ， <code>false</code> 传换成大写格式
     * @return 十六进制char[]
     */
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data
     *            byte[]
     * @param toDigits
     *            用于控制输出的char[]
     * @return 十六进制char[]
     */
    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data
     *            byte[]
     * @return 十六进制String
     */
    public static String encodeHexStr(byte[] data) {
        return encodeHexStr(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data
     *            byte[]
     * @param toLowerCase
     *            <code>true</code> 传换成小写格式 ， <code>false</code> 传换成大写格式
     * @return 十六进制String
     */
    public static String encodeHexStr(byte[] data, boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data
     *            byte[]
     * @param toDigits
     *            用于控制输出的char[]
     * @return 十六进制String
     */
    protected static String encodeHexStr(byte[] data, char[] toDigits) {
        return new String(encodeHex(data, toDigits));
    }

    /**
     * 将十六进制字符数组转换为字节数组
     *
     * @param data
     *            十六进制char[]
     * @return byte[]
     * @throws RuntimeException
     *             如果源十六进制字符数组是一个奇怪的长度，将抛出运行时异常
     */
    public static byte[] decodeHex(char[] data) {

        int len = data.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * 将十六进制字符转换成一个整数
     *
     * @param ch
     *            十六进制char
     * @param index
     *            十六进制字符在字符数组中的位置
     * @return 一个整数
     * @throws RuntimeException
     *             当ch不是一个合法的十六进制字符时，抛出运行时异常
     */
    protected static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch
                    + " at index " + index);
        }
        return digit;
    }

    /**
     * 将截取16进制两个字节字符串转换为byte类型
     * @param str
     * @return
     */
    public static int getCode(String str) {
        int h = Integer.parseInt(str.substring(0, 1), 16);
        int l = Integer.parseInt(str.substring(1, 2), 16);
//		byte H = (byte) h;
//		byte L = (byte) l;
//		int s = H<<4 | L;
        byte s = (byte) (((byte) h << 4) + (byte) l);
        return s;

    }

    /**
     *  假设 传递进来的值是：byte [] b = {0,8,80,75,6,78,56,99,99,7,6};
     * @param
     * @return
     */
    public static String getDatas(byte [] b){

//        String b = "0008504b066d0000000706";
        int position = 16;
        int position1 = 1;
        int position2 = 16*16*16;
        int position3 = 16*16;
        int position4 = 16*16*16*16*16;
        int position5 = 16*16*16*16;
        int position6 = 16*16*16*16*16*16*16;
        int position7 = 16*16*16*16*16*16;

        String input = bytesToHexString(b);
        input = input.substring(10,18);
        List<Long> list = new ArrayList<>();
        for(int i=0;i<input.length();i++){
            char sss = input.charAt(i);
            long ew= charToByte2(sss);
            list.add(ew);
        }

        long diff =  list.get(0) * position + list.get(1) * position1+ list.get(2) * position2+ list.get(3) * position3
                + list.get(4) * position4+ list.get(5) * position5+ list.get(6) * position6+ list.get(7) * position7;
        long year=diff / (60 * 60 * 24*365);
        long month=(diff - year * (60 * 60 * 24*365)) / (60 * 60 * 24*30);
        long days = (diff - year * 60 * 60 * 24*365-month*60*60*24*30) / ( 60 * 60 * 24);
        long hours = ((diff - year * 60 * 60 * 24*365-month*60*60*24*30) - days * (60 * 60 * 24)) / (60 * 60);
        long minutes = (diff - year * 60 * 60 * 24*365-month*60*60*24*30 - days * (60 * 60 * 24) - hours* 60 * 60)/ 60;
        long second = (diff - year * 60 * 60 * 24*365-month*60*60*24*30) - days * (60 * 60 * 24) - hours* (60 * 60)- minutes * 60;
        StringBuffer buffer = new StringBuffer();

        if(year>0){
            buffer.append( year + "年");
        }
        if(month>0){
            buffer.append( month + "月");
        }
        if(days>0) {
            buffer.append( days + "天");
        }
        if(hours!=0){
            buffer.append(hours+"小时");
        }
        if (minutes!=0) {
            buffer.append(minutes+"分钟");
        }
        if (second!=0){
            buffer.append(second+"秒");
        }
        return buffer.toString();
    }

    public static byte[] hex2byte(String hex) {
        String digital = "0123456789ABCDEF";
        char[] hex2char = hex.toCharArray();
        byte[] bytes = new byte[hex.length() / 2];
        int temp;
        for (int i = 0; i < bytes.length; i++) {
            temp = digital.indexOf(hex2char[2 * i]) * 16;
            temp += digital.indexOf(hex2char[2 * i + 1]);
            bytes[i] = (byte) (temp & 0xff);        }
        return bytes;
    }
    /**
     * @param: [c]
     * @return: int
     * @description:将字符转化为数字
     */
    public static int charToDecimal(char c){
        if(c >= 'A' && c <= 'F'){
            return 10 + c - 'A';
        } else{
            return c - '0';
        }

    }

    public static void main(String[] args) {
        String srcStr = "待转换字符串";
        String encodeStr = encodeHexStr(srcStr.getBytes());
        String decodeStr = new String(decodeHex(encodeStr.toCharArray()));
        System.out.println("转换前：" + srcStr);
        System.out.println("转换后：" + encodeStr);
        System.out.println("还原后：" + decodeStr);

//        byte [] b = {0,8,80 ,75 ,6 ,63 ,0 ,0, 0};
//        byte [] b = {0,8,80 ,75 ,6 ,78 ,56 ,02, 00};
//        byte [] b = {0,8,80,75,6,78,56,99,99,7,6};
//        getData(b);

//        byte [] b = {0,5,80,68,3,34,111,77};  // 十进制的数据
        // ,6f,4d
        byte [] b = {(byte)0x44,(byte)0x41,(byte)0x42,(byte)0x43,(byte)0x44,(byte)0x78,(byte)0x56,(byte)0x34,(byte)0x12,(byte)0x80,(byte)0xa0,(byte)0x4d,(byte)0x5d};

//        public static String CRC16_Check(byte Pushdata[],int length) { 44,41,42,43,44,78,56,34,12,80,a0,4d,5d,e,df
//        byte[] aa = {0x02, 0x05, 0x00, 0x03, (byte) 0xff, 0x00,0x00,0x01 };
        //

//        String v = HexUtil.getDatas(b);
//        String title = "获取喷雾时长成功：当前已使用" + v + "  ";
//        System.out.println("结果===========================" + title);
//        char [] c = buffer.toString().toCharArray();
//        for(int i=0;i<c.length;i++){
//            char ch=c[i];
//            // 判断是否是数字
//            if (Character.isDigit(ch)){
//                int num = Integer.parseInt(String.valueOf(ch));
//            }
//
//            long t= sw*16;
//            System.out.println("结果===========================" + t);
//        }
    }


}
