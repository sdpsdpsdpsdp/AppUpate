package com.laisontech.appupdatelibrary.server.upload;


import com.laisontech.appupdatelibrary.server.ProgressListener;

/**
 * ================================================
 * 描    述：全局的上传监听
 * 修订历史：
 * ================================================
 */
public abstract class UploadListener<T> implements ProgressListener<T> {

    public final Object tag;

    public UploadListener(Object tag) {
        this.tag = tag;
    }
}
