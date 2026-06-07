package com.player.prompt.entity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@ToString
@Data
@Schema(description = "AI智能体提示词实体")
public class UserPromptEntity {
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "提示词标题")
    private String title;

    @Schema(description = "提示词内容")
    private String content;

    @Schema(description = "是否禁用：0-启用，1-禁用")
    private Integer disabled;

    @Schema(description = "分类id")
    private String categoryId;

    @Schema(description = "提示词标签")
    private String tags;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "创建人ID")
    private String createdBy;

    @Schema(description = "更新人ID")
    private String updatedBy;
}