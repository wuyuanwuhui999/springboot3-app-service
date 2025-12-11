package com.player.agent.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AgentParamsEntity {
    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "会话id")
    private String chatId;

    @Schema(description = "模型id")
    private String modelId;

    @Schema(description = "是否深度思考")
    private Boolean showThink;

    @Schema(description = "类型，document:文档，db：数据库")
    private String type;

    @Schema(description = "语言，zh/cn")
    private String language;
}
