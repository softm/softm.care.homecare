package net.softm.data.kata;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageButton2 extends LinearLayout {

        LinearLayout bg;
        ImageView symbol;
        TextView text;

        public ImageButton2(Context context) {

            super(context);
            initView();

        }

        public ImageButton2(Context context, AttributeSet attrs) {

            super(context, attrs);

            initView();
            getAttrs(attrs);

        }

        public ImageButton2(Context context, AttributeSet attrs, int defStyle) {

            super(context, attrs);
            initView();
            getAttrs(attrs, defStyle);

        }


        private void initView() {

            String infService = Context.LAYOUT_INFLATER_SERVICE;

            LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
            View v = li.inflate(R.layout.image_button, this, false);
            addView(v);

            bg = (LinearLayout) findViewById(R.id.bg);
            symbol = (ImageView) findViewById(R.id.symbol);

            text = (TextView) findViewById(R.id.text);

        }

        private void getAttrs(AttributeSet attrs) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ImageButton);

            setTypeArray(typedArray);
        }


        private void getAttrs(AttributeSet attrs, int defStyle) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ImageButton, defStyle, 0);
            setTypeArray(typedArray);

        }


        private void setTypeArray(TypedArray typedArray) {


//            int bg_resID = typedArray.getResourceId(R.styleable.ImageButton_bg, R.drawable.ic_baseline_home_24);
//            bg.setBackgroundResource(bg_resID);

            int symbol_resID = typedArray.getResourceId(R.styleable.ImageButton_symbol, R.drawable.ic_baseline_home_24);
            symbol.setImageResource(symbol_resID);

            int textColor = typedArray.getColor(R.styleable.ImageButton_textColor, 0);
            text.setTextColor(textColor);

            String text_string = typedArray.getString(R.styleable.ImageButton_text);
            text.setText(text_string);

            typedArray.recycle();

        }


        void setBg(int bg_resID) {

            bg.setBackgroundResource(bg_resID);
        }

        void setSymbol(int symbol_resID) {
            symbol.setImageResource(symbol_resID);
        }

        void setTextColor(int color) {

            text.setTextColor(color);
        }

        void setText(String text_string) {
            text.setText(text_string);
        }

        void setText(int text_resID) {
            text.setText(text_resID);
        }
    }
