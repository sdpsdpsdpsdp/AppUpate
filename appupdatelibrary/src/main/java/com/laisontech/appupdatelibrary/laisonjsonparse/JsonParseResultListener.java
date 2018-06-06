package com.laisontech.appupdatelibrary.laisonjsonparse;

import java.util.ArrayList;

/**
 * Created by SDP on 2017/6/15.
 */

public abstract class JsonParseResultListener {

    //信息对象集合
    public void onResponseListInfo(ArrayList<?> listData) {

    }

    //信息对象
    public void onResponseInfo(Object obj) {

    }

    //字符串
    public void onResponseStr(String str) {

    }
}
