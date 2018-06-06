package com.laisontech.appupdatelibrary.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.laisontech.appupdatelibrary.DownloadProgressBar;
import com.laisontech.appupdatelibrary.R;
import com.laisontech.appupdatelibrary.AppUpdateInfo;
import com.laisontech.appupdatelibrary.okhttp.OkHttpServerUtils;
import com.laisontech.appupdatelibrary.okhttp.model.Progress;
import com.laisontech.appupdatelibrary.okhttp.request.GetRequest;
import com.laisontech.appupdatelibrary.server.OkHttpDownload;
import com.laisontech.appupdatelibrary.server.download.DownloadListener;
import com.laisontech.appupdatelibrary.server.download.DownloadTask;

import java.io.File;
import java.text.NumberFormat;

/**
 * ..................................................................
 * .         The Buddha said: I guarantee you have no bug!          .
 * .                                                                .
 * .                            _ooOoo_                             .
 * .                           o8888888o                            .
 * .                           88" . "88                            .
 * .                           (| -_- |)                            .
 * .                            O\ = /O                             .
 * .                        ____/`---'\____                         .
 * .                      .   ' \\| |// `.                          .
 * .                       / \\||| : |||// \                        .
 * .                     / _||||| -:- |||||- \                      .
 * .                       | | \\\ - /// | |                        .
 * .                     | \_| ''\---/'' | |                        .
 * .                      \ .-\__ `-` ___/-. /                      .
 * .                   ___`. .' /--.--\ `. . __                     .
 * .                ."" '< `.___\_<|>_/___.' >'"".                  .
 * .               | | : `- \`.;`\ _ /`;.`/ - ` : | |               .
 * .                 \ \ `-. \_ __\ /__ _/ .-` / /                  .
 * .         ======`-.____`-.___\_____/___.-`____.-'======          .
 * .                            `=---='                             .
 * ..................................................................
 * Created by SDP on 2018/6/5.
 * 下载帮助类
 */

public class DownloadUtils {
    public static void refreshUi(Activity context, Progress progress, Button btnCancel, DownloadProgressBar downloadProgressBar) {
        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(2);
        downloadProgressBar.setProgress((int) (progress.fraction * 100));
        switch (progress.status) {
            case Progress.WAITING:
                downloadProgressBar.setState(DownloadProgressBar.STATUS_PROGRESS_BAR_WAIT);
                btnCancel.setText(context.getResources().getString(R.string.update_notnow));
                break;
            case Progress.ERROR:
                downloadProgressBar.setState(DownloadProgressBar.STATUS_PROGRESS_BAR_ERROR);
                btnCancel.setText(context.getResources().getString(R.string.update_notnow));
                break;
            case Progress.NONE:
            case Progress.PAUSE:
                downloadProgressBar.setState(DownloadProgressBar.STATUS_PROGRESS_BAR_PAUSE);
                btnCancel.setText(context.getResources().getString(R.string.update_notnow));
                break;
            case Progress.LOADING:
                downloadProgressBar.setState(DownloadProgressBar.STATUS_PROGRESS_BAR_DOWNLOADING);
                btnCancel.setText(context.getResources().getString(R.string.backgroundDownload));
                break;
            case Progress.FINISH:
                //安装还是下载
                downloadProgressBar.setState(DownloadProgressBar.STATUS_PROGRESS_BAR_INSTALL);
                btnCancel.setText(context.getResources().getString(R.string.update_notnow));
                break;
        }
    }

    //根据下载进度条执行不同的下载操作
    public static void setOperateDownloadBar(Activity context, int state, AppUpdateInfo laisonAppBean, DownloadListener listener) {
        if (laisonAppBean == null) return;
        DownloadTask downloadTask = DownloadUtils.getDownloadTask(laisonAppBean, listener);
        switch (state) {
            case DownloadProgressBar.STATUS_PROGRESS_BAR_START_DOWNLOAD:
            case DownloadProgressBar.STATUS_PROGRESS_BAR_UPDATE:
                if (DownloadUtils.isNetWorkWell(context)) {
                    downloadTask.start();
                }
                break;
            case DownloadProgressBar.STATUS_PROGRESS_BAR_DOWNLOADING:
                if (DownloadUtils.isNetWorkWell(context)) {
                    downloadTask.pause();
                }
                break;
            case DownloadProgressBar.STATUS_PROGRESS_BAR_PAUSE:
                if (DownloadUtils.isNetWorkWell(context)) {
                    downloadTask.start();
                }
                break;
            case DownloadProgressBar.STATUS_PROGRESS_BAR_INSTALL:
                PackageUtils.install(context, downloadTask.progress.filePath);
                break;
            case DownloadProgressBar.STATUS_PROGRESS_BAR_OPEN:
                PackageUtils.launcherApp(context, laisonAppBean.getProjectname());
                break;
            case DownloadProgressBar.STATUS_PROGRESS_BAR_WAIT:
                Toast.makeText(context, context.getResources().getString(R.string.YouCanOnlyDownloadThree), Toast.LENGTH_SHORT).show();
                break;
            case DownloadProgressBar.STATUS_PROGRESS_BAR_ERROR:
                if (DownloadUtils.isNetWorkWell(context)) {
                    downloadTask.remove(true);//首先从数据中移除
                    downloadTask = getDownloadTask(laisonAppBean,listener);//再重新构建任务
                    downloadTask.start();
                }
                break;

        }
    }


    private static DownloadTask getDownloadTask(AppUpdateInfo laisonAppBean, DownloadListener listener) {
        if (laisonAppBean == null) return null;
        DownloadTask downloadTask;
        if (OkHttpDownload.getInstance().hasTask(laisonAppBean.getApkdownloadlink())) {
            downloadTask = OkHttpDownload.getInstance().getTask(laisonAppBean.getApkdownloadlink());
        } else {
            //添加新的下载任务
            GetRequest<File> request = OkHttpServerUtils.<File>get(laisonAppBean.getApkdownloadlink())
                    .headers("LaisonApp", "laisontech")
                    .params("laisontechApp", "laison");
            downloadTask = OkHttpDownload.request(laisonAppBean.getApkdownloadlink(), request)
                    .priority(laisonAppBean.getPriority())
                    .extra1(laisonAppBean)
                    .save();
        }
        downloadTask.register(listener);
        return downloadTask;
    }

    private static boolean isNetWorkWell(Activity context) {
        boolean networkState = getNetworkState(context);
        if (!networkState) {
            Toast.makeText(context, context.getResources().getString(R.string.NoConnectNet), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private static boolean getNetworkState(Context activity) {
        if (activity == null) return false;
        ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }

        NetworkInfo[] info = manager.getAllNetworkInfo();
        if (info == null) {
            return false;
        }

        NetworkInfo.State state;
        boolean connected = false;
        try {
            for (NetworkInfo networkInfo : info) {
                if (networkInfo == null) continue;
                state = networkInfo.getState();
                if (state != NetworkInfo.State.CONNECTED) {
                    continue;
                }

                connected = true;
                break;
            }
        } catch (NullPointerException e) {
            Log.e("LoginFunc", "GetNetworkState: ", e);
        }
        return connected;
    }

}
