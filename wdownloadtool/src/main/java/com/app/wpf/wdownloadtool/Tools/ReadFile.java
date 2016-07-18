package com.app.wpf.wdownloadtool.Tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by 王朋飞 on 11-26-0026.
 * 读取文件
 */

public class ReadFile {

    public static String readData(String filePath,String fileName) {
        return readDataFormFile(new File(filePath + fileName));
    }

    public static String readDataFormFile(File file) {
        StringBuilder result = new StringBuilder();
        BufferedReader bre = null;
        String str;
        try {
            bre = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while ((str = bre.readLine()) != null) {
                result.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return result.toString();
    }
}
