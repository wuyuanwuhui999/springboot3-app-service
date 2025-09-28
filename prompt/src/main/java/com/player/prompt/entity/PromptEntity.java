package com.player.prompt.entity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "AI智能体提示词实体")
public class PromptEntity {
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "提示词标题")
    private String title;

    @Schema(description = "提示词内容")
    private String content;

    @Schema(description = "是否禁用：0-启用，1-禁用")
    private Integer disabled;

    @Schema(description = "适用行业")
    private String industry;

    @Schema(description = "提示词标签")
    private String tags;

    @Schema(description = "创建时间")
    private Date createDate;

    @Schema(description = "更新时间")
    private Date updateDate;

    @Schema(description = "创建人ID")
    private String createdBy;

    @Schema(description = "更新人ID")
    private String updatedBy;


    @Override
    public String toString() {
        return "PromptEntity{" +
                "id='" + id + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", disabled=" + disabled +
                ", industry='" + industry + '\'' +
                ", tags='" + tags + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}