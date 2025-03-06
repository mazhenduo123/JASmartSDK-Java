package com.jingansi.smart.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/17 21:51
 **/
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonMessage {
    String tid;
    String bid;
    String version;
    String method;
    Long timestamp;
    Map<String, Object> data;
    String code;
    String message;
}
