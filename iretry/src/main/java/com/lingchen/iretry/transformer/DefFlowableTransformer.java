package com.lingchen.iretry.transformer;

import com.lingchen.iretry.IIRetryManager;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;


/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/18
 * Function  默认实现类
 */

public class DefFlowableTransformer<T> implements FlowableTransformer<T, T> {
    private List<IIRetryManager<T>> mRetryManagers=new ArrayList<>();
    public static <T> DefFlowableTransformer<T> create(IIRetryManager<T>... iRetryManager) {
        return new DefFlowableTransformer<T>(Arrays.asList(iRetryManager));
    }

    public static <T> DefFlowableTransformer<T> create(List<IIRetryManager<T>> mRetryManagers) {
        return new DefFlowableTransformer<T>(mRetryManagers);
    }

    private DefFlowableTransformer(List<IIRetryManager<T>> mRetryManagers) {
        this.mRetryManagers.addAll(mRetryManagers);
    }

    public IIRetryManager getIRetryManager(Throwable throwable) {
        for (IIRetryManager iiRetryManager:mRetryManagers) {
            if(iiRetryManager!=null && iiRetryManager.intercept(throwable)){
                return iiRetryManager;
            }
        }
        return null;
    }

    @Override
    public Publisher<T> apply(Flowable<T> upstream) {
        return upstream.retryWhen(new Function<Flowable<Throwable>, Publisher<T>>() {
            @Override
            public Publisher<T> apply(Flowable<Throwable> throwableFlowable) throws Exception {
                return throwableFlowable.flatMap((Function<Throwable, Publisher<T>>) (Throwable throwable) -> {
                    IIRetryManager iiRetryManager=getIRetryManager(throwable);
                    if (iiRetryManager!=null) {
                        iiRetryManager.start();
                        return iiRetryManager.getSubject().flatMap(new Function<T, ObservableSource<T>>() {
                            @Override
                            public ObservableSource<T> apply(T t) throws Exception {
                                try {
                                    iiRetryManager.checkedResultSuccess(t);
                                    return Observable.just(t);
                                } catch (Throwable throwable2) {
                                    throwable2.printStackTrace();
                                    return Observable.error(throwable2);
                                }
                            }
                        }).toFlowable(BackpressureStrategy.BUFFER);
                    } else {
                        return Flowable.error(throwable);
                    }
                });
            }
        });
    }
}
