package com.lingchen.iretry.result;

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
    public void clearResult() {
        result = null;
        lastTime = 0L;
    }

    @Override
    public boolean intercept() {
        return result != null && System.currentTimeMillis() - lastTime <= outTime;
    }

    @Override
    public T getResult() {
        return result;
    }
}
