package com.lingchen.iretry.transformer;

import android.annotation.SuppressLint;

import com.lingchen.iretry.ITransformerListener;

import org.reactivestreams.Publisher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.subjects.Subject;


/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/18
 * Function  默认实现类
 */

public class DefFlowableTransformer<T> implements FlowableTransformer<T, T> {
    private ITransformerListener<T> tiTransformer;

    private Subject<T> subject;

    public static <T> DefFlowableTransformer<T> create(ITransformerListener<T> tiTransformer, Subject<T> subject) {
        return new DefFlowableTransformer<T>(tiTransformer, subject);
    }

    private DefFlowableTransformer(ITransformerListener<T> tiTransformer, Subject<T> subject) {
        this.tiTransformer = tiTransformer;
        this.subject = subject;
    }

    @SuppressLint("LongLogTag")
    public boolean isSend(Throwable throwable) {
        if (tiTransformer == null || subject == null) return false;
        return tiTransformer.intercept(throwable);
    }

    @Override
    public Publisher<T> apply(Flowable<T> upstream) {
        return upstream.retryWhen(new Function<Flowable<Throwable>, Publisher<T>>() {
            @Override
            public Publisher<T> apply(Flowable<Throwable> throwableFlowable) throws Exception {
                return throwableFlowable.flatMap((Function<Throwable, Publisher<T>>) (Throwable throwable) -> {
                    if (isSend(throwable)) {
                        tiTransformer.start();
                        return subject.flatMap(new Function<T, ObservableSource<T>>() {
                            @Override
                            public ObservableSource<T> apply(T t) throws Exception {
                                try {
                                    tiTransformer.checkedResultSuccess(t);
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
