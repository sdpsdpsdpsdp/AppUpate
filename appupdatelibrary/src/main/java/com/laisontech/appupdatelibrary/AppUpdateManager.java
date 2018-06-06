package com.laisontech.appupdatelibrary;

import android.app.Activity;
import android.content.DialogInterface;

import com.laisontech.appupdatelibrary.interfaces.OnAppUpdateListener;
import com.laisontech.appupdatelibrary.interfaces.QueryAppInfoListener;
import com.laisontech.appupdatelibrary.okhttp.db.DownloadManager;
import com.laisontech.appupdatelibrary.server.OkHttpDownload;
import com.laisontech.appupdatelibrary.server.download.DownloadTask;
import com.laisontech.appupdatelibrary.utils.CalculateUtils;
import com.laisontech.appupdatelibrary.utils.DisplayUtil;
import com.laisontech.appupdatelibrary.utils.PackageUtils;

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
 * Created by SDP on 2018/6/4.
 */

public class AppUpdateManager implements DialogInterface.OnDismissListener, UpdateAppDialog.OnDialogClickListener, UpdateAppDialog.OnForceCancelUpdateListener, QueryAppInfoListener<AppUpdateInfo> {
    private OnAppUpdateListener mListener;
    private Activity mActivity;
    private UpdateAppDialog mAppDialog;
    private OkHttpDownload mOkHttpDownload;

    public AppUpdateManager(Activity activity, OnAppUpdateListener listener) {
        mActivity = activity;
        this.mListener = listener;
        mOkHttpDownload = OkHttpDownload.getInstance();
        mOkHttpDownload.restore(DownloadManager.getInstance().getAll());//从数据库中恢复数据到内存中
    }

    /**
     * 初始化,开始请求
     */
    public void queryAppInfoFromServer() {
        String appPackageName = PackageUtils.getAppPackageName(mActivity);
        if (appPackageName == null || appPackageName.isEmpty()) return;
        String appServer = UpdateInitializer.getInstance(mActivity).getUpdateServerAddress();
        new QueryAppInfoTask(this).execute(appServer, appPackageName);
    }

    @Override
    public void startQuery() {
        mListener.startCheckUpdate();
    }

    @Override
    public void queryInfo(AppUpdateInfo appUpdateInfo) {
        mListener.checkUpdateFinished();
        if (appUpdateInfo == null) {//获取数据出错时，提示
            mListener.checkUpdateError(getStr(R.string.QueryAppInfoError));
            return;
        }
        //数据不为空，则检查服务端获取的app的版本号与当前版本号对比
        boolean needUpdate = CalculateUtils.checkNeedUpdate(mActivity, appUpdateInfo);
        if (!needUpdate) {
            mListener.checkUpdateError(getStr(R.string.CurrentIsLatestVersion));
            return;
        }
        //如果已经下载了app，如果是旧版本，则删除文件，移除任务。
        restoreDBInfo(appUpdateInfo);
        //恢复数据库中保存的信息
        //不是最新版本，弹出dialog
        if (mAppDialog == null) {
            mAppDialog = new UpdateAppDialog(mActivity, DisplayUtil.getWidth(mActivity), appUpdateInfo);
        }
        mAppDialog.setDismiss(this)
                .setCanCancel(false)
                .setForceCancelListener(this)
                .setOnClickBottomListener(this)
                .show();
    }

    private void restoreDBInfo(AppUpdateInfo appUpdateInfo) {
        String downloadUrl = appUpdateInfo.getApkdownloadlink();
        boolean hasTask = mOkHttpDownload.hasTask(downloadUrl);
        if (hasTask) {
            DownloadTask task = mOkHttpDownload.getTask(downloadUrl);
            AppUpdateInfo saveAppInfo = (AppUpdateInfo) task.progress.extra1;
            if (saveAppInfo != null) {
                if (appUpdateInfo.getVersioncode() > saveAppInfo.getVersioncode()) {
                    task.remove(true);
                }
            }
        }
    }

    private void closeDialog() {
        if (mAppDialog != null) {
            if (mAppDialog.isShowing()) {
                mAppDialog.dismiss();
            }
            mAppDialog = null;
        }
    }

    //对话框消失事件
    @Override
    public void onDismiss(DialogInterface dialog) {
        closeDialog();
    }

    //对话框确认事件,进行更新
    @Override
    public void onClick(boolean isConfirm) {

    }

    private String getStr(int resId) {
        return mActivity.getResources().getString(resId);
    }

    @Override
    public void onForceCancelUpdate(boolean forceCancel) {
        mListener.finishedDialog(forceCancel);
    }


}
