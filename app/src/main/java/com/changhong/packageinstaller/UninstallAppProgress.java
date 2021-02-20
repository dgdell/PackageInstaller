package com.changhong.packageinstaller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver.Stub;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class UninstallAppProgress extends Activity implements OnClickListener {
    public static final int FAILED = 0;
    public static final int SUCCEEDED = 1;
    private final String TAG = "UninstallAppProgress";
    private final int UNINSTALL_COMPLETE = 1;
    private boolean localLOGV = false;
    private ApplicationInfo mAppInfo;
    private Button mDeviceManagerButton;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int i = 1;
            switch (msg.what) {
                case 1:
                    if (getIntent().getBooleanExtra("android.intent.extra.RETURN_RESULT", false)) {
                        Intent result = new Intent();
                        result.putExtra("android.intent.extra.INSTALL_RESULT", msg.arg1);
                        if (msg.arg1 == 1) {
                            i = -1;
                        }
                        setResult(i, result);
                        finish();
                        return;
                    }
                    int statusText;
                    mResultCode = msg.arg1;
                    String packageName = (String) msg.obj;
                    switch (msg.arg1) {
                        case -2:
                            Log.d("UninstallAppProgress", "Uninstall failed because " + packageName + " is a device admin");
                            mDeviceManagerButton.setVisibility(View.VISIBLE);
                            statusText = R.string.uninstall_failed_device_policy_manager;
                            break;
                        case 1:
                            statusText = R.string.uninstall_done;
                            break;
                        default:
                            Log.d("UninstallAppProgress", "Uninstall failed for " + packageName + " with code " + msg.arg1);
                            statusText = R.string.uninstall_failed;
                            break;
                    }
                    mStatusTextView.setText(statusText);
                    mOkPanel.setVisibility(View.VISIBLE);
                    return;
                default:
                    return;
            }
        }
    };
    private Button mOkButton;
    private View mOkPanel;
    private volatile int mResultCode = -1;
    private TextView mStatusTextView;

    class PackageDeleteObserver extends Stub {
        PackageDeleteObserver() {
        }

        public void packageDeleted(String packageName, int returnCode) {
            Message msg = UninstallAppProgress.this.mHandler.obtainMessage(1);
            msg.arg1 = returnCode;
            msg.obj = packageName;
            UninstallAppProgress.this.mHandler.sendMessage(msg);
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mAppInfo = (ApplicationInfo) getIntent().getParcelableExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO);
        initView();
    }

    void setResultAndFinish(int retCode) {
        setResult(retCode);
        finish();
    }

    public void initView() {
        boolean isUpdate;
        if ((this.mAppInfo.flags & 128) != 0) {
            isUpdate = true;
        } else {
            isUpdate = false;
        }
        setTitle(isUpdate ? R.string.uninstall_update_title : R.string.uninstall_application_title);
        setContentView(R.layout.uninstall_progress);
        PackageUtil.initSnippetForInstalledApp(this, this.mAppInfo, findViewById(R.id.app_snippet));
        this.mStatusTextView = (TextView) findViewById(R.id.center_text);
        this.mStatusTextView.setText(R.string.uninstalling);
        this.mDeviceManagerButton = (Button) findViewById(R.id.device_manager_button);
        this.mDeviceManagerButton.setVisibility(View.GONE);
        this.mDeviceManagerButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", "com.android.settings.Settings$DeviceAdminSettingsActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                UninstallAppProgress.this.startActivity(intent);
                UninstallAppProgress.this.finish();
            }
        });
        this.mOkPanel = findViewById(R.id.ok_panel);
        this.mOkButton = (Button) findViewById(R.id.ok_button);
        this.mOkButton.setOnClickListener(this);
        this.mOkPanel.setVisibility(View.INVISIBLE);
        getPackageManager().deletePackage(this.mAppInfo.packageName, new PackageDeleteObserver(), 0);
    }

    public void onClick(View v) {
        if (v == this.mOkButton) {
            Log.i("UninstallAppProgress", "Finished uninstalling pkg: " + this.mAppInfo.packageName);
            setResultAndFinish(this.mResultCode);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent ev) {
        if (ev.getKeyCode() == 4) {
            if (this.mResultCode == -1) {
                return true;
            }
            setResult(this.mResultCode);
        }
        return super.dispatchKeyEvent(ev);
    }
}
