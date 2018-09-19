package com.lingchen.iretrydemo.iretry;

import com.lingchen.iretry.IRetry;
import com.lingchen.iretry.IRetryLog;
import com.lingchen.iretry.IRetryManager;

import java.util.Random;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/17
 * Function  模仿token失效拦截
 */

public class SimpleTokenRetryManager extends IRetryManager<Integer> {
    public static int token;

    private volatile static SimpleTokenRetryManager tokenManagerDef;

    public static synchronized SimpleTokenRetryManager newInstance() {
        if (tokenManagerDef == null)
            tokenManagerDef = new SimpleTokenRetryManager();
        return tokenManagerDef;
    }

    public SimpleTokenRetryManager() {
        //设置结果回调  保存成功数据超时时间
        setRetryResult(IRetry.makeIRetryResult(5000));
    }

    @Override
    protected Integer createError() {
        return -1;
    }

    @Override
    protected Integer createSuccess() {
        return 1;
    }


    @Override
    public Integer checked(Integer data) throws Exception {
        if (data == 0) throw new Exception("token过期");
        return data;
    }

    Random random = new Random();

    @Override
    public void createTokenObservableAndSend() {
        Single.create((SingleOnSubscribe<Integer>) emitter -> {
            IRetryLog.d("开始获取token");
            Thread.sleep(random.nextInt(2) * 1000);
            if (random.nextInt(2) == 1) {
                token = 0;
                IRetryLog.d("获取token失败");
                emitter.onSuccess(0);
            } else {
                token = 1;
                IRetryLog.d("获取token成功");
                emitter.onSuccess(1);
            }
        }).doOnSuccess(s -> {
            if (s == 1) {
                sendSuccess();
            } else {
                sendError(new Throwable("获取token失败了"));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public boolean intercept(Throwable throwable) {
        return throwable.getMessage().equals("token过期");
    }
}
