package com.yaoxiaowen.download.execute;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.yaoxiaowen.download.DownloadConstant;
import com.yaoxiaowen.download.DownloadListener;
import com.yaoxiaowen.download.DownloadStatus;
import com.yaoxiaowen.download.bean.DownloadInfo;
import com.yaoxiaowen.download.FileInfo;
import com.yaoxiaowen.download.db.DbHolder;
import com.yaoxiaowen.download.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author www.yaoxiaowen.com
 * time:  2017/12/19 19:12
 * @since 1.0.0
 * <p>
 * Todo 这个地方可以改用 FutureTask来做
 */
public class DownloadTask implements Runnable {

    public static final String TAG = "DownloadTask";

    private Context context;
    private DownloadInfo info;
    private FileInfo mFileInfo;
    private DbHolder dbHolder;
    private boolean isPause;
    private boolean isCancle;
    private DownloadListener downloadListener;
    private DownloadListener notifyDownloadListener;

    public DownloadTask(Context context, DownloadInfo info, DbHolder dbHolder) {
        this.context = context;
        this.info = info;
        this.dbHolder = dbHolder;

        //初始化下载文件信息
        mFileInfo = new FileInfo();
        mFileInfo.setId(info.getUniqueId());
        mFileInfo.setDownloadUrl(info.getUrl());
        mFileInfo.setFilePath(info.getFile().getAbsolutePath());

        LogUtils.i(TAG, "构造函数 -> 初始化 mFileInfo=" + mFileInfo);

        FileInfo fileInfoFromDb = dbHolder.getFileInfo(info.getUniqueId());
        long location = 0;
        long fileSize = 0;
        if (null != fileInfoFromDb) {
            location = fileInfoFromDb.getDownloadLocation();
            fileSize = fileInfoFromDb.getSize();

            if (location == 0) {
                if (info.getFile().exists()) {
                    info.getFile().delete();
                }
            } else {
                //因为未知的原因, 这个文件不存在了,(虽然数据库记录表明我们的确已经下载过了),所以我们要从头开始
                if (!info.getFile().exists()) {
                    LogUtils.i(TAG, "file = " + info.getFile());
                    Log.i(TAG, "数据库记录表明我们下载过该文件, 但是现在该文件不存在,所以从头开始");
                    dbHolder.deleteFileInfo(info.getUniqueId());
                    location = 0;
                    fileSize = 0;
                }
            }
        } else {
            if (info.getFile().exists()) {
                info.getFile().delete();
            }
        }

        mFileInfo.setSize(fileSize);
        mFileInfo.setDownloadLocation(location);

        LogUtils.i(TAG, "构造函数() -> 初始化完毕  mFileInfo=" + mFileInfo);
    }

    public DownloadTask(Context context, DownloadInfo info, DbHolder dbHolder, DownloadListener downloadListener, DownloadListener notifyDownloadListener) {
        this(context, info, dbHolder);
        if (null == downloadListener) {
            this.downloadListener = new DefaultListenner();
        } else {
            this.downloadListener = downloadListener;
        }
        if (null == notifyDownloadListener) {
            this.notifyDownloadListener = new DefaultListenner();
        } else {
            this.notifyDownloadListener = notifyDownloadListener;
        }
    }

    @Override
    public void run() {
        download();
    }

    public void pause() {
        isPause = true;
    }

    public void cancle() {
        isCancle = true;
    }

    public int getStatus() {
        if (null != mFileInfo) {
            return mFileInfo.getDownloadStatus();
        }
        return DownloadStatus.FAIL;
    }

    public void setFileStatus(int status) {
        mFileInfo.setDownloadStatus(status);
    }

    public DownloadInfo getDownLoadInfo() {
        return info;
    }

    public DownloadListener getDownloadListener() {
        return downloadListener;
    }

