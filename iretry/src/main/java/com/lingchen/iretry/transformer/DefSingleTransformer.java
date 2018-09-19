package com.lingchen.iretry.transformer;

import android.annotation.SuppressLint;


import com.lingchen.iretry.ITransformerListener;

import org.reactivestreams.Publisher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.Subject;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/18
 * Function  默认实现类
 */

public class DefSingleTransformer<T> implements SingleTransformer<T, T> {
    private ITransformerListener<T> tiTransformer;

    private Subject<T> subject;

    public static <T> DefSingleTransformer<T> create(ITransformerListener<T> tiTransformer, Subject<T> subject) {
        return new DefSingleTransformer<T>(tiTransformer, subject);
    }

    private DefSingleTransformer(ITransformerListener<T> tiTransformer, Subject<T> subject) {
        this.tiTransformer = tiTransformer;
        this.subject = subject;
    }

    @SuppressLint("LongLogTag")
    public boolean isSend(Throwable throwable) {
        if (tiTransformer == null || subject == null) return false;
        return tiTransformer.intercept(throwable);
    }

    @Override
    public SingleSource<T> apply(Single<T> upstream) {
        return upstream.retryWhen(new Function<Flowable<Throwable>, Publisher<T>>() {
            @Override
            public Publisher<T> apply(Flowable<Throwable> throwableFlowable) throws Exception {
                return throwableFlowable.flatMap(new Function<Throwable, Publisher<T>>() {
                    @Override
                    public Publisher<T> apply(Throwable throwable) throws Exception {
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
