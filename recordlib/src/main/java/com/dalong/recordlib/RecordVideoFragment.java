package com.dalong.recordlib;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dalong.recordlib.utils.BitmapUtils;
import com.dalong.recordlib.view.RecordStartView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.leefeng.promptlibrary.PromptButton;
import me.leefeng.promptlibrary.PromptButtonListener;
import me.leefeng.promptlibrary.PromptDialog;


public class RecordVideoFragment extends Fragment implements RecordVideoInterface, RecordStartView.OnRecordButtonListener, View.OnClickListener {

    private final String TAG = "RecordVideoFragment";
    private SizeSurfaceView mRecordView;
    private RecordStartView mRecorderBtn;//录制按钮

    private ImageButton mFacing;//前置后置切换按钮

    private ImageButton mFlash;//闪光灯

    private RelativeLayout mBaseLayout;

    private String videoPath;
    private long maxSize;
    private int maxTime;
    private RecordVideoControl mRecordControl;
    private TextView mRecordTV;
    private ImageView mCancel;


    public RecordVideoFragment() {
    }

    @SuppressLint("ValidFragment")
    public RecordVideoFragment(String videoPath, long maxSize, int maxTime) {
        this.videoPath = videoPath;
        this.maxSize = maxSize;
        this.maxTime = maxTime;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_video, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mRecordView = (SizeSurfaceView) view.findViewById(R.id.recorder_view);
        mBaseLayout = (RelativeLayout) view.findViewById(R.id.activity_recorder_video);
        mRecorderBtn = (RecordStartView) view.findViewById(R.id.recorder_videobtn);
        mFacing = (ImageButton) view.findViewById(R.id.recorder_facing);
        mFlash = (ImageButton) view.findViewById(R.id.recorder_flash);
        mCancel = (ImageView) view.findViewById(R.id.recorder_cancel);
        mRecordTV = (TextView) view.findViewById(R.id.record_tv);
        mRecorderBtn.setOnRecordButtonListener(this);
        mRecordControl = new RecordVideoControl(getActivity(), videoPath, mRecordView, this);
        mRecordControl.setMaxSize(maxSize);
        mRecordControl.setMaxTime(maxTime);
        mRecorderBtn.setMaxTime(maxTime);
        mCancel.setOnClickListener(this);
        mFlash.setOnClickListener(this);
        mFacing.setOnClickListener(this);
        setupFlashMode();
    }

    @Override
    public void startRecord() {
        Log.v(TAG, "startRecord");
    }

    @Override
    public void onRecording(long recordTime) {
        Log.v(TAG, "onRecording:" + recordTime);
        if (recordTime / 1000 >= 1) {
            mRecordTV.setText(recordTime / 1000 + "秒");
        }
    }

    @Override
    public void onRecordFinish(String videoPath) {
        Log.v(TAG, "onRecordFinish:" + videoPath);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new VideoPlayFragment(videoPath, VideoPlayFragment.FILE_TYPE_VIDEO), VideoPlayFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onRecordError() {
        Log.v(TAG, "onRecordError");
    }

    @Override
    public void onTakePhoto(Bitmap bitmap) {
        try {
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getActivity().getString(R.string.camera_photo_path));
            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdirs();
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String path = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";

            BitmapUtils.storeImage(bitmap, path);

            File file = new File(path);

            Log.i(TAG,"--->>>file.getAbsolutePath():"+file.getAbsolutePath()+",file.getName()："+file.getName());
            // 其次把文件插入到系统图库
            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), file.getAbsolutePath(), file.getName(), null);
            // 最后通知图库更新
            getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));

            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new VideoPlayFragment(path, VideoPlayFragment.FILE_TYPE_PHOTO, mRecordControl.getCameraFacing()), VideoPlayFragment.TAG).addToBackStack(null).commit();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 开始录制
     */
    @Override
    public void onStartRecord() {
        try {
            mRecordControl.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("请在系统设置中打开视频录制权限！");
        }
    }


    private void showErrorDialog(String msg) {
        final PromptButton confirm = new PromptButton("确定", new PromptButtonListener() {
            @Override
            public void onClick(PromptButton button) {
                getActivity().finish();
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });

        PromptDialog promptDialog = new PromptDialog(getActivity());
        //Alert的调用
        promptDialog.showWarnAlert(msg, new PromptButton("取消", new PromptButtonListener() {
            @Override
            public void onClick(PromptButton button) {
                getActivity().finish();
            }
        }), confirm);
    }


    /**
     * 结束录制
     */
    @Override
    public void onStopRecord() {
        mRecordControl.stopRecording(true);
    }

    /**
     * 拍照
     */
    @Override
    public void onTakePhoto() {
        try {
            mRecordControl.takePhoto();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("请在系统设置中打开拍照权限！");
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.recorder_cancel) {
            getActivity().finish();
        } else if (i == R.id.recorder_flash) {
            if (mRecordControl.getCameraFacing() == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
                mRecordControl.setFlashMode(RecordVideoControl.flashType == RecordVideoControl.FLASH_MODE_ON
                        ? RecordVideoControl.FLASH_MODE_OFF
                        : RecordVideoControl.FLASH_MODE_ON);
            }
            setupFlashMode();
        } else if (i == R.id.recorder_facing) {
            mRecordControl.changeCamera(mFacing);
            setupFlashMode();
        }
    }

    private void setupFlashMode() {
        if (mRecordControl.getCameraFacing() == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mFlash.setVisibility(View.GONE);
            return;
        } else {
            mFlash.setVisibility(View.VISIBLE);
        }

        final int res;
        switch (RecordVideoControl.flashType) {
            case RecordVideoControl.FLASH_MODE_ON:
                res = R.drawable.pdh;
                break;
            case RecordVideoControl.FLASH_MODE_OFF:
                res = R.drawable.pdg;
                break;
            default:
                res = R.drawable.pdg;
        }
        mFlash.setBackgroundResource(res);
    }
}
