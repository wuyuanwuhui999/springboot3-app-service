package com.player.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
@Data
@ToString
public class UserEntity {
    @Schema(description = "用户uid")
    private String id;

    @Schema(description = "用户账号")
    private String userAccount;

    @Schema(description = "创建日期")
    private Date createDate;

    @Schema(description = "更新日期")
    private Date updateDate;

    @Schema(description = "昵称")
    private String username;

    @Schema(description = "电话号码")
    private String telephone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avater;

    @Schema(description = "出生年月日")
    private String birthday;

    @Schema(description = "性别")
    private int sex;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "个性签名")
    private String sign;

    @Schema(description = "地区")
    private String region;

    @Schema(description = "是否禁用")
    private int disabled;

    @Schema(description = "权限")
    private int permission;
}
