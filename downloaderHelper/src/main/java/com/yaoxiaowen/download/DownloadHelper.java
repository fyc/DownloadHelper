package com.yaoxiaowen.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.yaoxiaowen.download.config.InnerConstant;
import com.yaoxiaowen.download.bean.DownloadInfo;
import com.yaoxiaowen.download.bean.RequestInfo;
import com.yaoxiaowen.download.service.DownloadService;
import com.yaoxiaowen.download.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author www.yaoxiaowen.com
 * time:  2017/12/20 18:10
 * @since 1.0.0
 */
public class DownloadHelper {

    public static final String TAG = "DownloadHelper";

    private volatile static DownloadHelper SINGLETANCE;

    private static ArrayList<RequestInfo> requests = new ArrayList<>();

    private HashMap<String, DownloadListener> downListeners = new HashMap<>();

    private DownloadService.DownloadBinder mDownloadBinder;

    private ServiceConnection connection = new ServiceConnection() {

        /**
         * 当取消绑定的时候被回调。但正常情况下是不被调用的，它的调
         * 用时机是当Service服务被意外销毁时，
         * 例如内存的资源不足时这个方法才被自动调用。
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDownloadBinder.stop();
            //解绑
            mDownloadBinder = null;
            requests.clear();
            downListeners.clear();
        }

        /**
         * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方
         * 法获取绑定Service传递过来的IBinder对象，
         * 通过这个IBinder对象，实现宿主和Service的交互。
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadBinder = (DownloadService.DownloadBinder) service;
            mDownloadBinder.startRequest(requests, downListeners);
            requests.clear();
            downListeners.clear();
        }
    };

    private DownloadHelper() {
    }

    public static DownloadHelper getInstance() {
        if (SINGLETANCE == null) {
            synchronized (DownloadHelper.class) {
                if (SINGLETANCE == null) {
                    SINGLETANCE = new DownloadHelper();
                }
            }
        }
        return SINGLETANCE;
    }

    /**
     * 提交  下载/暂停  等任务.(提交就意味着开始执行生效)
     *
     * @param context
     */
    public synchronized void submit(Context context) {
        if (requests.isEmpty()) {
            LogUtils.w("没有下载任务可供执行");
            return;
        }

        if (null != mDownloadBinder) {
            mDownloadBinder.startRequest(requests, downListeners);
            requests.clear();
            downListeners.clear();
        } else {
            //绑定
            Intent bindIntent = new Intent(context, DownloadService.class);
            context.bindService(bindIntent, connection, context.BIND_AUTO_CREATE);
        }

    }// end of "submit(..."

    /**
     * 提交  下载/暂停  等任务.(提交就意味着开始执行生效)
     *
     * @param context
     */
    public synchronized void submitForeground(Context context) {
        if (requests.isEmpty()) {
            LogUtils.w("没有下载任务可供执行");
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(InnerConstant.Inner.SERVICE_INTENT_EXTRA, requests);
        context.startService(intent);
        requests.clear();
        downListeners.clear();
    }// end of "submit(..."

    /**
     * 添加 新的下载任务
     *
     * @param url              下载的url
     * @param file             存储在某个位置上的文件
     * @param downloadListener 下载过程的监听接口
     * @return DownloadHelper自身 (方便链式调用)
     */
    public DownloadHelper addTask(String url, File file, DownloadListener downloadListener) {
        RequestInfo requestInfo = createRequest(url, file, InnerConstant.Request.loading);
        downListeners.put(requestInfo.getDownloadInfo().getUniqueId(), downloadListener);
        LogUtils.i(TAG, "addTask() requestInfo=" + requestInfo);

        requests.add(requestInfo);
        return this;
    }

    public DownloadHelper addTask(String url, File file, CharSequence contentTitle, CharSequence contentText) {
        RequestInfo requestInfo = createRequest(url, file, contentTitle, contentText, InnerConstant.Request.loading);
        LogUtils.i(TAG, "addTask() requestInfo=" + requestInfo);

        requests.add(requestInfo);
        return this;
    }

    /**
     * 暂停某个下载任务
     *
     * @param url  下载的url
     * @param file 存储在某个位置上的文件
     * @return DownloadHelper自身 (方便链式调用)
     */
    public DownloadHelper pauseTask(String url, File file) {
        RequestInfo requestInfo = createRequest(url, file, InnerConstant.Request.pause);
        LogUtils.i(TAG, "pauseTask() -> requestInfo=" + requestInfo);
        requests.add(requestInfo);
        return this;
    }

    public DownloadHelper cancleTask(String url, File file) {
        RequestInfo requestInfo = createRequest(url, file, InnerConstant.Request.cancle);
        LogUtils.i(TAG, "pauseTask() -> requestInfo=" + requestInfo);
        requests.add(requestInfo);
        return this;
    }

    private RequestInfo createRequest(String url, File file, int dictate) {
        RequestInfo request = new RequestInfo();
        request.setDictate(dictate);
        request.setDownloadInfo(new DownloadInfo(url, file));
        return request;
    }

    private RequestInfo createRequest(String url, File file, CharSequence contentTitle, CharSequence contentText, int dictate) {
        RequestInfo request = createRequest(url, file, dictate);
        request.setContentTitle(contentTitle);
        request.setContentText(contentText);
        return request;
    }

    public synchronized void unbindService(Context context) {
        try {
            //解绑 防止java.lang.IllegalArgumentException: Service not registered
            if (null == mDownloadBinder) return;
            context.unbindService(connection);
        } catch (Exception e) {

        }

    }// end of "submit(..."
}
