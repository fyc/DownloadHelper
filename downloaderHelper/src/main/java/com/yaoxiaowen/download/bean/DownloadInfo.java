package com.yaoxiaowen.download.bean;


import java.io.File;
import java.io.Serializable;

/**
 * @author www.yaoxiaowen.com
 * time:  2017/12/18 21:44
 * @since 1.0.0
 */
public class DownloadInfo implements Serializable {
    private String url;
    private File file;

    public DownloadInfo(String url, File file) {
        this.url = url;
        this.file = file;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "url='" + url + '\'' +
                ", file=" + file +
                '}';
    }

    public String getUniqueId() {
        return url + file.getAbsolutePath();
    }

}
