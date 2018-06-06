package com.laisontech.appupdatelibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.laisontech.appupdatelibrary.constants.Constants;
import com.laisontech.appupdatelibrary.okhttp.OkHttpServerUtils;

import java.io.File;

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

public class UpdateInitializer {
    private SharedPreferences mSp;
    @SuppressLint("StaticFieldLeak")
    private static UpdateInitializer mInstance;
    private Context mContext;

    public static UpdateInitializer getInstance(Context context) {
        if (null == mInstance) {
            synchronized (UpdateInitializer.class) {
                if (null == mInstance) {
                    mInstance = new UpdateInitializer(context);
                }
            }
        }
        return mInstance;
    }

    private UpdateInitializer(Context context) {
        mContext = context;
        mSp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
    }

    //先初始化数据
    public void init() {
        OkHttpServerUtils.getInstance().init(mContext);
    }

    public void initUpdateServerAddress(String address) {
        mSp.edit().putString(Constants.KEY_SAVE_APP_SERVER_ADDRESS, address).apply();
    }

    public String getUpdateServerAddress() {
        return mSp.getString(Constants.KEY_SAVE_APP_SERVER_ADDRESS, Constants.DEFAULT_SERVER_ADDRESS);
    }
    public static void cancelConnect() {
      OkHttpServerUtils.getInstance().cancelTag(Constants.TAG_QUERY_APP_INFO);
    }
}
