package com.player.music.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class MusicFavoriteDirectoryEntity {

    @Schema(description = "主键")
    private Long id;//主键

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "收藏夹名称")
    private String name;

    @Schema(description = "收藏夹总歌曲数据")
    private int total;

    @Schema(description = "当前这首歌曲是否在这个收藏夹内")
    private int checked;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
