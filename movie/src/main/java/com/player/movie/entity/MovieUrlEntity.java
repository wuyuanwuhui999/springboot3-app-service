package com.player.movie.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MovieUrlEntity {
    @Schema(description = "主键")
    private int id;//主键

    @Schema(description = "电影名称")
    private String movieName;

    @Schema(description = "对应的电影的id")
    private int movieId;

    @Schema(description = "源地址")
    private String href;

    @Schema(description = "集数")
    private String label;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "播放地址")
    private String updateTime;

    @Schema(description = "播放地址")
    private String url;

    @Schema(description = "播放分组，1, 2")
    private String playGroup;

}
