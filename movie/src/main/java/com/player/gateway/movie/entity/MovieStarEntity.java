package com.player.gateway.movie.entity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class MovieStarEntity {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "演员名称")
    private String starName;

    @Schema(description = "演员图片地址")
    private String img;

    @Schema(description = "演员本地本地图片")
    private String localImg;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "创建时间")
    private Date updateTime;

    @Schema(description = "电影的id")
    private String movieId;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "演员的豆瓣链接地址")
    private String href;

    @Schema(description = "代表作")
    private String works;
}
