package com.yaoxiaowen.download.execute;


import com.yaoxiaowen.download.DownloadStatus;
import com.yaoxiaowen.download.utils.LogUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author   www.yaoxiaowen.com
 * time:  2017/12/20 18:36
 * @since 1.0.0
 */
public class DownloadExecutor extends ThreadPoolExecutor{
    
    public static final String TAG = "DownloadExecutor";
    
    public DownloadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                            TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public void executeTask(DownloadTask task){
        int status = task.getStatus();
        if (status== DownloadStatus.PAUSE || status== DownloadStatus.FAIL){
            task.setFileStatus(DownloadStatus.WAIT);
            task.getDownloadListener().onWait();
            task.getNotifyDownloadListener().onWait();
            execute(task);
        }else {
            LogUtils.w(TAG, "文件状态不正确, 不进行下载 FileInfo=" + task.getFileInfo());
        }
    }
}
