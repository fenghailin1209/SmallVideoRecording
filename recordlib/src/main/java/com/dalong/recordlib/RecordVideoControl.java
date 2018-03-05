package com.dalong.recordlib;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dalong.recordlib.utils.AndroidUtil;
import com.dalong.recordlib.utils.BitmapUtils;
import com.dalong.recordlib.utils.CameraUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import me.leefeng.promptlibrary.PromptDialog;


/**
 * 录制视频控制类
 * Created by dalong on 2017/1/3.
 */

public class RecordVideoControl implements SurfaceHolder.Callback, MediaRecorder.OnInfoListener,
        MediaRecorder.OnErrorListener, Runnable {

    public final String TAG = RecordVideoControl.class.getSimpleName();
    public static final int FLASH_MODE_OFF = 0;
    public static final int FLASH_MODE_ON = 1;
    public static int flashType = FLASH_MODE_OFF;
    private int previewWidth = 0;//预览宽
    private int previewHeight = 0;//预览高
    private int videoSizeWidth = 0;//录像的视频宽
    private int videoSizeHeight = 0;//录像的视频高
    private int maxTime = 10000;//最大录制时间
    private long maxSize = 30 * 1024 * 1024;//最大录制大小 默认30m
    public Activity mActivity;
    public String videoPath;//保存的位置
    public SizeSurfaceView mSurfaceView;
    public RecordVideoInterface mRecordVideoInterface;
    private SurfaceHolder mSurfaceHolder;
    private int mCameraId;//摄像头方向id
    private boolean isRecording;//是否录制中
    private Camera mCamera;//camera对象
    private boolean mIsPreviewing;  //是否预览
    private MediaRecorder mediaRecorder;
    private int defaultVideoFrameRate = 10;    //默认的视频帧率
    private int mCountTime;//当前录制时间
    private Camera.Parameters mParameters;

    private int smallWidth;

    public RecordVideoControl(Activity mActivity, String videoPath, SizeSurfaceView mSurfaceView, RecordVideoInterface mRecordVideoInterface) {
        this.mActivity = mActivity;
        this.videoPath = videoPath;
        this.mSurfaceView = mSurfaceView;
        this.mRecordVideoInterface = mRecordVideoInterface;
        mSurfaceHolder = this.mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        //这里设置当摄像头数量大于1的时候就直接设置后摄像头  否则就是前摄像头
//        if (Build.VERSION.SDK_INT > 8) {
//            if (Camera.getNumberOfCameras() > 1) {
//                mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
//            } else {
//                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
//            }
//        }

        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        smallWidth = Integer.parseInt(mActivity.getResources().getString(R.string.small_width));
    }

    /**
     * 摄像头方向
     *
     * @return
     */
    public int getCameraFacing() {
        return mCameraId;
    }

    /**
     * 开启摄像头预览
     *
     * @param holder
     */
    private void startCameraPreview(SurfaceHolder holder) {
        mIsPreviewing = false;
        setCameraParameter();
        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            destroyCamera();
            return;
        }
        mCamera.startPreview();
        mIsPreviewing = true;
        if (previewHeight > 0 && previewWidth > 0) {
            mSurfaceView.setVideoDimension(previewHeight, previewWidth);
        }
    }

    /**
     * 释放 Camera
     */
    public void destroyCamera() {
        if (mCamera != null) {
            if (mIsPreviewing) {
                mCamera.stopPreview();
                mIsPreviewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.setPreviewCallbackWithBuffer(null);
            }
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 切换摄像头
     *
     * @param v 点击切换的view 这里处理了点击事件
     */
    public void changeCamera(final View v) {
        if (v != null)
            v.setEnabled(false);
        changeCamera();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (v != null)
                    v.setEnabled(true);
            }
        }, 1000);
    }

    /**
     * 切换摄像头
     */
    @SuppressWarnings("deprecation")
    private void changeCamera() {
        if (isRecording) {
            Toast.makeText(mActivity, "录制中无法切换", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT < 9) {
            return;
        }
        int cameraid = 0;
        if (Camera.getNumberOfCameras() > 1) {
            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraid = Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else {
                cameraid = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
        } else {
            cameraid = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        if (mCameraId == cameraid) {
            return;
        } else {
            mCameraId = cameraid;
        }
        destroyCamera();
        try {
            mCamera = Camera.open(mCameraId);
            if (mCamera != null) {
                startCameraPreview(mSurfaceHolder);
            }
        } catch (Exception e) {
            e.printStackTrace();
            destroyCamera();
        }

    }

    /**
     * 设置camera 的 Parameters
     */
    private void setCameraParameter() {
        mParameters = mCamera.getParameters();

        Camera.Size size = CameraUtil.getInstance().getCommonPropSizeByHeight(mParameters.getSupportedPreviewSizes(), smallWidth);
        previewWidth = size.width;
        previewHeight = size.height;
        mParameters.setPreviewSize(previewWidth, previewHeight);

        Camera.Size pictrueSize = CameraUtil.getInstance().getCommonPropSizeByHeight(mParameters.getSupportedPictureSizes(), smallWidth);
        int pictrueWidth = pictrueSize.width;
        int pictrueHeight = pictrueSize.height;
        mParameters.setPictureSize(pictrueSize.width, pictrueSize.height);
        Log.i(TAG, "--->>>setCameraParameter previewWidth:" + previewWidth + ",previewHeight:" + previewHeight+",pictrueWidth:"+pictrueWidth+",pictrueHeight:"+pictrueHeight);

        if (Build.VERSION.SDK_INT < 9) {
            return;
        }
        List<String> supportedFocus = mParameters.getSupportedFocusModes();
        boolean isHave = supportedFocus == null ? false :
                supportedFocus.indexOf(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) >= 0;
        if (isHave) {
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mParameters.setFlashMode(flashType == FLASH_MODE_ON ?
                Camera.Parameters.FLASH_MODE_TORCH :
                Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(mParameters);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i(TAG,"--->>>surfaceChanged");
        init(surfaceHolder);
    }

    private void init(SurfaceHolder surfaceHolder) {
        try {
            mSurfaceHolder = surfaceHolder;
            if (surfaceHolder.getSurface() == null) {
                return;
            }
            if (mCamera == null) {
                if (Build.VERSION.SDK_INT < 9) {
                    mCamera = Camera.open();
                } else {
                    mCamera = Camera.open(mCameraId);
                }
            }
            if (mCamera != null)
                mCamera.stopPreview();
            mIsPreviewing = false;
            handleSurfaceChanged(mCamera);

            startCameraPreview(mSurfaceHolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 就是这里的问题，之前初始化放在这里导致频繁计算，出现错误
     * @param surfaceHolder
     * @param i
     * @param i1
     * @param i2
     */
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        try {
            destroyCamera();
            releaseRecorder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSurfaceChanged(Camera mCamera) {
        boolean hasSupportRate = false;
        List<Integer> supportedPreviewFrameRates = mCamera.getParameters().getSupportedPreviewFrameRates();
        if (supportedPreviewFrameRates != null
                && supportedPreviewFrameRates.size() > 0) {
            Collections.sort(supportedPreviewFrameRates);
            for (int i = 0; i < supportedPreviewFrameRates.size(); i++) {
                int supportRate = supportedPreviewFrameRates.get(i);

                if (supportRate == 10) {
                    hasSupportRate = true;
                }
            }
            if (hasSupportRate) {
                defaultVideoFrameRate = 10;
            } else {
                defaultVideoFrameRate = supportedPreviewFrameRates.get(0);
            }
        }
    }

    /**
     * 开始录制
     *
     * @return
     */
    public boolean startRecording() throws Exception {
        isRecording = true;
        mCountTime = 0;
        releaseRecorder();
        mCamera.stopPreview();
        mCamera.unlock();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        //set video size
        videoSizeWidth = previewWidth;
        videoSizeHeight = previewHeight;
        List<Camera.Size> videoSizes = mParameters.getSupportedVideoSizes();
        boolean isContain = CameraUtil.getInstance().isVideoSizesContainPropSize(previewWidth, previewHeight, videoSizes);
        if (!isContain) {
            Camera.Size supportedVideoSizes = CameraUtil.getInstance().getCommonPropSizeByHeight(videoSizes, smallWidth);
            videoSizeWidth = supportedVideoSizes.width;
            videoSizeHeight = supportedVideoSizes.height;
        }
        Log.i(TAG, "---->videoSizeWidth:" + videoSizeWidth + ",videoSizeHeight:" + videoSizeHeight + ",previewWidth：" + previewWidth + ",previewHeight:" + previewHeight + ",isContain:" + isContain);
        //设置分辨率，应设置在格式和编码器设置之后
        mediaRecorder.setVideoSize(videoSizeWidth, videoSizeHeight);
        mSurfaceView.setVideoDimension(videoSizeHeight,videoSizeWidth);
        //值越大，越清晰，但是大小也越大
        mediaRecorder.setVideoEncodingBitRate(7 * 1024 * 1024);

        mediaRecorder.setVideoFrameRate(CamcorderProfile.get(CamcorderProfile.QUALITY_480P).videoFrameRate);//after setVideoSource(),after setOutFormat(
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setOnInfoListener(this);
        mediaRecorder.setOnErrorListener(this);
        // 设置最大录制时间
        mediaRecorder.setMaxFileSize(maxSize);
        mediaRecorder.setMaxDuration(maxTime);
        mediaRecorder.setOutputFile(videoPath);
        if (mCameraId == 1) {
            mediaRecorder.setOrientationHint(270);
        } else {
            mediaRecorder.setOrientationHint(90);
        }

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            if (mRecordVideoInterface != null) {
                mRecordVideoInterface.startRecord();
            }
            new Thread(this).start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * 停止录制
     */
    public void stopRecording(boolean isSucessed) {
        if (!isRecording) {
            return;
        }
        try {
            if (mediaRecorder != null && isRecording) {
                isRecording = false;
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                mCountTime = 0;
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
                if (isSucessed) {
                    if (mRecordVideoInterface != null) {
                        mRecordVideoInterface.onRecordFinish(videoPath);
                    }
                } else {
                    if (mRecordVideoInterface != null) {
                        mRecordVideoInterface.onRecordError();
                    }
                    updateCallBack(0);
                }

            }
        } catch (Exception e) {
            updateCallBack(0);
            Log.e(TAG, "stopRecording error:" + e.getMessage());
        }
    }

    /**
     * 设置闪光灯模式
     *
     * @param flashType
     */
    public void setFlashMode(int flashType) {
        this.flashType = flashType;
        String flashMode = null;
        switch (flashType) {
            case FLASH_MODE_ON:
                flashMode = Camera.Parameters.FLASH_MODE_TORCH;
                break;
            case FLASH_MODE_OFF:
                flashMode = Camera.Parameters.FLASH_MODE_OFF;
            default:
                break;
        }
        if (flashMode != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(flashMode);
            mCamera.setParameters(parameters);
        }
    }


    private PromptDialog promptDialog;
    /**
     * 拍照
     */
    public void takePhoto() throws Exception {
        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] data, final Camera camera) {
                camera.setPreviewCallback(null);

                showDialog();

                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        Camera.Parameters parameters = camera.getParameters();
                        int width = parameters.getPreviewSize().width;
                        int height = parameters.getPreviewSize().height;
                        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
                        byte[] bytes = out.toByteArray();

                        Bitmap originalMap = BitmapUtils.Bytes2Bimap(bytes);

                        originalMap = CameraUtil.getInstance().setTakePicktrueOrientation(mCameraId, originalMap);//0 前置

                        if (mRecordVideoInterface != null) {
                            mRecordVideoInterface.onTakePhoto(originalMap);
                        }

                        if (originalMap != null && !originalMap.isRecycled()) {
                            originalMap.recycle();
                        }

                        dismissDialog();
                    }
                }.start();
            }
        });
    }

    private void dismissDialog() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(promptDialog != null){
                    promptDialog.dismiss();
                }
            }
        });
    }

    private void showDialog() {
        promptDialog = new PromptDialog(mActivity);
        promptDialog.showLoading("处理中...");
    }

    /**
     * 释放mediaRecorder
     */
    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }


    @Override
    public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
        Log.v(TAG, "onInfo");
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            Log.v(TAG, "最大录制时间已到");
            stopRecording(true);
        }
    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int i, int i1) {
        Log.e(TAG, "recording onError:");
        stopRecording(false);
    }

    @Override
    public void run() {
        while (isRecording) {
            updateCallBack(mCountTime);
            try {
                mCountTime += 100;
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 回调录制时间
     *
     * @param recordTime
     */
    private void updateCallBack(final int recordTime) {
        if (mActivity != null && !mActivity.isFinishing()) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRecordVideoInterface != null) {
                        mRecordVideoInterface.onRecording(recordTime);
                    }
                }
            });
        }
    }

    /**
     * 获取最大录制时间
     *
     * @return
     */
    public int getMaxTime() {
        return maxTime;
    }

    /**
     * 设置录制时间
     *
     * @param maxTime
     */
    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    /**
     * 获取最大录制大小
     *
     * @return
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     * 设置录制大小
     *
     * @param maxSize
     */
    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * 获取录制视频保存路径
     *
     * @return
     */
    public String getVideoPath() {
        return videoPath;
    }

    /**
     * 设置录制保存路径
     *
     * @param videoPath
     */
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    /**
     * 是否录制
     *
     * @return
     */
    public boolean isRecording() {
        return isRecording;
    }

}
