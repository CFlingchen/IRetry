package com.lingchen.iretrydemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.lingchen.iretry.IRetry;
import com.lingchen.iretry.IRetryLog;
import com.lingchen.iretrydemo.iretry.SimpleTokenRetryManager;

import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IRetry.enableDebug(true);
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
        SimpleTokenRetryManager.newInstance().work(() -> send(tag, SimpleTokenRetryManager.token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> IRetryLog.e(String.format("请求%d 成功", tag)),
                        throwable -> IRetryLog.e(String.format("请求%d 失败--%s", tag, throwable.getMessage())));
    }

    public Observable<Integer> send(int tag, int token) {
        return Observable.create(emitter -> {
            IRetryLog.e(String.format("开始发送请求-->%d,获取token==%d", tag, token));
            Thread.sleep(random.nextInt(5) * 1000);
            IRetryLog.e(String.format("发送请求-->%d 返回", tag));
            emitter.onNext(token);
        });
    }
}
