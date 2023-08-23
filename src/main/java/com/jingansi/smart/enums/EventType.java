package com.jingansi.smart.enums;

import lombok.Getter;

/**
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/17 19:42
 **/
@Getter
public enum EventType {
    INFO("info"),
    WARNING("warning"),
    ERROR("error");

    String value;

    EventType(String value) {
        this.value = value;
    }
}
