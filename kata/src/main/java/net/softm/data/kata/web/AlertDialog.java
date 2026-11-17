package net.softm.data.kata.web;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.softm.lib.common.WLog;
import net.softm.data.kata.R;
import net.softm.data.kata.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AlertDialog extends Dialog implements View.OnClickListener{
    private Context mContext;

    public String title;
    public String msg;

    @BindView(R.id.ll_root)
    public LinearLayout mRoot;

    @BindView(R.id.tv_title)
    public TextView mTvTitle = null;

    @BindView(R.id.tv_msg)
    public TextView mTvMsg = null;

    @BindView(R.id.bt_negative)
    public Button mBtNegative;

    @BindView(R.id.bt_positive)
    public Button mBtPositive;

    public String mNText = "";
    public String mPText = "";

    public int mMsgGravity;

    public View.OnClickListener mNListener;
    public View.OnClickListener mPListener;

    public AlertDialog(Context context) {
        super(context);
        mContext = context;
    }

    public AlertDialog(Context context, String title, String msg, int gravity) {
//        super(context, R.style.NewDialog);
        super(context, R.style.AlertDialog2);
//        super(context);
        mContext = context;
        this.title = title;
        this.msg = msg;
        this.mMsgGravity = gravity;
    }


    public AlertDialog(Context context, String title, String pBtnText, String nBtnText, String msg, int gravity) {
//        super(context, R.style.NewDialog);
        super(context, R.style.AlertDialog2);
//        super(context);
        mContext = context;
        this.title = title;
        this.msg = msg;
        this.mMsgGravity = gravity;
        this.mPText = pBtnText;
        this.mNText = nBtnText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_alert);
        ButterKnife.bind(this);
        initView();
    }

    private void initView(){

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.25f;
        getWindow().setAttributes(lp);
        WLog.d("TAG", "lp.dimAmount : " + lp.dimAmount);
//        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
//        Drawable d = new ColorDrawable(Color.BLACK);
//        d.setAlpha(130);
//        this.getWindow().setBackgroundDrawable(d);
//        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        Window window = this.getWindow();
        window.setGravity(Gravity.CENTER);

        setCancelable(false);
        if (Utils.isNotEmpty(title)) {
            mTvTitle.setVisibility(View.VISIBLE);
        }
        if(title == null || "".equals(title)){
            title = mContext.getString(R.string.common_notice);
        }
        if(msg == null || "".equals(msg)){
            msg = mContext.getString(R.string.common_no_msg);
        }
        mTvTitle.setText(title);
        mTvMsg.setText(msg);
        if(mMsgGravity > 0) {
            mTvTitle.setGravity(mMsgGravity);
            mTvMsg.setPadding(mTvMsg.getPaddingBottom(), mTvMsg.getPaddingTop(), mTvMsg.getPaddingBottom(), mTvMsg.getPaddingBottom());
        }

        mBtNegative.setOnClickListener(this);
        mBtPositive.setOnClickListener(this);

        if(mNText != null && !"".equals(mNText)){
            mBtNegative.setText(mNText);
        }

        if(mPText != null && !"".equals(mPText)){
            mBtPositive.setText(mPText);
        }

        if(mNListener == null){
            mBtNegative.setVisibility(View.GONE);
        } else {
            mBtNegative.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_negative:
                if(mContext instanceof Activity && !((Activity)mContext).isFinishing()) {
                    dismiss();
                }
                if(mNListener != null) {
                    mNListener.onClick(view);
                }
                break;
            case R.id.bt_positive:
                if(mContext instanceof Activity && !((Activity)mContext).isFinishing()) {
                    dismiss();
                }
                if(mPListener != null) {
                    mPListener.onClick(view);
                }
                break;
        }
    }
}
