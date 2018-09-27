package com.lingchen.iretry;

import io.reactivex.subjects.Subject;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/18
 * Function  管理类
 */

public interface IIRetryManager<T> {
    /**
     * 根据源请求返回的结果来进行检查
     * 抛出异常之后 将会触发重试逻辑
     *
     * @param data 数据
     * @throws Exception 直接抛出异常
     */
    void checked(T data) throws Exception;

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
     * 获取Subject 核心东西
     * 根据发射的数据 可以判断是否重试触发
     */
    Subject<T> getSubject();

    /**
     * 根据 触发请求返回的结果来决定 是否触发重试
     *
     * @param result 结果
     * @throws Throwable 如果抛出异常 不会再一次触发源请求重新订阅
     */
    void checkedResultSuccess(T result) throws Throwable;

}
