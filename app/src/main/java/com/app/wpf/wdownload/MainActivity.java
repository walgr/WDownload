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

import com.app.wpf.wdownloadtool.WDownloadTool;


public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        WDownloadTool.WDownloadToolInformationListener,
        WDownloadTool.WDownloadToolListener,
        WDownloadTool.WDownloadToolResultListener {

    private ProgressBar mProgressBar;
    private String downloadUrl = "http://img15.3lian.com/2015/f2/50/d/71.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button_start);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        assert button != null;
        button.setOnClickListener(this);
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
        new WDownloadTool()
                .setWDownloadToolInformationListener(this)
                .setWDownloadToolListener(this)
                .setWDownloadToolResultListener(this)
                .setOpenSpeedCheck(true)
                .setFilePath(Environment.getExternalStorageDirectory()+"/Test/")
                .setFileName("1.jpg")
                .download(downloadUrl);
    }

    @Override
    public void DownloadInformation(String name, int size) {
        //mProgressBar.setMax(size);
    }

    @Override
    public void DownloadPercent(int percent) {
        mProgressBar.setProgress(percent);
    }

    @Override
    public void DownloadSpeed(double speed) {
        Log.e("速度",speed+"");
    }

    @Override
    public void DownloadDetailed(int downSize) {

    }

    @Override
    public void DownloadFailed(String msg) {
        Toast.makeText(this,"下载失败--->" + msg,Toast.LENGTH_LONG).show();
    }

    @Override
    public void DownloadSuccess() {
        Toast.makeText(this,"下载成功",Toast.LENGTH_LONG).show();
    }
}