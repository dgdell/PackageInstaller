package com.changhong.packageinstaller;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.widget.Button;
import android.content.pm.PackageParser.Package;

import com.changhong.packageinstaller.util.Constants;
import com.changhong.packageinstaller.view.IDialogConfirmCallBack;
import com.changhong.packageinstaller.view.MyDialog;

/**
 * 作者:libeibei
 * 创建日期:20190410
 * 类说明:app安装弹窗Activity
 **/
public class PackageInstallerActivity extends Activity implements OnClickListener, OnCancelListener {
    public static final int DLG_BASE = 0;
    public static final int DLG_REPLACE_APP = 1;
    public static final int DLG_UNKNOWN_APPS = 2;
    public static final int DLG_PACKAGE_ERROR = 3;
    public static final int DLG_OUT_OF_SPACE = 4;
    public static final int DLG_INSTALL_ERROR = 5;
    public static final int DLG_ALLOW_SOURCE = 6;
    static final String PREFS_ALLOWED_SOURCES = "allowed_sources";
    private String cmccplat_appCode;
    private String cmccplat_appName;
    private boolean localLOGV = false;
    private ApplicationInfo mAppInfo = null;
    private Button mCancel, mOk;
    View mInstallConfirm;
    private Uri mPackageURI;
    Package mPkgInfo;
    PackageManager mPm;
    ApplicationInfo mSourceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mPackageURI = intent.getData();
        mPm = getPackageManager();
        mPkgInfo = PackageUtil.getPackageInfo(mPackageURI);
        mSourceInfo = mPkgInfo.applicationInfo;
        if (mPkgInfo == null) {
            Loger.w("Parse error when parsing manifest. Discontinuing installation");
            showDialogInner(3);
            setPmResult(-2);
            return;
        }
        setContentView(R.layout.install_start);
        mInstallConfirm = findViewById(R.id.install_confirm_panel);
        mInstallConfirm.setVisibility(View.INVISIBLE);
        PackageUtil.initSnippetForNewApp(this, PackageUtil.getAppSnippet(this, mPkgInfo.applicationInfo, mPackageURI), R.id.app_snippet);
        cmccplat_appName = intent.getStringExtra(Constants.EXTR_INSTALL_APP_NAME);
        cmccplat_appCode = intent.getStringExtra(Constants.EXTR_INSTALL_APP_CODE);
        if (TextUtils.isEmpty(cmccplat_appCode)) {
            cmccplat_appCode = "1350537921780591";
        }
        if (TextUtils.isEmpty(cmccplat_appName)) {
            cmccplat_appName = mPkgInfo.applicationInfo.loadLabel(mPm).toString();
        }
        Loger.d("install apkpath=" + mPackageURI.getPath() + ", appname=" + cmccplat_appName + ", appcode=" + cmccplat_appCode);
        if (isInstallingUnknownAppsAllowed()) {
            initiateInstall();
        } else {
            showDialogInner(2);
        }
    }

    private void startInstallConfirm() {
        this.mInstallConfirm.setVisibility(View.VISIBLE);
        this.mOk = (Button) findViewById(R.id.ok_button);
        this.mCancel = (Button) findViewById(R.id.cancel_button);
        this.mOk.setOnClickListener(this);
        this.mCancel.setOnClickListener(this);
    }

    private void showDialogInner(int id) {
        removeDialog(id);
        showDialog(id);
    }

    public Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
            case DLG_REPLACE_APP:
                int msgId = R.string.dlg_app_replacement_statement;
                if (!(this.mAppInfo == null || (this.mAppInfo.flags & 1) == 0)) {
                    msgId = R.string.dlg_sys_app_replacement_statement;
                }
                return new MyDialog(this, 1, mSourceInfo.loadIcon(this.mPm), mSourceInfo.loadLabel(this.mPm).toString(), getResources().getString(msgId), new IDialogConfirmCallBack() {
                    public void result(boolean isConfirm) {
                        if (isConfirm) {
                            startInstallConfirm();
                            return;
                        }
                        setResult(0);
                        finish();
                    }
                });
            case DLG_UNKNOWN_APPS:
                return new MyDialog(this, 2, mSourceInfo.loadIcon(this.mPm), mSourceInfo.loadLabel(this.mPm).toString(), getResources().getString(R.string.unknown_apps_dlg_text), new IDialogConfirmCallBack() {
                    public void result(boolean isConfirm) {
                        if (isConfirm) {
                            finish();
                        }
                    }
                });
            case DLG_PACKAGE_ERROR:
                return new MyDialog(this, 3, mSourceInfo.loadIcon(this.mPm), mSourceInfo.loadLabel(this.mPm).toString(), getResources().getString(R.string.Parse_error_dlg_text), new IDialogConfirmCallBack() {
                    public void result(boolean isConfirm) {
                        if (isConfirm) {
                            finish();
                        }
                    }
                });
            case DLG_OUT_OF_SPACE:
                CharSequence appTitle = this.mPm.getApplicationLabel(this.mPkgInfo.applicationInfo);
                return new MyDialog(this, 4, this.mSourceInfo.loadIcon(this.mPm), this.mSourceInfo.loadLabel(this.mPm).toString(), getString(R.string.out_of_space_dlg_text, new Object[]{appTitle.toString()}), new IDialogConfirmCallBack() {
                    public void result(boolean isConfirm) {
                        if (isConfirm) {
                            Intent intent = new Intent("android.intent.action.MANAGE_PACKAGE_STORAGE");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            return;
                        }
                        finish();
                    }
                });
            case DLG_INSTALL_ERROR:
                CharSequence appTitle1 = this.mPm.getApplicationLabel(this.mPkgInfo.applicationInfo);
                return new MyDialog(this, 5, this.mSourceInfo.loadIcon(this.mPm), this.mSourceInfo.loadLabel(this.mPm).toString(), getString(R.string.install_failed_msg, new Object[]{appTitle1.toString()}), new IDialogConfirmCallBack() {
                    public void result(boolean isConfirm) {
                        if (isConfirm) {
                            finish();
                        }
                    }
                });
            case DLG_ALLOW_SOURCE:
                CharSequence appTitle2 = this.mPm.getApplicationLabel(this.mSourceInfo);
                return new MyDialog(this, 6, this.mSourceInfo.loadIcon(this.mPm), this.mSourceInfo.loadLabel(this.mPm).toString(), getString(R.string.allow_source_dlg_text, new Object[]{appTitle2.toString()}), new IDialogConfirmCallBack() {
                    public void result(boolean isConfirm) {
                        if (isConfirm) {
                            getSharedPreferences(PackageInstallerActivity.PREFS_ALLOWED_SOURCES, 0).edit().putBoolean(PackageInstallerActivity.this.mSourceInfo.packageName, true).apply();
                            startInstallConfirm();
                            return;
                        }
                        setResult(0);
                        finish();
                    }
                });
            default:
                return null;
        }
    }

    private void launchSettingsAppAndFinish() {
        Intent launchSettingsIntent = new Intent("android.settings.SECURITY_SETTINGS");
        launchSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(launchSettingsIntent);
        finish();
    }

    private boolean isInstallingUnknownAppsAllowed() {
        return !TextUtils.isEmpty(cmccplat_appName) && !TextUtils.isEmpty(cmccplat_appCode);
    }

    private void initiateInstall() {
        String pkgName = this.mPkgInfo.packageName;
        String[] oldName = this.mPm.canonicalToCurrentPackageNames(new String[]{pkgName});
        if (!(oldName == null || oldName.length <= 0 || oldName[0] == null)) {
            pkgName = oldName[0];
            mPkgInfo.setPackageName(pkgName);
        }
        try {
            mAppInfo = mPm.getApplicationInfo(pkgName, 8192);
        } catch (PackageManager.NameNotFoundException e) {
            mAppInfo = null;
        }
        if (mAppInfo == null || getIntent().getBooleanExtra("android.intent.extra.ALLOW_REPLACE", false)) {
            startInstallConfirm();
            return;
        }
        if (this.localLOGV) {
            Loger.i("Replacing existing package:" + this.mPkgInfo.applicationInfo.packageName);
        }
        startInstallConfirm();
    }

    void setPmResult(int pmResult) {
        int i = 1;
        Intent result = new Intent();
        result.putExtra("android.intent.extra.INSTALL_RESULT", pmResult);
        if (pmResult == 1) {
            i = -1;
        }
        setResult(i, result);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v == mOk) {
            Intent newIntent = new Intent();
            newIntent.putExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO, mPkgInfo.applicationInfo);
            newIntent.setData(mPackageURI);
            newIntent.setClass(this, InstallAppProgress.class);
            String installerPackageName = getIntent().getStringExtra("android.intent.extra.INSTALLER_PACKAGE_NAME");
            if (installerPackageName != null) {
                newIntent.putExtra("android.intent.extra.INSTALLER_PACKAGE_NAME", installerPackageName);
            }
            if (getIntent().getBooleanExtra("android.intent.extra.RETURN_RESULT", false)) {
                newIntent.putExtra("android.intent.extra.RETURN_RESULT", true);
                newIntent.addFlags(33554432);
            }
            newIntent.putExtra(Constants.EXTR_INSTALL_APP_CODE, cmccplat_appCode);
            newIntent.putExtra(Constants.EXTR_INSTALL_APP_NAME, cmccplat_appName);
            Loger.i("downloaded app uri=" + mPackageURI + ", appname:" + cmccplat_appName + ", appcode:" + cmccplat_appCode);
            startActivity(newIntent);
            finish();
        } else if (v == mCancel) {
            setResult(0);
            finish();
        }
    }

}
