package com.player.gateway.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChatModelEntity {
    @Schema(description = "主键")
    private String id;

    @Schema(description = "大模型类型：ollama/deepseek/tongyi")
    private String type;

    @Schema(description = "在线大模型的api_key")
    private String apiKey;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "API基础路径")
    private String baseUrl;

    @Schema(description = "是否禁用：0-启用，1-禁用")
    private Integer disabled;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}
