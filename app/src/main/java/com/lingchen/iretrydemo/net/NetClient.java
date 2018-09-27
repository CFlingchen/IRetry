package com.lingchen.iretrydemo.net;


import android.util.Log;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2016/11/4
 * Function  网络客户端
 */

public class NetClient {
    public static final String URL="";
    private static final String TAG = "NetClient";
    private static volatile NetClient netClient;
    private Retrofit retrofit;

    private static NetClient getDefault(){
        if(netClient==null){
            synchronized (NetClient.class){
                if(netClient==null){
                    netClient=new NetClient();
                }
            }
        }
        return netClient;
    }


    public static <T> T createService(final Class<T> service){
        return getDefault().create(service);
    }

    public NetClient() {
        retrofit=createRetrofit(URL);
    }


    public <T> T create(final Class<T> service){
        return retrofit.create(service);
    }

    /**
     * 创建 Retrofit
     */
    public Retrofit createRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .client(getClient().build())
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
    }


    /**
     * 获取 OkHttpClient
     *
     * @return OkHttpClient
     */
    public  OkHttpClient.Builder getClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .proxy(Proxy.NO_PROXY)
                .addInterceptor(new HttpLoggingInterceptor(message -> Log.e(TAG, message))
                        .setLevel(HttpLoggingInterceptor.Level.BODY));//日志
    }

}
