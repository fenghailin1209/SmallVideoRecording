package com.dalong.recordlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.dalong.recordlib.utils.AndroidUtil;

/**
 * Created by dalong on 2017/1/3.
 */

public class SizeSurfaceView_back extends SurfaceView {

    private static final String TAG = SizeSurfaceView_back.class.getSimpleName();
    private boolean isUserSize = false;

    private int mVideoWidth;
    private int mVideoHeight;

    private int mMeasuredWidth;
    private int mMeasuredHeight;

    public SizeSurfaceView_back(Context context) {
        super(context);
    }

    public SizeSurfaceView_back(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SizeSurfaceView_back(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isUserSize) {
            doMeasure(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(mMeasuredWidth, mMeasuredHeight);
            setCameraDistance(0.5f);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        Log.i(TAG, "--->>>isUserSize:" + isUserSize + ",mMeasuredWidth:" + mMeasuredWidth + ",mMeasuredHeight:" + mMeasuredHeight);

    }

    public boolean isUserSize() {
        return isUserSize;
    }

    public void setUserSize(boolean isUserSize) {
        this.isUserSize = isUserSize;
    }

    /**
     * 设置视频宽高
     *
     * @param width
     * @param height
     */
    public void setVideoDimension(int width, int height) {
        Log.i(TAG, "--->>>setVideoDimension width:" + width + ",height:" + height);
        mVideoWidth = width;
        mVideoHeight = height;
    }

    /**
     * 测量
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    private void doMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sW = AndroidUtil.getScreenWidth(getContext());
        int sH = AndroidUtil.getScreenHeight(getContext());
        int width = View.getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = View.getDefaultSize(mVideoHeight, heightMeasureSpec);
        Log.i(TAG, "--->>>doMeasure width:" + width + ",height:" + height + ",sW:" + sW + ",sH:" + sH);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
            float specAspectRatio = (float) widthSpecSize / (float) heightSpecSize;
            float displayAspectRatio = (float) mVideoWidth / (float) mVideoHeight;
            boolean shouldBeWider = displayAspectRatio > specAspectRatio;
            Log.i(TAG, "--->>>widthSpecSize:" + widthSpecSize + ",heightSpecSize:" + heightSpecSize + ",specAspectRatio:" + specAspectRatio + ",displayAspectRatio:" + displayAspectRatio + ",shouldBeWider:" + shouldBeWider+",mVideoWidth:"+mVideoWidth+",mVideoHeight:"+mVideoHeight);

            if (shouldBeWider) {
                height = heightSpecSize;
                width = (int) (height * displayAspectRatio);
            } else {
                width = widthSpecSize;
                height = (int) (width / displayAspectRatio);
            }
        }
        mMeasuredWidth = width;
        mMeasuredHeight = height;
        Log.i(TAG, "--->>>widthSpecSize END mMeasuredWidth:" + mMeasuredWidth + ",mMeasuredHeight:" + mMeasuredHeight);
    }
}
