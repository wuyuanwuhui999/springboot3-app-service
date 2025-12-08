package com.player.gateway.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

// 网关请求日志表
@Data
@ToString
public class LogEntity {
    @Schema(description = "主键")
    private String id;//使用UUID生成32随机随机id

    @Schema(description = "请求唯一ID")
    private String requestId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "请求路径")
    private String path;

    @Schema(description = "HTTP方法")
    private String method;

    @Schema(description = "查询参数")
    private String queryParams;

    @Schema(description = "请求体")
    private String requestBody;

    @Schema(description = "请求头")
    private String requestHeaders;

    @Schema(description = "客户端IP")
    private String clientIp;

    @Schema(description = "响应状态码")
    private Integer responseStatus;  // 改为Integer类型

    @Schema(description = "响应体")
    private String responseBody;

    @Schema(description = "响应头")
    private String responseHeaders;

    @Schema(description = "执行时间(毫秒)")
    private Long executeTime;  // 保持Long类型

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}