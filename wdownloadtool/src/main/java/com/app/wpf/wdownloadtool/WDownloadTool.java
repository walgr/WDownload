package com.app.wpf.wdownloadtool;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.app.wpf.wdownloadtool.DownloadImpl.DownComplexInfo;
import com.app.wpf.wdownloadtool.DownloadImpl.DownSimpleInfo;
import com.app.wpf.wdownloadtool.Thread.DownloadThread;
import com.app.wpf.wdownloadtool.Thread.GetFileInfoThread;

/**
 * Created by wazsj on 5-16-0016.
 *
 */

public class WDownloadTool {

    private DownSimpleInfo downSimpleInfo;
    private DownComplexInfo downComplexInfo;
    public static int threadNum = 2;
    private DownloadThread[] downloadThreads = new DownloadThread[threadNum];
    private String downloadUrl = "", fileName = "", filePath = Environment.getExternalStorageDirectory().getPath() + "/Download/";
    private int fileSize = 0, downloadSize = 0, oldDownloadSize = 0, intervals = 100;
    private boolean isFail, isFinish;

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

    private String dealFailStr(String str) {
        switch (str) {
            case "thread interrupted":
                return "用户停止";
            default:
                return "未知原因";
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:                      //下载失败
                    if (!isFail) {
                        String failStr = dealFailStr((String) msg.obj);
                        if(downSimpleInfo != null)
                            downSimpleInfo.downloadFailed(failStr);
                        if(downComplexInfo != null)
                            downComplexInfo.downloadFailed(failStr);
                        isFail = true;
                        isFinish = true;
                    }
                    break;
                case 0x02:                      //下载速度
                    double speed = ((double) downloadSize - oldDownloadSize) / intervals;
                    oldDownloadSize = downloadSize;
                    if (speed != 0 && downComplexInfo != null) {
                        downComplexInfo.downloadSpeed(speed);
                    }
                    break;
                case 0x03:                      //第一个获取下载信息接口
                    fileSize = msg.arg1;
                    if(fileName.isEmpty()) {
                        fileName = (String) msg.obj;
                        String[] strings = fileName.split("/");
                        fileName = strings[strings.length - 1];
                    }
//                    downSimpleInfo.fileInfo((String) msg.obj, fileSize);

                    for (int i = 0; i < threadNum; ++i) {
                        downloadThreads[i] = new DownloadThread()
                                .setThreadID(i)
                                .setDownloadUrl(downloadUrl)
                                .setHandler(handler)
                                .setFileSize(fileSize)
                                .setFilePath(filePath)
                                .setFileName(fileName);
                        downloadThreads[i].start();
                    }
                    break;
                case 0x04:
                    downloadSize += msg.arg1;
                    int percent = (int) ((double) downloadSize * 100 / fileSize);
                    if(downComplexInfo != null) {
                        downComplexInfo.downloadDetailed(downloadSize);
                        downComplexInfo.downloadPercent(percent);
                    }
                    if (percent == 100) {
                        if(downSimpleInfo != null) downSimpleInfo.downloadSuccess();
                        if(downComplexInfo != null) downComplexInfo.downloadSuccess();
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

    public void download(String url,DownSimpleInfo downSimpleInfo) {
        this.downSimpleInfo = downSimpleInfo;
        download(url);
    }

    public void download(String url,DownComplexInfo downComplexInfo) {
        this.downComplexInfo = downComplexInfo;
        download(url);
    }

    public void download(String url, String filePath, String fileName, DownSimpleInfo downSimpleInfo) {
        this.filePath = filePath;
        this.fileName = fileName;
        download(url,downSimpleInfo);
    }

    public void download(String url, String filePath, String fileName, DownComplexInfo downComplexInfo) {
        this.filePath = filePath;
        this.fileName = fileName;
        download(url,downComplexInfo);
    }

    public void stop() {
        for(DownloadThread downloadThread : downloadThreads) {
            if(downloadThread.isAlive())
                downloadThread.interrupt();
        }
    }

    public void pause() {
        for(DownloadThread downloadThread : downloadThreads) {
            if(downloadThread.isAlive())
                try {
                    downloadThread.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
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
}