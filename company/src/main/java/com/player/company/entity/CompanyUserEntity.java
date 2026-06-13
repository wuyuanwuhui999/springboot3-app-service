package com.player.company.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户企业关联实体")
public class CompanyUserEntity {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户帐号")
    private String userAccount;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户电话")
    private String telephone;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户性别")
    private String sex;

    @Schema(description = "用户所属地区")
    private String region;

    @Schema(description = "用户头像")
    private String avater;

    @Schema(description = "用户签名")
    private String sign;

    @Schema(description = "企业ID")
    private String companyId;

    @Schema(description = "职位ID")
    private String positionId;

    @Schema(description = "职位名称")
    private String positionName;

    @Schema(description = "部门ID")
    private String departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "角色：2-超级管理员，1-管理员，0-普通成员")
    private Integer role;

    @Schema(description = "加入时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinDate;

    @Schema(description = "状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "创建人ID")
    private String createBy;
}