package com.laisontech.appupdatelibrary.okhttp.request;

import com.laisontech.appupdatelibrary.okhttp.model.HttpMethod;
import com.laisontech.appupdatelibrary.okhttp.request.base.NoBodyRequest;

import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 描    述：Get请求的实现类，注意需要传入本类的泛型
 */
public class GetRequest<T> extends NoBodyRequest<T, GetRequest<T>> {

    public GetRequest(String url) {
        super(url);
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.GET;
    }

    @Override
    public okhttp3.Request generateRequest(RequestBody requestBody) {
        Request.Builder requestBuilder = generateRequestBuilder(requestBody);
        return requestBuilder.get().url(url).tag(tag).build();
    }
}
