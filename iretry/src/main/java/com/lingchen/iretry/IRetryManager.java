package com.lingchen.iretry;


import com.lingchen.iretry.result.IRetryResult;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
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

public abstract class IRetryManager<T> implements IRetryChecked<T>, ITransformerListener<T> {
    private volatile Subject<T> subject;

    private volatile AtomicBoolean isSend;
    private IRetryResult<T> iRetryResult;
    private T success, error;
    private Throwable throwable;

    public IRetryManager() {
        subject = PublishSubject.create();
        isSend = new AtomicBoolean();
        success = createSuccess();
        error = createError();
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
    public abstract void createObservableAndSend();

    /**
     * 设置结果监听  可以用来记录结果 设置结果的超市时间
     */
    public void setRetryResult(IRetryResult<T> iRetryResult) {
        this.iRetryResult = iRetryResult;
    }

    public Flowable<T> work(WorkFlowable<T> workFlowable) {
        return Flowable.defer(workFlowable::create)
                .map(this::checked)//检查是否成功
                .compose(IRetry.makeFlowableTransformer(this, subject));//添加依附效果
    }


    public Single<T> work(WorkSingle<T> workSingle) {
        return Single.defer(workSingle::create)
                .map(this::checked)//检查是否成功
                .compose(IRetry.makeSingleTransformer(this, subject));//添加依附效果
    }

    public Observable<T> work(WorkObservable<T> workPrepare) {
        return Observable.defer(workPrepare::create)
                .map(this::checked)//检查是否成功
                .compose(IRetry.makeObservableTransformer(this, subject));//添加依附效果
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
                IRetryLog.i("开始创建获取token请求任务");
                createObservableAndSend();
            }
        } else {
            IRetryLog.i("任务正在进行...");
        }
    }


    public interface WorkObservable<T> {
        Observable<T> create();
    }

    public interface WorkSingle<T> {
        Single<T> create();
    }

    public interface WorkFlowable<T> {
        Flowable<T> create();
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


    @Override
    public void checkedResultSuccess(T t) throws Throwable {
        if (t == error) {
            throw throwable;
        }
    }

    /**
     * 管理Rx生命周期
     */
    private ListCompositeDisposable listCompositeDisposable = new ListCompositeDisposable();

    protected void addDisposable(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            listCompositeDisposable.add(disposable);
        }
    }

    protected void reDisposable(Disposable disposable) {
        if (disposable != null) {
            listCompositeDisposable.remove(disposable);
        }
    }

    protected void clear() {
        if (!listCompositeDisposable.isDisposed()) {
            listCompositeDisposable.clear();
        }
    }

}
