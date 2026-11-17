package net.softm.data.kata;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class CustomSwitch extends FrameLayout {

    private boolean isChecked = false;
    private View background;
    private ImageView thumb;

    public CustomSwitch(Context context) {
        super(context);
        init(context);
    }

    public CustomSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Inflate custom layout
        LayoutInflater.from(context).inflate(R.layout.custom_switch, this, true);
        background = findViewById(R.id.switch_background);
        thumb = findViewById(R.id.switch_thumb);

        // Set click listener for toggling state
        setOnClickListener(v -> toggle());
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
        updateUI();
    }

    public boolean isChecked() {
        return isChecked;
    }

    private void toggle() {
        isChecked = !isChecked;
        updateUI();
        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChanged(this, isChecked);
        }
    }

    private void updateUI() {
        // Update background and thumb position
        background.setBackgroundResource(isChecked ? R.drawable.switch_background_on : R.drawable.switch_background_off);
        float thumbPosition = isChecked ? background.getWidth() - thumb.getWidth() - 10 : 0;
        thumb.animate().translationX(thumbPosition).setDuration(200).start();
    }

    // Listener for external changes
    private OnCheckedChangeListener onCheckedChangeListener;

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.onCheckedChangeListener = listener;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(CustomSwitch customSwitch, boolean isChecked);
    }
}
