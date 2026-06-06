package com.player.company.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "企业实体")
public class CompanyEntity {

    @Schema(description = "企业ID")
    private String id;

    @Schema(description = "企业名称")
    private String name;

    @Schema(description = "企业编码")
    private String code;

    @Schema(description = "企业描述")
    private String description;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

    @Schema(description = "创建人ID")
    private String createdBy;

    @Schema(description = "更新人ID")
    private String updatedBy;

    @Schema(description = "用户在企业中的角色（仅关联查询时使用）")
    private Integer role;
}