package com.player.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChatModelEntity {
    @Schema(description = "主键")
    private Long id;
    @Schema(description = "模型名称")
    private String modelName;
    @Schema(description = "创建时间")
    private String createTime;
    @Schema(description = "更新时间")
    private String updateTime;
}
