package com.lingchen.iretry;


import com.lingchen.iretry.result.IRetryResult;
import com.lingchen.iretry.result.TimeOutIRetryResult;
import com.lingchen.iretry.transformer.DefFlowableTransformer;
import com.lingchen.iretry.transformer.DefObservableTransformer;
import com.lingchen.iretry.transformer.DefSingleTransformer;

import io.reactivex.subjects.Subject;

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
     * @param tiTransformer Transformer监听流程
     * @param subject       发送者
     */
    public static <T> DefFlowableTransformer<T> makeFlowableTransformer(ITransformerListener<T> tiTransformer, Subject<T> subject) {
        return DefFlowableTransformer.create(tiTransformer, subject);
    }

    /**
     * 创建 ObservableTransformer
     *
     * @param tiTransformer Transformer监听流程
     * @param subject       发送者
     */
    public static <T> DefObservableTransformer<T> makeObservableTransformer(ITransformerListener<T> tiTransformer, Subject<T> subject) {
        return DefObservableTransformer.create(tiTransformer, subject);
    }

    /**
     * 创建 SingleTransformer
     *
     * @param tiTransformer Transformer监听流程
     * @param subject       发送者
     */
    public static <T> DefSingleTransformer<T> makeSingleTransformer(ITransformerListener<T> tiTransformer, Subject<T> subject) {
        return DefSingleTransformer.create(tiTransformer, subject);
    }


}
