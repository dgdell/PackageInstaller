package com.changhong.packageinstaller;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class PackageUtil {
    public static final String INTENT_ATTR_APPLICATION_INFO = "com.changhong.installservice.applicationInfo";
    public static final String INTENT_ATTR_INSTALL_STATUS = "com.changhong.installservice.installStatus";
    public static final String INTENT_ATTR_PACKAGE_NAME = "com.changhong.installservice.PackageName";
    public static final String INTENT_ATTR_PERMISSIONS_LIST = "com.changhong.installservice.PermissionsList";
    public static final String PREFIX = "com.changhong.installservice.";

    public static class AppSnippet {
        Drawable icon;
        CharSequence label;

        public AppSnippet(CharSequence label, Drawable icon) {
            this.label = label;
            this.icon = icon;
        }
    }

    public static ApplicationInfo getApplicationInfo(Uri packageURI) {
        String archiveFilePath = packageURI.getPath();
        PackageParser packageParser = new PackageParser(archiveFilePath);
        File sourceFile = new File(archiveFilePath);
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        Package pkg = packageParser.parsePackage(sourceFile, archiveFilePath, metrics, 0);
        if (pkg == null) {
            return null;
        }
        return pkg.applicationInfo;
    }

    public static Package getPackageInfo(Uri packageURI) {
        String archiveFilePath = packageURI.getPath();
        PackageParser packageParser = new PackageParser(archiveFilePath);
        File sourceFile = new File(archiveFilePath);
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        return packageParser.parsePackage(sourceFile, archiveFilePath, metrics, 0);
    }

    public static View initSnippet(View snippetView, CharSequence label, Drawable icon) {
        ((ImageView) snippetView.findViewById(R.id.app_icon)).setImageDrawable(icon);
        ((TextView) snippetView.findViewById(R.id.app_name)).setText(label);
        return snippetView;
    }

    public static View initSnippetForInstalledApp(Activity pContext, ApplicationInfo appInfo, View snippetView) {
        PackageManager pm = pContext.getPackageManager();
        return initSnippet(snippetView, appInfo.loadLabel(pm), appInfo.loadIcon(pm));
    }

    public static View initSnippetForNewApp(Activity pContext, AppSnippet as, int snippetId) {
        View appSnippet = pContext.findViewById(snippetId);
        ((ImageView) appSnippet.findViewById(R.id.app_icon)).setImageDrawable(as.icon);
        ((TextView) appSnippet.findViewById(R.id.app_name)).setText(as.label);
        return appSnippet;
    }

    public static boolean isPackageAlreadyInstalled(Activity context, String pkgName) {
        List<PackageInfo> installedList = context.getPackageManager().getInstalledPackages(PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES);
        int installedListSize = installedList.size();
        for (int i = 0; i < installedListSize; i++) {
            if (pkgName.equalsIgnoreCase(((PackageInfo) installedList.get(i)).packageName)) {
                return true;
            }
        }
        return false;
    }

    public static AppSnippet getAppSnippet(Activity pContext, ApplicationInfo appInfo, Uri packageURI) {
        String archiveFilePath = packageURI.getPath();
        Resources pRes = pContext.getResources();
        AssetManager assmgr = new AssetManager();
        assmgr.addAssetPath(archiveFilePath);
        Resources res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration());
        CharSequence label = null;
        if (appInfo.labelRes != 0) {
            try {
                label = res.getText(appInfo.labelRes);
            } catch (NotFoundException e) {
            }
        }
        if (label == null) {
            label = appInfo.nonLocalizedLabel != null ? appInfo.nonLocalizedLabel : appInfo.packageName;
        }
        Drawable icon = null;
        if (appInfo.icon != 0) {
            try {
                icon = res.getDrawable(appInfo.icon);
            } catch (NotFoundException e2) {
            }
        }
        if (icon == null) {
            icon = pContext.getPackageManager().getDefaultActivityIcon();
        }
        return new AppSnippet(label, icon);
    }
}
