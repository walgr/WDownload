package com.app.wpf.wdownloadtool.Tools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 写入文件
 */

public class WriteFile {

    public static boolean Write(InputStream is, String filePath, String fileName) throws IOException {
        if (!fileName.isEmpty()) {
            FileOutputStream fos = new FileOutputStream(filePath + fileName);
            byte[] buff = new byte[1024];
            int len;
            while ((len = is.read(buff)) != -1) {
                fos.write(buff, 0, len);
            }
            is.close();
            fos.close();
            return true;
        }
        return false;
    }

    public static boolean WriteByByte(int byteOffset,byte[] data,String filePath,String fileName) throws IOException {
        if(data != null && data.length != 0) {
            if (!fileName.isEmpty()) {
                FileOutputStream out = new FileOutputStream(filePath + fileName,true);
                out.write(data);
                out.flush();
                out.close();
                return true;
            }
        }
        return false;
    }

}
