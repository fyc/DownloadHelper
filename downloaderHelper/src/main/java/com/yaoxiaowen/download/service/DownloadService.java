package com.yaoxiaowen.download.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.yaoxiaowen.download.DownloadListener;
import com.yaoxiaowen.download.DownloadStatus;
import com.yaoxiaowen.download.FileInfo;
import com.yaoxiaowen.download.R;
import com.yaoxiaowen.download.bean.DownloadInfo;
import com.yaoxiaowen.download.bean.RequestInfo;
import com.yaoxiaowen.download.config.InnerConstant;
import com.yaoxiaowen.download.db.DbHolder;
import com.yaoxiaowen.download.execute.DownloadExecutor;
import com.yaoxiaowen.download.execute.DownloadTask;
import com.yaoxiaowen.download.utils.LogUtils;
import com.yaoxiaowen.download.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author www.yaoxiaowen.com
 * time:  2017/12/18 14:25
 * @since 1.0.0
 */
public class DownloadService extends Service {

    public static final String TAG = "DownloadService";
    private Handler mainHandler;
    /**
     * id不可设置为0,否则不能设置为前台service
     */
    private static final int NOTIFICATION_DOWNLOAD_PROGRESS_ID = 0x0001;

    public static boolean canRequest = true;

    //关于线程池的一些配置
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(3, CPU_COUNT / 2);
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private static final long KEEP_ALIVE_TIME = 0L;

