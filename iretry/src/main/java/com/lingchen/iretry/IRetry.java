package com.lingchen.iretry;


import com.lingchen.iretry.result.IRetryResult;
import com.lingchen.iretry.result.TimeOutIRetryResult;
import com.lingchen.iretry.transformer.DefFlowableTransformer;
import com.lingchen.iretry.transformer.DefObservableTransformer;
import com.lingchen.iretry.transformer.DefSingleTransformer;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/19
 * Function  重试
 * 封装类的构建
 */

public class IRetry {

    public static void enableDebug(boolean debug) {
        IRetryLog.debug = debug;
    }


    public static  <T> Flowable<T> with(IRetry.WorkFlowable<T> workFlowable,IIRetryManager<T>... iRetryManagers) {
        return Flowable.defer(workFlowable::create)
                .map(makeCheckedFunction(iRetryManagers))//检查是否成功
                .compose(IRetry.makeFlowableTransformer(iRetryManagers));//添加依附效果
    }

    public static <T> Single<T> with(IRetry.WorkSingle<T> workSingle,IIRetryManager<T>... iRetryManagers) {
        return Single.defer(workSingle::create)
                .map(makeCheckedFunction(iRetryManagers))//检查是否成功
                .compose(IRetry.makeSingleTransformer(iRetryManagers));//添加依附效果
    }

    public static <T> Observable<T> with(IRetry.WorkObservable<T> workPrepare,IIRetryManager<T>... iRetryManagers) {
        return Observable.defer(workPrepare::create)
                .map(makeCheckedFunction(iRetryManagers))//检查是否成功
                .compose(IRetry.makeObservableTransformer(iRetryManagers));//添加依附效果
    }


    /**
     * 创建超时结果回调
     *
     * @param outTime 超时时间  ms
     */
    public static <T> IRetryResult<T> makeIRetryResult(long outTime) {
        return new TimeOutIRetryResult<>(outTime);
    }

    /**
     * 创建 FlowableTransformer
     *
     */
    public static <T> DefFlowableTransformer<T> makeFlowableTransformer(IIRetryManager<T>... iRetryManager) {
        return DefFlowableTransformer.create(iRetryManager);
    }

    /**
     * 创建 ObservableTransformer
     */
    public static <T> DefObservableTransformer<T> makeObservableTransformer(IIRetryManager<T>... iRetryManager) {
        return DefObservableTransformer.create(iRetryManager);
    }

    /**
     * 创建 SingleTransformer
     *
     */
    public static <T> DefSingleTransformer<T> makeSingleTransformer(IIRetryManager<T>... iRetryManager) {
        return DefSingleTransformer.create(iRetryManager);
    }

    /**
     * 创建 检查机制
     *
     */
    public static <T> CheckedFunction<T> makeCheckedFunction(IIRetryManager<T>... iRetryManager) {
        return new CheckedFunction<T>(iRetryManager);
    }

    /**
     * 创建 检查机制
     *
     */
    public static <T> CheckedFunction<T> makeCheckedFunction(List<IIRetryManager<T>> iRetryManager) {
        return new CheckedFunction<T>(iRetryManager);
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
     * 根据数据检查数据异常
     */
    public static class CheckedFunction<T> implements Function<T,T>{
        private List<IIRetryManager<T>> iRetryManagers;

        public CheckedFunction(IIRetryManager<T>... iRetryManagers) {
            this(Arrays.asList(iRetryManagers));
        }

        public CheckedFunction(List<IIRetryManager<T>> iRetryManagers) {
            this.iRetryManagers = iRetryManagers;
        }

        @Override
        public T apply(T t) throws Exception {
            for (IIRetryManager<T> iiRetryManager:iRetryManagers) {
                iiRetryManager.checked(t);
            }
            return t;
        }
    }

}
