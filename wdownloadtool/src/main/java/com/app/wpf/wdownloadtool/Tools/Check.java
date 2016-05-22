package com.app.wpf.wdownloadtool.Tools;

import java.io.File;

/**
 *  检查类
 */

public class Check {

    //检查文件夹是否存在，不存在创建
    public static boolean CheckDir(String filePath) {
        File file = new File(filePath);
        return file.exists() || file.mkdirs();
    }

    //检查文件夹是否存在，不存在不创建
    public static boolean CheckDirNoMkDir(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    //检查文件是否存在
    public static boolean CheckFile(String filePath,String fileName) {
        File file = new File(filePath+fileName);
        return file.exists();
    }
}
