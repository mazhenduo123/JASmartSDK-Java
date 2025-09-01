package com.jingansi.smart;

import com.jingansi.smart.enums.EventType;
import com.jingansi.smart.listener.JASmartServiceCallback;
import com.jingansi.smart.listener.JASmartThingServiceReply;

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
     * 属性上报
     * @param data 属性
     */
    void thingPropertyPost(String productKey, String deviceId, Map<String, Object> data);

    /**
     * 事件上报
     * @param eventType 事件类型
     * @param eventName 事件名称
     * @param data 事件报文
     */
    void thingEventPost(EventType eventType, String eventName, Map<String, Object> data);

    /**
     * 事件上报并接收回应消息
     * @param eventType 事件类型
     * @param eventName 事件名称
     * @param data 事件报文
     * @param reply 成功或失败的响应
     */
    void thingEventPost(EventType eventType, String eventName, Map<String, Object> data, JASmartThingServiceReply reply);

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
     * 平台能力调用
     * @param identity 能力名称
     * @param request 请求参数
     * @param jaSmartThingServiceReply 响应回调
     * @return
     */
    void platformServiceInvoke(String productKey, String deviceId, String identity, Map<String, Object> request,
                               JASmartThingServiceReply jaSmartThingServiceReply);

    /**
     * 平台能力调用
     * @param identity 能力名称
     * @param request 请求参数
     * @param jaSmartThingServiceReply 响应回调
     * @return
     */
    void platformServiceInvoke(String identity, Map<String, Object> request,
                       JASmartThingServiceReply jaSmartThingServiceReply);

    /**
     * 启动
     */
    void start();

    /**
     * 释放客户端
     */
    void release();
}
