package com.player.music.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class MusicCategoryEntity {

    @Schema(description = "主键")
    private int id;

    @Schema(description = "类型图片")
    private String img;

    @Schema(description = "小分类")
    private String category;

    @Schema(description = "大分类")
    private String classify;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
