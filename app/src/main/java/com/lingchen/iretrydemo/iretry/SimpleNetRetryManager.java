package com.lingchen.iretrydemo.iretry;

import com.lingchen.iretry.IRetry;
import com.lingchen.iretry.IRetryLog;
import com.lingchen.iretry.IRetryManager;
import com.lingchen.iretrydemo.App;
import com.lingchen.iretrydemo.BaseEntry;
import com.lingchen.iretrydemo.rx.NetWorksFlowable;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/19
 * Function  模拟网络变化 重试
 */

public class SimpleNetRetryManager extends IRetryManager<BaseEntry> {
    public static boolean hasNet;

    private volatile static SimpleNetRetryManager netRetryManager;

    public static synchronized SimpleNetRetryManager newInstance() {
        if (netRetryManager == null)
            netRetryManager = new SimpleNetRetryManager();
        return netRetryManager;
    }

    public SimpleNetRetryManager() {
        //网络适当调低点 或者 大家可以直接自己监听网络变化  去判断固定的值 或者每次都去检测网络是否有
        setRetryResult(IRetry.makeIRetryResult(1000));
    }

    @Override
    public BaseEntry checked(BaseEntry data) throws Exception {
        //因为网络层的错误 基本上都是直接抛出异常 所以这里直接返回数据
        return data;
    }


    @Override
    public boolean intercept(Throwable throwable) {
        return throwable.getMessage().equals("网络异常");
    }

    @Override
    protected BaseEntry createError() {
        return new BaseEntry();
    }

    @Override
    protected BaseEntry createSuccess() {
        return new BaseEntry();
    }


    @Override
    public void createTokenObservableAndSend() {
        clear();
        addDisposable(NetWorksFlowable.single(App.getApp())
                .filter(aBoolean -> aBoolean)
                .doOnNext(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        hasNet = aBoolean;
                        if (aBoolean) {
                            IRetryLog.d("有网了");
                            sendSuccess();
                        } else {
                            IRetryLog.d("断网了");
                            sendError(new Throwable("断网了"));
                        }
                    }
                })
                .onErrorResumeNext(Flowable.empty())
                .subscribe());
    }
}
