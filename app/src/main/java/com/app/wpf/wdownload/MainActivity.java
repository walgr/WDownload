package com.app.wpf.wdownload;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.wpf.wdownloadtool.DownloadImpl.DownComplexInfo;
import com.app.wpf.wdownloadtool.WDownloadTool;
import com.wpf.requestpermission.RequestPermission;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements
        View.OnClickListener {

    private ProgressBar mProgressBar;
    private Button button_start,button_stop;
    private RequestPermission requestPermission;
    private ArrayList<String> downloadList = new ArrayList<>();
//    private String downloadUrl = "https://codeload.github.com/EugeneHoran/Android-Material-SearchView/zip/master";
    private String downloadUrl = "http://img15.3lian.com/2015/f2/50/d/71.jpg";
    private WDownloadTool wDownloadTool = new WDownloadTool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_start = (Button) findViewById(R.id.button_start);
        button_stop = (Button) findViewById(R.id.button_stop);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        assert button_start != null;
        button_start.setOnClickListener(this);
        button_stop.setOnClickListener(this);
        downloadList.add(downloadUrl);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start:
                requestPermission = new RequestPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE,1) {
                    @Override
                    public void onSuccess() {
                        download();
                    }

                    @Override
                    public void onFail(String[] failList) {

                    }
                };
                break;
            case R.id.button_stop:
                wDownloadTool.stop();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        requestPermission.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    private void download() {
        button_start.setEnabled(false);
        button_stop.setEnabled(true);
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        wDownloadTool = new WDownloadTool();
        wDownloadTool.download(downloadUrl, filePath, new DownComplexInfo() {

            @Override
            public void downloadPercent(int percent) {
                mProgressBar.setProgress(percent);
            }

            @Override
            public void downloadDetailed(long downSize) {

            }

            @Override
            public void downloadSpeed(double speed) {
                Log.e("速度", speed + "");
            }

            @Override
            public void downloadSuccess() {
                Log.d("结果", "下载成功");
                Toast.makeText(MainActivity.this, "下载成功", Toast.LENGTH_LONG).show();
                button_start.setEnabled(true);
                button_stop.setEnabled(false);
            }

            @Override
            public void downloadFailed(String msg) {
                Log.d("结果", "下载失败:" + msg);
                Toast.makeText(MainActivity.this, "下载失败:" + msg, Toast.LENGTH_LONG).show();
                button_start.setEnabled(true);
                button_stop.setEnabled(false);
            }
        });
    }
}