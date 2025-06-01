package com.player.music.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChatEntity {
    @Schema(description = "主键")
    private Long id;
    @Schema(description = "用户id")
    private String userId;
    @Schema(description = "文件")
    private String files;
    @Schema(description = "会话id")
    private String chatId;
    @Schema(description = "提示词")
    private String prompt;
    @Schema(description = "模型")
    private String model;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "创建时间")
    private String createTime;
}
