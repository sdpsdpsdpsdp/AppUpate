package com.laisontech.appupdatelibrary.laisonjsonparse;

/**
 * Created by admin on 2017/6/12.
 */
public class HttpPostJson {
    private String operatetype;
    private Object data;

    public HttpPostJson(String operatetype, Object obj) {
        this.operatetype = operatetype;
        this.data = obj;
    }

    public String getOperatetype() {
        return operatetype;
    }

    public void setOperatetype(String operatetype) {
        this.operatetype = operatetype;
    }

    public Object getDataBean() {
        return data;
    }

    public void setDataBean(Object object) {
        this.data = object;
    }

}
