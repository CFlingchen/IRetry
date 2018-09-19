package com.lingchen.iretrydemo;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/19
 * Function  基础对象
 */

public class BaseEntry<T> {
    public static final int SUCCESS = 1;
    private int code = SUCCESS;
    private String msg;
    private T data;

    public BaseEntry() {
    }

    public BaseEntry(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public BaseEntry(T data) {
        this.data = data;
    }

    public BaseEntry(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public boolean isSuccess() {
        return code == SUCCESS;
    }

    @Override
    public String toString() {
        return "BaseEntry{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
