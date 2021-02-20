package com.changhong.packageinstaller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.changhong.aidl.InstallCallback.Stub;
import com.changhong.packageinstaller.util.Constants;

import java.util.List;

public class InstallAppProgress extends Activity implements OnClickListener, OnCancelListener {
    private static final int DLG_OUT_OF_SPACE = 1;
    private final int INSTALL_COMPLETE = 1;
    private final String TAG = "InstallAppProgress";
    private String cmccplat_appCode;
    private String cmccplat_appName;
    private boolean localLOGV = false;
    private ApplicationInfo mAppInfo;
    private Button mDoneButton;

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int i = 1;
            switch (msg.what) {
                case 1:
                    if (getIntent().getBooleanExtra("android.intent.extra.RETURN_RESULT", false)) {
                        Intent result = new Intent();
                        result.putExtra("android.intent.extra.INSTALL_RESULT", msg.arg1);
                        InstallAppProgress installAppProgress = InstallAppProgress.this;
                        if (msg.arg1 == 1) {
                            i = -1;
                        }
                        installAppProgress.setResult(i, result);
                        finish();
                        return;
                    }
                    int centerTextLabel;
                    if (msg.arg1 == 1) {
                        mLaunchButton.setVisibility(View.VISIBLE);
                        centerTextLabel = R.string.install_done;
                        mLaunchIntent = getPackageManager().getLaunchIntentForPackage(mAppInfo.packageName);
                        boolean enabled = false;
                        if (mLaunchIntent != null) {
                            List<ResolveInfo> list = getPackageManager().queryIntentActivities(mLaunchIntent, 0);
                            if (list != null && list.size() > 0) {
                                enabled = true;
                            }
                        }
                        if (enabled) {
                            mLaunchButton.setOnClickListener(InstallAppProgress.this);
                        } else {
                            mLaunchButton.setEnabled(false);
                        }
                    } else if (msg.arg1 == -4) {
                        showDialogInner(1);
                        return;
                    } else {
                        centerTextLabel = R.string.install_failed;
                        mLaunchButton.setVisibility(View.GONE);
                    }
                    mStatusTextView.setText(centerTextLabel);
                    mDoneButton.setOnClickListener(InstallAppProgress.this);
                    mOkPanel.setVisibility(View.VISIBLE);
                    return;
                default:
                    return;
            }
        }
    };
    private CharSequence mLabel;
    private Button mLaunchButton;
    private Intent mLaunchIntent;
    private View mOkPanel;
    private Uri mPackageURI;
    private TextView mStatusTextView;

    class MyCallback extends Stub {
        MyCallback() {
        }

        public void onInstallResult(int result, String msg) throws RemoteException {
            Message message = mHandler.obtainMessage(1);
            if (result == 0) {
                message.arg1 = 1;
            } else {
                message.arg1 = result;
            }
            message.obj = msg;
            mHandler.sendMessage(message);
        }
    }

    private int getViewWidth(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
        return view.getMeasuredWidth();
    }

    private String getExplanationFromErrorCode(int errCode) {
        Log.d("InstallAppProgress", "Installation error code: " + errCode);
        switch (errCode) {
            case -20005:
                return "应用效验错误！";
            case Constants.INSTALL_FAILED_EXT_ERROR_FORMAT /*-20004*/:
                return "apk包名取值错误！";
            case Constants.INSTALL_FAILED_EXT_IS_NOT_FILE /*-20003*/:
                return "路径指向为非文件类型！";
            case Constants.INSTALL_FAILED_EXT_APKFILE_NOT_FOUND /*-20002*/:
                return "文件未找到，异常日志。";
            case Constants.INSTALL_FAILED_EXT_BASE /*-20000*/:
                return "";
            case -113:
                return "did not match any of the ABIs supported by the system.";
            case -112:
                return "it is attempting to define a permission that is already defined by some existing package.";
            case -111:
                return "because the user is restricted from installing apps.";
            case -110:
                return "because of system issues.";
            case -109:
                return "if the parser did not find any actionable tags (instrumentation or application) in the manifest.";
            case -108:
                return "if the parser encountered some structural problem in the manifest.";
            case -107:
                return "if the parser encountered a bad shared user id name in the manifest.";
            case -106:
                return "if the parser encountered a bad or missing package name in the manifest.";
            case -105:
                return "if the parser encountered a CertificateEncodingException in one of the files in the .apk.";
            case -104:
                return "if the parser found inconsistent certificates on the files in the .apk.";
            case -103:
                return "if the parser did not find any certificates in the .apk.";
            case -102:
                return "if the parser encountered an unexpected exception.";
            case -101:
                return "if the parser was unable to retrieve the AndroidManifest.xml file.";
            case -100:
                return "if the parser was given a path that is not a file, or does not end with the expected";
            case -25:
                return "if the new package has an older version code than the currently installed package.";
            case -24:
                return "if the new package is assigned a different UID than it previously held.";
            case -23:
                return "if the package changed from what the calling program expected.";
            case -22:
                return "if the new package couldn't be installed because the verification did not succeed.";
            case -21:
                return "if the new package couldn't be installed because the verification timed out.";
            case -20:
                return "if the new package couldn't be installed in the specified install location because the media is not available.";
            case -19:
                return "if the new package couldn't be installed in the specified install location.";
            case -18:
                return "if a secure container mount point couldn't be accessed on external media.";
            case -17:
                return "if the new package uses a feature that is not available.";
            case -16:
                return "if the package being installed contains native code, but none that is compatible with the device's CPU_ABI.";
            case -15:
                return "if the new package failed because it has specified that it is a test-only package and the caller has not supplied the flag.";
            case -14:
                return "if the new package failed because the current SDK version is newer than that required by the package.";
            case -13:
                return "a provider already installed in the system.";
            case -12:
                return "the current SDK version is older than that required by the package.";
            case -11:
                return "either because there was not enough storage or the validation failed.";
            case -10:
                return "if the new package uses a shared library that is not available.";
            case -9:
                return "if the new package uses a shared library that is not available.";
            case -8:
                return "device and does not have matching signature.";
            case -7:
                return "than the new package (and the old package's data was not removed).";
            case -6:
                return "if the requested shared user does not exist.";
            case -5:
                return "if a package is already installed with the same name.";
            case -4:
                return "if the package manager service found that the device didn't               have enough storage space to install the app.";
            case -3:
                return "if the URI passed in is invalid.";
            case -2:
                return "if the package archive file is invalid.";
            case -1:
                return "if the package is  already installed.";
            default:
                return "未知异常";
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        this.mAppInfo = (ApplicationInfo) intent.getParcelableExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO);
        this.mPackageURI = intent.getData();
        this.cmccplat_appName = intent.getStringExtra(Constants.EXTR_INSTALL_APP_NAME);
        this.cmccplat_appCode = intent.getStringExtra(Constants.EXTR_INSTALL_APP_CODE);
        initView();
    }

    public Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
            case 1:
                return new Builder(this).setTitle(R.string.out_of_space_dlg_title).setMessage(getString(R.string.out_of_space_dlg_text, new Object[]{this.mLabel})).setPositiveButton(R.string.manage_applications, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent("android.intent.action.MANAGE_PACKAGE_STORAGE"));
                        finish();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("InstallAppProgress", "Canceling installation");
                        finish();
                    }
                }).setOnCancelListener(this).create();
            default:
                return null;
        }
    }

    private void showDialogInner(int id) {
        removeDialog(id);
        showDialog(id);
    }

    public void initView() {
        setContentView(R.layout.op_progress);
        int installFlags = 0;
        try {
            if (getPackageManager().getPackageInfo(this.mAppInfo.packageName, 0) != null) {
                installFlags = 0 | 2;
            }
        } catch (NameNotFoundException e) {
        }
        if ((installFlags & 2) != 0) {
            Log.w("InstallAppProgress", "Replacing package:" + this.mAppInfo.packageName);
        }
        PackageUtil.AppSnippet as = PackageUtil.getAppSnippet(this, this.mAppInfo, this.mPackageURI);
        this.mLabel = as.label;
        PackageUtil.initSnippetForNewApp(this, as, R.id.app_snippet);
        this.mStatusTextView = (TextView) findViewById(R.id.center_text);
        this.mStatusTextView.setText(R.string.installing);
        this.mOkPanel = findViewById(R.id.buttons_panel);
        this.mDoneButton = (Button) findViewById(R.id.done_button);
        this.mLaunchButton = (Button) findViewById(R.id.launch_button);
        this.mOkPanel.setVisibility(View.INVISIBLE);
        new InstallHelper(this).install(mPackageURI.getPath(), cmccplat_appName, cmccplat_appCode, new MyCallback());
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClick(View v) {
        if (v == this.mDoneButton) {
            if (this.mAppInfo.packageName != null) {
                Log.i("InstallAppProgress", "Finished installing " + this.mAppInfo.packageName);
            }
            finish();
        } else if (v == this.mLaunchButton) {
            startActivity(this.mLaunchIntent);
            finish();
        }
    }

    public void onCancel(DialogInterface dialog) {
        finish();
    }
}
