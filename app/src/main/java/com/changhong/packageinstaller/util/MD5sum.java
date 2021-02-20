package com.changhong.packageinstaller.util;

import com.changhong.packageinstaller.Loger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5sum {
    public static String md5sum(byte[] src) throws Exception {
        return StringTool.toHex(MessageDigest.getInstance("MD5").digest(src));
    }

    public static String md5sum(String fileName) {
        return md5sum(new File(fileName));
    }

    public static String md5sum(File file) {
        String md5 = null;
        try {
            BufferedInputStream e = new BufferedInputStream(new FileInputStream(file));
            byte[] buf = new byte[10240];
            MessageDigest alg = MessageDigest.getInstance("MD5");
            while (true) {
                int iCount1 = e.read(buf);
                if (iCount1 == -1) {
                    break;
                }
                alg.update(buf, 0, iCount1);
            }
            e.close();
            md5 = StringTool.toHex(alg.digest());
        } catch (FileNotFoundException arg11) {
            Loger.w(arg11.toString());
        } catch (NoSuchAlgorithmException arg12) {
            Loger.w(arg12.toString());
        } catch (IOException arg13) {
            Loger.w(arg13.toString());
        } finally {
            Loger.d("MD5sum md5=" + md5);
        }
        return md5;
    }
}
