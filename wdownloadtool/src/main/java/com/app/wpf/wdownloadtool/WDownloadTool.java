package com.app.wpf.wdownloadtool;

import android.os.Handler;
import android.os.Message;

/**
 * Created by wazsj on 5-16-0016.
 *
 */

public class WDownloadTool {

    private WDownloadToolInformationListener wDownloadToolInformationListener;
    private WDownloadToolListener wDownloadToolListener;
    private WDownloadToolResultListener wDownloadToolResultListener;

    public static int threadNum = 2;
    private String downloadUrl = "",fileName,filePath = "";
    private int fileSize = 0,downloadSize = 0,oldDownloadSize = 0,intervals = 100;
    private boolean isFail,isFinish;

    private Thread speedThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!isFinish) {
                try {
                    handler.sendEmptyMessage(0x02);
                    Thread.sleep(intervals);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:                      //下载失败
                    if(!isFail) {
                        wDownloadToolResultListener.DownloadFailed((String) msg.obj);
                        isFail = true;
                        isFinish = true;
                    }
                    break;
                case 0x02:                      //下载速度
                    double speed = ((double) downloadSize - oldDownloadSize) / intervals;
                    oldDownloadSize = downloadSize;
                    if(speed != 0)
                        wDownloadToolListener.DownloadSpeed(speed);
                    break;
                case 0x03:                      //第一个获取下载信息接口
                    fileSize = msg.arg1;
                    assert fileSize >= 0;
                    wDownloadToolInformationListener.DownloadInformation((String) msg.obj,fileSize);
                    for(int i = 0;i<threadNum;++i) {
                        new DownloadThread()
                                .setThreadID(i)
                                .setDownloadUrl(downloadUrl)
                                .setHandler(handler)
                                .setFileSize(fileSize)
                                .setFilePath(filePath)
                                .setFileName(fileName)
                                .start();
                    }
                    break;
                case 0x04:
                    downloadSize += msg.arg1;
                    int percent = (int)((double)downloadSize*100/fileSize);
                    wDownloadToolListener.DownloadDetailed(downloadSize);
                    wDownloadToolListener.DownloadPercent(percent);
                    if(percent == 100) {
                        wDownloadToolResultListener.DownloadSuccess();
                        isFinish = true;
                    }
                    break;
                case 0x05:
                    break;
            }
        }
    };

    public void download(String url) {
        this.downloadUrl = url;
        new GetFileInfoThread(handler,url).start();
    }

    public WDownloadTool setWDownloadToolInformationListener(WDownloadToolInformationListener wDownloadToolInformationListener) {
        this.wDownloadToolInformationListener = wDownloadToolInformationListener;
        return this;
    }

    public WDownloadTool setWDownloadToolListener(WDownloadToolListener wDownloadToolListener) {
        this.wDownloadToolListener = wDownloadToolListener;
        return this;
    }

    public WDownloadTool setWDownloadToolResultListener(WDownloadToolResultListener wDownloadToolResultListener) {
        this.wDownloadToolResultListener = wDownloadToolResultListener;
        return this;
    }

    public WDownloadTool setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public WDownloadTool setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public WDownloadTool setOpenSpeedCheck(boolean openSpeedCheck) {
        if(openSpeedCheck)
            speedThread.start();
        return this;
    }

    public interface WDownloadToolInformationListener {
        void DownloadInformation(String name, int size);
    }

    public interface WDownloadToolListener {
        void DownloadPercent(int percent);
        void DownloadDetailed(int downSize);
        void DownloadSpeed(double speed);
    }

    public interface WDownloadToolResultListener {
        void DownloadFailed(String msg);
        void DownloadSuccess();
    }
}