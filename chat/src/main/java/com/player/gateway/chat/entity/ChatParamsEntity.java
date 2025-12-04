package com.player.gateway.chat.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;

@Data
public class ChatParamsEntity {
    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "文档id")
    private ArrayList<String> docIds;

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "系统提示词")
    private String systemPrompt;

    @Schema(description = "会话id")
    private String chatId;

    @Schema(description = "租户id")
    private String tenantId;

    @Schema(description = "模型id")
    private String modelId;

    @Schema(description = "是否深度思考")
    private Boolean showThink;

    @Schema(description = "类型，document:文档，db：数据库")
    private String type;

    @Schema(description = "语言，zh/cn")
    private String language;
}
