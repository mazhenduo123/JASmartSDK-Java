package com.jingansi.smart;

import com.alibaba.fastjson.JSON;
import com.jingansi.smart.channel.MQTTChannel;
import com.jingansi.smart.common.ErrorInfo;
import com.jingansi.smart.enums.EventType;
import com.jingansi.smart.listener.JASmartServiceCallback;
import com.jingansi.smart.listener.JASmartThingServiceReply;
import com.jingansi.smart.report.CommonMessage;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 客户端默认实现
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/17 20:35
 **/
public class DefaultJASmartClient implements JASmartClient {
    String endpoint;
    String productKey;
    String deviceId;
    String deviceSecret;
    String username;
    String password;
    MQTTChannel messageChannel;
    MessageReceiveAdapter adapter;
    List<String> topics;

    public DefaultJASmartClient(String endpoint, String productKey, String deviceId, String deviceSecret,
                                String username, String password) {
        this.endpoint = endpoint;
        this.productKey = productKey;
        this.deviceId = deviceId;
        this.deviceSecret = deviceSecret;
        this.username = username;
        this.password = password;
        adapter = new MessageReceiveAdapter();
        topics = Arrays.asList(
                // 兼容 历史属性设置的topic
                "sys/" + productKey + "/" + deviceId + "/thing/properties/set",
                // 标准topic订阅
                "sys/" + productKey + "/" + deviceId + "/thing/service/+/post",
                "sys/" + productKey + "/" + deviceId + "/thing/service/property/set",
                "sys/" + productKey + "/" + deviceId + "/platform/service/+/post_reply",
                "sys/" + productKey + "/" + deviceId + "/thing/event/+/info_reply",
                "sys/" + productKey + "/" + deviceId + "/thing/event/+/warning_reply",
                "sys/" + productKey + "/" + deviceId + "/thing/event/+/error_reply"
        );
        messageChannel = new MQTTChannel(endpoint, productKey, deviceId, username, password, topics,
                adapter);
        adapter.setMessageChannel(messageChannel);
    }

    @Override
    public void thingPropertyPost(Map<String, Object> data) {
        messageChannel.send("sys/" + productKey + "/" + deviceId + "/thing/event/property/post", JSON.toJSONString(CommonMessage.builder()
                .tid(UUID.randomUUID().toString())
                .bid(UUID.randomUUID().toString())
                .version("1.0")
                .timestamp(System.currentTimeMillis())
                .method("event.property.post")
                .data(data)
                .build()));
    }

    @Override
    public void thingEventPost(EventType eventType, String eventName, Map<String, Object> data) {
        messageChannel.send("sys/" + productKey + "/" + deviceId + "/thing/event/" + eventName + "/" + eventType.getValue(),
                JSON.toJSONString(CommonMessage.builder()
                .tid(UUID.randomUUID().toString())
                .bid(UUID.randomUUID().toString())
                .version("1.0")
                .timestamp(System.currentTimeMillis())
                .method("event." + eventName + "." + eventType.getValue())
                .data(data)
                .build()));
    }

    @Override
    public void thingEventPost(EventType eventType, String eventName, Map<String, Object> data, JASmartThingServiceReply reply) {
        String tid = UUID.randomUUID().toString();
        String bid = UUID.randomUUID().toString();
        adapter.addServiceInvokeCallback(tid, reply);
        messageChannel.send("sys/" + productKey + "/" + deviceId + "/thing/event/" + eventName + "/" + eventType.getValue(),
                JSON.toJSONString(CommonMessage.builder()
                        .tid(tid)
                        .bid(bid)
                        .version("1.0")
                        .timestamp(System.currentTimeMillis())
                        .method("event." + eventName + "." + eventType.getValue())
                        .data(data)
                        .build()));
    }

    @Override
    public void setServiceHandle(String identity, JASmartServiceCallback jaSmartServiceCallback) {
        adapter.addServiceCallback("service." + identity + ".post", jaSmartServiceCallback);
    }

    @Override
    public void setPropertySetHandle(JASmartServiceCallback jaSmartServiceCallback) {
        // 兼容 历史属性设置的topic
        adapter.addServiceCallback("thing.properties.set", jaSmartServiceCallback);
        // 标准属性设置的method
        adapter.addServiceCallback("service.property.set", jaSmartServiceCallback);
    }

    @Override
    public void platformServiceInvoke(String identity, Map<String, Object> request, JASmartThingServiceReply jaSmartThingServiceReply) {
        String tid = UUID.randomUUID().toString();
        String bid = UUID.randomUUID().toString();
        adapter.addServiceInvokeCallback(tid, jaSmartThingServiceReply);
        messageChannel.send("sys/" + productKey + "/" + deviceId + "/platform/service/" + identity + "/post",
                JSON.toJSONString(CommonMessage.builder()
                .tid(tid)
                .bid(bid)
                .version("1.0")
                .timestamp(System.currentTimeMillis())
                .method("platform.service." + identity + ".post")
                .data(request)
                .build()));
    }

    @Override
    public void start() {
        messageChannel.start();
    }

    @Override
    public void release() {
        adapter.release();
        messageChannel.stop();
    }
}
