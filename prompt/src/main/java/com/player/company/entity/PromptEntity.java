package com.player.company.entity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@ToString
@Data
@Schema(description = "提示词")
public class PromptEntity {
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "提示词标题")
    private String prompt;

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}