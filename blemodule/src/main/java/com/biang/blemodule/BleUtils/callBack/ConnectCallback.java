package com.biang.blemodule.BleUtils.callBack;

/**
 * 类名: ConnectCallback
 * 作者: 陈海明
 * 时间: 2017-08-18 13:53
 * 描述: 连接回调
 */
public interface ConnectCallback {
    /**
     *  获得通知之后
     */
    void onConnSuccess();

    /**
     * 断开或连接失败
     */
    void onConnFailed();
}
