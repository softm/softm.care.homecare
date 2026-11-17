package net.softm.data.kata;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

public class ACSFrameLayout extends FrameLayout {

        private float startX;
        private float startY;
        private float endX;
        private float endY;

        private Rect mRect;
        private Paint mPaint;
        // 이 생성자는 LayoutInflter에서 사용됩니다.
        public ACSFrameLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            // Rect와 Paint객체 생성
            // Paint객체에 스타일과 색상 입힘
            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0x800000FF);

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ACSFrameLayout,
                    0, 0);

//            startX = a.getInteger(R.styleable.ACSFrameLayout_startX, 0);

        }

    public void setStartX(float startX) {this.startX = startX;}
    public void setStartY(float startY) {this.startY = startY;}
    public void setEndX(float endX) {this.endX = endX;}
    public void setEndY(float endY) {this.endY = endY;}
        public ACSFrameLayout(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
//            Canvas canvas = new Canvas();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(6f);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            canvas.drawLine(startX,startY,endX,endY,paint);
//            draw(canvas);
//            mLayout.draw(canvas);
//            canvas.restore();

        }

}
