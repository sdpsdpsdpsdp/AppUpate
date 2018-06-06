package com.laisontech.appupdatelibrary;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.laisontech.appupdatelibrary.interfaces.DownloadTaskListenerImpl;
import com.laisontech.appupdatelibrary.interfaces.OnProgressStateChangeListener;
import com.laisontech.appupdatelibrary.interfaces.OnShowDownloadProgressListener;
import com.laisontech.appupdatelibrary.okhttp.model.Progress;
import com.laisontech.appupdatelibrary.server.OkHttpDownload;
import com.laisontech.appupdatelibrary.server.download.DownloadTask;
import com.laisontech.appupdatelibrary.utils.CalculateUtils;
import com.laisontech.appupdatelibrary.utils.DisplayUtil;
import com.laisontech.appupdatelibrary.utils.DownloadUtils;


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
 * App更新的dialog
 */

public class UpdateAppDialog extends Dialog implements View.OnClickListener, OnProgressStateChangeListener, OnShowDownloadProgressListener {

    private TextView tvTitle;
    private TextView tvAppVersionName;
    private TextView tvAppSize;
    private TextView tvReleaseDate;
    private LinearLayout ll_updateContent;
    private TextView tvUpdates;
    private TextView tvUpdateContent;
    private Button btnCancel;
    private DownloadProgressBar downloadBar;
    private FrameLayout flClose;
    private Activity mContext;
    private OnDialogClickListener mOnClickBottomListener;
    private int mRequestWidth;
    private AppUpdateInfo mUpdateInfo;
    private boolean isForceUpdating = false;//是否可以关闭dialog
    private OkHttpDownload mHttpDownload;//任务下载器
    private OnForceCancelUpdateListener mListener;

    public UpdateAppDialog(@NonNull Activity context, AppUpdateInfo appUpdateInfo) {
        super(context, R.style.CustomDialog);
        mContext = context;
        mUpdateInfo = appUpdateInfo;
        mRequestWidth = 0;
    }

