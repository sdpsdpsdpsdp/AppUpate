package com.laisontech.appupdatelibrary;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.format.Formatter;

import com.laisontech.appupdatelibrary.constants.Constants;
import com.laisontech.appupdatelibrary.interfaces.DownloadTaskListenerImpl;
import com.laisontech.appupdatelibrary.interfaces.OnShowDownloadProgressListener;
import com.laisontech.appupdatelibrary.okhttp.model.Progress;
import com.laisontech.appupdatelibrary.server.OkHttpDownload;
import com.laisontech.appupdatelibrary.utils.PackageUtils;

import java.io.Serializable;

import static com.laisontech.appupdatelibrary.okhttp.model.Progress.TAG;

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
 */

public class UpdateDownloadProgressService extends Service implements OnShowDownloadProgressListener {
    private static final String ACTION_START_UPDATE_NOTIFY = "com.laisontech.start_update";
    private static final String ACTION_STOP_UPDATE_NOTIFY = "com.laisontech.stop_update";
    //定义notification实用的ID
    private static final int MSG_NOTIFY_DOWNLOAD = 0x110;
    private NotificationCompat.Builder mNotifyBuilder;
    private NotificationManager mNotificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        //初始化notification
        mNotifyBuilder = new NotificationCompat.Builder(this);
        mNotifyBuilder.setSmallIcon(R.drawable.logo);
        mNotifyBuilder.setContentTitle(PackageUtils.getAppName(this));
        mNotifyBuilder.setContentText(getResources().getString(R.string.Downloading));
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MSG_NOTIFY_DOWNLOAD, mNotifyBuilder.build());
        mNotifyBuilder.setProgress(100, 0, false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && !action.isEmpty()) {
                switch (action) {
                    case ACTION_START_UPDATE_NOTIFY:
                        Serializable serializable = intent.getSerializableExtra(Constants.KEY_APP_INFO);
                        AppUpdateInfo appUpdateInfo = (AppUpdateInfo) serializable;
                        startUpdate(appUpdateInfo);
                        break;
                }
            }
        }
        return START_STICKY;
    }

    //注册下载监听事件
    private void startUpdate(AppUpdateInfo appUpdateInfo) {
        if (appUpdateInfo == null) return;
        OkHttpDownload.getInstance().getTask(appUpdateInfo.getApkdownloadlink()).register(new DownloadTaskListenerImpl(TAG, this));
    }

    //获取进度更新进度条
    @Override
    public void onShowProgress(Progress progress) {
        String currentSize = Formatter.formatFileSize(this, progress.currentSize);
        String totalSize = Formatter.formatFileSize(this, progress.totalSize);
        String speed = Formatter.formatFileSize(this, progress.speed);
        int currentProgress = (int) (progress.fraction * 100);
        mNotifyBuilder.setProgress(100, currentProgress, false);
        String sb = (currentSize + "/" + totalSize + "    ") + String.format("%s/s", speed);
        mNotifyBuilder.setContentText(sb);
        mNotificationManager.notify(MSG_NOTIFY_DOWNLOAD, mNotifyBuilder.build());
        if (currentProgress == 100) {  //取消显示
            closeService();
            mNotificationManager.cancel(MSG_NOTIFY_DOWNLOAD);
            PackageUtils.install(this, progress.filePath);
        }
    }

    public static void openNotify(Context context, AppUpdateInfo appUpdateInfo) {
        Intent intent = new Intent(context, UpdateDownloadProgressService.class);
        intent.setAction(ACTION_START_UPDATE_NOTIFY);
        intent.putExtra(Constants.KEY_APP_INFO, appUpdateInfo);
        context.startService(intent);
    }

    public static void closeNotify(Context context) {
        Intent intent = new Intent(context, UpdateDownloadProgressService.class);
        intent.setAction(ACTION_STOP_UPDATE_NOTIFY);
        context.stopService(intent);
    }

    private void closeService() {
        this.stopSelf();
    }
}
