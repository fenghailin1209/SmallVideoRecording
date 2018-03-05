package com.dalong.recordlib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowManager;
import android.widget.VideoView;

/**
 * 自动全屏的VideoView
 */
public class FullScreenVideoView extends VideoView {

    private static final String TAG = FullScreenVideoView.class.getSimpleName();
    private int videoWidth;
    private int videoHeight;

    public FullScreenVideoView(Context context) {
        super(context);
    }

    public FullScreenVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullScreenVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(videoWidth, widthMeasureSpec);
        int height = getDefaultSize(videoHeight, heightMeasureSpec);
        if (videoWidth > 0 && videoHeight > 0) {
            int currentHeight = (width * videoHeight) / videoWidth;
            setMeasuredDimension(width, currentHeight);
            Log.i(TAG, "--->>>videoWidth:" + videoWidth + "videoHeight:" + videoHeight + "height:" + height + ",width:" + width+",currentHeight:"+currentHeight);
        }else{
            setMeasuredDimension(width, height);
        }
    }

    public void setVideoSize(int videoWidth, int videoHeight) {
        Log.i(TAG, "--->>>setVideoSize videoWidth:" + videoWidth + ",videoHeight：" + videoHeight);
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        requestLayout();
    }
}
