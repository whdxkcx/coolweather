package com.coolweather.app.coolweather.util;

/**
 * Created by root on 2017/8/24.
 */

public interface HttpCallBackListener {
    void onFinish(String response);

    void onError(Exception e);
}
