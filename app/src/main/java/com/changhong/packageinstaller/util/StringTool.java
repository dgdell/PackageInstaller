package com.changhong.packageinstaller.util;

import java.io.ByteArrayOutputStream;

public class StringTool {
    public static String null2Empty(String inStr) {
        return inStr == null ? "" : inStr;
    }

    public static final String toHex(byte[] hash) {
        StringBuffer buf = new StringBuffer(hash.length * 2);
        for (int i = 0; i < hash.length; i++) {
            if ((hash[i] & 255) < 16) {
                buf.append("0");
            }
            buf.append(Long.toString((long) (hash[i] & 255), 16));
        }
        return buf.toString().toUpperCase();
    }

    public static final byte[] toByte(String strHex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int posStart = 0;
        while (posStart < strHex.length()) {
            int posEnd1 = posStart + 2;
            baos.write(Integer.parseInt(strHex.substring(posStart, posEnd1), 16) & 255);
            posStart = posEnd1;
        }
        return baos.toByteArray();
    }
}
