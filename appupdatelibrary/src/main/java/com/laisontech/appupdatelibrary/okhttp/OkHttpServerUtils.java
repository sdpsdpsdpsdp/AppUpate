package com.laisontech.appupdatelibrary.okhttp;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.laisontech.appupdatelibrary.okhttp.cache.CacheEntity;
import com.laisontech.appupdatelibrary.okhttp.cache.CacheMode;
import com.laisontech.appupdatelibrary.okhttp.cookie.CookieJarImpl;
import com.laisontech.appupdatelibrary.okhttp.https.HttpsUtils;
import com.laisontech.appupdatelibrary.okhttp.interceptor.HttpLoggingInterceptor;
import com.laisontech.appupdatelibrary.okhttp.model.HttpHeaders;
import com.laisontech.appupdatelibrary.okhttp.model.HttpParams;
import com.laisontech.appupdatelibrary.okhttp.request.DeleteRequest;
import com.laisontech.appupdatelibrary.okhttp.request.GetRequest;
import com.laisontech.appupdatelibrary.okhttp.request.HeadRequest;
import com.laisontech.appupdatelibrary.okhttp.request.OptionsRequest;
import com.laisontech.appupdatelibrary.okhttp.request.PatchRequest;
import com.laisontech.appupdatelibrary.okhttp.request.PostRequest;
import com.laisontech.appupdatelibrary.okhttp.request.PutRequest;
import com.laisontech.appupdatelibrary.okhttp.request.TraceRequest;
import com.laisontech.appupdatelibrary.okhttp.utils.HttpUtils;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * 描    述：网络请求的入口类
 */
public class OkHttpServerUtils {
    public static final long DEFAULT_MILLISECONDS = 60000;      //默认的超时时间
    public static long REFRESH_TIME = 300;                      //回调刷新时间（单位ms）

    private Context context;            //全局上下文
    private Handler mDelivery;              //用于在主线程执行的调度器
    private OkHttpClient okHttpClient;      //ok请求的客户端
    private HttpParams mCommonParams;       //全局公共请求参数
    private HttpHeaders mCommonHeaders;     //全局公共请求头
    private int mRetryCount;                //全局超时重试次数
    private CacheMode mCacheMode;           //全局缓存模式
    private long mCacheTime;                //全局缓存过期时间,默认永不过期

