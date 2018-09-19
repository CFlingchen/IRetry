package com.lingchen.iretry.transformer;

import android.annotation.SuppressLint;


import com.lingchen.iretry.ITransformerListener;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.Subject;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/18
 * Function  默认实现类
 */

public class DefObservableTransformer<T> implements ObservableTransformer<T, T> {
    private ITransformerListener<T> tiTransformer;

    private Subject<T> subject;

    public static <T> DefObservableTransformer<T> create(ITransformerListener<T> tiTransformer, Subject<T> subject) {
        return new DefObservableTransformer<T>(tiTransformer, subject);
    }

    private DefObservableTransformer(ITransformerListener<T> tiTransformer, Subject<T> subject) {
        this.tiTransformer = tiTransformer;
        this.subject = subject;
    }

    @SuppressLint("LongLogTag")
    public boolean isSend(Throwable throwable) {
        if (tiTransformer == null || subject == null) return false;
        return tiTransformer.intercept(throwable);
    }

    @Override
    public ObservableSource<T> apply(Observable<T> upstream) {
        return upstream.retryWhen(throwableObservable -> throwableObservable.flatMap((Function<Throwable, ObservableSource<T>>) throwable -> {
            if (isSend(throwable)) {
                tiTransformer.start();
                return subject.flatMap((Function<T, ObservableSource<T>>) t -> {
                    try {
                        tiTransformer.checkedResultSuccess(t);
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
