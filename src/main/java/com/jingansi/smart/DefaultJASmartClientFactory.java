package com.jingansi.smart;

/**
 * 工厂默认实现
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/5 19:24
 **/
public class DefaultJASmartClientFactory implements JASmartClientFactory {

    @Override
    public JASmartClient create(String endpoint, String productKey, String deviceId, String deviceSecret) {
        return new DefaultJASmartClient(endpoint, productKey, deviceId, deviceSecret, "emqx_test", "emqx_test_password");
    }
}
