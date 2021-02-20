package com.changhong.packageinstaller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.changhong.aidl.InstallCallback;
import com.changhong.aidl.InstallService;
import com.changhong.aidl.InstallService.Stub;

public class InstallHelper {
    private static final String TAG = InstallHelper.class.getName();
    private final String INSTALL_SERVICE_ACTION = "com.changhong.shcmcc.installservice";
    private int count = 0;
    private boolean installTag = false;
    private Context mContext;
    private ServiceConnection mInstallConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            InstallHelper.this.mInstallService = Stub.asInterface(service);
            InstallHelper.this.installTag = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            InstallHelper.this.mInstallService = null;
        }
    };
    private InstallService mInstallService;

    public InstallHelper(Context mContext) {
        this.mContext = mContext;
        bindInstallService();
    }

    private void bindInstallService() {
        this.mContext.bindService(new Intent("com.changhong.shcmcc.installservice"), this.mInstallConn, Context.BIND_AUTO_CREATE);
    }

    public void install(String apkPath, String apkName, String appCode, InstallCallback cb) {
        final Timer mTimer = new Timer();
        final String str = apkPath;
        final String str2 = apkName;
        final String str3 = appCode;
        final InstallCallback installCallback = cb;
        mTimer.schedule(new TimerTask() {
            public void run() {
                InstallHelper.this.count = InstallHelper.this.count + 1;
                if (InstallHelper.this.installTag) {
                    mTimer.cancel();
                    InstallHelper.this.installRun(str, str2, str3, installCallback);
                }
                if (InstallHelper.this.count > 100) {
                    mTimer.cancel();
                }
            }
        }, 1000, 100);
    }

    private void installRun(String apkPath, String apkName, String appCode, InstallCallback cb) {
        if (cb == null) {
            cb = new InstallCallback.Stub() {
                public void onInstallResult(int result, String msg) throws RemoteException {
                    Log.w(InstallHelper.TAG, "install result: " + result + " msg: " + msg);
                }
            };
        }
        try {
            if (this.mInstallService == null) {
                Log.w(TAG, "install service is null, could not install");
                return;
            }
            Log.w(TAG, "Start install service");
            this.mInstallService.install(apkPath, apkName, appCode, cb);
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.w(TAG, "install exception " + e.getMessage());
        }
    }

    public void install(Context mContext, String apkPath, String apkName) {
        if (apkPath != null && apkName != null) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(Uri.fromFile(new File(apkPath, apkName)), "application/vnd.android.package-archive");
            mContext.startActivity(intent);
        }
    }
}
