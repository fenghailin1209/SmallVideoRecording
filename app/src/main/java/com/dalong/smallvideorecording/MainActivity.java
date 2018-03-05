package com.dalong.smallvideorecording;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.dalong.recordlib.RecordVideoActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    String videoPath;

    public static final int TAKE_DATA = 200;
    private String video_dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        video_dir = Environment.getExternalStorageDirectory() + "/" + getString(R.string.camera_video_path) + "/";
        File path = new File(video_dir);
        if (!path.exists()) {
            path.mkdirs();
        }
        videoPath = path.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".mp4";
    }

    /**
     * 录制
     *
     * @param view
     */
    public void doRecording(View view) {
        Intent intent = new Intent(this, RecordVideoActivity.class);
        intent.putExtra(RecordVideoActivity.RECORD_VIDEO_PATH, videoPath);
        startActivityForResult(intent, TAKE_DATA);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_DATA:
                if (resultCode == RecordVideoActivity.TAKE_VIDEO_CODE) {
                    String videoPath = data.getStringExtra(RecordVideoActivity.TAKE_VIDEO_PATH);
                    Toast.makeText(this, "视频路径：" + videoPath, Toast.LENGTH_SHORT).show();
                } else if (resultCode == RecordVideoActivity.TAKE_PHOTO_CODE) {
                    String photoPath = data.getStringExtra(RecordVideoActivity.TAKE_PHOTO_PATH);
                    Toast.makeText(this, "图片路径：" + photoPath, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteDir(video_dir);
    }

    //删除文件夹和文件夹里面的文件
    public void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete(); // 删除所有文件
            } else if (file.isDirectory()) {
                deleteDirWihtFile(file); // 递规的方式删除文件夹
            }
        }
        dir.delete();// 删除目录本身
    }
}
