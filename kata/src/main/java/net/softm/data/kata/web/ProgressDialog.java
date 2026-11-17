package net.softm.data.kata.web;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.silvestrpredko.dotprogressbar.DotProgressBar;

import net.softm.lib.common.WLog;
import net.softm.data.kata.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import dilatingdotsprogressbar.DilatingDotsProgressBar;

public class ProgressDialog extends Dialog {
    private Context mContext;

    public String title;
    public String msg;

    @BindView(R.id.ll_progress)
    public LinearLayout mProgress;

    @BindView(R.id.ll_msg1)
    public TextView mMsg1 = null;

    @BindView(R.id.ll_msg2)
    public TextView mMsg2 = null;

    @BindView(R.id.ll_msg3)
    public TextView mMsg3 = null;

    public String mNText = "";
    public String mPText = "";

    @BindView(R.id.progress)
    DotProgressBar mDotProgressBar = null;
    DilatingDotsProgressBar mDilatingDotsProgressBar = null;

    public ProgressDialog(Context context) {
        super(context);
        mContext = context;
    }

    public ProgressDialog(Context context, int gravity) {
        super(context, R.style.NewDialog);
//        super(context);
        mContext = context;
        this.title = title;
        this.msg = msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress);
        ButterKnife.bind(this);
        initView();
//        mDilatingDotsProgressBar.setNumberOfDots(18);
//        mDilatingDotsProgressBar.setDotRadius(4.2f);
//        mDilatingDotsProgressBar.show(0);
        mDotProgressBar.setVisibility(View.VISIBLE);
    }

    private void initView(){
        setCancelable(false);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.25f; // alpha = 25%
        getWindow().setAttributes(lp);
        WLog.d("TAG", "lp.dimAmount : " + lp.dimAmount);
//        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

//        Drawable d = new ColorDrawable(Color.BLACK);
//        d.setAlpha(130);
//        this.getWindow().setBackgroundDrawable(d);
//        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//        setContentView(R.layout.dialog_progre    @Override
////    public void dismiss() {
////        super.dismiss();
////        mDilatingDotsProgressBar.hide();
////        mDilatingDotsProgressBar = null;
////    }ss);

        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        Window window = this.getWindow();
        window.setGravity(Gravity.TOP);
        params.y = 0;
        //params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setAttributes(params);
//        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setTitle(null);
        setCancelable(false);
    }

//
}