    public DownloadListener getNotifyDownloadListener() {
        return notifyDownloadListener;
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    public void setNotifyDownloadListener(DownloadListener notifyDownloadListener) {
        this.notifyDownloadListener = notifyDownloadListener;
    }

    public FileInfo getFileInfo() {
        return mFileInfo;
    }

    private void download() {
        mFileInfo.setDownloadStatus(DownloadStatus.PREPARE);
        LogUtils.i(TAG, "准备开始下载");
        downloadListener.onPepare();
        notifyDownloadListener.onPepare();
        RandomAccessFile accessFile = null;
        HttpURLConnection http = null;
        InputStream inStream = null;

        try {
            String realUrl = getRedirectionUrl(info.getUrl());
            URL sizeUrl = new URL(realUrl);
            HttpURLConnection sizeHttp = (HttpURLConnection) sizeUrl.openConnection();
            sizeHttp.setRequestMethod("GET");
            sizeHttp.connect();

            long totalSize = sizeHttp.getContentLength();
            sizeHttp.disconnect();

            if (totalSize <= 0) {
                if (info.getFile().exists()) {
                    info.getFile().delete();
                }
                dbHolder.deleteFileInfo(info.getUniqueId());
                LogUtils.e(TAG, "文件大小 = " + totalSize + "\t, 终止下载过程");
                return;
            }

            mFileInfo.setSize(totalSize);
            accessFile = new RandomAccessFile(info.getFile(), "rwd");

            URL url = new URL(realUrl);
            http = (HttpURLConnection) url.openConnection();
            http.setConnectTimeout(10000);
            http.setRequestProperty("Connection", "Keep-Alive");
            http.setReadTimeout(10000);
            http.setRequestProperty("Range", "bytes=" + mFileInfo.getDownloadLocation() + "-");
            http.connect();

            inStream = http.getInputStream();
            byte[] buffer = new byte[1024 * 8];
            int offset;

            accessFile.seek(mFileInfo.getDownloadLocation());
            long millis = SystemClock.uptimeMillis();
            while ((offset = inStream.read(buffer)) != -1) {
                if (isPause) {
                    LogUtils.i(TAG, "下载过程 设置了 暂停");
                    mFileInfo.setDownloadStatus(DownloadStatus.PAUSE);
                    isPause = false;
                    dbHolder.saveFile(mFileInfo);

                    downloadListener.onPaused();
                    notifyDownloadListener.onPaused();

                    http.disconnect();
                    accessFile.close();
                    inStream.close();
                    return;
                }
                if(isCancle){
                    LogUtils.i(TAG, "下载过程 设置了 取消");
                    mFileInfo.setDownloadStatus(DownloadStatus.CANCLE);
                    isCancle = false;
                    dbHolder.saveFile(mFileInfo);

                    downloadListener.onCanceled();
                    notifyDownloadListener.onCanceled();

                    http.disconnect();
                    accessFile.close();
                    inStream.close();

                    if (info.getFile().exists()) {
                        info.getFile().delete();
                    }
                    return;
                }
                accessFile.write(buffer, 0, offset);
                mFileInfo.setDownloadLocation(mFileInfo.getDownloadLocation() + offset);
                mFileInfo.setDownloadStatus(DownloadStatus.LOADING);

                if (SystemClock.uptimeMillis() - millis >= 1000) {
                    millis = SystemClock.uptimeMillis();
                    dbHolder.saveFile(mFileInfo);
                    downloadListener.onLoading(mFileInfo);
                    notifyDownloadListener.onLoading(mFileInfo);
                }
            }// end of "while(..."

            mFileInfo.setDownloadStatus(DownloadStatus.COMPLETE);
            dbHolder.saveFile(mFileInfo);
            downloadListener.onLoading(mFileInfo);
            notifyDownloadListener.onLoading(mFileInfo);
            downloadListener.onComplete();
            notifyDownloadListener.onComplete();
        } catch (Exception e) {
            LogUtils.e(TAG, "下载过程发生失败");
            mFileInfo.setDownloadStatus(DownloadStatus.FAIL);
            dbHolder.saveFile(mFileInfo);
            downloadListener.onFailed();
            notifyDownloadListener.onFailed();
            e.printStackTrace();
        } finally {
            try {
                if (accessFile != null) {
                    accessFile.close();
                }
                if (inStream != null) {
                    inStream.close();
                }
                if (http != null) {
                    http.disconnect();
                }
            } catch (IOException e) {
                LogUtils.e(TAG, "finally 块  关闭文件过程中 发生异常");
                e.printStackTrace();
            }
        }


    }//end of "download()"


    private String getRedirectionUrl(String sourceUrl) {

        String redirUrl = sourceUrl;

        try {
            URL url = new URL(sourceUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == 302) {
                redirUrl = conn.getHeaderField("Location");
                LogUtils.i(TAG, " 下载地址重定向为 = " + redirUrl);

            }
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return redirUrl;
    }

    class DefaultListenner implements DownloadListener {

        @Override
        public void onPepare() {

        }

        @Override
        public void onWait() {

        }

        @Override
        public void onLoading(FileInfo fileInfo) {

        }

        @Override
        public void onFailed() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onCanceled() {

        }
    }
}
