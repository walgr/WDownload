package com.app.wpf.wdownloadtool.Tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by wazsj on 5-16-0016.
 * 发送消息
 */

public class SendMessage {

    public static void send(Handler handler, int what, Object obj, int arg1, int arg2) {
        Message message = handler.obtainMessage();
        message.what = what;
        message.obj = obj;
        message.arg1 = arg1;
        message.arg2 = arg2;
        handler.sendMessage(message);
    }

    public static void sendData(Handler handler, int what, Bundle bundle) {
        Message message = handler.obtainMessage();
        message.what = what;
        message.setData(bundle);
        handler.sendMessage(message);
    }
}
