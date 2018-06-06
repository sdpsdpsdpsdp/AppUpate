package com.laisontech.appupdatelibrary.interfaces;

import android.content.Context;


import com.laisontech.appupdatelibrary.okhttp.model.Progress;
import com.laisontech.appupdatelibrary.server.download.DownloadListener;

import java.io.File;

/**
 * Created by SDP on 2018/1/11.
 */

public class DownloadTaskListenerImpl extends DownloadListener {
    private OnShowDownloadProgressListener listener;

    public DownloadTaskListenerImpl(Object tag, OnShowDownloadProgressListener listener) {
        super(tag);
        this.listener = listener;
    }

    @Override
    public void onStart(Progress progress) {

    }

    @Override
    public void onProgress(Progress progress) {
        listener.onShowProgress(progress);
    }

    @Override
    public void onError(Progress progress) {
        Throwable throwable = progress.exception;
        if (throwable != null) throwable.printStackTrace();
    }

    @Override
    public void onFinish(File file, Progress progress) {
//        Toast.makeText(mContext, R.string.DownloadInstall + progress.filePath, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemove(Progress progress) {

    }
}
