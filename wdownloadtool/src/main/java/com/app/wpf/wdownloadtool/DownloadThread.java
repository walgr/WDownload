package com.app.wpf.wdownloadtool;

import android.os.Handler;

import com.app.wpf.wdownloadtool.Tools.Check;
import com.app.wpf.wdownloadtool.Tools.SendMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by wazsj on 5-16-0016.
 * 下载线程
 */

public class DownloadThread extends Thread {

    private int threadID = 0;
    private String downloadUrl;
    private int fileSize = 0;
    private String fileName,filePath;

    private Handler handler;

    public DownloadThread() {

    }

    @Override
    public void run() {
        super.run();

        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(downloadUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(3000);
            int downloadPosition = downloadPosition(fileSize);
            int len,downloadSize = downloadSize(fileSize);
            httpURLConnection.setRequestProperty("range", "bytes=" + downloadPosition + "-" + (downloadPosition+downloadSize));
            httpURLConnection.connect();
            InputStream is = httpURLConnection.getInputStream();
            RandomAccessFile fos = new RandomAccessFile (filePath + fileName,"rw");
            fos.seek(downloadPosition);
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) > 0) {
                SendMessage.send(handler,0x04,"",len,0);
                fos.write(buf, 0, len);
            }
            is.close();
            fos.close();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            SendMessage.send(handler,0x01,e.getMessage(),0,0);
        } catch (IOException e) {
            e.printStackTrace();
            SendMessage.send(handler,0x01,e.getMessage(),0,0);
        }
        assert httpURLConnection != null;
        httpURLConnection.disconnect();
    }

    private int downloadPosition(int fileSize) {
        int pos;
        int averageSize = fileSize / WDownloadTool.threadNum;
        pos = averageSize * threadID;
        return pos;
    }

    private int downloadSize(int fileSize) {
        int downloadSize;
        int averageSize = fileSize / WDownloadTool.threadNum;
        if(threadID < WDownloadTool.threadNum - 1)
           downloadSize = averageSize;
        else
            downloadSize = fileSize - (WDownloadTool.threadNum - 1) * averageSize;
        return downloadSize;
    }

    public DownloadThread setThreadID(int threadID) {
        this.threadID = threadID;
        return this;
    }

    public DownloadThread setDownloadUrl(String url) {
        downloadUrl = url;
        return this;
    }

    public DownloadThread setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    public DownloadThread setFileSize(int fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public DownloadThread setFilePath(String filePath) {
        this.filePath = filePath;
        Check.CheckDir(filePath);
        return this;
    }

    public DownloadThread setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }
}
