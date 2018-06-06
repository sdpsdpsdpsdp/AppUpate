package com.laisontech.appupdatelibrary;

import android.os.AsyncTask;

import com.laisontech.appupdatelibrary.constants.Constants;
import com.laisontech.appupdatelibrary.interfaces.QueryAppInfoListener;
import com.laisontech.appupdatelibrary.laisonjsonparse.JsonResultParseUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
 * Created by SDP on 2018/6/6.
 */

public class QueryAppInfoTask extends AsyncTask<String, Void, AppUpdateInfo> {
    private QueryAppInfoListener<AppUpdateInfo> listener;

    public QueryAppInfoTask(QueryAppInfoListener<AppUpdateInfo> listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        listener.startQuery();
    }

    @Override
    protected AppUpdateInfo doInBackground(String... strings) {
        String serverAddress = strings[0];
        String appPackageName = strings[1];
        String queryAppUrl = serverAddress
                + "?function="
                + Constants.FUNC_NAME
                + "&"
                + Constants.APP_PRO_NAME
                + "="
                + appPackageName;
        HttpURLConnection conn = null;
        BufferedReader rd = null;
        StringBuilder sb = new StringBuilder();
        String line;
        String response = null;
        try {
            conn = (HttpURLConnection) new URL(queryAppUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setReadTimeout(20000);
            conn.setConnectTimeout(20000);
            conn.setUseCaches(false);
            conn.connect();
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            response = sb.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rd != null) {
                    rd.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (response == null) {
            return null;
        }
        //解析
        JsonResultParseUtils.ParseResultBean result = JsonResultParseUtils.getResult(response, AppUpdateInfo.class, JsonResultParseUtils.ResultType.OBJECT);
        int errorCode = result.getErrorCode();
        if (errorCode != 0) {
            return null;
        }
        return (AppUpdateInfo) result.getParseResult();
    }

    @Override
    protected void onPostExecute(AppUpdateInfo appUpdateInfo) {
        listener.queryInfo(appUpdateInfo);
    }
}
