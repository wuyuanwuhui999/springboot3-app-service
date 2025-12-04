package com.player.gateway.chat.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DirectoryEntity {
    @Schema(description = "提示词")
    private String id;

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "目录名称")
    private String directory;

    @Schema(description = "租户id")
    private String tenantId;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "创建时间")
    private String createTime;
}
