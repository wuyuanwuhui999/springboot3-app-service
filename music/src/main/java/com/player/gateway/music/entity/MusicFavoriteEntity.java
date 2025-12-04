package com.player.gateway.music.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class MusicFavoriteEntity {

    @Schema(description = "主键")
    private Long id;//主键

    @Schema(description = "收藏夹id")
    private Long favoriteId;

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "音乐")
    private Long musicId;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
