package com.player.gateway.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AuthorEntity {
    @Schema(description = "主键")
    private Long id;//主键

    @Schema(description = "用户id")
    private String authorId;

    @Schema(description = "用户名称")
    private String name;

    @Schema(description = "用户签名")
    private String authorDesc;

    @Schema(description = "头像地址")
    private String avatarUrl;

    @Schema(description = "用户描述")
    private String description;

    @Schema(description = "粉丝数量")
    private String followersCount;

    @Schema(description = "核实内容")
    private String verifiedContent;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

    @Schema(description = "用户连接地址")
    private String authorHref;
}
