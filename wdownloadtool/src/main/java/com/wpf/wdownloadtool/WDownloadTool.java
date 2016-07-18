package com.wpf.wdownloadtool;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.wpf.wdownloadtool.DownloadImpl.DownComplexInfo;
import com.wpf.wdownloadtool.DownloadImpl.DownSimpleInfo;
import com.wpf.wdownloadtool.Thread.DownloadThread;
import com.wpf.wdownloadtool.Thread.GetFileInfoThread;
import com.wpf.wdownloadtool.Tools.DownloadInfo;
import com.wpf.wdownloadtool.Tools.ReadFile;
import com.wpf.wdownloadtool.Tools.SendMessage;
import com.wpf.wdownloadtool.Tools.WriteFile;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

/**
 * Created by wazsj on 5-16-0016.
 *
 */

public class WDownloadTool {

    private DownSimpleInfo downSimpleInfo;
    private DownComplexInfo downComplexInfo;
    private DownloadInfo downloadInfo = new DownloadInfo();
    public static int threadNum = 2;
    private DownloadThread[] downloadThreads;
    private String downloadUrl = "", fileName = "",
            filePath = Environment.getExternalStorageDirectory().getPath() + "/Download/";
    private long fileSize = 0, downloadSize = 0, oldDownloadSize = 0, intervals = 1000;
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
                    saveDownloadInfoThread();
                }
            }
        }
    });

    private String dealFailStr(String str) {
        if(str.contains("thread interrupted")) return "用户停止";
        else if(str.contains("No address associated with hostname")) return "没有网络,请重新连接网络";
        else return "未知原因";
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
                    double speed = ((double) downloadSize - oldDownloadSize);
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
                    getDownloadThread();
                    break;
                case 0x04:
                    downloadSize += msg.arg1;
                    int percent = (int) ((double) downloadSize * 100 / fileSize);
                    if(downComplexInfo != null) {
                        downComplexInfo.downloadDetailed(downloadSize);
                        downComplexInfo.downloadPercent(percent);
                    }
                    if (percent == 100 && !isFinish) {
                        if(downSimpleInfo != null) downSimpleInfo.downloadSuccess();
                        if(downComplexInfo != null) downComplexInfo.downloadSuccess();
                        isFinish = true;
                        saveDownloadInfo();
                        clear();
                    }
                    saveDownloadInfoThread();
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

    public void download(String url, String filePath, DownSimpleInfo downSimpleInfo) {
        this.filePath = filePath;
        download(url,downSimpleInfo);
    }

    public void download(String url, String filePath, DownComplexInfo downComplexInfo) {
        this.filePath = filePath;
        download(url,downComplexInfo);
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
        if(downloadThreads == null) return;
        for (DownloadThread downloadThread : downloadThreads) {
            if (downloadThread != null && downloadThread.isAlive()) {
                downloadThread.setStop();
                downloadThread.interrupt();
            }
        }
        saveDownloadInfo();
        if(downSimpleInfo != null) downSimpleInfo.downloadFailed("用户停止");
        if(downComplexInfo != null) downComplexInfo.downloadFailed("用户停止");
        isFinish = true;
    }

    public void pause() {
        if(downloadThreads == null) return;
        for (DownloadThread downloadThread : downloadThreads) {
            if (downloadThread != null && downloadThread.isAlive())
                try {
                    downloadThread.setStop();
                    downloadThread.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        saveDownloadInfo();
        if(downSimpleInfo != null) downSimpleInfo.downloadFailed("用户暂停");
        if(downComplexInfo != null) downComplexInfo.downloadFailed("用户暂停");
    }

    private void clear() {
        downloadInfo = new DownloadInfo();
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

    public void getDownloadThread() {
        boolean haveCFG = false;
        String downloadInfoString = ReadFile.readData(filePath,getCfgName()+".cfg");
        if(!downloadInfoString.isEmpty()) {
            try {
                downloadInfo = new Gson().fromJson(downloadInfoString,DownloadInfo.class);
                if(downloadInfo.threadNum != 0) {
                    threadNum = downloadInfo.threadNum;
                    haveCFG = true;
                }
            } catch (JsonSyntaxException json) {
                json.printStackTrace();
            }
        }
        if(haveCFG) {
            downloadThreads = new DownloadThread[threadNum];
            for(DownloadInfo.ThreadDownloadInfo threadDownloadInfo : downloadInfo.threadDownloadInfo) {
                downloadSize += threadDownloadInfo.downSize;
            }
            if(downloadSize == fileSize) {
                SendMessage.send(handler,0x04,"",0,0);
                return;
            }
            for (int i = 0; i < threadNum; ++i) {
                downloadThreads[i] = new DownloadThread()
                        .setThreadID(i)
                        .setDownloadUrl(downloadUrl)
                        .setHandler(handler)
                        .setFileSize(fileSize)
                        .setFilePath(filePath)
                        .setFileName(fileName);
                downloadThreads[i].setThreadDownloadInfo(downloadInfo.threadDownloadInfo[i]);
                downloadThreads[i].start();
            }
        } else {
            downloadThreads = new DownloadThread[threadNum];
            downloadInfo.threadNum = threadNum;
            downloadInfo.threadDownloadInfo = new DownloadInfo.ThreadDownloadInfo[threadNum];
            for (int i = 0; i < threadNum; ++i) {
                downloadInfo.threadDownloadInfo[i] = new DownloadInfo.ThreadDownloadInfo();
                downloadInfo.threadDownloadInfo[i].threadId = i;
                downloadThreads[i] = new DownloadThread()
                        .setThreadID(i)
                        .setDownloadUrl(downloadUrl)
                        .setHandler(handler)
                        .setFileSize(fileSize)
                        .setFilePath(filePath)
                        .setFileName(fileName);
                downloadThreads[i].setThreadDownloadInfo(downloadInfo.threadDownloadInfo[i]);
                downloadThreads[i].start();
            }
        }
    }

    private Thread thread;
    public void saveDownloadInfoThread() {
        if(thread == null) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!isFinish) {
                        saveDownloadInfo();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
        }
    }

    private void saveDownloadInfo() {
        try {
            String downInfo = new Gson().toJson(downloadInfo);
            WriteFile.WriteString(downInfo, filePath, getCfgName() + ".cfg");
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    private String getCfgName() {
        return fileName.split("\\.")[0];
    }
}