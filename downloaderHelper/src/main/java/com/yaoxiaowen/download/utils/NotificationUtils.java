package com.yaoxiaowen.download.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.RemoteViews;

import com.yaoxiaowen.download.NotificationBroadcastReceiver;
import com.yaoxiaowen.download.R;

public class NotificationUtils {

    public static boolean checkNotifySetting(Context context) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        // areNotificationsEnabled方法的有效性官方只最低支持到API 19，低于19的仍可调用此方法不过只会返回true，即默认为用户已经开启了通知。
        return manager.areNotificationsEnabled();
    }

    //通知栏权限
//    private void checkNotify(){
//        if(!NotificationUtils.checkNotifySetting(MainActivity.this)){
//            MyAlertDialog myAlertDialog = new MyAlertDialog(this).builder()
//                    .setTitle("通知权限")
//                    .setMsg("尚未开启通知权限，点击去开启")
//                    .setPositiveButton("确认", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            try {
//                                Intent intent = new Intent();
//                                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
//                                //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
//                                intent.putExtra(EXTRA_APP_PACKAGE, getPackageName());
//                                intent.putExtra(EXTRA_CHANNEL_ID, getApplicationInfo().uid);
//
//                                //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
//                                intent.putExtra("app_package", getPackageName());
//                                intent.putExtra("app_uid", getApplicationInfo().uid);
//
//                                // 小米6 -MIUI9.6-8.0.0系统，是个特例，通知设置界面只能控制"允许使用通知圆点"——然而这个玩意并没有卵用，我想对雷布斯说：I'm not ok!!!
//                                //  if ("MI 6".equals(Build.MODEL)) {
//                                //      intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                //      Uri uri = Uri.fromParts("package", getPackageName(), null);
//                                //      intent.setData(uri);
//                                //      // intent.setAction("com.android.settings/.SubSettings");
//                                //  }
//                                startActivity(intent);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                // 出现异常则跳转到应用设置界面：锤子坚果3——OC105 API25
//                                Intent intent = new Intent();
//
//                                //下面这种方案是直接跳转到当前应用的设置界面。
//                                //https://blog.csdn.net/ysy950803/article/details/71910806
//                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                Uri uri = Uri.fromParts("package", getPackageName(), null);
//                                intent.setData(uri);
//                                startActivity(intent);
//                            }
//                        }
//                    }).setNegativeButton("取消", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {}
//                    });
//            myAlertDialog.show();
//        }
//    }