    private OkHttpServerUtils() {
        mDelivery = new Handler(Looper.getMainLooper());
        mRetryCount = 3;
        mCacheTime = CacheEntity.CACHE_NEVER_EXPIRE;
        mCacheMode = CacheMode.NO_CACHE;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkHttpServerUtils");
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
        loggingInterceptor.setColorLevel(Level.INFO);
        builder.addInterceptor(loggingInterceptor);

        builder.readTimeout(OkHttpServerUtils.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.writeTimeout(OkHttpServerUtils.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.connectTimeout(OkHttpServerUtils.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);

        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        builder.hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier);
        okHttpClient = builder.build();
    }

    public static OkHttpServerUtils getInstance() {
        return OkHttpHolder.holder;
    }

    private static class OkHttpHolder {
        private static OkHttpServerUtils holder = new OkHttpServerUtils();
    }

    /**
     * get请求
     */
    public static <T> GetRequest<T> get(String url) {
        return new GetRequest<>(url);
    }

    /**
     * post请求
     */
    public static <T> PostRequest<T> post(String url) {
        return new PostRequest<>(url);
    }

    /**
     * put请求
     */
    public static <T> PutRequest<T> put(String url) {
        return new PutRequest<>(url);
    }

    /**
     * head请求
     */
    public static <T> HeadRequest<T> head(String url) {
        return new HeadRequest<>(url);
    }

    /**
     * delete请求
     */
    public static <T> DeleteRequest<T> delete(String url) {
        return new DeleteRequest<>(url);
    }

    /**
     * options请求
     */
    public static <T> OptionsRequest<T> options(String url) {
        return new OptionsRequest<>(url);
    }

    /**
     * patch请求
     */
    public static <T> PatchRequest<T> patch(String url) {
        return new PatchRequest<>(url);
    }

    /**
     * trace请求
     */
    public static <T> TraceRequest<T> trace(String url) {
        return new TraceRequest<>(url);
    }

    /**
     * 必须在全局Application先调用，获取context上下文，否则缓存无法使用
     */
    public OkHttpServerUtils init(Context context) {
        this.context = context;
        return this;
    }

    /**
     * 认证规则，具体每个业务是否需要验证与服务端确定
     */
    private class SafeHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            //验证主机名是否匹配
            return true;
        }
    }

    /**
     * 获取全局上下文
     */
    public Context getContext() {
        HttpUtils.checkNotNull(context, "please call OkHttpServerUtils.getInstance().init() first in application!");
        return context;
    }

    public Handler getDelivery() {
        return mDelivery;
    }

    public OkHttpClient getOkHttpClient() {
        HttpUtils.checkNotNull(okHttpClient, "please call OkHttpServerUtils.getInstance().setOkHttpClient() first in application!");
        return okHttpClient;
    }

    /**
     * 必须设置
     */
    public OkHttpServerUtils setOkHttpClient(OkHttpClient okHttpClient) {
        HttpUtils.checkNotNull(okHttpClient, "okHttpClient == null");
        this.okHttpClient = okHttpClient;
        return this;
    }

    /**
     * 获取全局的cookie实例
     */
    public CookieJarImpl getCookieJar() {
        return (CookieJarImpl) okHttpClient.cookieJar();
    }

    /**
     * 超时重试次数
     */
    public OkHttpServerUtils setRetryCount(int retryCount) {
        if (retryCount < 0) throw new IllegalArgumentException("retryCount must > 0");
        mRetryCount = retryCount;
        return this;
    }

    /**
     * 超时重试次数
     */
    public int getRetryCount() {
        return mRetryCount;
    }

    /**
     * 全局的缓存模式
     */
    public OkHttpServerUtils setCacheMode(CacheMode cacheMode) {
        mCacheMode = cacheMode;
        return this;
    }

    /**
     * 获取全局的缓存模式
     */
    public CacheMode getCacheMode() {
        return mCacheMode;
    }

    /**
     * 全局的缓存过期时间
     */
    public OkHttpServerUtils setCacheTime(long cacheTime) {
        if (cacheTime <= -1) cacheTime = CacheEntity.CACHE_NEVER_EXPIRE;
        mCacheTime = cacheTime;
        return this;
    }

    /**
     * 获取全局的缓存过期时间
     */
    public long getCacheTime() {
        return mCacheTime;
    }

    /**
     * 获取全局公共请求参数
     */
    public HttpParams getCommonParams() {
        return mCommonParams;
    }

    /**
     * 添加全局公共请求参数
     */
    public OkHttpServerUtils addCommonParams(HttpParams commonParams) {
        if (mCommonParams == null) mCommonParams = new HttpParams();
        mCommonParams.put(commonParams);
        return this;
    }

    /**
     * 获取全局公共请求头
     */
    public HttpHeaders getCommonHeaders() {
        return mCommonHeaders;
    }

    /**
     * 添加全局公共请求参数
     */
    public OkHttpServerUtils addCommonHeaders(HttpHeaders commonHeaders) {
        if (mCommonHeaders == null) mCommonHeaders = new HttpHeaders();
        mCommonHeaders.put(commonHeaders);
        return this;
    }

    /**
     * 根据Tag取消请求
     */
    public void cancelTag(Object tag) {
        if (tag == null) return;
        for (Call call : getOkHttpClient().dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : getOkHttpClient().dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    /**
     * 根据Tag取消请求
     */
    public static void cancelTag(OkHttpClient client, Object tag) {
        if (client == null || tag == null) return;
        for (Call call : client.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    /**
     * 取消所有请求请求
     */
    public void cancelAll() {
        for (Call call : getOkHttpClient().dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : getOkHttpClient().dispatcher().runningCalls()) {
            call.cancel();
        }
    }

    /**
     * 取消所有请求请求
     */
    public static void cancelAll(OkHttpClient client) {
        if (client == null) return;
        for (Call call : client.dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : client.dispatcher().runningCalls()) {
            call.cancel();
        }
    }
}
