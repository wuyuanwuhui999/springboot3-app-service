package com.player.company.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业部门实体
 */
@Data
@Schema(description = "企业部门实体")
public class CompanyDepartmentEntity {

    @Schema(description = "部门ID")
    private String id;

    @Schema(description = "所属企业ID")
    private String companyId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "部门描述")
    private String description;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}