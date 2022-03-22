package com.lindum.mlkit.vision.dev.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.lindum.mlkit.vision.dev.R;

public class RollImageView extends View {
    private int mBitmapH;
    private int mBitmapW;
    private Bitmap mBitmap;
    private int mW;
    private int mH;
    private ValueAnimator mValueAnimator;
    private int mValue;

    private Rect mSrcRect = new Rect();
    private Rect mDestRect = new Rect();

    public RollImageView(Context context) {
        super(context);
        init();
    }

    public RollImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RollImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mW = w;
        mH = h;
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ui);
        mBitmapH = mBitmap.getHeight();
        mBitmapW = mBitmap.getWidth();
        int scaleWidth = (int) (((float)  mH / (float) mBitmapH) * mBitmapW);
        int scaleHeight = mH;
        mBitmap = Bitmap.createScaledBitmap(mBitmap, scaleWidth, scaleHeight, true);
        mBitmapH = mBitmap.getHeight();
        mBitmapW = mBitmap.getWidth();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // 不首尾相接的循环
//        mValueAnimator = ValueAnimator.ofInt(0, mBitmapW-w);
        // 首尾相接的循环
        mValueAnimator = ValueAnimator.ofInt(0, mBitmapW);
        mValueAnimator.setDuration(mBitmapW*5);
        mValueAnimator.setRepeatCount(-1);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.addUpdateListener(valueAnimator -> {
            mValue = (int) valueAnimator.getAnimatedValue();
            invalidate();
        });

        mValueAnimator.start();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mSrcRect.set(mValue, 0, mW + mValue, mBitmapH);
        mDestRect.set(0, 0, mW, mH);
        canvas.drawBitmap(mBitmap, mSrcRect, mDestRect, null);

        if (mValue >= mBitmapW - mW) {
            mSrcRect.set(0, 0, mValue - (mBitmapW - mW), mBitmapH);
            mDestRect.set(mW - (int) ((float) (mValue - (mBitmapW - mW)) / ((float) mBitmapH / (float) mH)), 0, mW, mH);
            canvas.drawBitmap(mBitmap, mSrcRect, mDestRect, null);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        //设置宽高
        setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        //wrap_content
        if (specMode == MeasureSpec.AT_MOST) {
        }
        //fill_parent或者精确值
        else if (specMode == MeasureSpec.EXACTLY) {

        }
        return specSize;
    }

    //根据xml的设定获取高度
    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        //wrap_content
        if (specMode == MeasureSpec.AT_MOST) {
//            specSize = mBitmapH;
        }
        //fill_parent或者精确值
        else if (specMode == MeasureSpec.EXACTLY) {
        }
        return specSize;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
    }
}