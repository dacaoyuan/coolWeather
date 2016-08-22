package com.coolweather.app.util;

/**
 * Created by abc on 2016/8/22.
 */
public interface HttpCallBackListener {
    void onFinish(String response);
    void onError(Exception e);
}
