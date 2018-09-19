package com.lingchen.iretry;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/18
 * Function  监听Transformer处理流程
 */

public interface ITransformerListener<T> {
    /**
     * 根据源请求抛出的异常来决定是否拦截
     *
     * @param throwable 异常
     * @return true 拦截 开始走拦截重试流程
     */
    boolean intercept(Throwable throwable);

    /**
     * 开始触发请求
     */
    void start();

    /**
     * 根据 触发请求返回的结果来决定 是否触发重试
     *
     * @param result 结果
     * @throws Throwable 如果抛出异常 不会再一次触发源请求重新订阅
     */
    void checkedResultSuccess(T result) throws Throwable;

}
