package com.laisontech.appupdatelibrary.okhttp.request;

import com.laisontech.appupdatelibrary.okhttp.model.HttpMethod;
import com.laisontech.appupdatelibrary.okhttp.request.base.BodyRequest;

import okhttp3.Request;
import okhttp3.RequestBody;

public class PutRequest<T> extends BodyRequest<T, PutRequest<T>> {

    public PutRequest(String url) {
        super(url);
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.PUT;
    }

    @Override
    public Request generateRequest(RequestBody requestBody) {
        Request.Builder requestBuilder = generateRequestBuilder(requestBody);
        return requestBuilder.put(requestBody).url(url).tag(tag).build();
    }
}
