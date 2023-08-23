package com.jingansi.smart.channel;

/**
 * 消息通道
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/16 17:40
 **/
public interface MessageChannel {
     /**
      * 发送消息
      * @param method
      * @param data
      */
     void send(String method, String data);

     /**
      * 启动
      */
     void start();

     /**
      * 停止
      */
     void stop();

     /**
      * 消息接收回调接口
      */
     interface MessageReceiveCallback {
          /**
           * 回调函数
           * @param topic 主题
           * @param data 消息报文
           */
         void receive(String topic, String data);
     }
}