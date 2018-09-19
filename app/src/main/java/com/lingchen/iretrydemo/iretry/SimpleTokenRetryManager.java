package com.lingchen.iretrydemo.iretry;

import com.lingchen.iretry.IRetry;
import com.lingchen.iretry.IRetryLog;
import com.lingchen.iretry.IRetryManager;
import com.lingchen.iretrydemo.BaseEntry;

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

public class SimpleTokenRetryManager extends IRetryManager<BaseEntry> {
    //测试使用 正常开发应该自己保存  这里只是为了测试使用
    //外部模仿请求的时候 根据此值来判断
    //1 为成功
    public static int token;
    //随即成功
    Random random = new Random();
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

    /**
     * 创建error对象 这个只是用来标记 直接new即可
     */
    @Override
    protected BaseEntry createError() {
        return new BaseEntry();
    }

    /**
     * 创建success对象 这个只是用来标记 直接new即可
     */
    @Override
    protected BaseEntry createSuccess() {
        return new BaseEntry();
    }

    /**
     * 检查结果
     */
    @Override
    public BaseEntry checked(BaseEntry data) throws Exception {
        //这里我只是判断了code  大家可以根据实际情况来分别
        //这里只要抛出异常即可  可以自定义异常
        if (!data.isSuccess()) throw new Exception("token过期");
        return data;
    }

    /**
     * 创建请求
     */
    @Override
    public void createObservableAndSend() {
        //取消上一次监听
        clear();
        addDisposable(Single.create((SingleOnSubscribe<Integer>) emitter -> {
            IRetryLog.d("开始获取token");
            Thread.sleep(random.nextInt(2) * 1000);
            if (random.nextInt(2) == 1) {
                IRetryLog.d("获取token失败");
                emitter.onSuccess(0);
            } else {
                IRetryLog.d("获取token成功");
                emitter.onSuccess(1);
            }
        }).doOnSuccess(s -> {
            if (s == 1) {
                token = 1;
                sendSuccess();
            } else {
                token = 0;
                sendError(new Throwable("获取token失败了"));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }

    /**
     * 拦截异常
     *
     * @param throwable 异常
     * @return 将会触发重新请求
     */
    @Override
    public boolean intercept(Throwable throwable) {
        return throwable.getMessage().equals("token过期");
    }
}
