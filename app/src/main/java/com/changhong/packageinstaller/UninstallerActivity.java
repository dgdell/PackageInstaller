package com.changhong.packageinstaller;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * 作者:libeibei
 * 创建日期:20190410
 * 类说明:app卸载弹窗Activity
 **/
public class UninstallerActivity extends Activity implements OnClickListener, OnCancelListener {
    private static final int DLG_BASE = 0;
    private static final int DLG_APP_NOT_FOUND = 1;
    private static final int DLG_UNINSTALL_FAILED = 2;
    private static final String TAG = "UninstallerActivity";
    private boolean localLOGV = false;
    private ApplicationInfo mAppInfo;
    private Button mCancel;
    private Button mOk;
    PackageManager mPm;

    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DLG_APP_NOT_FOUND:
                return new Builder(this).setTitle(R.string.app_not_found_dlg_title).setIcon(R.drawable.ic_fail).setMessage(R.string.app_not_found_dlg_text).setNeutralButton(getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UninstallerActivity.this.setResult(1);
                        UninstallerActivity.this.finish();
                    }
                }).create();
            case DLG_UNINSTALL_FAILED:
                CharSequence appTitle = this.mPm.getApplicationLabel(this.mAppInfo);
                return new Builder(this).setTitle(R.string.uninstall_failed).setIcon(R.drawable.ic_fail).setMessage(getString(R.string.uninstall_failed_msg, new Object[]{appTitle.toString()})).setNeutralButton(getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UninstallerActivity.this.setResult(1);
                        UninstallerActivity.this.finish();
                    }
                }).create();
            default:
                return null;
        }
    }

    private void startUninstallProgress() {
        Intent newIntent = new Intent("android.intent.action.VIEW");
        newIntent.putExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO, mAppInfo);
        if (getIntent().getBooleanExtra("android.intent.extra.RETURN_RESULT", false)) {
            newIntent.putExtra("android.intent.extra.RETURN_RESULT", true);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        newIntent.setClass(this, UninstallAppProgress.class);
        startActivity(newIntent);
        finish();
    }

    public void onCreate(Bundle icicle) {
        boolean isUpdate = true;
        super.onCreate(icicle);
        Uri packageURI = getIntent().getData();
        String packageName = packageURI.getEncodedSchemeSpecificPart();
        if (packageName == null) {
            Log.e(TAG, "Invalid package name:" + packageName);
            showDialog(1);
            return;
        }
        Log.i(TAG,"onCreate uninstall package name:" + packageName);
        this.mPm = getPackageManager();
        boolean errFlag = false;
        try {
            this.mAppInfo = this.mPm.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            errFlag = true;
        }
        String className = packageURI.getFragment();
        if (className != null) {
            try {
                ActivityInfo activityInfo = this.mPm.getActivityInfo(new ComponentName(packageName, className), 0);
            } catch (NameNotFoundException e2) {
                errFlag = true;
            }
        }
        if (this.mAppInfo == null || errFlag) {
            Log.e(TAG, "Invalid packageName or componentName in " + packageURI.toString());
            showDialog(1);
            return;
        }
        if ((this.mAppInfo.flags & 128) == 0) {
            isUpdate = false;
        }
        setContentView(R.layout.uninstall_confirm);
        TextView confirm = (TextView) findViewById(R.id.uninstall_confirm);
        if (isUpdate) {
            setTitle(R.string.uninstall_update_title);
            confirm.setText(R.string.uninstall_update_text);
        } else {
            setTitle(R.string.uninstall_application_title);
            confirm.setText(R.string.uninstall_application_text);
        }
        PackageUtil.initSnippetForInstalledApp(this, this.mAppInfo, findViewById(R.id.uninstall_activity_snippet));
        this.mOk = (Button) findViewById(R.id.ok_button);
        this.mCancel = (Button) findViewById(R.id.cancel_button);
        this.mOk.setOnClickListener(this);
        this.mCancel.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v == this.mOk) {
            startUninstallProgress();
        } else if (v == this.mCancel) {
            finish();
        }
    }

    public void onCancel(DialogInterface dialog) {
        finish();
    }
}
