package com.zrkd.blemodule.utils;

public class ByteDataConvertUtil {
    private static String format = "UTF-8";

    public static int a(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = bytes.length - 1; i >= 0; --i) {
            int var3 = bytes[i] & 255;
            String var4 = Integer.toHexString(var3);
            if (var4.length() < 2) {
                stringBuilder.append(0);
            }

            stringBuilder.append(var4);
        }

        return Integer.valueOf(stringBuilder.toString(), 16);
    }

    public ByteDataConvertUtil() {
    }

    public static String b(byte[] var0) {
        StringBuilder var1 = new StringBuilder();
        if (var0 != null && var0.length > 0) {
            for (int var2 = 0; var2 < var0.length; ++var2) {
                int var3 = var0[var2] & 255;
                String var4 = Integer.toHexString(var3);
                if (var4.length() < 2) {
                    var1.append(0);
                }

                var1.append(var4);
            }

            return var1.toString();
        } else {
            return null;
        }
    }

    public static byte a(int var0) {
        return (byte) (var0 & 255);
    }

    public static int a(byte var0) {
        return var0 >= 0 ? var0 : 256 + var0;
    }

    public static byte[] a(int var0, int var1) {
        byte[] var2 = new byte[var1];
        int var3 = var1 - 1;

        for (int var4 = 0; var3 >= 0; ++var4) {
            var2[var4] = (byte) (var0 >> 8 * var3);
            --var3;
        }

        return var2;
    }

    public static byte[] b(int var0, int var1) {
        byte[] var2 = a(var0, var1);
        byte[] var3 = new byte[var2.length];

        for (int var4 = 0; var4 < var3.length; ++var4) {
            var3[var4] = var2[var2.length - 1 - var4];
        }

        return var3;
    }

    public static int a(byte[] var0, int var1, int var2) {
        int var3 = 0;
        int var4 = var2 - 1;

        for (int var5 = var1; var4 >= 0; ++var5) {
            var3 = var3 << 8 | var0[var5] & 255;
            --var4;
        }

        return var3;
    }

    public static byte[] b(int var0) {
        byte var1 = (byte) var0;
        byte[] var2 = new byte[8];

        for (int var3 = 0; var3 <= 7; ++var3) {
            var2[var3] = (byte) (var1 & 1);
            var1 = (byte) (var1 >> 1);
        }

        return var2;
    }

    public static int c(byte[] var0) {
        int var1 = var0.length;
        int var2 = 0;

        for (int var3 = var1 - 1; var3 >= 0; --var3) {
            var2 += var0[var3] << var1 - 1 - var3;
        }

        return var2;
    }
}

