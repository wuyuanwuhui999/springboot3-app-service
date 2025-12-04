package com.player.prompt.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class PromptCategoryEntity {
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "类型")
    private String category;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
