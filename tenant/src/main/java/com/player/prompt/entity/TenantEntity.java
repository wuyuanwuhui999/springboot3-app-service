package com.player.prompt.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "租户实体")
public class TenantEntity {
    @Schema(description = "租户ID")
    private String id;

    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "租户编码")
    private String code;

    @Schema(description = "租户描述")
    private String description;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private Date createDate;

    @Schema(description = "更新时间")
    private Date updateDate;

    @Schema(description = "创建人ID")
    private String createdBy;

    @Schema(description = "更新人ID")
    private String updatedBy;
}
