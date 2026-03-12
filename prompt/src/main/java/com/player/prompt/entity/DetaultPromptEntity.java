package com.player.prompt.entity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@ToString
@Data
@Schema(description = "系统提示词")
public class DetaultPromptEntity {
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "提示词id")
    private String tenantId;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}