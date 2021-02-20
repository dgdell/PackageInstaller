package com.changhong.packageinstaller;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.changhong.aidl.InstallCallback;
import com.changhong.aidl.InstallService.Stub;
import com.changhong.aidl.UnInstallCallback;
import com.changhong.packageinstaller.util.ApkTool;
import com.changhong.packageinstaller.util.Constants;
import com.changhong.packageinstaller.util.KeyEnums;
import com.changhong.packageinstaller.util.MD5sum;

public class InstallService extends Service {
    public static final int INSTALL_SUCCESS = 0;
    public static InstallService mInstance = null;
    protected static String mXmppAccount = null;
    private final InstallBinder mBinder = new InstallBinder();

    public class CmccAuthFailedException extends Exception {
        public CmccAuthFailedException(String message) {
            super(message);
        }
    }

    public class CmccRequestException extends Exception {
        public CmccRequestException(String message) {
            super(message);
        }
    }

    public class InstallBinder extends Stub {
        private String accessToken = null;
        private Handler handler = null;
        private boolean isReload = true;
        private String openId = null;
        private String reserved = null;
        private String tvid = null;

        public boolean isReload() {
            return this.isReload;
        }

        public void setReload(boolean isReload) {
            this.isReload = isReload;
        }

        public String getReserved() {
            return this.reserved;
        }

        public void setReserved(String reserved) {
            this.reserved = reserved;
        }

        public String getAccessToken() {
            return this.accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getTvid() {
            return this.tvid;
        }

        public void setTvid(String tvid) {
            this.tvid = tvid;
        }

        public String getOpenId() {
            return this.openId;
        }

        public void setOpenId(String openId) {
            this.openId = openId;
        }

        public InstallService getService() {
            return InstallService.this;
        }

        public void setHandler(Handler handler) {
            this.handler = handler;
        }

        public Handler getHandler() {
            return this.handler;
        }

        public void install(String apkPath, String appName, String appToken, InstallCallback cb) {
            getService().install(apkPath, appName, appToken, cb);
        }

        public void uninstall(String packageName, String appName, String appToken, UnInstallCallback cb) {
            getService().uninstall(packageName, cb);
        }
    }

    public IBinder onBind(Intent intent) {
        Loger.d("start IBinder~~~");
        return this.mBinder;
    }

    public void onCreate() {
        Loger.d("start onCreate~~~");
        super.onCreate();
        mInstance = this;
    }

    public void onDestroy() {
        Loger.d("start onDestroy~~~");
        super.onDestroy();
    }

    public boolean onUnbind(Intent intent) {
        Loger.d("start onUnbind~~~");
        return super.onUnbind(intent);
    }

