package com.player.gateway.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Data
@ToString
@Document(collection = "gateway") // 指定集合名
public class LogEntity {

    @Id
    @Schema(description = "主键（MongoDB ObjectId 自动填充，但这里用 String 兼容 UUID）")
    private String id; // 可保留为 UUID 字符串，或改为 ObjectId（推荐用 String 简单）

    @Field("request_id")
    @Schema(description = "请求唯一ID")
    private String requestId;

    @Field("user_id")
    @Schema(description = "用户ID")
    private String userId;

    @Field("path")
    @Schema(description = "请求路径")
    private String path;

    @Field("method")
    @Schema(description = "HTTP方法")
    private String method;

    @Field("query_params")
    @Schema(description = "查询参数")
    private String queryParams;

    @Field("request_body")
    @Schema(description = "请求体")
    private String requestBody;

    @Field("request_headers")
    @Schema(description = "请求头")
    private String requestHeaders;

    @Field("client_ip")
    @Schema(description = "客户端IP")
    private String clientIp;

    @Field("response_status")
    @Schema(description = "响应状态码")
    private Integer responseStatus;

    @Field("response_body")
    @Schema(description = "响应体")
    private String responseBody;

    @Field("response_headers")
    @Schema(description = "响应头")
    private String responseHeaders;

    @Field("execute_time")
    @Schema(description = "执行时间(毫秒)")
    private Long executeTime;

    @Field("error_message")
    @Schema(description = "错误信息")
    private String errorMessage;

    @Field("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Field("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}