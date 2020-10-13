package com.zrkd.blemodule.utils;

public class SumCheckUtil {
    private static int sum = 0;
    public static String makeChecksum(String hexdata) {
        if (hexdata == null || hexdata.equals("")) {
            return "00";
        }
        hexdata = hexdata.replaceAll(" ", "");
        int total = 0;
        int len = hexdata.length();
        if (len % 2 != 0) {
            return "00";
        }
        int num = 0;
        while (num < len) {
            String s = hexdata.substring(num, num + 2);
            total += Integer.parseInt(s, 16);
            num = num + 2;
        }
        LogUtil.d("sum:" + total);
        int checkNum = total * 86 + 90;
        String checkNumString=intToHex(checkNum);
        LogUtil.d("checkNum:"+checkNumString);
        return checkNumString.substring(checkNumString.length()-2);
    }

    private static String intToHex(int n) {
        StringBuffer s = new StringBuffer();
        String a;
        char []b = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        while(n != 0){
            s = s.append(b[n%16]);
            n = n/16;
        }
        a = s.reverse().toString();
        if (a.length()<2){
            a="0" +a;
        }
        return a;
    }


}
