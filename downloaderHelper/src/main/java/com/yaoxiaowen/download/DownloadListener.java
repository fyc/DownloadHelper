package com.yaoxiaowen.download;

public interface DownloadListener {

    void onPepare();

    void onWait();

    //下载进度显示
    void onLoading(FileInfo fileInfo);

    //下载状态显示,成功,失败,暂停,取消
    void onFailed();

    void onPaused();

    void onComplete();

    void onCanceled();
}

