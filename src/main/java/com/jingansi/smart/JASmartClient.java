package com.jingansi.smart;

import com.jingansi.smart.enums.EventType;
import com.jingansi.smart.listener.JASmartServiceCallback;

import java.util.Map;

/**
 * 设备客户端
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/4 19:51
 **/
public interface JASmartClient {
    /**
     * 属性上报
     * @param data 属性
     */
    void thingPropertyPost(Map<String, Object> data);

    /**
     * 事件上报
     * @param eventType 事件类型
     * @param eventName 事件名称
     * @param data 事件报文
     */
    void thingEventPost(EventType eventType, String eventName, Map<String, Object> data);

    /**
     * 能力调用回调处理
     * @param identity 能力名称
     * @param jaSmartServiceCallback 能力处理回调
     */
    void setServiceHandle(String identity, JASmartServiceCallback jaSmartServiceCallback);

    /**
     * 属性设置处理回调
     * @param jaSmartServiceCallback
     */
    void setPropertySetHandle(JASmartServiceCallback jaSmartServiceCallback);

    /**
     * 启动
     */
    void start();

    /**
     * 释放客户端
     */
    void release();
}
