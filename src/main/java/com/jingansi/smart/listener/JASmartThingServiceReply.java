package com.jingansi.smart.listener;

import com.jingansi.smart.common.ErrorInfo;

import java.util.Map;

/**
 * 消息响应回调
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/17 20:16
 **/
public interface JASmartThingServiceReply {
    /**
     * 完成 正常返回
     * @param response
     */
    void complete(Map<String, Object> response);

    /**
     * 错误，响应错误信息
     * @param errorInfo 错误信息
     */
    void error(ErrorInfo errorInfo);
}
