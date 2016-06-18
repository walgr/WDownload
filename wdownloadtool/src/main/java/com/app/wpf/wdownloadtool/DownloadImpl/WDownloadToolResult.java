package com.app.wpf.wdownloadtool.DownloadImpl;

/**
 * Created by wazsj on 6-18-0018.
 * 下载结果
 */

abstract class WDownloadToolResult {
    public abstract void downloadSuccess();
    public abstract void downloadFailed(String msg);
}