    public void install(String apkPath, String appName, String appCode, InstallCallback cb) {
        Loger.d("apkPath=" + apkPath + ",install,cb=" + cb + ",appName=" + appName + ",appCode=" + appCode);
        final String str = apkPath;
        final String str2 = appName;
        final String str3 = appCode;
        final InstallCallback installCallback = cb;
        new Timer().schedule(new TimerTask() {
            public void run() {
                Message retMsg = Message.obtain();
                try {
                    File file = new File(str);
                    if (!file.exists()) {
                        throw new FileNotFoundException("file not exists!");
                    } else if (file.isFile()) {
                        Loger.d("md5=" + MD5sum.md5sum((str2 + str3).getBytes()));
                        String appToken = MD5sum.md5sum((str2 + str3).getBytes()).substring(8, 24);
                        Map<String, String> apkInfos = ApkTool.getPkgInfo(str);
                        apkInfos.put("appName", str2);
                        apkInfos.put("appToken", appToken);
                        Log.i("InstallService:", "InstallService:prepare");
                        retMsg = ApkTool.matrixGogogo(str);
                        if (1 == retMsg.arg1) {
                            retMsg.arg1 = 0;
                        }
                        if (retMsg.arg1 == 0 || retMsg.arg2 != Constants.INSTALL_FAILED_EXT_SYSTEM_FAILED) {
                            InstallService.this.invokeCallback(installCallback, retMsg.arg1, (String) retMsg.obj);
                        } else {
                            InstallService.this.invokeCallback(installCallback, retMsg.arg2, (String) retMsg.obj);
                        }
                    } else {
                        throw new FileNotFoundException("not a apk file!");
                    }
                } catch (FileNotFoundException FE) {
                    retMsg.arg1 = Constants.INSTALL_FAILED_EXT_APKFILE_NOT_FOUND;
                    retMsg.obj = FE.getMessage();
                    FE.printStackTrace();
                    if (1 == retMsg.arg1) {
                        retMsg.arg1 = 0;
                    }
                    if (retMsg.arg1 == 0 || retMsg.arg2 != Constants.INSTALL_FAILED_EXT_SYSTEM_FAILED) {
                        InstallService.this.invokeCallback(installCallback, retMsg.arg1, (String) retMsg.obj);
                    } else {
                        InstallService.this.invokeCallback(installCallback, retMsg.arg2, (String) retMsg.obj);
                    }
                } catch (CmccRequestException RE) {
                    retMsg.arg1 = -20005;
                    retMsg.obj = RE.getMessage();
                    RE.printStackTrace();
                    if (1 == retMsg.arg1) {
                        retMsg.arg1 = 0;
                    }
                    if (retMsg.arg1 == 0 || retMsg.arg2 != Constants.INSTALL_FAILED_EXT_SYSTEM_FAILED) {
                        InstallService.this.invokeCallback(installCallback, retMsg.arg1, (String) retMsg.obj);
                    } else {
                        InstallService.this.invokeCallback(installCallback, retMsg.arg2, (String) retMsg.obj);
                    }
                } catch (CmccAuthFailedException AE) {
                    retMsg.arg1 = -20005;
                    retMsg.obj = AE.getMessage();
                    AE.printStackTrace();
                    if (1 == retMsg.arg1) {
                        retMsg.arg1 = 0;
                    }
                    if (retMsg.arg1 == 0 || retMsg.arg2 != Constants.INSTALL_FAILED_EXT_SYSTEM_FAILED) {
                        InstallService.this.invokeCallback(installCallback, retMsg.arg1, (String) retMsg.obj);
                    } else {
                        InstallService.this.invokeCallback(installCallback, retMsg.arg2, (String) retMsg.obj);
                    }
                } catch (Exception E) {
                    Loger.e(E.toString());
                    retMsg.arg1 = Constants.INSTALL_FAILED_EXT_UNKNOW;
                    retMsg.obj = E.getMessage();
                    E.printStackTrace();
                    if (1 == retMsg.arg1) {
                        retMsg.arg1 = 0;
                    }
                    if (retMsg.arg1 == 0 || retMsg.arg2 != Constants.INSTALL_FAILED_EXT_SYSTEM_FAILED) {
                        InstallService.this.invokeCallback(installCallback, retMsg.arg1, (String) retMsg.obj);
                    } else {
                        InstallService.this.invokeCallback(installCallback, retMsg.arg2, (String) retMsg.obj);
                    }
                } catch (Throwable th) {
                    Throwable th2 = th;
                    if (1 == retMsg.arg1) {
                        retMsg.arg1 = 0;
                    }
                    if (retMsg.arg1 == 0 || retMsg.arg2 != Constants.INSTALL_FAILED_EXT_SYSTEM_FAILED) {
                        InstallService.this.invokeCallback(installCallback, retMsg.arg1, (String) retMsg.obj);
                    } else {
                        InstallService.this.invokeCallback(installCallback, retMsg.arg2, (String) retMsg.obj);
                    }
                }
            }
        }, 10);
    }

    public void uninstall(final String packageName, final UnInstallCallback cb) {
        new Timer().schedule(new TimerTask() {
            public void run() {
                int result = 0;
                String msg = "OK";
                try {
                    ApkTool.matrixGone(packageName);
                } catch (Exception E) {
                    result = 1;
                    msg = E.getMessage();
                } finally {
                    InstallService.this.invokeCallback(cb, result, msg);
                }
            }
        }, 10);
    }

    private void invokeCallback(Object cb, int result, String msg) {
        Loger.d("Callback type=" + cb + ",result=" + result + ",msg=" + msg);
        if (cb instanceof InstallCallback) {
            try {
                ((InstallCallback) cb).onInstallResult(result, msg);
            } catch (Exception E) {
                Loger.w(E.toString());
            }
        } else if (cb instanceof UnInstallCallback) {
            try {
                ((UnInstallCallback) cb).onUnInstallResult(result, msg);
            } catch (Exception E2) {
                Loger.w(E2.toString());
            }
        }
    }

    private void checkResult(KeyEnums key, String code) throws Exception {
        if (TextUtils.isEmpty(code) || !code.equals("0")) {
            throw new Exception("fail:key=" + key + "code=" + code);
        }
    }

    private void installApk(String path) {
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, "数据错误,无法安装...", Toast.LENGTH_LONG).show();
            return;
        }
        Uri uri = Uri.parse(path);
        Intent intent = new Intent();
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}
