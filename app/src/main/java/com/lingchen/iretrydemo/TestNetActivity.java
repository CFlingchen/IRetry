package com.lingchen.iretrydemo;

import android.util.Log;
import android.view.View;

import com.lingchen.iretry.IRetryLog;
import com.lingchen.iretry.IRetryManager;
import com.lingchen.iretrydemo.iretry.SimpleNetRetryManager;
import com.lingchen.iretrydemo.iretry.SimpleTokenRetryManager;

import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/19
 * Function  测试网络
 */

public class TestNetActivity extends BaseActivity {
    Random random = new Random();

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_token;
    }

    public void cancel(View view) {
        clear();
    }

    public void test(View view) {
        //模拟网络没有 自动去检测
        SimpleNetRetryManager.hasNet = false;
        for (int i = 0; i < 10; i++) {
            sendPost(i);
        }
    }


    /**
     * 模仿请求
     */
    public void sendPost(int tag) {
        addDisposable(SimpleNetRetryManager.newInstance().work(new IRetryManager.WorkObservable<BaseEntry>() {
            @Override
            public Observable<BaseEntry> create() {
                //这里需要注意 这里不能添加任何其他操作符 否则无效
                //这里直接可以直接用retrfit的框架的调用方法
                //interface.send();
                return send(tag);
            }
        }).subscribeOn(Schedulers.io())//追加额外操作符
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> IRetryLog.e(String.format("请求%d 成功", tag, integer.toString())),
                        throwable -> IRetryLog.e(String.format("请求%d 失败--%s", tag, throwable.getMessage()))));
    }

    public Observable<BaseEntry> send(int tag) {
        return Observable.create(emitter -> {
            IRetryLog.e(String.format("开始发送请求-->%d", tag));
            try {
                Thread.sleep(random.nextInt(5) * 1000);
            } catch (InterruptedException e) {
            }
            if (SimpleNetRetryManager.hasNet) {//有网络
                emitter.onNext(new BaseEntry<>(10));
            } else {
                throw new Exception("网络异常");
            }

        });
    }

}
