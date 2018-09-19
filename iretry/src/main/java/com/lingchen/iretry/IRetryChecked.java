package com.lingchen.iretry;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/18
 * Function  检查源数据是否满足条件
 */

public interface IRetryChecked<T> {
    /**
     * 根据源请求返回的结果来进行检查
     * 抛出异常之后 将会触发重试逻辑
     *
     * @param data 数据
     * @return 返回数据
     * @throws Exception 直接抛出异常
     *
     * @see ITransformerListener
     */
    T checked(T data) throws Exception;

}
