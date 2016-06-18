package com.app.wpf.wdownload;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.wpf.wdownloadtool.DownloadImpl.DownComplexInfo;
import com.app.wpf.wdownloadtool.WDownloadTool;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements
        View.OnClickListener {

    private ProgressBar mProgressBar;
    private ArrayList<String> downloadList = new ArrayList<>();
    private String downloadUrl = "https://dl.google.com/dl/android/studio/ide-zips/2.2.0.2/android-studio-ide-145.2949926-windows.zip";
    private boolean isDownload;
    private WDownloadTool wDownloadTool = new WDownloadTool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button_start);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        assert button != null;
        button.setOnClickListener(this);

        downloadList.add(downloadUrl);
//        downloadList.add(downloadUrl);
//        downloadList.add(downloadUrl);
//        downloadList.add(downloadUrl);
//        downloadList.add(downloadUrl);
    }

    @Override
    public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
            } else download();
        } else download();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1) download();
    }

    private void download() {
        if (!isDownload) {
            isDownload = true;
            int i = 0;
            String filePath = Environment.getExternalStorageDirectory().getPath() + "/Download/";
            wDownloadTool.download(downloadUrl, filePath, i + ".jpg", new DownComplexInfo() {

                    @Override
                    public void downloadPercent(int percent) {
                        mProgressBar.setProgress(percent);
                    }

                    @Override
                    public void downloadDetailed(int downSize) {

                    }

                    @Override
                    public void downloadSpeed(double speed) {
                        Log.e("速度", speed + "");
                    }

                    @Override
                    public void downloadSuccess() {
                        Log.d("结果", "下载成功");
                        Toast.makeText(MainActivity.this, "下载成功", Toast.LENGTH_LONG).show();
                        isDownload = false;
                        mProgressBar.setProgress(0);
                    }

                    @Override
                    public void downloadFailed(String msg) {
                        Log.d("结果", "下载失败:" + msg);
                        Toast.makeText(MainActivity.this, "下载失败:" + msg, Toast.LENGTH_LONG).show();
                        isDownload = false;
                        mProgressBar.setProgress(0);
                    }
                });
                i++;
        } else {
            wDownloadTool.stop();
            mProgressBar.setProgress(0);
            wDownloadTool = new WDownloadTool();
        }
    }
}