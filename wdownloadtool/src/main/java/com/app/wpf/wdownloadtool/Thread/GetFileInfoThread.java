package com.app.wpf.wdownloadtool.Thread;

import android.os.Handler;

import com.app.wpf.wdownloadtool.Tools.SendMessage;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by wazsj on 5-16-0016.
 * 获取文件信息
 */

public class GetFileInfoThread extends Thread {

    private Handler handler;
    private String downloadUrl;

    public GetFileInfoThread(Handler handler,String downloadUrl) {
        this.handler = handler;
        this.downloadUrl = downloadUrl;
    }

    @Override
    public void run() {
        super.run();
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(downloadUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setAllowUserInteraction(true);
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("User-Agent", "Net");
            httpURLConnection.setRequestProperty(
                    "Accept",
                    "image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
                            + "application/x-shockwave-flash, application/xaml+xml, "
                            + "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
                            + "application/x-ms-application, application/vnd.ms-excel, "
                            + "application/vnd.ms-powerpoint, application/msword, */*");
            httpURLConnection.setRequestProperty("Accept-Language", "zh-CN");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            int fileSize = httpURLConnection.getContentLength();
            SendMessage.send(handler, 0x03, httpURLConnection.getURL().getFile(), fileSize, 0);
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
}
