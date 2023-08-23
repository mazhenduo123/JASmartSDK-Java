package com.jingansi.smart.common;

import lombok.Builder;
import lombok.Data;

/**
 * 响应错误信息
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/17 20:32
 **/
@Data
@Builder
public class ErrorInfo {
    /**
     * 错误码
     */
    Integer code;
    /**
     * 错误说明
     */
    String message;
}