//    int index = 0;
//
//    /**
//     * 弹出通知提醒
//     */
//    private void tapNotification(Context context) {
//        index = index + 1;
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        //准备intent
//        Intent clickIntent = new Intent(context, NotificationBroadcastReceiver.class);
//        clickIntent.setAction("com.xxx.xxx.click");
//        // 构建 PendingIntent
//        PendingIntent clickPI = PendingIntent.getBroadcast(context, 1, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//        //准备intent
//        Intent cacelIntent = new Intent(context, NotificationBroadcastReceiver.class);
//        cacelIntent.setAction("com.xxx.xxx.cancel");
//        // 构建 PendingIntent
//        PendingIntent cacelPI = PendingIntent.getBroadcast(context, 2, cacelIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//        //准备intent
//        Intent fullscreenIntent = new Intent(context, NotificationBroadcastReceiver.class);
//        fullscreenIntent.setAction("com.xxx.xxx.fullscreen");
//        // 构建 PendingIntent
//        PendingIntent fullscreenPI = PendingIntent.getBroadcast(context, 2, fullscreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        String channelID = "1";
//
//        //Notification.Builder builder = new Notification.Builder(MainActivity.this);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID);
//
//        String title = "标题" + index;
//        String conttext = "我是一个通知" + index;
//        //NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
//        //bigTextStyle.setBigContentTitle(title);
//        //bigTextStyle.setSummaryText(conttext);
//        //bigTextStyle.bigText("一二三西思思");
//        //builder.setStyle(bigTextStyle);
//
//        //NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle("UserName");
//        //messagingStyle.addMessage("message",System.currentTimeMillis(),"JulyYu");
//        //messagingStyle.setConversationTitle("Messgae Title");
//        //builder.setStyle(messagingStyle);
//
//
//        //NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//        //inboxStyle.setBigContentTitle(title);
//        //inboxStyle.setSummaryText(conttext);
//        //inboxStyle.addLine("A");
//        //inboxStyle.addLine("B");
//
//
//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.activity_main);
//        builder.setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle(title)//设置通知标题
//                .setContentText(conttext)//设置通知内容
//                .setContentIntent(clickPI)// 设置pendingIntent,点击通知时就会用到
//                .setAutoCancel(true)//设为true，点击通知栏移除通知
//                .setDeleteIntent(cacelPI)//设置pendingIntent,左滑右滑通知时就会用到
//                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))//设置大图标
//                .setNumber(index)//显示在右边的数字
//                .setOngoing(false)//设置是否是正在进行中的通知，默认是false
//                .setOnlyAlertOnce(false)//设置是否只通知一次
//                .setProgress(100, 20, false)
//                //.setStyle(messagingStyle)
//                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.publicnotify))
//                .setVibrate(new long[]{3000, 1000, 500, 700, 500, 300})//延迟3秒，然后震动1000ms，再延迟500ms，接着震动700ms,最后再延迟500ms，接着震动300ms。
//                .setLights(Color.RED, 2000, Color.BLUE)
//                //.setDefaults(Notification.DEFAULT_LIGHTS)
//                .setSubText("我是一个SubText")
//                .setTicker("通知测试")//提示
//                .setUsesChronometer(true)
//                .setWhen(System.currentTimeMillis())
//                .setLocalOnly(true)//设置此通知是否仅与当前设备相关。如果设置为true，通知就不能桥接到其他设备上进行远程显示。
//                .setShowWhen(true);
//        if (index % 5 == 0) {
//            builder.setSortKey("A");//设置针对一个包内的通知进行排序的键值
//        } else if (index % 5 == 1) {
//            builder.setSortKey("B");//设置针对一个包内的通知进行排序的键值
//        } else if (index % 5 == 2) {
//            builder.setSortKey("C");//设置针对一个包内的通知进行排序的键值
//        } else if (index % 5 == 3) {
//            builder.setSortKey("D");//设置针对一个包内的通知进行排序的键值
//        } else if (index % 5 == 4) {
//            builder.setSortKey("E");//设置针对一个包内的通知进行排序的键值
//        }
//        //响应紧急状态的全屏事件（例如来电事件），也就是说通知来的时候，跳过在通知区域点击通知这一步，直接执行fullScreenIntent代表的事件
//        //builder.setFullScreenIntent(fullscreenPI, true);
//        //Bundle bundle = new Bundle();
//        //builder.setExtras(bundle);
//        //                if(index % 2 == 0){
//        //                    builder.setCategory(NotificationCompat.CATEGORY_CALL);
//        //                }else if(index % 2 == 1){
//        //                    builder.setCategory(NotificationCompat.CATEGORY_EMAIL);
//        //                }
//
//        builder.setVisibility(Notification.VISIBILITY_PUBLIC);//悬挂通知（横幅）
//
//        builder.setCustomBigContentView(remoteViews)//设置通知的布局
//                .setCustomHeadsUpContentView(remoteViews)//设置悬挂通知的布局
//                .setCustomContentView(remoteViews);
//        //builder.setChronometerCountDown()//已舍弃
//
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//Android7.0以上
////
////            //                if(index == 1){
////            //                    builder.setGroupSummary(true);//设置是否为一组通知的第一个显示
////            //                }
////            //                builder.setGroup("notification_test");//捆绑通知
////
////
////            //准备intent
////            Intent replyPendingIntent = new Intent(context, NotificationBroadcastReceiver.class);
////            replyPendingIntent.setAction("com.xxx.xxx.replypending");
////            replyPendingIntent.putExtra("messageId", index);
////            // 构建 PendingIntent
////            PendingIntent replyPendingPI = PendingIntent.getBroadcast(context, 2, replyPendingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
////
////            builder.setRemoteInputHistory(new String[]{"这条通知可以点击下面按钮直接回复..."});
////            //创建一个可添加到通知操作的 RemoteInput.Builder 实例。 该类的构造函数接受系统用作文本输入密钥的字符串。 之后，手持式设备应用使用该密钥检索输入的文本。
////            RemoteInput remoteInput = new RemoteInput.Builder("key_text_reply")
////                    .setLabel("回复")
////                    .build();
////
////            //使用 addRemoteInput() 向操作附加 RemoteInput 对象。
////            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "点击直接回复", replyPendingPI)
////                    .addRemoteInput(remoteInput)
////                    .build();
////
////            //对通知应用操作。
////            builder.addAction(action);
////
////        }
//
//        //builder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE);//设置角标类型（无效）
//        //builder.setSettingsText();已舍弃
//        //builder.setShortcutId("100");
//        //builder.setColorized(true);//启用通知的背景颜色设置
//        //builder.setColor(Color.RED);
//
//        //builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//Android 8.0以上
//            String channelName = "我是通知渠道";
//            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
//            //channel.setShowBadge(true);//显示通知角标
//            //boolean aa = channel.canShowBadge();
//            //channel.setBypassDnd(true);// 设置绕过请勿打扰模式
//            //boolean ca = channel.canBypassDnd();
//            channel.enableLights(true);//设置通知出现时的闪灯（如果 android 设备支持的话）
//            channel.enableVibration(true);// 设置通知出现时的震动（如果 android 设备支持的话）
//            channel.setDescription("AAAAAAAAAA");//设置渠道的描述信息
//            //channel.setGroup("AAAA");
//            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
//            channel.setLightColor(Color.YELLOW);
//            //channel.setLockscreenVisibility();
//            channel.setName("wweqw");
//            channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.publicnotify), new AudioAttributes.Builder().build());
//            channel.setVibrationPattern(new long[]{200, 200, 1000, 200, 1000, 200});
//            notificationManager.createNotificationChannel(channel);
//            //创建通知时指定channelID
//            builder.setChannelId(channelID);
//            //builder.setTimeoutAfter(5000);//设置超时时间，超时之后自动取消（Android8.0有效）
//        }
//
//        Notification notification = builder.build();
//        //锁屏时显示通知
//        builder.setPublicVersion(notification);
//        notificationManager.notify(index, notification);
//    }
}