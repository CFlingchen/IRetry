package com.lingchen.iretry.result;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/19
 * Function  结果额外处理
 */

public interface IRetryResult<T> {
    /**
     * 保存结果
     *
     * @param t
     */
    void saveResult(T t);

    /**
     * 清理结果
     */
    void clearResult();

    /**
     * 是否拦截
     *
     * @return true 如果拦截 将会自动发送结果
     * @see #getResult()
     */
    boolean intercept();

    /**
     * 获取结果
     *
     * @return
     */
    T getResult();
}
