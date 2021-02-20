package com.changhong.packageinstaller.util;

import android.content.pm.IPackageDeleteObserver.Stub;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Message;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.changhong.packageinstaller.InstallService;

public class ApkTool {
    private static final int INSTALL_COMPLETE = 1;
    private static final int INSTALL_TIME_OUT = 1800000;
    public static PackageManager PKG_MGR = InstallService.mInstance.getPackageManager();
    private static final int UNINSTALL_TIME_OUT = 600000;

    public static Map<String, String> getPkgInfo(String apkPath) {
        Map<String, String> ret = new HashMap(3);
        PackageInfo info = PKG_MGR.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        ret.put("package_name", info.packageName);
        ret.put("version", info.versionName);
        ret.put("md5sum", MD5sum.md5sum(apkPath));
        return ret;
    }

    private static String getPackageName(String apkPath) {
        return PKG_MGR.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES).packageName;
    }

    public static synchronized void matrixGone(String packageName) throws Exception {
        synchronized (ApkTool.class) {
            final Message msg = Message.obtain();
            msg.arg1 = -1;
            msg.obj = null;
            try {
                PKG_MGR.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                PKG_MGR.deletePackage(packageName, new Stub() {
                    public void packageDeleted(String packageName, int returnCode) {
                        msg.what = 1;
                        msg.arg1 = returnCode;
                        msg.obj = "" + packageName;
                    }
                }, 0);
                int tickCount = 0;
                while (msg.obj == null && tickCount < UNINSTALL_TIME_OUT) {
                    tickCount += 500;
                    Thread.sleep(500);
                }
                if (msg.what != 1) {
                    throw new Exception("uninstall failed,timeout=600secs");
                }
                switch (msg.arg1) {
                    case 1:
                    default:
                        throw new Exception("uninstall failed,errcode=" + msg.arg1);
                }
            } catch (NameNotFoundException e) {
                throw new Exception("NameNotFound:" + packageName);
            }
        }
    }

    public static synchronized Message matrixGogogo(String matrixPlace) throws Exception {
        final Message msg;
        synchronized (ApkTool.class) {
            File file = new File(matrixPlace);
            msg = Message.obtain();
            msg.arg1 = 0;
            msg.obj = null;
            if (!file.exists()) {
                msg.what = 1;
                msg.arg1 = Constants.INSTALL_FAILED_EXT_APKFILE_NOT_FOUND;
                msg.obj = "file not exists!";
            } else if (file.isFile()) {
                Uri uri = Uri.fromFile(file);
                int installFlags = 0;
                String nickName = getPackageName(matrixPlace);
                if (TextUtils.isEmpty(nickName)) {
                    msg.what = 1;
                    msg.arg1 = Constants.INSTALL_FAILED_EXT_ERROR_FORMAT;
                    msg.obj = "no PackageName value in apk file!!";
                } else {
                    try {
                        if (PKG_MGR.getPackageInfo(nickName, 0) != null) {
                            installFlags = 0 | 2;
                        }
                    } catch (NameNotFoundException e) {
                    }
                    PKG_MGR.installPackage(uri, new IPackageInstallObserver.Stub() {
                        public void packageInstalled(String packageName, int returnCode) {
                            msg.what = 1;
                            msg.arg1 = returnCode;
                            if (returnCode != 1) {
                                msg.arg2 = Constants.INSTALL_FAILED_EXT_SYSTEM_FAILED;
                            }
                            msg.obj = "" + packageName;
                        }
                    }, installFlags, nickName);
                    int tickCount = 0;
                    while (msg.obj == null && tickCount < INSTALL_TIME_OUT) {
                        tickCount += 500;
                        Thread.sleep(500);
                    }
                    if (msg.what != 1) {
                        msg.what = 1;
                        msg.arg1 = Constants.INSTALL_FAILED_EXT_ERROR_FORMAT;
                        msg.obj = "install failed,timeout=1800secs";
                    }
                }
            } else {
                msg.what = 1;
                msg.arg1 = Constants.INSTALL_FAILED_EXT_IS_NOT_FILE;
                msg.obj = "not a apk file!";
            }
        }
        return msg;
    }
}
