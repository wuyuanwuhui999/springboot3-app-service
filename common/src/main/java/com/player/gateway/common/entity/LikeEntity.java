package com.player.gateway.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class LikeEntity {
    @Schema(description = "主键")
    private Long id;//主键

    @Schema(description = "类型，movie，aiqiyi，article")
    private String type;

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "用户名称")
    private String username;

    @Schema(description = "关联的影片id")
    private Long relationId;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
