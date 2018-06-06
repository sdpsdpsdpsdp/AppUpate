package com.laisontech.appupdatelibrary.interfaces;


import com.laisontech.appupdatelibrary.okhttp.model.Progress;

/**
 * Created by SDP on 2018/1/11.
 */

public interface OnShowDownloadProgressListener {
    void onShowProgress(Progress progress);
}
