package com.lingchen.iretrydemo;

import android.view.View;

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
        addDisposable(SimpleNetRetryManager.newInstance().work(() -> send(tag))
                .subscribeOn(Schedulers.io())
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
