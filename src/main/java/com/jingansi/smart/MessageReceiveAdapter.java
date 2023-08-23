package com.jingansi.smart;

import com.alibaba.fastjson.JSON;
import com.jingansi.smart.channel.MessageChannel;
import com.jingansi.smart.common.ErrorInfo;
import com.jingansi.smart.listener.JASmartServiceCallback;
import com.jingansi.smart.listener.JASmartThingServiceReply;
import com.jingansi.smart.report.CommonMessage;
import com.jingansi.smart.report.ReplyMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息解析适配
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/17 21:36
 **/
@Slf4j
public class MessageReceiveAdapter implements MessageChannel.MessageReceiveCallback {
    Map<String, JASmartServiceCallback> services = new HashMap<>();
    MessageChannel messageChannel;

    public void setMessageChannel(MessageChannel messageChannel) {
        this.messageChannel = messageChannel;
    }

    public void addServiceCallback(String method, JASmartServiceCallback jaSmartServiceCallback) {
        services.put(method, jaSmartServiceCallback);
    }

    @Override
    public void receive(String topic, String data) {
        CommonMessage commonMessage;
        try {
            commonMessage = JSON.parseObject(data, CommonMessage.class);
        } catch (Exception e) {
            log.error("Not a invalid JSON message. {}", data, e);
            return;
        }

        JASmartServiceCallback callback = services.get(commonMessage.getMethod());
        if (null != callback) {
            callback.process(commonMessage.getTid(), commonMessage.getData(), new JASmartThingServiceReply() {
                @Override
                public void complete(Map<String, Object> response) {
                    messageChannel.send(topic + "_reply", JSON.toJSONString(ReplyMessage.builder()
                            .code("OK")
                            .message("SUCCESS")
                            .tid(commonMessage.getTid())
                            .bid(commonMessage.getBid())
                            .version(commonMessage.getVersion())
                            .timestamp(System.currentTimeMillis())
                            .method(commonMessage.getMethod() + ".reply")
                            .data(response)
                            .build()));
                }

                @Override
                public void error(ErrorInfo errorInfo) {
                    messageChannel.send(topic + "_reply", JSON.toJSONString(ReplyMessage.builder()
                            .code("ERROR")
                            .message(errorInfo.getMessage())
                            .tid(commonMessage.getTid())
                            .bid(commonMessage.getBid())
                            .version(commonMessage.getVersion())
                            .timestamp(System.currentTimeMillis())
                            .method(commonMessage.getMethod() + ".reply")
                            .build()));
                }
            });
        } else {
            messageChannel.send(topic + "_reply", JSON.toJSONString(ReplyMessage.builder()
                    .code("OK")
                    .message("The terminal did not implement this capability")
                    .tid(commonMessage.getTid())
                    .bid(commonMessage.getBid())
                    .version(commonMessage.getVersion())
                    .timestamp(System.currentTimeMillis())
                    .method(commonMessage.getMethod() + ".reply")
                    .build()));
        }
    }
}
