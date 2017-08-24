package com.coolweather.app.coolweather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by root on 2017/8/24.
 */

public class HttpUtil {

    public  static  void sendHttpRequest(final String address,final HttpCallBackListener listener){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL(address);
                        //建立连接
                        connection = (HttpURLConnection) url.openConnection();
                        //设置访问方式，访问超时时间，读超时时间
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
                        //发送请求，返回结果
                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        if (listener != null) {
                            //回调onFinish()方法
                            listener.onFinish(response.toString());
                        }
                    } catch (Exception e) {
                        if (listener != null) {
                            //回调onError()方法
                            listener.onError(e);
                        }
                    } finally {
                        if(connection!=null){
                            connection.disconnect();
                        }
                    }
                }
            });
    }
}