    public UpdateAppDialog(@NonNull Activity context, int requestWidth, AppUpdateInfo appUpdateInfo) {
        super(context, R.style.CustomDialog);
        mContext = context;
        mUpdateInfo = appUpdateInfo;
        mRequestWidth = DisplayUtil.px2dip(mContext, requestWidth) - 20 * 2;//先转换为dp
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_app_layout);
        if (mRequestWidth != 0) {
            Window window = getWindow();
            if (window != null) {
                window.setLayout(DisplayUtil.dip2px(mContext, mRequestWidth), LinearLayout.LayoutParams.WRAP_CONTENT);
            }
        }
        initViews();
        initData();
        initEvents();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvAppVersionName = findViewById(R.id.tv_app_versionName);
        tvAppSize = findViewById(R.id.tv_app_size);
        tvReleaseDate = findViewById(R.id.tv_app_ReleaseDate);
        ll_updateContent = findViewById(R.id.ll_app_update_content);
        tvUpdates = findViewById(R.id.tv_updates);
        tvUpdateContent = findViewById(R.id.tv_app_update_content);
        btnCancel = findViewById(R.id.btn_cancel);
        downloadBar = findViewById(R.id.download_progressBar);
        flClose = findViewById(R.id.fl_close);
    }

    private void initData() {
        isForceUpdating = mUpdateInfo.getForceupdating() == 1;//1立即更新
        mHttpDownload = OkHttpDownload.getInstance();
    }

    private void initEvents() {
        btnCancel.setOnClickListener(this);
        flClose.setOnClickListener(this);
        //注册下载监听事件
        downloadBar.setStateChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.btn_cancel) {
            if (btnCancel.getText().equals(mContext.getResources().getString(R.string.backgroundDownload))) {
                //开始service发送消息
                UpdateDownloadProgressService.openNotify(mContext, mUpdateInfo);
                mListener.onForceCancelUpdate(false);
                this.dismiss();
            } else if (btnCancel.getText().equals(mContext.getResources().getString(R.string.update_notnow))) {
                //下次再说：暂停下载，结束界面。
                boolean hasTask = mHttpDownload.hasTask(mUpdateInfo.getApkdownloadlink());
                if (hasTask) {
                    DownloadTask task = mHttpDownload.getTask(mUpdateInfo.getApkdownloadlink());
                    task.pause();
                }
                mListener.onForceCancelUpdate(false);
                this.dismiss();
            } else {
                mListener.onForceCancelUpdate(false);
                this.dismiss();
            }
        } else if (i == R.id.fl_close) {
            if (isForceUpdating) {
                mListener.onForceCancelUpdate(true);
                this.dismiss();
            }
        }
    }

    //bar的点击事件
    @Override
    public void onProgressBarStateChange(int state) {
        //操作
        DownloadUtils.setOperateDownloadBar(mContext, state, mUpdateInfo,
                new DownloadTaskListenerImpl(mUpdateInfo.getApkdownloadlink(), this));
    }

    @Override
    public void onShowProgress(Progress progress) {
        DownloadUtils.refreshUi(mContext, progress, btnCancel, downloadBar);  //刷新
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }

    private void refreshView() {
        tvTitle.setText(R.string.update_updatetitle);
        setText(tvAppVersionName, R.string.Version, mUpdateInfo.getVersionname());
        setText(tvAppSize, R.string.Size, CalculateUtils.getPointDouble(mUpdateInfo.getApkfilesize(), 2) + "Mb");
        setText(tvReleaseDate, R.string.ReleaseDate, CalculateUtils.formatDetailDate(mUpdateInfo.getUpdatedate()));
        String updateinfo = mUpdateInfo.getUpdateinfo();
        if (updateinfo == null || updateinfo.isEmpty()) {
            ll_updateContent.setVisibility(View.GONE);
        } else {
            ll_updateContent.setVisibility(View.VISIBLE);
            tvUpdates.setText(R.string.Updates);
            tvUpdateContent.setText(updateinfo);
        }
        if (isForceUpdating) {//强制更新不可点击取消
            btnCancel.setVisibility(View.GONE);
            flClose.setVisibility(View.VISIBLE);
        } else {//不是强制更新关闭按钮不显示
            flClose.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText(R.string.update_notnow);
        }
        checkDownloadState(mUpdateInfo.getApkdownloadlink());
    }

    private void checkDownloadState(String downloadUrl) {
        boolean hasTask = mHttpDownload.hasTask(downloadUrl);
        if (!hasTask) {//如果没有下载过，则显示默认信息，立即更新
            downloadBar.setState(DownloadProgressBar.STATUS_PROGRESS_BAR_START_DOWNLOAD);
        } else {//如果下载过，则根据进度显示信息
            DownloadUtils.refreshUi(mContext, mHttpDownload.getTask(downloadUrl).progress, btnCancel, downloadBar);
            mHttpDownload.getTask(downloadUrl).register(new DownloadTaskListenerImpl(downloadUrl, this));
        }
    }

    public UpdateAppDialog setOnClickBottomListener(OnDialogClickListener mOnClickBottomListener) {
        this.mOnClickBottomListener = mOnClickBottomListener;
        return this;
    }

    private void showListener(boolean isConfirm) {
        if (mOnClickBottomListener != null) {
            mOnClickBottomListener.onClick(isConfirm);
        }
        dismiss();
    }

    public UpdateAppDialog setCanCancel(boolean canCancel) {
        this.setCancelable(canCancel);
        return this;
    }


    public UpdateAppDialog setDismiss(OnDismissListener listener) {
        this.setOnDismissListener(listener);
        return this;
    }

    public UpdateAppDialog setForceCancelListener(OnForceCancelUpdateListener listener) {
        this.mListener = listener;
        return this;
    }

    public interface OnDialogClickListener {
        void onClick(boolean isConfirm);
    }

    public interface OnForceCancelUpdateListener {
        void onForceCancelUpdate(boolean forceCancel);
    }

    private void setText(TextView tv, int resBeforeId, String msg) {
        if (msg == null || TextUtils.isEmpty(msg)) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(getStr(resBeforeId, msg));
        }
    }

    private String getStr(int resId, String msg) {
        return mContext.getResources().getString(resId) + msg;
    }

}
