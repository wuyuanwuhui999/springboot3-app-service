package com.player.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PasswordEntity {
    @Schema(description = "用户id")
    private String id;

    @Schema(description = "新密码")
    private String newPassword;

    @Schema(description = "旧密码")
    private String oldPassword;
}
