package com.player.ai.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChatParamsEntity {
    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "目录id，默认为public")
    private String directoryId;

    @Schema(description = "会话id")
    private String chatId;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "是否深度思考")
    private Boolean showThink;

    @Schema(description = "类型，document:文档，db：数据库")
    private String type;

    @Schema(description = "语言，zh/cn")
    private String language;
}