    private DownloadExecutor mExecutor = new DownloadExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
            KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());

    //存储任务
    private HashMap<String, DownloadTask> mTasks = new HashMap<>();

    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mainHandler = new Handler(getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (canRequest) {
            LogUtils.i(TAG, "onStartCommand() -> 启动了service服务 intent=" + intent + "\t this=" + this);
            canRequest = false;

            if (null != intent && intent.hasExtra(InnerConstant.Inner.SERVICE_INTENT_EXTRA)) {
                try {
                    ArrayList<RequestInfo> requesetes =
                            (ArrayList<RequestInfo>) intent.getSerializableExtra(InnerConstant.Inner.SERVICE_INTENT_EXTRA);
                    if (null != requesetes && requesetes.size() > 0) {
                        for (final RequestInfo request : requesetes) {
                            executeDownload(request, null, new DownloadListener() {
                                @Override
                                public void onPepare() {
                                    if (NotificationUtils.checkNotifySetting(DownloadService.this)) {
                                        createNotification(request);
                                    } else {
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(DownloadService.this, "没有通知权限", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onWait() {

                                }

                                @Override
                                public void onLoading(FileInfo fileInfo) {
                                    float pro = (float) (fileInfo.getDownloadLocation() * 1.0 / fileInfo.getSize());
                                    int progress = (int) (pro * 100);
                                    notificationBuilder.setProgress(100, progress, false);
                                    notificationBuilder.setContentText("下载进度:" + progress + "%");
//                                    notification = notificationBuilder.build();
                                    notificationManager.notify(1, notificationBuilder.build());
                                }

                                @Override
                                public void onFailed() {

                                }

                                @Override
                                public void onPaused() {
                                    notificationBuilder.setContentText("下载暂停");
//                                    notification = notificationBuilder.build();
                                    notificationManager.notify(1, notificationBuilder.build());
                                }

                                @Override
                                public void onComplete() {
                                    notificationBuilder.setProgress(100, 100, false);
                                    notificationBuilder.setContentText("下载完成");
//                                    notification = notificationBuilder.build();
                                    notificationManager.notify(1, notificationBuilder.build());
                                }

                                @Override
                                public void onCanceled() {

                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    LogUtils.i(TAG, "onStartCommand()-> 接受数据,启动线程中发生异常");
                    e.printStackTrace();
                }
            }
            canRequest = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    NotificationManager notificationManager;
    NotificationCompat.Builder notificationBuilder;

    /**
     * Notification
     */
    public void createNotification(RequestInfo request) {
        String id = "my_channel_01";
        String name = "我是渠道名字";
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
            Log.i(TAG, mChannel.toString());
            notificationManager.createNotificationChannel(mChannel);
            notification = new Notification.Builder(this)
                    .setChannelId(id)
                    .setContentTitle(request.getContentTitle())
                    .setContentText(request.getContentTitle())
                    .setSmallIcon(R.mipmap.ic_launcher).build();
        } else {
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle(request.getContentTitle())
                    .setContentText(request.getContentTitle())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(true)
                    .setProgress(100, 0, false);
            notification = notificationBuilder.build();
        }
//        notificationManager.notify(111123, notification);
        startForeground(NOTIFICATION_DOWNLOAD_PROGRESS_ID, notification);
    }

    //Todo  除了简单的synchronized, 是否有更好的方式来进行加锁呢
    private synchronized void executeDownload(RequestInfo requestInfo, DownloadListener downloadListener, DownloadListener notifyDownloadListener) {
        DownloadInfo mDownloadInfo = requestInfo.getDownloadInfo();

        //先看看在任务列表里，是否有这个任务
        DownloadTask task = mTasks.get(mDownloadInfo.getUniqueId());
        DbHolder dbHolder = new DbHolder(getBaseContext());
        FileInfo mFileInfo = dbHolder.getFileInfo(mDownloadInfo.getUniqueId());

        LogUtils.i(TAG, "executeDownload() -> task=" + task + "\t mFileInfo=" + mFileInfo);

        if (null == task) { //之前没有类似任务
            if (null != mFileInfo) {
                if (mFileInfo.getDownloadStatus() == DownloadStatus.LOADING
                        || mFileInfo.getDownloadStatus() == DownloadStatus.PREPARE) {
                    //修正文件状态
                    dbHolder.updateState(mFileInfo.getId(), DownloadStatus.PAUSE);
                } else if (mFileInfo.getDownloadStatus() == DownloadStatus.COMPLETE) {
                    if (mDownloadInfo.getFile().exists()) {
                        if (null != downloadListener) {
                            downloadListener.onComplete();//已下载完成
                        }
                        if (null != notifyDownloadListener) {
                            notifyDownloadListener.onComplete();//已下载完成
                        }
                        return;
                    } else {
                        dbHolder.deleteFileInfo(mDownloadInfo.getUniqueId());
                    }
                }
            }//end of "  null != mFileInfo "

            //创建一个下载任务
            if (requestInfo.getDictate() == InnerConstant.Request.loading) {
                task = new DownloadTask(this, mDownloadInfo, dbHolder, downloadListener, notifyDownloadListener);
                mTasks.put(mDownloadInfo.getUniqueId(), task);
            }
        } else {
            // 什么情况下, 可能存在这种这种状态
            if (task.getStatus() == DownloadStatus.COMPLETE || task.getStatus() == DownloadStatus.LOADING) {
                if (!mDownloadInfo.getFile().exists()) {
                    task.pause();
                    mTasks.remove(mDownloadInfo.getUniqueId());
                    LogUtils.i(TAG, " 状态标示完成，但是文件不存在，重新执行下载文件  ");
                    executeDownload(requestInfo, downloadListener, notifyDownloadListener);
                    return;
                }
            }
        }

        if (null != task) {
            if (requestInfo.getDictate() == InnerConstant.Request.loading) {
                if (null != downloadListener) {
                    task.setDownloadListener(downloadListener);
                }
                if (null != notifyDownloadListener) {
                    task.setNotifyDownloadListener(notifyDownloadListener);
                }
                mExecutor.executeTask(task);
            } else {
                task.pause();
            }
        }
    }

    /**
     * 内部binder类
     */
    public class DownloadBinder extends Binder {

        public void startRequest(ArrayList<RequestInfo> requesetes, HashMap<String, DownloadListener> downListeners) {
            Log.d(TAG, "startRequest() executed");
            // 执行具体的下载任务
            if (null != requesetes && requesetes.size() > 0) {
                try {
                    for (RequestInfo request : requesetes) {
                        executeDownload(request, downListeners.get(request.getDownloadInfo().getUniqueId()), null);
                    }
                } catch (Exception e) {
                    LogUtils.i(TAG, "onStartCommand()-> 接受数据,启动线程中发生异常");
                    e.printStackTrace();
                }
            }
        }

        public void stop() {
            for (Map.Entry<String, DownloadTask> task : mTasks.entrySet()) {
                task.getValue().pause();
            }
            mTasks.clear();
        }
    }
}
