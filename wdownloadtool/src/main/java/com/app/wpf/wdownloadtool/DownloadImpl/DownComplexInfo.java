package com.app.wpf.wdownloadtool.DownloadImpl;

/**
 * Created by wazsj on 6-18-0018.
 * 复杂信息
 */

public abstract class DownComplexInfo extends WDownloadToolResult {
    public abstract void downloadPercent(int percent);
    public abstract void downloadDetailed(long downSize);
    public abstract void downloadSpeed(double speed);
}
