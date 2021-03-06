package ch.uzh.supersede.feedbacklibrary.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Color picker view.
 */
public class ColorPickerView extends View {
    private static final int CENTER_X = 100;
    private static final int CENTER_Y = 100;
    private static final int CENTER_RADIUS = 32;
    private static final float PI = 3.1415926f;
    private final int[] mColors = new int[]{
            0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
            0xFFFFFF00, 0xFFFF0000
    };
    private TextView textView;
    private Paint mPaint;
    private Paint mCenterPaint;
    private RectF newRectF;
    private int changedColor;

    public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerView(Context context) {
        super(context);
    }

    public ColorPickerView(Context c, int initialColor, TextView textView) {
        super(c);
        this.textView = textView;
        changedColor = initialColor;
        Shader s = new SweepGradient(0, 0, mColors, null);

        newRectF = new RectF();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setShader(s);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(32);

        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterPaint.setColor(initialColor);
        mCenterPaint.setStrokeWidth(5);
    }

    private int ave(int s, int d, float p) {
        return s + Math.round(p * (d - s));
    }

    /**
     * This method returns the changed color.
     *
     * @return the changed color
     */
    public int getChangedColor() {
        return changedColor;
    }

    private int interpolateColor(int colors[], float unit) {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
            return colors[colors.length - 1];
        }

        float p = unit * (colors.length - 1);
        int i = (int) p;
        p -= i;

        // now p is just the fractional part [0...1) and i is the index
        int c0 = colors[i];
        int c1 = colors[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        return Color.argb(a, r, g, b);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float r = CENTER_X - mPaint.getStrokeWidth() * 0.5f;
        canvas.translate(CENTER_X, CENTER_X);
        newRectF.set(-r, -r, r, r);
        canvas.drawOval(newRectF, mPaint);
        canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(CENTER_X * 2, CENTER_Y * 2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - CENTER_X;
        float y = event.getY() - CENTER_Y;

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float angle = (float) Math.atan2(y, x);
                // need to turn angle [-PI ... PI] into unit [0....1]
                float unit = angle / (2 * PI);
                if (unit < 0) {
                    unit += 1;
                }
                mCenterPaint.setColor(interpolateColor(mColors, unit));
                textView.setTextColor(mCenterPaint.getColor());
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                changedColor = mCenterPaint.getColor();
                break;
        }
        return true;
    }
}
