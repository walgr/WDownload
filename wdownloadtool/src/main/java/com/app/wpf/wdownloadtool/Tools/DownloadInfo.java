package com.app.wpf.wdownloadtool.Tools;

/**
 * Created by 王朋飞 on 7-18-0018.
 * 下载进度信息
 */

public class DownloadInfo {
    public int threadNum = 0;
    public ThreadDownloadInfo[] threadDownloadInfo;

    public static class ThreadDownloadInfo {
        public int threadId = 0;
        public long curPosition = 0;
        public long downSize = 0;
    }
}
