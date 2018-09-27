package com.lingchen.iretry;


import com.lingchen.iretry.result.IRetryResult;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.ListCompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/17
 * Function  重试管理类
 * 使用者 需要继承 进行扩展
 */

public abstract class IRetryManager<T> implements IIRetryManager<T>{
    private volatile Subject<T> subject;
    /**
     * 标记唯一一次请求  避免触发多次请求
     */
    private volatile AtomicBoolean isSend;
    /**
     * 管理Rx生命周期
     */
    private ListCompositeDisposable listCompositeDisposable = new ListCompositeDisposable();
    /**
     * 结果额外监听  可以设置超时时间 避免不必要的请求
     */
    private IRetryResult<T> iRetryResult;
    private T success, error;
    private Throwable throwable;

    public IRetryManager() {
        subject = PublishSubject.create();
        isSend = new AtomicBoolean();
        success = createSuccess();
        error = createError();
    }

    @Override
    public Subject<T> getSubject() {
        return subject;
    }

    /**
     *  默认不拦截  这里可以重写 自己拦截
     * @param data 数据
     * @throws Exception
     */
    @Override
    public void checked(T data) throws Exception {

    }

    /**
     * 创建错误对象
     */
    protected abstract T createError();

    /**
     * 创建正确对象
     */
    protected abstract T createSuccess();

    /**
     * 触发刷新数据  如调用刷新token方法
     *
     * @see #sendSuccess()  触发重试
     * @see #sendError(Throwable) 将不会触发重试
     */
    public abstract Disposable createObservableAndSend();

    /**
     * 设置结果监听  可以用来记录结果 设置结果的超市时间
     */
    public void setRetryResult(IRetryResult<T> iRetryResult) {
        this.iRetryResult = iRetryResult;
    }


    @Override
    public void start() {
        IRetryLog.i("开始触发重试逻辑--开始检测");
        if (isSend.compareAndSet(false, true)) {
            IRetryResult<T> iRetryResult = this.iRetryResult;
            IRetryLog.i("开始判断是否需要发送老的数据");
            if (iRetryResult != null && iRetryResult.intercept()) {
                IRetryLog.i("距离上个结果相差很短 直接发送最后一个结果...");
                sendResult(iRetryResult.getResult(), false);
            } else {
                IRetryLog.i("开始创建请求任务,请等待...");
                clear();
               addDisposable(createObservableAndSend());
            }
        } else {
            IRetryLog.i("任务正在进行,请等待结果...");
        }
    }




    /**
     * 发送成功
     */
    protected void sendSuccess() {
        sendResult(success, true);
    }

    /**
     * 发送失败
     *
     * @param throwable 错误异常  将会直接抛出
     */
    protected void sendError(Throwable throwable) {
        this.throwable = throwable;
        sendResult(error, false);
    }

    /**
     * 发送结果
     *
     * @param t          返回值
     * @param saveResult 是否需要保存结果 如果保存 将会触发检测最后保存结果机制
     */
    private void sendResult(T t, boolean saveResult) {
        if (isSend.compareAndSet(true, false)) {
            T result = t == null ? success : t;
            IRetryResult<T> iRetryResult = this.iRetryResult;
            if (iRetryResult != null && saveResult) {
                iRetryResult.saveResult(result);
            }
            subject.onNext(result);
        }
    }

    /**
     * 这里直接检测返回的结果 如果是 error 对象 直接抛出异常
     * 当然用户可以重写
     */
    @Override
    public void checkedResultSuccess(T t) throws Throwable {
        if (t == error) {
            throw throwable;
        }
    }

    protected void addDisposable(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            listCompositeDisposable.add(disposable);
        }
    }

    protected void clear() {
        if (!listCompositeDisposable.isDisposed()) {
            listCompositeDisposable.clear();
        }
    }

}
