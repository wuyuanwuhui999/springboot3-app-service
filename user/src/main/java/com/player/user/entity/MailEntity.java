package com.player.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MailEntity {

    @Schema(description = "发送的用户")
    private String email;

    @Schema(description = "主题")
    private String subject;

    @Schema(description = "发送的文本")
    private String text;

    @Schema(description = "验证码")
    private String code;
}
