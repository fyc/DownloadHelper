package com.yaoxiaowen.download.bean;


import com.yaoxiaowen.download.utils.DebugUtils;

import java.io.Serializable;

/**
 * @author   www.yaoxiaowen.com
 * time:  2017/12/18 21:29
 * @since 1.0.0
 */
public class RequestInfo implements Serializable{

    private int dictate;   //下载的控制状态

    private CharSequence contentTitle,contentText;

    private DownloadInfo downloadInfo;

    public RequestInfo() {
    }

    public int getDictate() {
        return dictate;
    }

    public void setDictate(int dictate) {
        this.dictate = dictate;
    }

    public CharSequence getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(CharSequence contentTitle) {
        this.contentTitle = contentTitle;
    }

    public CharSequence getContentText() {
        return contentText;
    }

    public void setContentText(CharSequence contentText) {
        this.contentText = contentText;
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    public void setDownloadInfo(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }


    @Override
    public String toString() {
        return "RequestInfo{" +
                "dictate=" + DebugUtils.getRequestDictateDesc(dictate) +
                ", downloadInfo=" + downloadInfo +
                '}';
    }
}
