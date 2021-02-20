package com.changhong.packageinstaller.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.changhong.packageinstaller.R;

public class MyDialog extends Dialog implements OnKeyListener {
    private String mAppName;
    private IDialogConfirmCallBack mCallBack;
    private Context mContext;
    private Drawable mIcon;
    private String mMsg;
    private int mType;

    public MyDialog(Context context, int type, Drawable icon, String appName, String msg, IDialogConfirmCallBack callback) {
        super(context, R.style.transplate);
        this.mContext = context;
        this.mIcon = icon;
        this.mType = type;
        this.mAppName = appName;
        this.mMsg = msg;
        this.mCallBack = callback;
        getWindow().setSoftInputMode(2);
        getWindow().setType(2003);
        initView();
    }

    private void initView() {
        setContentView(R.layout.install_start);
        ((ImageView) findViewById(R.id.app_icon)).setImageDrawable(this.mIcon);
        ((TextView) findViewById(R.id.app_name)).setText(this.mAppName);
        ((TextView) findViewById(R.id.install_confirm_question)).setText(this.mMsg);
        Button btnCancel = (Button) findViewById(R.id.cancel_button);
        if (2 == this.mType || 3 == this.mType || 5 == this.mType) {
            btnCancel.setVisibility(View.GONE);
        }
        btnCancel.setOnKeyListener(this);
        Button btnOk = (Button) findViewById(R.id.ok_button);
        btnOk.setOnKeyListener(this);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                MyDialog.this.exit(false);
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                MyDialog.this.exit(true);
            }
        });
    }

    protected void exit(boolean isConfirm) {
        this.mCallBack.result(isConfirm);
        dismiss();
    }

    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if (4 == keyCode) {
            return true;
        }
        return false;
    }
}
