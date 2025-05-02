package com.player.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResetPasswordEntity {

    @Schema(description = "接收人")
    private String email;

    @Schema(description = "验证码")
    private int code;

    @Schema(description = "新密码")
    private String password;
}
