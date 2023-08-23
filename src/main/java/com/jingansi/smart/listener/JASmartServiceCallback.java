package com.jingansi.smart.listener;

import java.util.Map;

/**
 * 能力处理回调接口
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/17 20:14
 **/
public interface JASmartServiceCallback {
    /**
     * 回调函数
     * @param tid 消息唯一标识
     * @param data 数据内容
     * @param reply 响应回调
     */
    void process(String tid, Map<String, Object> data, JASmartThingServiceReply reply);
}
