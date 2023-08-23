package com.jingansi.smart;

/**
 * 客户端初始化工厂
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/5 19:24
 **/
public interface JASmartClientFactory {
    /**
     * 创建客户端
     * @param endpoint 服务地址
     * @param productKey 产品key
     * @param deviceId 设备id
     * @param deviceSecret 设备接入密钥
     * @return
     */
    JASmartClient create(String endpoint, String productKey, String deviceId, String deviceSecret);
}
