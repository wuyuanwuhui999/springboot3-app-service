package com.player.gateway.prompt.entity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@ToString
@Data
@Schema(description = "系统提示词")
public class SystemPromptEntity {
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "提示词类型id")
    private String categoryId;

    @Schema(description = "是否收藏")
    private int isCollect;

    @Schema(description = "是否禁用：0-启用，1-禁用")
    private Integer disabled;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}