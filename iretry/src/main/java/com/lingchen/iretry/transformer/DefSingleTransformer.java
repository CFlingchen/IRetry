package com.lingchen.iretry.transformer;

import com.lingchen.iretry.IIRetryManager;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.functions.Function;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/18
 * Function  默认实现类
 */

public class DefSingleTransformer<T> implements SingleTransformer<T, T> {
    private List<IIRetryManager<T>> mRetryManagers=new ArrayList<>();
    public static <T> DefSingleTransformer<T> create(IIRetryManager<T>... iRetryManager) {
        return new DefSingleTransformer<T>(Arrays.asList(iRetryManager));
    }

    public static <T> DefSingleTransformer<T> create(List<IIRetryManager<T>> mRetryManagers) {
        return new DefSingleTransformer<T>(mRetryManagers);
    }

    private DefSingleTransformer(List<IIRetryManager<T>> mRetryManagers) {
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
    public SingleSource<T> apply(Single<T> upstream) {
        return upstream.retryWhen(new Function<Flowable<Throwable>, Publisher<T>>() {
            @Override
            public Publisher<T> apply(Flowable<Throwable> throwableFlowable) throws Exception {
                return throwableFlowable.flatMap(new Function<Throwable, Publisher<T>>() {
                    @Override
                    public Publisher<T> apply(Throwable throwable) throws Exception {
                        IIRetryManager iiRetryManager=getIRetryManager(throwable);
                        if (iiRetryManager!=null) {
                            iiRetryManager.start();
                            return iiRetryManager.getSubject().flatMap((Function<T, ObservableSource<T>>) t -> {
                                try {
                                    iiRetryManager.checkedResultSuccess(t);
                                    return Observable.just(t);
                                } catch (Throwable throwable2) {
                                    throwable2.printStackTrace();
                                    return Observable.error(throwable2);
                                }
                            }).toFlowable(BackpressureStrategy.BUFFER);
                        } else {
                            return Flowable.error(throwable);
                        }
                    }
                });
            }
        });

    }

}
