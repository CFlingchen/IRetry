package com.lingchen.iretrydemo;


import android.view.View;

import com.lingchen.iretry.IRetry;
import com.lingchen.iretry.IRetryLog;
import com.lingchen.iretrydemo.iretry.SimpleNetRetryManager;
import com.lingchen.iretrydemo.iretry.SimpleTokenRetryManager;

import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/19
 * Function  测试token
 */

public class TestTokenActivity extends BaseActivity {
    Random random = new Random();

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_token;
    }

    public void cancel(View view) {
        clear();
    }

    public void test(View view) {
        SimpleTokenRetryManager.token = 0;
        for (int i = 0; i < 10; i++) {
            sendPost(i);
        }
    }

    /**
     * 模仿请求
     */
    public void sendPost(int tag) {
        addDisposable(IRetry.with(() -> {
                    //这里需要注意 这里不能添加任何其他操作符 否则无效
                    //这里直接可以直接用retrfit的框架的调用方法
                    //interface.send();
                    return send(tag, SimpleTokenRetryManager.token);
                }, SimpleTokenRetryManager.newInstance())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> IRetryLog.e(String.format("请求%d 成功{%s}", tag, integer.toString())),
                        throwable -> IRetryLog.e(String.format("请求%d 失败--%s", tag, throwable.getMessage()))));
    }

    public Observable<BaseEntry> send(int tag, int token) {
        return Observable.create(emitter -> {
            IRetryLog.e(String.format("开始发送请求-->%d,获取token==%d", tag, token));
            try {
                Thread.sleep(random.nextInt(5) * 1000);
            } catch (InterruptedException e) {
            }
            BaseEntry<Integer> entry;
            if (token == 1) {//token 正确
                entry = new BaseEntry<>(10);
            } else {
                entry = new BaseEntry<>(-1, "token失效或者错误");
            }
            emitter.onNext(entry);
        });
    }

}
