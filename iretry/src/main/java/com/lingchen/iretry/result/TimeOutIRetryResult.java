package com.lingchen.iretry.result;

import com.lingchen.iretry.IRetryLog;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/19
 * Function  超时管理
 */

public class TimeOutIRetryResult<T> implements IRetryResult<T> {
    private long outTime = 3000;
    private long lastTime;
    private T result;

    public TimeOutIRetryResult() {
    }

    public TimeOutIRetryResult(long outTime) {
        this.outTime = outTime;
    }

    @Override
    public void saveResult(T t) {
        result = t;
        lastTime = System.currentTimeMillis();
    }

    @Override
    public boolean intercept() {
        boolean time = System.currentTimeMillis() - lastTime <= outTime;
        IRetryLog.i(result + "--" + System.currentTimeMillis() + "--" + lastTime + "--" + time);
        return result != null && time;
    }

    @Override
    public T getResult() {
        return result;
    }
}
