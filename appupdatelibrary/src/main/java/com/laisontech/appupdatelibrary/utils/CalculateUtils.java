package com.laisontech.appupdatelibrary.utils;

import android.content.Context;

import com.laisontech.appupdatelibrary.AppUpdateInfo;

import java.math.RoundingMode;
import java.text.DecimalFormat;

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

public class CalculateUtils {

    //将double转为为几位小数
    public static String getPointDouble(double val, int decimalPlace) {
        if (decimalPlace < 1) return String.valueOf(val);
        StringBuilder sb = new StringBuilder();
        sb.append("0.");
        for (int i = 0; i < decimalPlace; i++) {
            sb.append("0");
        }
        DecimalFormat df = new DecimalFormat(sb.toString());
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(val);
    }

    public static String formatDetailDate(String date) {
        if (date == null || date.isEmpty() || date.length() != 14) {
            return "";
        }
        return new StringBuffer()
                .append(date.substring(4, 6))
                .append("/")
                .append(date.substring(6, 8))
                .append("/")
                .append(date.substring(0, 4))
                .append("  ")
                .append(date.substring(8, 10))
                .append(":")
                .append(date.substring(10, 12))
                .append(":")
                .append(date.substring(12, 14))
                .toString();
    }

    /**
     * 根据服务端返回信息判断是否需要更新app
     */
    public static boolean checkNeedUpdate(Context context, AppUpdateInfo appUpdateInfo) {
        if (appUpdateInfo == null) return false;
        String serverProjectName = appUpdateInfo.getProjectname();
        String appPackageName = PackageUtils.getAppPackageName(context);
        if (!appPackageName.equals(serverProjectName)) return false;//不是同一个包名，不能更新
        int serverVersion = appUpdateInfo.getVersioncode();
        int appVersionCode = PackageUtils.getAppVersionCode(context);
        return serverVersion > appVersionCode;
    }

}
