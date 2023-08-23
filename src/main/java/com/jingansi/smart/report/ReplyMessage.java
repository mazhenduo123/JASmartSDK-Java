package com.jingansi.smart.report;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/17 22:11
 **/
@Builder
@Data
public class ReplyMessage {
    String code;
    String message;
    String tid;
    String bid;
    String version;
    String method;
    Long timestamp;
    Map<String, Object> data;
}
