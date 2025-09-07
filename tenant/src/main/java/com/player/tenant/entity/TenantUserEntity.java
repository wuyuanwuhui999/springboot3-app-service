package com.player.tenant.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "租户用户关联实体")
public class TenantUserEntity {
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "角色类型：0-普通用户，1-租户管理员，2-超级管理员")
    private Integer roleType;

    @Schema(description = "加入时间")
    private Date joinDate;

    @Schema(description = "创建人ID")
    private String createBy;

    @Schema(description = "是否禁用：0-否，1-是")
    private Integer disabled;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avater;

    @Schema(description = "邮箱")
    private String email;
}