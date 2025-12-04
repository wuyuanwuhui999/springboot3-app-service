package com.player.gateway.music.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class MusiPlayMenuEntity {
    @Schema(description = "主键")
    private int id;//主键

    @Schema(description = "歌单名称")
    private String name;

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "歌单里面的歌曲总数")
    private int total;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
