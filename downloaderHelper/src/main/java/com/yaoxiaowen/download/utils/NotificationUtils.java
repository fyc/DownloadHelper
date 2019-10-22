package com.yaoxiaowen.download.utils;

import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;

public class NotificationUtils {

    public static boolean checkNotifySetting(Context context) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        // areNotificationsEnabled方法的有效性官方只最低支持到API 19，低于19的仍可调用此方法不过只会返回true，即默认为用户已经开启了通知。
        return manager.areNotificationsEnabled();
    }
}