package com.player.company.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业职位实体
 */
@Data
@Schema(description = "企业职位实体")
public class CompanyPositionEntity {

    @Schema(description = "职位ID")
    private String id;

    @Schema(description = "职位名称")
    private String positionName;

    @Schema(description = "所属部门ID")
    private String departmentId;

    @Schema(description = "职位描述")
    private String description;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}