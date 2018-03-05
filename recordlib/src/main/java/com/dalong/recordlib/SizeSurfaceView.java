package com.dalong.recordlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.dalong.recordlib.utils.AndroidUtil;

/**
 * Created by dalong on 2017/1/3.
 */

public class SizeSurfaceView extends SurfaceView {


    public SizeSurfaceView(Context context) {
        super(context);
    }

    public SizeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SizeSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SizeSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 设置视频宽高
     */
    public void setVideoDimension(int videoSizeWidth, int videoSizeHeight) {
       final int w = AndroidUtil.getScreenWidth(getContext());
        int width = w;
        int height = w * videoSizeHeight / videoSizeWidth;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
        lp.width = width;
        lp.height = height;
        setLayoutParams(lp);
    }
}
