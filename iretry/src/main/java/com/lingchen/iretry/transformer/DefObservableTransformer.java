package com.lingchen.iretry.transformer;

import com.lingchen.iretry.IIRetryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/18
 * Function  默认实现类
 */

public class DefObservableTransformer<T> implements ObservableTransformer<T, T> {
    private List<IIRetryManager<T>> mRetryManagers=new ArrayList<>();
    public static <T> DefObservableTransformer<T> create(IIRetryManager<T>... iRetryManager) {
        return new DefObservableTransformer<T>(Arrays.asList(iRetryManager));
    }

    public static <T> DefObservableTransformer<T> create(List<IIRetryManager<T>> mRetryManagers) {
        return new DefObservableTransformer<T>(mRetryManagers);
    }

    private DefObservableTransformer(List<IIRetryManager<T>> mRetryManagers) {
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
    public ObservableSource<T> apply(Observable<T> upstream) {
        return upstream.retryWhen(throwableObservable -> throwableObservable.flatMap((Function<Throwable, ObservableSource<T>>) throwable -> {
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
                });
            } else {
                return Observable.error(throwable);
            }

        }));
    }


}
