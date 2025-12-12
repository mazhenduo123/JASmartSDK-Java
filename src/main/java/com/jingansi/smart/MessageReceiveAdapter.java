package com.jingansi.smart;

import com.alibaba.fastjson.JSON;
import com.jingansi.smart.channel.MessageChannel;
import com.jingansi.smart.common.ErrorInfo;
import com.jingansi.smart.listener.JASmartServiceCallback;
import com.jingansi.smart.listener.JASmartThingServiceReply;
import com.jingansi.smart.report.CommonMessage;
import com.jingansi.smart.report.ReplyMessage;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 消息解析适配
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/17 21:36
 **/
@Slf4j
public class MessageReceiveAdapter implements MessageChannel.MessageReceiveCallback {
    Map<String, JASmartServiceCallback> services = new ConcurrentHashMap<>();
    MessageChannel messageChannel;
    Map<String, ReplyEntity> replys = new ConcurrentHashMap<>();
    Timer replyExpireTimer = new Timer();
    ExecutorService replyExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);
    public MessageReceiveAdapter() {
        replyExpireTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Iterator<String> iterator = replys.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    ReplyEntity reply = replys.get(key);
                    if (reply.expiresTimestamp < System.currentTimeMillis()) {
                        iterator.remove();
                        replyExecutor.execute(() -> reply.getReply().error(ErrorInfo.builder()
                                .code(-1)
                                .message("请求超时")
                                .build()));
                    }
                }
            }
        }, 0, 1000);
    }

    public void setMessageChannel(MessageChannel messageChannel) {
        this.messageChannel = messageChannel;
    }

    public void addServiceCallback(String method, JASmartServiceCallback jaSmartServiceCallback) {
        services.put(method, jaSmartServiceCallback);
    }

    public void addServiceInvokeCallback(String tid, JASmartThingServiceReply jaSmartThingServiceReply) {
        replys.put(tid, ReplyEntity.builder()
                .expiresTimestamp(System.currentTimeMillis() + 10 * 1000)
                .reply(jaSmartThingServiceReply)
                .build());
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

        if (commonMessage.getMethod().endsWith(".reply")) {
            Optional.ofNullable(replys.get(commonMessage.getTid())).ifPresent(item -> {
                        replys.remove(commonMessage.getTid());
                        replyExecutor.execute(() -> {
                            if (null == commonMessage.getCode() || commonMessage.getCode().equals("OK"))
                                item.getReply().complete(commonMessage.getData());
                            else
                                item.getReply().error(ErrorInfo.builder()
                                        .code(-1)
                                        .message(commonMessage.getMessage())
                                        .build());
                        });
                    }
            );
            return;
        }
        JASmartServiceCallback callback = services.get(commonMessage.getMethod());
        if (null != callback) {
            String[] topics = topic.split("/");
            String deviceUuid = topics[1]  + "_" + topics[2];
            replyExecutor.execute(() -> callback.process(deviceUuid, commonMessage.getData(), new JASmartThingServiceReply() {
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
            }));
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

    public void release() {
        // 取消定时器
        if (replyExpireTimer != null) {
            replyExpireTimer.cancel();
        }
        // 关闭线程池
        if (replyExecutor != null && !replyExecutor.isShutdown()) {
            replyExecutor.shutdown();
            try {
                if (!replyExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    replyExecutor.shutdownNow();
                    if (!replyExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        log.error("线程池未能正常终止");
                    }
                }
            } catch (InterruptedException e) {
                replyExecutor.shutdownNow();
                Thread.currentThread().interrupt();
                log.error("线程池关闭时被中断", e);
            }
        }
        // 清理资源
        services.clear();
        replys.clear();
    }

    @Data
    @Builder
    public static class ReplyEntity {
        /**
         * 过期时间
         */
        Long expiresTimestamp;

        /**
         * 回调函数
         */
        JASmartThingServiceReply reply;
    }
}
